package com.embea.policy.services;

import com.embea.policy.dto.Policy;
import com.embea.policy.model.PolicyCreationRequest;
import com.embea.policy.repository.PolicyRepo;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@AllArgsConstructor
public class PolicyService {

  private final PolicyRepo policyRepo;

  /**
   * Insert new policy to database
   *
   * @param policyCreationRequest Policy creation request with insured persons and start date
   * @return Newly created policy object
   */
  public Policy insertPolicy(PolicyCreationRequest policyCreationRequest) {
    Policy createdPolicy = policyRepo.save(buildPolicyObject(policyCreationRequest));
    log.debug("Policy created with policy Id {}", createdPolicy.getPolicyId());
    return createdPolicy;
  }

  /**
   * Update policy with latest information
   *
   * @param policy Policy Object
   * @return Updated policy object
   */
  public Policy savePolicy(Policy policy) {
    return policyRepo.save(policy);
  }

  /**
   * Fetch policy using policy id
   *
   * @param policyId Policy Id
   * @return Fetched policy object
   */
  public Policy getPolicy(String policyId) {
    return policyRepo.findById(policyId).orElse(null);
  }

  /**
   * Fetch policy using policy id and effective date
   *
   * @param policyId Policy Id
   * @param effectiveDate Effective date
   * @return Fetched policy object
   */
  public Policy getPolicy(String policyId, Date effectiveDate) {
    return policyRepo
        .getPolicyByPolicyIdAndEffectiveDateBefore(policyId, effectiveDate)
        .orElse(null);
  }

  private Policy buildPolicyObject(PolicyCreationRequest policyCreationRequest) {
    return Policy.builder().startDate(policyCreationRequest.getStartDate()).build();
  }
}
