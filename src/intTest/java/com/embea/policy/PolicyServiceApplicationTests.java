package com.embea.policy;

import static org.assertj.core.api.Assertions.assertThat;

import com.embea.policy.controllers.PolicyController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PolicyServiceApplicationTests {

  @Autowired private PolicyController policyController;

  @Test
  @DisplayName(
      "Test to verify spring boot application is starting up and controller is initialized")
  void contextLoads() {
    assertThat(policyController).isNotNull();
  }
}
