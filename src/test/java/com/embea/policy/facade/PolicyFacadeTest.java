package com.embea.policy.facade;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.embea.policy.dto.Person;
import com.embea.policy.dto.Policy;
import com.embea.policy.dto.PolicyMapping;
import com.embea.policy.exception.PolicyNotFoundException;
import com.embea.policy.model.*;
import com.embea.policy.services.PersonService;
import com.embea.policy.services.PolicyMappingService;
import com.embea.policy.services.PolicyService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PolicyFacadeTest {

  private static final Date CURRENT_DATE = new Date();
  private static final Date OLD_DATE = new Date(CURRENT_DATE.getTime() - 432000000);
  private static final Date START_DATE = new Date(CURRENT_DATE.getTime() + 86400000);
  private static final Date UPDATED_DATE = new Date(START_DATE.getTime() + 432000000);
  private static final String FIRST_NAME_1 = "Jane";
  private static final String SECOND_NAME_1 = "Jackson";
  private static final BigDecimal PREMIUM_1 = BigDecimal.valueOf(12.90);
  private static final String FIRST_NAME_2 = "Jack";
  private static final String SECOND_NAME_2 = "Doe";
  private static final BigDecimal PREMIUM_2 = BigDecimal.valueOf(15.90);
  private static final String FIRST_NAME_3 = "Will";
  private static final String SECOND_NAME_3 = "Smith";
  private static final BigDecimal PREMIUM_3 = BigDecimal.valueOf(12.90);
  private static final BigDecimal TOTAL_PREMIUM = BigDecimal.valueOf(28.80);
  private static final BigDecimal UPDATED_TOTAL_PREMIUM = BigDecimal.valueOf(25.80);
  private static final String POLICY_ID = UUID.randomUUID().toString();
  private static final Long PERSON_ID_1 = 1L;
  private static final Long PERSON_ID_2 = 2L;
  private static final Long PERSON_ID_3 = 3L;

  @Mock private PolicyService policyService;

  @Mock private PersonService personService;

  @Mock private PolicyMappingService policyMappingService;

  @InjectMocks private PolicyFacade policyFacade;

  @Mock private Policy mockPolicy;

  @Mock private PolicyMapping mockPolicyMapping;

  @Captor private ArgumentCaptor<InsuredPerson> insuredPersonCaptor;

  @Captor private ArgumentCaptor<Policy> policyArgumentCaptor;

  @Test
  @DisplayName(
      "Given create policy request with valid inputs "
          + "When we try to create policy "
          + "Then it returns created Policy object"
          + "And person objects with their newly generated ids.")
  void createPolicySuccessScenario() {
    PolicyCreationRequest policyCreationRequest = buildPolicyCreationRequest();
    Policy createdPolicy = Policy.builder().policyId(POLICY_ID).startDate(START_DATE).build();
    doReturn(createdPolicy).when(policyService).insertPolicy(policyCreationRequest);
    long id = 1L;
    for (InsuredPerson insuredPerson : policyCreationRequest.getInsuredPersons()) {
      Person storedPerson =
          Person.builder()
              .firstName(insuredPerson.getFirstName())
              .secondName(insuredPerson.getSecondName())
              .build();
      storedPerson.setPersonId(id++);
      doReturn(storedPerson).when(personService).storePersonEntry(insuredPerson);
      lenient()
          .doReturn(mockPolicyMapping)
          .when(policyMappingService)
          .storePolicyMapping(anyString(), any(InsuredPerson.class), any(Date.class));
    }
    doReturn(mockPolicy).when(policyService).savePolicy(any(Policy.class));

    PolicyResponse policyResponse = policyFacade.createPolicy(policyCreationRequest);

    validateCreatePolicyResponse(policyCreationRequest, policyResponse);
  }

  @Test
  @DisplayName(
      "Given create policy request with valid inputs "
          + "When we try to create policy "
          + "And policyService throws an exception "
          + "Then throws exception back to the caller.")
  void createPolicyFailureScenario() {
    PolicyCreationRequest policyCreationRequest = buildPolicyCreationRequest();

    doThrow(IllegalArgumentException.class).when(policyService).insertPolicy(policyCreationRequest);

    assertThrows(
        IllegalArgumentException.class, () -> policyFacade.createPolicy(policyCreationRequest));

    verify(policyService).insertPolicy(policyCreationRequest);
    verify(personService, never()).storePersonEntry(any(InsuredPerson.class));
    verify(policyMappingService, never())
        .storePolicyMapping(anyString(), any(InsuredPerson.class), any(Date.class));
  }

  @Test
  @DisplayName(
      "Given modify policy request with valid policy Id and future effective date "
          + "And one person added and one person removed "
          + "When we try to modify policy "
          + "Then policy details returned with correct person details and future effective date.")
  void modifyPolicySuccessScenarioWithAdditionAndRemoval() {
    PolicyModificationRequest policyModificationRequest = createPolicyModificationRequest();
    policyModificationRequest.setEffectiveDate(UPDATED_DATE);

    Policy createdPolicy = Policy.builder().policyId(POLICY_ID).startDate(START_DATE).build();
    doReturn(createdPolicy).when(policyService).getPolicy(POLICY_ID, UPDATED_DATE);

    InsuredPerson personToNewlyAdd =
        InsuredPerson.builder().firstName(FIRST_NAME_3).secondName(SECOND_NAME_3).build();
    Person personNewlyAdded =
        Person.builder()
            .personId(PERSON_ID_3)
            .firstName(FIRST_NAME_3)
            .secondName(SECOND_NAME_3)
            .build();
    doReturn(personNewlyAdded).when(personService).storePersonEntry(personToNewlyAdd);
    doReturn(mockPolicyMapping)
        .when(policyMappingService)
        .storePolicyMapping(anyString(), any(InsuredPerson.class), any(Date.class));

    PolicyMapping policyMapping1 =
        PolicyMapping.builder()
            .id(1L)
            .policyId(POLICY_ID)
            .personId(PERSON_ID_1)
            .premium(PREMIUM_1)
            .build();
    PolicyMapping policyMapping2 =
        PolicyMapping.builder()
            .id(2L)
            .policyId(POLICY_ID)
            .personId(PERSON_ID_2)
            .premium(PREMIUM_2)
            .build();
    PolicyMapping policyMapping3 =
        PolicyMapping.builder()
            .id(3L)
            .policyId(POLICY_ID)
            .personId(PERSON_ID_3)
            .premium(PREMIUM_3)
            .build();
    List<PolicyMapping> policyMappingList = List.of(policyMapping1, policyMapping2, policyMapping3);
    doReturn(policyMappingList).when(policyMappingService).findPersonsForPolicy(POLICY_ID);

    doReturn(1)
        .when(policyMappingService)
        .removePersonsFromPolicy(anyList(), anyString(), any(Date.class));

    PolicyResponse policyResponse = policyFacade.modifyPolicy(policyModificationRequest);

    validatePolicyModificationResponse(policyResponse);
  }

  @Test
  @DisplayName(
      "Given modify policy request with valid policy Id "
          + "When we try to modify policy "
          + "And policyService throws an exception "
          + "Then throws exception back to the caller.")
  void modifyPolicyThrowsException() {
    PolicyModificationRequest policyModificationRequest = createPolicyModificationRequest();

    doThrow(IllegalArgumentException.class).when(policyService).getPolicy(POLICY_ID, START_DATE);

    assertThrows(
        IllegalArgumentException.class, () -> policyFacade.modifyPolicy(policyModificationRequest));
  }

  @Test
  @DisplayName(
      "Given modify policy request with invalid policy Id "
          + "When we try to retrieve policy "
          + "Then throws PolicyNotFoundException back to the caller.")
  void modifyPolicyWithInvalidPolicyId() {
    PolicyModificationRequest policyModificationRequest = createPolicyModificationRequest();

    doReturn(null).when(policyService).getPolicy(POLICY_ID, START_DATE);

    assertThrows(
        PolicyNotFoundException.class, () -> policyFacade.modifyPolicy(policyModificationRequest));
  }

  @Test
  @DisplayName(
      "Given fetch policy request with valid policy Id and request date"
          + "When we try to retrieve policy "
          + "Then policy details returned.")
  void testFetchPolicySuccessScenario() {
    PolicyFetchRequest policyFetchRequest = createPolicyFetchRequest();
    Policy fetchedPolicy = Policy.builder().policyId(POLICY_ID).startDate(START_DATE).build();
    doReturn(fetchedPolicy).when(policyService).getPolicy(POLICY_ID, START_DATE);

    PolicyMapping policyMapping1 =
        PolicyMapping.builder()
            .id(1L)
            .policyId(POLICY_ID)
            .personId(PERSON_ID_1)
            .premium(PREMIUM_1)
            .build();
    PolicyMapping policyMapping2 =
        PolicyMapping.builder()
            .id(2L)
            .policyId(POLICY_ID)
            .personId(PERSON_ID_2)
            .premium(PREMIUM_2)
            .build();
    List<PolicyMapping> policyMappingList = List.of(policyMapping1, policyMapping2);
    doReturn(policyMappingList)
        .when(policyMappingService)
        .findPersonsForPolicyAndRequestDate(POLICY_ID, START_DATE);

    Person person1 =
        Person.builder()
            .personId(PERSON_ID_1)
            .firstName(FIRST_NAME_1)
            .secondName(SECOND_NAME_1)
            .build();
    doReturn(person1).when(personService).getPerson(PERSON_ID_1);
    Person person2 =
        Person.builder()
            .personId(PERSON_ID_2)
            .firstName(FIRST_NAME_2)
            .secondName(SECOND_NAME_2)
            .build();
    doReturn(person2).when(personService).getPerson(PERSON_ID_2);

    PolicyResponse policyResponse = policyFacade.fetchPolicy(policyFetchRequest);

    validatePolicyFetchResponse(policyResponse, START_DATE);
  }

  @Test
  @DisplayName(
      "Given fetch policy request with valid policy Id and no request date"
          + "When we try to retrieve policy "
          + "Then policy details returned with current date.")
  void testFetchPolicySuccessScenarioWithNoRequestDate() {
    PolicyFetchRequest policyFetchRequest = createPolicyFetchRequest();
    policyFetchRequest.setRequestDate(null);
    Policy fetchedPolicy = Policy.builder().policyId(POLICY_ID).startDate(CURRENT_DATE).build();
    doReturn(fetchedPolicy).when(policyService).getPolicy(eq(POLICY_ID), any(Date.class));

    PolicyMapping policyMapping1 =
        PolicyMapping.builder()
            .id(1L)
            .policyId(POLICY_ID)
            .personId(PERSON_ID_1)
            .premium(PREMIUM_1)
            .build();
    PolicyMapping policyMapping2 =
        PolicyMapping.builder()
            .id(2L)
            .policyId(POLICY_ID)
            .personId(PERSON_ID_2)
            .premium(PREMIUM_2)
            .build();
    List<PolicyMapping> policyMappingList = List.of(policyMapping1, policyMapping2);
    doReturn(policyMappingList)
        .when(policyMappingService)
        .findPersonsForPolicyAndRequestDate(eq(POLICY_ID), any(Date.class));

    Person person1 =
        Person.builder()
            .personId(PERSON_ID_1)
            .firstName(FIRST_NAME_1)
            .secondName(SECOND_NAME_1)
            .build();
    doReturn(person1).when(personService).getPerson(PERSON_ID_1);
    Person person2 =
        Person.builder()
            .personId(PERSON_ID_2)
            .firstName(FIRST_NAME_2)
            .secondName(SECOND_NAME_2)
            .build();
    doReturn(person2).when(personService).getPerson(PERSON_ID_2);

    PolicyResponse policyResponse = policyFacade.fetchPolicy(policyFetchRequest);

    validatePolicyFetchResponse(policyResponse, CURRENT_DATE);
  }

  @Test
  @DisplayName(
      "Given fetch policy request with valid policy Id "
          + "When we try to retrieve policy "
          + "And policyService throws an exception "
          + "Then throws exception back to the caller.")
  void testFetchPolicyThrowsException() {
    PolicyFetchRequest policyFetchRequest = createPolicyFetchRequest();

    doThrow(IllegalArgumentException.class).when(policyService).getPolicy(POLICY_ID, START_DATE);

    assertThrows(
        IllegalArgumentException.class, () -> policyFacade.fetchPolicy(policyFetchRequest));
  }

  @Test
  @DisplayName(
      "Given fetch policy request with valid policy Id and old date"
          + "When we try to retrieve policy "
          + "Then throws PolicyNotFoundException back to the caller.")
  void testFetchPolicyWithInvalidDate() {
    PolicyFetchRequest policyFetchRequest = createPolicyFetchRequest();
    policyFetchRequest.setRequestDate(OLD_DATE);

    doReturn(null).when(policyService).getPolicy(POLICY_ID, OLD_DATE);

    assertThrows(PolicyNotFoundException.class, () -> policyFacade.fetchPolicy(policyFetchRequest));
  }

  private PolicyCreationRequest buildPolicyCreationRequest() {
    return PolicyCreationRequest.builder()
        .startDate(START_DATE)
        .insuredPersons(getInsuredPersonList(false, false))
        .build();
  }

  private PolicyModificationRequest createPolicyModificationRequest() {
    return PolicyModificationRequest.builder()
        .policyId(POLICY_ID)
        .insuredPersons(getInsuredPersonList(true, true))
        .effectiveDate(START_DATE)
        .build();
  }

  private PolicyFetchRequest createPolicyFetchRequest() {
    return PolicyFetchRequest.builder().policyId(POLICY_ID).requestDate(START_DATE).build();
  }

  private List<InsuredPerson> getInsuredPersonList(boolean setIds, boolean isModify) {
    List<InsuredPerson> insuredPersonList = new ArrayList<>();
    InsuredPerson insuredPerson1 =
        InsuredPerson.builder()
            .firstName(FIRST_NAME_1)
            .secondName(SECOND_NAME_1)
            .premium(PREMIUM_1)
            .build();
    if (setIds) {
      insuredPerson1.setId(PERSON_ID_1);
    }
    if (isModify) {
      InsuredPerson insuredPerson3 =
          InsuredPerson.builder()
              .firstName(FIRST_NAME_3)
              .secondName(SECOND_NAME_3)
              .premium(PREMIUM_3)
              .build();
      insuredPersonList.add(insuredPerson3);
      insuredPerson1.setId(PERSON_ID_1);
    } else {
      InsuredPerson insuredPerson2 =
          InsuredPerson.builder()
              .firstName(FIRST_NAME_2)
              .secondName(SECOND_NAME_2)
              .premium(PREMIUM_2)
              .build();
      if (setIds) {
        insuredPerson2.setId(PERSON_ID_2);
      }
      insuredPersonList.add(insuredPerson2);
    }
    insuredPersonList.add(insuredPerson1);
    return insuredPersonList;
  }

  private void validateCreatePolicyResponse(
      PolicyCreationRequest policyCreationRequest, PolicyResponse policyResponse) {
    assertNotNull(policyResponse);
    assertTrue(policyResponse instanceof PolicyCreationResponse);
    PolicyCreationResponse policyCreationResponse = (PolicyCreationResponse) policyResponse;
    assertEquals(POLICY_ID, policyCreationResponse.getPolicyId());
    assertEquals(TOTAL_PREMIUM, policyCreationResponse.getTotalPremium());
    List<Long> personIdList = List.of(PERSON_ID_1, PERSON_ID_2);
    for (InsuredPerson insuredPerson : policyCreationResponse.getInsuredPersons()) {
      assertTrue(personIdList.contains(insuredPerson.getId()));
    }
    Instant instant1 = START_DATE.toInstant().truncatedTo(ChronoUnit.DAYS);
    Instant instant2 =
        policyCreationResponse.getStartDate().toInstant().truncatedTo(ChronoUnit.DAYS);
    assertEquals(instant1, instant2);
    verify(policyService).insertPolicy(policyCreationRequest);
    verify(personService, times(2)).storePersonEntry(any(InsuredPerson.class));
    verify(policyMappingService, times(2))
        .storePolicyMapping(anyString(), any(InsuredPerson.class), any(Date.class));
  }

  private void validatePolicyModificationResponse(PolicyResponse policyResponse) {
    assertNotNull(policyResponse);
    assertTrue(policyResponse instanceof PolicyModificationResponse);
    PolicyModificationResponse policyModificationResponse =
        (PolicyModificationResponse) policyResponse;
    assertEquals(UPDATED_TOTAL_PREMIUM, policyModificationResponse.getTotalPremium());
    assertEquals(POLICY_ID, policyModificationResponse.getPolicyId());
    Instant instant1 = UPDATED_DATE.toInstant().truncatedTo(ChronoUnit.DAYS);
    Instant instant2 =
        policyModificationResponse.getEffectiveDate().toInstant().truncatedTo(ChronoUnit.DAYS);
    assertEquals(instant1, instant2);

    InsuredPerson insuredPerson1 =
        InsuredPerson.builder()
            .id(PERSON_ID_1)
            .firstName(FIRST_NAME_1)
            .secondName(SECOND_NAME_1)
            .premium(PREMIUM_1)
            .build();
    InsuredPerson insuredPerson3 =
        InsuredPerson.builder()
            .id(PERSON_ID_3)
            .firstName(FIRST_NAME_3)
            .secondName(SECOND_NAME_3)
            .premium(PREMIUM_3)
            .build();
    Set<InsuredPerson> insuredPersonSet = Set.of(insuredPerson1, insuredPerson3);
    for (InsuredPerson insuredPerson : policyModificationResponse.getInsuredPersons()) {
      assertTrue(insuredPersonSet.contains(insuredPerson));
    }
    verify(policyService).getPolicy(POLICY_ID, UPDATED_DATE);
    verify(personService).storePersonEntry(insuredPersonCaptor.capture());
    InsuredPerson personCaptorValue = insuredPersonCaptor.getValue();
    assertEquals(PERSON_ID_3, personCaptorValue.getId());
    verify(policyMappingService)
        .storePolicyMapping(anyString(), any(InsuredPerson.class), any(Date.class));
    verify(policyMappingService).findPersonsForPolicy(POLICY_ID);
    verify(policyMappingService).removePersonsFromPolicy(anyList(), anyString(), any(Date.class));
  }

  private void validatePolicyFetchResponse(PolicyResponse policyResponse, Date effectiveDate) {
    assertNotNull(policyResponse);
    assertTrue(policyResponse instanceof PolicyFetchResponse);
    PolicyFetchResponse policyFetchResponse = (PolicyFetchResponse) policyResponse;
    assertEquals(TOTAL_PREMIUM, policyFetchResponse.getTotalPremium());
    assertEquals(POLICY_ID, policyFetchResponse.getPolicyId());

    Instant instant1 = effectiveDate.toInstant().truncatedTo(ChronoUnit.DAYS);
    Instant instant2 =
        policyFetchResponse.getRequestDate().toInstant().truncatedTo(ChronoUnit.DAYS);
    assertEquals(instant1, instant2);

    InsuredPerson insuredPerson1 =
        InsuredPerson.builder()
            .id(PERSON_ID_1)
            .firstName(FIRST_NAME_1)
            .secondName(SECOND_NAME_1)
            .premium(PREMIUM_1)
            .build();
    InsuredPerson insuredPerson2 =
        InsuredPerson.builder()
            .id(PERSON_ID_2)
            .firstName(FIRST_NAME_2)
            .secondName(SECOND_NAME_2)
            .premium(PREMIUM_2)
            .build();
    Set<InsuredPerson> insuredPersonSet = Set.of(insuredPerson1, insuredPerson2);
    for (InsuredPerson insuredPerson : policyFetchResponse.getInsuredPersons()) {
      assertTrue(insuredPersonSet.contains(insuredPerson));
    }
  }
}
