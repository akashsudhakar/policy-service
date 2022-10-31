package com.embea.policy.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.embea.policy.dto.Policy;
import com.embea.policy.dto.PolicyMapping;
import com.embea.policy.model.InsuredPerson;
import com.embea.policy.repository.PolicyMappingRepo;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PolicyMappingServiceTest {

  private static final Date START_DATE = new Date();
  private static final String FIRST_NAME_1 = "Jane";
  private static final String SECOND_NAME_1 = "Jackson";
  private static final BigDecimal PREMIUM_1 = BigDecimal.valueOf(12.90);
  private static final BigDecimal PREMIUM_2 = BigDecimal.valueOf(15.90);
  private static final BigDecimal TOTAL_PREMIUM = BigDecimal.valueOf(28.80);
  private static final String POLICY_ID = UUID.randomUUID().toString();
  private static final String INVALID_POLICY_ID = UUID.randomUUID().toString();
  private static final Long PERSON_ID_1 = 1L;
  private static final Long PERSON_ID_2 = 2L;

  @Mock private PolicyMappingRepo policyMappingRepo;

  @InjectMocks private PolicyMappingService policyMappingService;

  @Mock private PolicyMapping mockPolicyMapping;

  @Mock private List<PolicyMapping> mockPolicyMappingList;

  @Captor private ArgumentCaptor<PolicyMapping> policyMappingCaptor;

  @Test
  @DisplayName(
      "Given valid policy and insured person objects "
          + "When we try to insert policy mapping into database "
          + "Then it returns inserted policy mapping object.")
  void testStorePolicyMappingWithValidValues() {
    Policy policy = createPolicy();
    InsuredPerson insuredPerson = createInsuredPerson();
    doReturn(mockPolicyMapping).when(policyMappingRepo).save(any(PolicyMapping.class));

    PolicyMapping policyMapping = policyMappingService.storePolicyMapping(policy, insuredPerson);
    assertEquals(mockPolicyMapping, policyMapping);
    verify(policyMappingRepo).save(policyMappingCaptor.capture());
    PolicyMapping captorValue = policyMappingCaptor.getValue();
    validatePolicyMapping(captorValue);
  }

  @Test
  @DisplayName(
      "Given invalid policy and insured person objects "
          + "When we try to insert policy mapping into database "
          + "Then throws IllegalArgumentException back to the caller.")
  void testStorePolicyMappingWithInvalidValues() {
    Policy policy = createPolicy();
    InsuredPerson insuredPerson = createInsuredPerson();
    doThrow(IllegalArgumentException.class).when(policyMappingRepo).save(any(PolicyMapping.class));

    assertThrows(
        IllegalArgumentException.class,
        () -> policyMappingService.storePolicyMapping(policy, insuredPerson));
    verify(policyMappingRepo).save(policyMappingCaptor.capture());
    PolicyMapping captorValue = policyMappingCaptor.getValue();
    validatePolicyMapping(captorValue);
  }

  @Test
  @DisplayName(
      "Given valid policy id "
          + "When we try to fetch policy mappings from database "
          + "Then it executes successfully and returns policy mappings.")
  void testFindPersonsForPolicyWithValidValues() {
    doReturn(mockPolicyMappingList).when(policyMappingRepo).findByPolicyId(POLICY_ID);

    List<PolicyMapping> policyMappings = policyMappingRepo.findByPolicyId(POLICY_ID);
    assertEquals(mockPolicyMappingList, policyMappings);
  }

  @Test
  @DisplayName(
      "Given invalid policy id "
          + "When we try to fetch policy mappings from database "
          + "Then it executes successfully and returns empty policy mappings list.")
  void testFindPersonsForPolicyWithInvalidValues() {
    doReturn(List.of()).when(policyMappingRepo).findByPolicyId(INVALID_POLICY_ID);

    List<PolicyMapping> policyMappings = policyMappingRepo.findByPolicyId(INVALID_POLICY_ID);
    assertEquals(0, policyMappings.size());
  }

  @Test
  @DisplayName(
      "Given valid policy id "
          + "When we try to fetch policy mappings from database "
          + "And api throws exception "
          + "Then exception thrown back to the caller.")
  void testFindPersonsForPolicyThrowsException() {
    doThrow(IllegalArgumentException.class).when(policyMappingRepo).findByPolicyId(POLICY_ID);

    assertThrows(IllegalArgumentException.class, () -> policyMappingRepo.findByPolicyId(POLICY_ID));
  }

  @Test
  @DisplayName(
      "Given list of person id and linked policy id "
          + "When we try to delete mapping between policy and person "
          + "Then it returns count of deleted entries.")
  void testDeletePersonIdsForPolicyWithMultiplePersonIds() {
    doReturn(1L).when(policyMappingRepo).deleteByPersonIdAndPolicyId(PERSON_ID_1, POLICY_ID);
    doReturn(1L).when(policyMappingRepo).deleteByPersonIdAndPolicyId(PERSON_ID_2, POLICY_ID);

    long deletedCount =
        policyMappingService.deletePersonIdsForPolicy(List.of(PERSON_ID_1, PERSON_ID_2), POLICY_ID);
    assertEquals(2L, deletedCount);
  }

  @Test
  @DisplayName(
      "Given list of person id and linked policy id "
          + "And one of the person id not available in database "
          + "When we try to delete mapping between policy and person "
          + "Then it returns count of only deleted entries.")
  void testDeletePersonIdsForPolicyWithOnePersonIdNotPresent() {
    doReturn(1L).when(policyMappingRepo).deleteByPersonIdAndPolicyId(PERSON_ID_1, POLICY_ID);
    doReturn(0L).when(policyMappingRepo).deleteByPersonIdAndPolicyId(PERSON_ID_2, POLICY_ID);

    long deletedCount =
        policyMappingService.deletePersonIdsForPolicy(List.of(PERSON_ID_1, PERSON_ID_2), POLICY_ID);
    assertEquals(1L, deletedCount);
  }

  @Test
  @DisplayName(
      "Given list of person id and linked policy id "
          + "When we try to delete mapping between policy and person "
          + "And API throws exception "
          + "Then exception thrown back to the caller.")
  void testDeletePersonIdsForPolicyThrowsException() {
    doThrow(IllegalArgumentException.class)
        .when(policyMappingRepo)
        .deleteByPersonIdAndPolicyId(PERSON_ID_1, POLICY_ID);

    assertThrows(
        IllegalArgumentException.class,
        () ->
            policyMappingService.deletePersonIdsForPolicy(
                List.of(PERSON_ID_1, PERSON_ID_2), POLICY_ID));
  }

  private Policy createPolicy() {
    return Policy.builder()
        .policyId(POLICY_ID)
        .effectiveDate(START_DATE)
        .totalPremium(TOTAL_PREMIUM)
        .build();
  }

  private InsuredPerson createInsuredPerson() {
    return InsuredPerson.builder()
        .firstName(FIRST_NAME_1)
        .secondName(SECOND_NAME_1)
        .premium(PREMIUM_1)
        .id(PERSON_ID_1)
        .build();
  }

  private void validatePolicyMapping(PolicyMapping captorValue) {
    assertEquals(POLICY_ID, captorValue.getPolicyId());
    assertEquals(PERSON_ID_1, captorValue.getPersonId());
    assertEquals(PREMIUM_1, captorValue.getPremium());
  }
}
