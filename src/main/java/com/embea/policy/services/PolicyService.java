package com.embea.policy.services;

import com.embea.policy.dto.Policy;
import com.embea.policy.model.PolicyCreationRequest;
import com.embea.policy.repository.PolicyRepo;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@AllArgsConstructor
public class PolicyService {

  private final PolicyRepo policyRepo;

  public Policy insertPolicy(PolicyCreationRequest policyCreationRequest) {
    Policy createdPolicy = policyRepo.save(buildPolicyObject(policyCreationRequest));
    log.debug("Policy created with policy Id {}", createdPolicy.getPolicyId());
    return createdPolicy;
  }

  public Policy savePolicy(Policy policy) {
    return policyRepo.save(policy);
  }

  public Policy getPolicy(String policyId) {
    return policyRepo.findById(policyId).orElse(null);
  }

  private Policy buildPolicyObject(PolicyCreationRequest policyCreationRequest) {
    return Policy.builder().effectiveDate(policyCreationRequest.getStartDate()).build();
  }
}
