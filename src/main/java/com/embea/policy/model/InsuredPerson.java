package com.embea.policy.model;

import java.math.BigDecimal;
import java.util.Objects;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InsuredPerson implements Comparable<InsuredPerson> {
  private Long id;

  @NotBlank(message = "First Name is mandatory")
  private String firstName;

  @NotBlank(message = "Second Name is mandatory")
  private String secondName;

  @DecimalMin(value = "0.01", message = "Premium should be greater than 0")
  @Digits(integer = 10, fraction = 2, message = "Provide valid amount in the format 10.25")
  private BigDecimal premium;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    InsuredPerson insuredPerson = (InsuredPerson) o;
    return Objects.equals(id, insuredPerson.id)
        && Objects.equals(firstName, insuredPerson.firstName)
        && Objects.equals(secondName, insuredPerson.secondName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, firstName, secondName);
  }

  @Override
  public int compareTo(InsuredPerson o) {
    return (int) (this.getId() - o.getId());
  }
}
