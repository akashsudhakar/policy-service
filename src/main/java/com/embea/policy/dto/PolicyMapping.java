package com.embea.policy.dto;

import java.math.BigDecimal;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "policy_mapping")
public class PolicyMapping {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", updatable = false, nullable = false)
  private Long id;

  @Column(name = "policy_id")
  private String policyId;

  @Column(name = "person_id")
  private Long personId;

  @Column(name = "premium")
  private BigDecimal premium;
}
