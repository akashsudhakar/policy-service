package com.embea.policy.model;

import com.embea.policy.utils.Constants;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class PolicyModificationRequest {

  @NotBlank(message = "Policy Id is mandatory")
  private String policyId;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATE_FORMAT)
  @FutureOrPresent(message = "effectiveDate must be a date in the present or in the future")
  @NonNull
  private Date effectiveDate;

  @Valid
  @NotEmpty(message = "Atleast 1 person should be present")
  private Set<InsuredPerson> insuredPersons;
}
