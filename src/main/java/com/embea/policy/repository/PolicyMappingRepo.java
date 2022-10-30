package com.embea.policy.repository;

import com.embea.policy.dto.PolicyMapping;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyMappingRepo extends CrudRepository<PolicyMapping, Long> {
  List<PolicyMapping> findByPolicyId(String policyId);

  long deleteByPersonIdAndPolicyId(Long personId, String policyId);
}
