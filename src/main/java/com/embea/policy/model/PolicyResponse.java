package com.embea.policy.model;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class PolicyResponse {
  private String policyId;
  private List<InsuredPerson> insuredPersons;
  private BigDecimal totalPremium;
}
