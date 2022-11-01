package com.embea.policy.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.embea.policy.dto.Policy;
import com.embea.policy.model.InsuredPerson;
import com.embea.policy.model.PolicyCreationRequest;
import com.embea.policy.repository.PolicyRepo;
import java.math.BigDecimal;
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
class PolicyServiceTest {

  private static final Date START_DATE = new Date();
  private static final String FIRST_NAME_1 = "Jane";
  private static final String SECOND_NAME_1 = "Jackson";
  private static final BigDecimal PREMIUM_1 = BigDecimal.valueOf(12.90);
  private static final String FIRST_NAME_2 = "Jack";
  private static final String SECOND_NAME_2 = "Doe";
  private static final BigDecimal PREMIUM_2 = BigDecimal.valueOf(15.90);
  private static final String POLICY_ID = UUID.randomUUID().toString();

  @Mock private PolicyRepo policyRepo;

  @InjectMocks private PolicyService policyService;

  @Mock private Policy mockPolicy;

  @Captor private ArgumentCaptor<Policy> policyCaptor;

  @Test
  @DisplayName(
      "Given create policy request with valid inputs "
          + "When we try to insert policy into database "
          + "Then it returns inserted Policy object.")
  void testInsertPolicyWithValidInputs() {
    PolicyCreationRequest policyCreationRequest = buildPolicyCreationRequest();
    doReturn(mockPolicy).when(policyRepo).save(any(Policy.class));

    Policy insertedPolicy = policyService.insertPolicy(policyCreationRequest);

    assertEquals(mockPolicy, insertedPolicy);
    verify(policyRepo).save(policyCaptor.capture());
    Policy policyCaptorValue = policyCaptor.getValue();
    assertEquals(policyCreationRequest.getStartDate(), policyCaptorValue.getStartDate());
  }

  @Test
  @DisplayName(
      "Given create policy request with invalid inputs "
          + "When we try to insert policy into database "
          + "Then throws IllegalArgumentException back to the caller.")
  void testInsertPolicyWithInvalidInputs() {
    PolicyCreationRequest policyCreationRequest = buildPolicyCreationRequest();
    doThrow(IllegalArgumentException.class).when(policyRepo).save(any(Policy.class));

    assertThrows(
        IllegalArgumentException.class, () -> policyService.insertPolicy(policyCreationRequest));

    verify(policyRepo).save(policyCaptor.capture());
    Policy policyCaptorValue = policyCaptor.getValue();
    assertEquals(policyCreationRequest.getStartDate(), policyCaptorValue.getStartDate());
  }

  @Test
  @DisplayName(
      "Given valid policy object "
          + "When we try to save policy into database "
          + "Then it executes successfully and returns saved Policy.")
  void testSavePolicyWithValidInputs() {
    doReturn(mockPolicy).when(policyRepo).save(any(Policy.class));

    Policy insertedPolicy = policyService.savePolicy(mockPolicy);

    assertEquals(mockPolicy, insertedPolicy);
    verify(policyRepo).save(mockPolicy);
  }

  @Test
  @DisplayName(
      "Given invalid policy object "
          + "When we try to save policy into database "
          + "Then throws IllegalArgumentException back to the caller.")
  void testSavePolicyWithInvalidInputs() {
    doThrow(IllegalArgumentException.class).when(policyRepo).save(any(Policy.class));

    assertThrows(IllegalArgumentException.class, () -> policyService.savePolicy(mockPolicy));

    verify(policyRepo).save(mockPolicy);
  }

  @Test
  @DisplayName(
      "Given valid policy id "
          + "When we try to fetch policy from database "
          + "Then it executes successfully and returns saved Policy.")
  void testGetPolicyReturnsPolicy() {
    doReturn(Optional.of(mockPolicy)).when(policyRepo).findById(POLICY_ID);

    Policy policy = policyService.getPolicy(POLICY_ID);

    assertEquals(mockPolicy, policy);
    verify(policyRepo).findById(POLICY_ID);
  }

  @Test
  @DisplayName(
      "Given invalid policy id "
          + "When we try to fetch policy from database "
          + "Then it executes successfully, but does not return any Policy.")
  void testGetPolicyDoesNotReturnsPolicy() {
    doReturn(Optional.empty()).when(policyRepo).findById(POLICY_ID);

    Policy policy = policyService.getPolicy(POLICY_ID);

    assertNull(policy);
    verify(policyRepo).findById(POLICY_ID);
  }

  @Test
  @DisplayName(
      "Given null policy id "
          + "When we try to fetch policy from database "
          + "Then throws IllegalArgumentException back to the caller.")
  void testGetPolicyThrowsException() {
    doThrow(IllegalArgumentException.class).when(policyRepo).findById(null);

    assertThrows(IllegalArgumentException.class, () -> policyService.getPolicy(null));

    verify(policyRepo).findById(null);
  }

  private PolicyCreationRequest buildPolicyCreationRequest() {
    return PolicyCreationRequest.builder()
        .startDate(START_DATE)
        .insuredPersons(getInsuredPersonList())
        .build();
  }

  private List<InsuredPerson> getInsuredPersonList() {
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
    List<InsuredPerson> insuredPersonList = new ArrayList<>();
    insuredPersonList.add(insuredPerson1);
    insuredPersonList.add(insuredPerson2);
    return insuredPersonList;
  }
}
