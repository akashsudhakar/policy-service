package com.embea.policy.repository;

import com.embea.policy.dto.PolicyMapping;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyMappingRepo extends CrudRepository<PolicyMapping, Long> {
  List<PolicyMapping> findByPolicyId(String policyId);

  @Query(
      "select pm from PolicyMapping as pm where policyId = :policyId and additionDate <= :requestDate and (removalDate > :requestDate or removalDate is null)")
  List<PolicyMapping> findByPolicyIdAndRequestDate(String policyId, Date requestDate);

  @Modifying
  @Query(
      "update PolicyMapping pm set pm.removalDate = :effectiveDate where pm.policyId = :policyId and pm.personId in :personIds")
  int setRemovalDateByPolicyIdAndPersonIds(
      List<Long> personIds, String policyId, Date effectiveDate);
}
