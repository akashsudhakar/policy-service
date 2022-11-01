package com.embea.policy.model;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
public class PolicyResponse {
  private String policyId;
  private List<InsuredPerson> insuredPersons;
  private BigDecimal totalPremium;
}
