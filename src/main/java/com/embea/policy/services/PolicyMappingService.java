package com.embea.policy.services;

import com.embea.policy.dto.PolicyMapping;
import com.embea.policy.model.InsuredPerson;
import com.embea.policy.repository.PolicyMappingRepo;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@AllArgsConstructor
public class PolicyMappingService {

  private final PolicyMappingRepo policyMappingRepo;

  /**
   * API to store policy mapping
   *
   * @param policyId Policy Id
   * @param insuredPerson Person to be mapped with policy
   * @param additionDate Date on which policy is active
   * @return PolicyMapping Object
   */
  public PolicyMapping storePolicyMapping(
      String policyId, InsuredPerson insuredPerson, Date additionDate) {
    PolicyMapping insertedPolicyMapping =
        policyMappingRepo.save(createPolicyMapping(policyId, insuredPerson, additionDate));
    log.debug("PolicyMapping created with Id {}", insertedPolicyMapping.getId());
    return insertedPolicyMapping;
  }

  /**
   * Find all persons related to a policy
   *
   * @param policyId Policy Id
   * @return List of PolicyMapping Object
   */
  public List<PolicyMapping> findPersonsForPolicy(String policyId) {
    return policyMappingRepo.findByPolicyId(policyId);
  }

  /**
   * Find all persons related to a policy from request date
   *
   * @param policyId Policy Id
   * @param requestDate Request date
   * @return List of PolicyMapping Object
   */
  public List<PolicyMapping> findPersonsForPolicyAndRequestDate(String policyId, Date requestDate) {
    return policyMappingRepo.findByPolicyIdAndRequestDate(policyId, requestDate);
  }

  /**
   * Remove mapping between person and policy from effective date
   *
   * @param personIds Ids of persons to remove
   * @param policyId Policy Id
   * @param effectiveDate Effective date
   * @return Number of persons removed
   */
  public Integer removePersonsFromPolicy(
      List<Long> personIds, String policyId, Date effectiveDate) {
    return policyMappingRepo.setRemovalDateByPolicyIdAndPersonIds(
        personIds, policyId, effectiveDate);
  }

  private PolicyMapping createPolicyMapping(
      String policyId, InsuredPerson insuredPerson, Date additionDate) {
    return PolicyMapping.builder()
        .policyId(policyId)
        .personId(insuredPerson.getId())
        .premium(insuredPerson.getPremium())
        .additionDate(additionDate)
        .build();
  }
}
