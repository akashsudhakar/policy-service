package com.embea.policy.repository;

import com.embea.policy.dto.Policy;
import java.util.Date;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyRepo extends CrudRepository<Policy, String> {

  @Query("select a from Policy as a where policyId = :policyId and startDate <= :effectiveDate")
  Optional<Policy> getPolicyByPolicyIdAndEffectiveDateBefore(String policyId, Date effectiveDate);
}
