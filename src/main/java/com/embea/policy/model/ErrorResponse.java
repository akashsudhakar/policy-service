package com.embea.policy.model;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
  private final Date timestamp;

  private final String message;

  private final String details;
}
