package com.embea.policy.exception;

/** Exception thrown when policy is not found in the system. */
public class PolicyNotFoundException extends RuntimeException {

  public PolicyNotFoundException(String message) {
    super(message);
  }
}
