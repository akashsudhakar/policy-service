package com.embea.policy.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.embea.policy.facade.PolicyFacade;
import com.embea.policy.model.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class PolicyControllerTest {

  private static final Date START_DATE = new Date();
  private static final Date EFFECTIVE_DATE = new Date(START_DATE.getTime() + 86400000);
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

  @Mock private PolicyFacade policyFacade;

  @InjectMocks private PolicyController policyController;

  @Test
  @DisplayName(
      "Given policy controller API is up "
          + "When we make a create policy request with valid input "
          + "Then it calls createPolicy API of PolicyFacade class "
          + "And returns PolicyCreationResponse object.")
  void testCreatePolicyWithValidInputs() {
    PolicyCreationRequest policyCreationRequest = buildPolicyCreationRequest();
    PolicyCreationResponse policyCreationResponse = buildPolicyCreationResponse();
    doReturn(policyCreationResponse).when(policyFacade).createPolicy(policyCreationRequest);

    ResponseEntity<PolicyResponse> responseEntity =
        policyController.createPolicy(policyCreationRequest);

    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    assertEquals(policyCreationResponse, responseEntity.getBody());
    verify(policyFacade).createPolicy(policyCreationRequest);
  }

  @Test
  @DisplayName(
      "Given policy controller API is up "
          + "When we make a modify policy request with valid input "
          + "Then it calls modifyPolicy API of PolicyFacade class "
          + "And returns PolicyModificationResponse object.")
  void testModifyPolicyWithValidInputs() {
    PolicyModificationRequest policyModificationRequest = createPolicyModificationRequest();
    PolicyModificationResponse policyModificationResponse = createPolicyModificationResponse();
    doReturn(policyModificationResponse).when(policyFacade).modifyPolicy(policyModificationRequest);

    ResponseEntity<PolicyResponse> responseEntity =
        policyController.modifyPolicy(policyModificationRequest);

    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    assertEquals(policyModificationResponse, responseEntity.getBody());
    verify(policyFacade).modifyPolicy(policyModificationRequest);
  }

  @Test
  @DisplayName(
      "Given policy controller API is up "
          + "When we make a fetch policy request with valid input "
          + "Then it calls fetchPolicy API of PolicyFacade class "
          + "And returns PolicyFetchResponse object.")
  void testFetchPolicyWithValidInputs() {
    PolicyFetchRequest policyFetchRequest = createPolicyFetchRequest();
    PolicyFetchResponse policyFetchResponse = createPolicyFetchResponse();
    doReturn(policyFetchResponse).when(policyFacade).fetchPolicy(policyFetchRequest);

    ResponseEntity<PolicyResponse> responseEntity =
        policyController.fetchPolicy(policyFetchRequest);

    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    assertEquals(policyFetchResponse, responseEntity.getBody());
    verify(policyFacade).fetchPolicy(policyFetchRequest);
  }

  private PolicyCreationRequest buildPolicyCreationRequest() {
    return PolicyCreationRequest.builder()
        .startDate(START_DATE)
        .insuredPersons(getInsuredPersonList(false, false))
        .build();
  }

  private PolicyCreationResponse buildPolicyCreationResponse() {
    return PolicyCreationResponse.builder()
        .policyId(POLICY_ID)
        .totalPremium(TOTAL_PREMIUM)
        .insuredPersons(getInsuredPersonList(true, false))
        .startDate(START_DATE)
        .build();
  }

  private PolicyModificationRequest createPolicyModificationRequest() {
    return PolicyModificationRequest.builder()
        .policyId(POLICY_ID)
        .insuredPersons(getInsuredPersonList(true, true))
        .effectiveDate(EFFECTIVE_DATE)
        .build();
  }

  private PolicyModificationResponse createPolicyModificationResponse() {
    return PolicyModificationResponse.builder()
        .policyId(POLICY_ID)
        .effectiveDate(START_DATE)
        .insuredPersons(getInsuredPersonList(true, true))
        .totalPremium(UPDATED_TOTAL_PREMIUM)
        .build();
  }

  private PolicyFetchRequest createPolicyFetchRequest() {
    return PolicyFetchRequest.builder().policyId(POLICY_ID).requestDate(START_DATE).build();
  }

  private PolicyFetchResponse createPolicyFetchResponse() {
    return PolicyFetchResponse.builder()
        .policyId(POLICY_ID)
        .insuredPersons(getInsuredPersonList(true, false))
        .requestDate(START_DATE)
        .totalPremium(TOTAL_PREMIUM)
        .build();
  }

  private List<InsuredPerson> getInsuredPersonList(boolean setIds, boolean isModify) {
    InsuredPerson insuredPerson1 =
        InsuredPerson.builder()
            .firstName(FIRST_NAME_1)
            .secondName(SECOND_NAME_1)
            .premium(PREMIUM_1)
            .build();
    InsuredPerson insuredPerson2 =
        InsuredPerson.builder()
            .firstName(FIRST_NAME_2)
            .secondName(SECOND_NAME_2)
            .premium(PREMIUM_2)
            .build();
    if (setIds) {
      insuredPerson1.setId(PERSON_ID_1);
      insuredPerson2.setId(PERSON_ID_2);
    }
    List<InsuredPerson> insuredPersonList = new ArrayList<>();
    insuredPersonList.add(insuredPerson1);
    insuredPersonList.add(insuredPerson2);
    if (isModify) {
      InsuredPerson insuredPerson3 =
          InsuredPerson.builder()
              .firstName(FIRST_NAME_3)
              .secondName(SECOND_NAME_3)
              .premium(PREMIUM_3)
              .build();
      insuredPersonList.add(insuredPerson3);
    }
    return insuredPersonList;
  }
}
