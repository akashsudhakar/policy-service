package com.embea.policy.services;

import com.embea.policy.dto.Policy;
import com.embea.policy.dto.PolicyMapping;
import com.embea.policy.model.InsuredPerson;
import com.embea.policy.repository.PolicyMappingRepo;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@AllArgsConstructor
public class PolicyMappingService {

  private final PolicyMappingRepo policyMappingRepo;

  public PolicyMapping storePolicyMapping(Policy policy, InsuredPerson insuredPerson) {
    PolicyMapping insertedPolicyMapping =
        policyMappingRepo.save(createPolicyMapping(policy, insuredPerson));
    log.debug("PolicyMapping created with Id {}", insertedPolicyMapping.getId());
    return insertedPolicyMapping;
  }

  public List<PolicyMapping> findPersonsForPolicy(String policyId) {
    return policyMappingRepo.findByPolicyId(policyId);
  }

  public Long deletePersonIdsForPolicy(List<Long> personIds, String policyId) {
    long deletedCount = 0;
    for (Long personId : personIds) {
      deletedCount += policyMappingRepo.deleteByPersonIdAndPolicyId(personId, policyId);
    }
    return deletedCount;
  }

  private PolicyMapping createPolicyMapping(Policy policy, InsuredPerson insuredPerson) {
    return PolicyMapping.builder()
        .policyId(policy.getPolicyId())
        .personId(insuredPerson.getId())
        .premium(insuredPerson.getPremium())
        .build();
  }
}
