package com.embea.policy.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.embea.policy.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
public class PolicyControllerIntTest {

  private static final Date CURRENT_DATE = new Date();
  private static final Date OLD_DATE = new Date(CURRENT_DATE.getTime() - 432000000);
  private static final Date START_DATE = new Date(CURRENT_DATE.getTime() + 86400000);
  private static final Date UPDATED_DATE = new Date(START_DATE.getTime() + 432000000);
  private static final String FIRST_NAME_1 = "Jane";
  private static final String SECOND_NAME_1 = "Jackson";
  private static final BigDecimal PREMIUM_1 = BigDecimal.valueOf(12.90);
  private static final String FIRST_NAME_2 = "Jack";
  private static final String SECOND_NAME_2 = "Doe";
  private static final BigDecimal PREMIUM_2 = BigDecimal.valueOf(15.90);
  private static final String FIRST_NAME_3 = "Will";
  private static final String SECOND_NAME_3 = "Smith";
  private static final BigDecimal PREMIUM_3 =
      BigDecimal.valueOf(12.90).setScale(2, RoundingMode.HALF_UP);
  private static final BigDecimal TOTAL_PREMIUM =
      BigDecimal.valueOf(28.80).setScale(2, RoundingMode.HALF_UP);
  private static final String POLICY_CREATE_URL = "/v1/policy/create";
  private static final String POLICY_FETCH_URL = "/v1/policy/fetch";
  private static final String POLICY_MODIFY_URL = "/v1/policy/modify";

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  @DisplayName(
      "Given invalid input type "
          + "When we invoke policy create API "
          + "Then 415 http status code returned.")
  public void testInvalidContentTypeInput() throws Exception {
    mockMvc
        .perform(post(POLICY_CREATE_URL).content("{}"))
        .andExpect(status().isUnsupportedMediaType());
  }

  @Test
  @DisplayName(
      "Given invalid input  "
          + "When we invoke policy create API "
          + "Then 400 http status code returned.")
  public void testCreatePolicyWithInvalidInput() throws Exception {
    PolicyCreationRequest policyCreationRequest = PolicyCreationRequest.builder().build();

    mockMvc
        .perform(
            post(POLICY_CREATE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(policyCreationRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName(
      "Given valid input  "
          + "When we invoke policy create API followed by fetch API on start date and older date "
          + "Then 200 http status code returned for start date "
          + "And policy Id and participant count matches between responses "
          + "And for older date, 404 not found status returned.")
  public void testCreatePolicyWithValidInput() throws Exception {
    PolicyCreationRequest policyCreationRequest = buildPolicyCreationRequest();

    MvcResult mvcResult1 = makeMvcCall(policyCreationRequest, POLICY_CREATE_URL);
    String jsonResponse1 = mvcResult1.getResponse().getContentAsString();
    PolicyCreationResponse policyCreationResponse =
        objectMapper.readValue(jsonResponse1, PolicyCreationResponse.class);
    assertNotNull(policyCreationResponse);

    String policyId = policyCreationResponse.getPolicyId();

    PolicyFetchRequest policyFetchRequest1 =
        PolicyFetchRequest.builder().policyId(policyId).requestDate(START_DATE).build();

    MvcResult mvcResult2 = makeMvcCall(policyFetchRequest1, POLICY_FETCH_URL);
    String jsonResponse2 = mvcResult2.getResponse().getContentAsString();
    PolicyFetchResponse policyFetchResponse1 =
        objectMapper.readValue(jsonResponse2, PolicyFetchResponse.class);
    assertNotNull(policyFetchResponse1);
    assertEquals(policyCreationResponse.getPolicyId(), policyFetchResponse1.getPolicyId());
    assertEquals(TOTAL_PREMIUM, policyFetchResponse1.getTotalPremium());
    assertEquals(2, policyFetchResponse1.getInsuredPersons().size());
    List<String> firstNameList = List.of(FIRST_NAME_1, FIRST_NAME_2);
    List<String> secondNameList = List.of(SECOND_NAME_1, SECOND_NAME_2);
    for (InsuredPerson insuredPerson : policyFetchResponse1.getInsuredPersons()) {
      assertNotNull(insuredPerson.getId());
      assertTrue(firstNameList.contains(insuredPerson.getFirstName()));
      assertTrue(secondNameList.contains(insuredPerson.getSecondName()));
    }

    PolicyFetchRequest policyFetchRequest2 =
        PolicyFetchRequest.builder().policyId(policyId).requestDate(OLD_DATE).build();
    mockMvc
        .perform(
            post(POLICY_CREATE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(policyFetchRequest2)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName(
      "Given valid input  "
          + "When we invoke create policy API with 2 participants on start date "
          + " And then invoke modify policy API with 1 participant added and earlier 2 removed on updated date "
          + "Then 200 http status code returned"
          + "And fetch API insured person count should be 2 on start date "
          + "And fetch API insured person count should be 1 on updated date.")
  public void testModifyPolicyAddingAndRemovingPerson() throws Exception {
    PolicyCreationRequest policyCreationRequest = buildPolicyCreationRequest();

    MvcResult mvcResult1 = makeMvcCall(policyCreationRequest, POLICY_CREATE_URL);
    String jsonResponse1 = mvcResult1.getResponse().getContentAsString();
    PolicyCreationResponse policyCreationResponse =
        objectMapper.readValue(jsonResponse1, PolicyCreationResponse.class);
    assertNotNull(policyCreationResponse);

    String policyId = policyCreationResponse.getPolicyId();

    PolicyModificationRequest policyModificationRequest =
        PolicyModificationRequest.builder()
            .policyId(policyCreationResponse.getPolicyId())
            .effectiveDate(UPDATED_DATE)
            .insuredPersons(
                List.of(
                    InsuredPerson.builder()
                        .firstName(FIRST_NAME_3)
                        .secondName(SECOND_NAME_3)
                        .premium(PREMIUM_3)
                        .build()))
            .build();

    MvcResult mvcResult3 = makeMvcCall(policyModificationRequest, POLICY_MODIFY_URL);
    String jsonResponse3 = mvcResult3.getResponse().getContentAsString();
    PolicyModificationResponse policyModificationResponse =
        objectMapper.readValue(jsonResponse3, PolicyModificationResponse.class);
    assertNotNull(policyModificationResponse);

    PolicyFetchRequest policyFetchRequest1 =
        PolicyFetchRequest.builder().policyId(policyId).requestDate(START_DATE).build();

    MvcResult mvcResult2 = makeMvcCall(policyFetchRequest1, POLICY_FETCH_URL);
    String jsonResponse2 = mvcResult2.getResponse().getContentAsString();
    PolicyFetchResponse policyFetchResponse1 =
        objectMapper.readValue(jsonResponse2, PolicyFetchResponse.class);
    assertNotNull(policyFetchResponse1);
    assertEquals(policyCreationResponse.getPolicyId(), policyFetchResponse1.getPolicyId());
    assertEquals(TOTAL_PREMIUM, policyFetchResponse1.getTotalPremium());
    assertEquals(2, policyFetchResponse1.getInsuredPersons().size());
    List<String> firstNameList = List.of(FIRST_NAME_1, FIRST_NAME_2);
    List<String> secondNameList = List.of(SECOND_NAME_1, SECOND_NAME_2);
    for (InsuredPerson insuredPerson : policyFetchResponse1.getInsuredPersons()) {
      assertNotNull(insuredPerson.getId());
      assertTrue(firstNameList.contains(insuredPerson.getFirstName()));
      assertTrue(secondNameList.contains(insuredPerson.getSecondName()));
    }

    PolicyFetchRequest policyFetchRequest2 =
        PolicyFetchRequest.builder().policyId(policyId).requestDate(UPDATED_DATE).build();

    MvcResult mvcResult4 = makeMvcCall(policyFetchRequest2, POLICY_FETCH_URL);
    String jsonResponse4 = mvcResult4.getResponse().getContentAsString();
    PolicyFetchResponse policyFetchResponse2 =
        objectMapper.readValue(jsonResponse4, PolicyFetchResponse.class);
    assertNotNull(policyFetchResponse2);
    assertEquals(policyModificationResponse.getPolicyId(), policyFetchResponse2.getPolicyId());
    assertEquals(PREMIUM_3, policyFetchResponse2.getTotalPremium());
    assertEquals(1, policyFetchResponse2.getInsuredPersons().size());
    for (InsuredPerson insuredPerson : policyFetchResponse2.getInsuredPersons()) {
      assertNotNull(insuredPerson.getId());
      assertEquals(FIRST_NAME_3, insuredPerson.getFirstName());
      assertEquals(SECOND_NAME_3, insuredPerson.getSecondName());
    }
  }

  private MvcResult makeMvcCall(Object request, String policyCreateUrl) throws Exception {
    return mockMvc
        .perform(
            post(policyCreateUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andReturn();
  }

  private PolicyCreationRequest buildPolicyCreationRequest() {
    return PolicyCreationRequest.builder()
        .startDate(START_DATE)
        .insuredPersons(getInsuredPersonList())
        .build();
  }

  private List<InsuredPerson> getInsuredPersonList() {
    InsuredPerson insuredPerson1 =
        InsuredPerson.builder()
            .firstName(FIRST_NAME_1)
            .secondName(SECOND_NAME_1)
            .premium(PREMIUM_1)
            .build();
    InsuredPerson insuredPerson2 =
        InsuredPerson.builder()
            .firstName(FIRST_NAME_2)
            .secondName(SECOND_NAME_2)
            .premium(PREMIUM_2)
            .build();
    List<InsuredPerson> insuredPersonList = new ArrayList<>();
    insuredPersonList.add(insuredPerson1);
    insuredPersonList.add(insuredPerson2);
    return insuredPersonList;
  }
}
