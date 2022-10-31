package com.embea.policy.model;

import java.math.BigDecimal;
import java.util.Set;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
public class PolicyResponse {
  private String policyId;
  private Set<InsuredPerson> insuredPersons;
  private BigDecimal totalPremium;
}
