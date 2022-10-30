package com.embea.policy.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.embea.policy.facade.PolicyFacade;
import com.embea.policy.model.PolicyCreationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class PolicyControllerIntTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private PolicyFacade policyFacade;

  @Test
  @DisplayName(
      "Given invalid input type "
          + "When we invoke policy create API "
          + "Then 415 http status code returned.")
  public void testInvalidContentTypeInput() throws Exception {
    mockMvc
        .perform(post("/v1/policy/create").content("{}"))
        .andExpect(status().isUnsupportedMediaType());
  }

  @Test
  @DisplayName(
      "Given Reindexing Gateway API is called "
          + "When valid inputs are provided "
          + "Then it returns status as 200")
  public void testCreatePolicyWithValidInput() throws Exception {
    PolicyCreationRequest policyCreationRequest = PolicyCreationRequest.builder().build();

    mockMvc
        .perform(
            post("/v1/policy/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(policyCreationRequest)))
        .andExpect(status().isBadRequest());
  }
}
