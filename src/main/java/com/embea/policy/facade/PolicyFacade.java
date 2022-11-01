package com.embea.policy.facade;

import com.embea.policy.dto.Person;
import com.embea.policy.dto.Policy;
import com.embea.policy.dto.PolicyMapping;
import com.embea.policy.exception.PolicyNotFoundException;
import com.embea.policy.model.*;
import com.embea.policy.services.PersonService;
import com.embea.policy.services.PolicyMappingService;
import com.embea.policy.services.PolicyService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Log4j2
@AllArgsConstructor
public class PolicyFacade {

  private final PolicyService policyService;
  private final PersonService personService;
  private final PolicyMappingService policyMappingService;

  /**
   * API to create policies.
   *
   * @param policyCreationRequest Policy creation request with start date and insured persons
   * @return Policy creation response with created policy id and insured person ids
   */
  public PolicyResponse createPolicy(PolicyCreationRequest policyCreationRequest) {
    log.info(
        String.format(
            "Going to create policy with start date %s", policyCreationRequest.getStartDate()));
    Policy createdPolicy = policyService.insertPolicy(policyCreationRequest);
    PolicyCreationResponse policyResponse =
        insertPersonAndMapping(policyCreationRequest, createdPolicy);
    policyService.savePolicy(createdPolicy);

    return policyResponse.toBuilder()
        .policyId(createdPolicy.getPolicyId())
        .startDate(createdPolicy.getStartDate())
        .build();
  }

  /**
   * API to modify policies. Throws PolicyNotFoundException if policy is not found for the provided
   * effective date. If insured person present in request without id, that person will be added. If
   * any old insured person not part of current request, that person will be removed from policy
   *
   * @param policyModificationRequest Policy modification request with to be updated information and
   *     effective date
   * @return Policy modification response with effective data
   */
  @Transactional
  public PolicyResponse modifyPolicy(PolicyModificationRequest policyModificationRequest) {
    String policyId = policyModificationRequest.getPolicyId();
    Date effectiveDate = policyModificationRequest.getEffectiveDate();
    Policy savedPolicy = policyService.getPolicy(policyId, effectiveDate);
    if (savedPolicy == null) {
      throw new PolicyNotFoundException(
          String.format(
              "No policy found with id - %s on effective date %s",
              policyModificationRequest.getPolicyId(), effectiveDate));
    } else {
      List<InsuredPerson> insuredPersons = new ArrayList<>();
      List<Long> idsPresent = new ArrayList<>();
      BigDecimal totalPremium = new BigDecimal("0.0");
      for (InsuredPerson insuredPerson : policyModificationRequest.getInsuredPersons()) {
        if (insuredPerson.getId() == null) {
          Person storePersonEntry = personService.storePersonEntry(insuredPerson);
          insuredPerson.setId(storePersonEntry.getPersonId());
          policyMappingService.storePolicyMapping(policyId, insuredPerson, effectiveDate);
        }
        insuredPersons.add(insuredPerson);
        idsPresent.add(insuredPerson.getId());
        totalPremium = totalPremium.add(insuredPerson.getPremium());
      }
      List<PolicyMapping> policyMappings = policyMappingService.findPersonsForPolicy(policyId);
      List<Long> idsToRemove = new ArrayList<>();
      for (PolicyMapping policyMapping : policyMappings) {
        if (!idsPresent.contains(policyMapping.getPersonId())) {
          idsToRemove.add(policyMapping.getPersonId());
        }
      }
      policyMappingService.removePersonsFromPolicy(idsToRemove, policyId, effectiveDate);
      return buildPolicyModificationResponse(
          policyModificationRequest, insuredPersons, totalPremium);
    }
  }

  /**
   * API to fetch policies as on request date. If no policy found for that policy id and request
   * date, PolicyNotFoundException is thrown. If no date is passed, then current date is considered
   * as request date.
   *
   * @param policyFetchRequest Policy fetch request with policy Id and optional request date
   * @return Policy fetch response with policy details as on request date
   */
  public PolicyResponse fetchPolicy(PolicyFetchRequest policyFetchRequest) {
    Date requestDate =
        policyFetchRequest.getRequestDate() != null
            ? policyFetchRequest.getRequestDate()
            : new Date(Instant.now().toEpochMilli());
    Policy fetchedPolicy = policyService.getPolicy(policyFetchRequest.getPolicyId(), requestDate);
    if (fetchedPolicy == null) {
      throw new PolicyNotFoundException(
          String.format(
              "No policy found with id - %s on request date %s",
              policyFetchRequest.getPolicyId(), requestDate));
    } else {
      List<PolicyMapping> policyMappings =
          policyMappingService.findPersonsForPolicyAndRequestDate(
              fetchedPolicy.getPolicyId(), requestDate);
      List<InsuredPerson> insuredPersons = new ArrayList<>();
      BigDecimal totalPremium = populatePersonDetails(policyMappings, insuredPersons);
      return buildPolicyFetchResponse(fetchedPolicy, insuredPersons, totalPremium, requestDate);
    }
  }

  private PolicyCreationResponse insertPersonAndMapping(
      PolicyCreationRequest policyCreationRequest, Policy createdPolicy) {
    BigDecimal totalPremium = new BigDecimal("0.0");
    for (InsuredPerson insuredPerson : policyCreationRequest.getInsuredPersons()) {
      Person storedPerson = personService.storePersonEntry(insuredPerson);
      insuredPerson.setId(storedPerson.getPersonId());
      totalPremium = totalPremium.add(insuredPerson.getPremium());
      policyMappingService.storePolicyMapping(
          createdPolicy.getPolicyId(), insuredPerson, createdPolicy.getStartDate());
    }
    return PolicyCreationResponse.builder()
        .insuredPersons(policyCreationRequest.getInsuredPersons())
        .totalPremium(totalPremium)
        .build();
  }

  private BigDecimal populatePersonDetails(
      List<PolicyMapping> policyMappings, List<InsuredPerson> insuredPersons) {
    BigDecimal totalPremium = new BigDecimal("0.0");
    for (PolicyMapping policyMapping : policyMappings) {
      Person person = personService.getPerson(policyMapping.getPersonId());
      if (person != null) {
        insuredPersons.add(getInsuredPerson(policyMapping, person));
        totalPremium = totalPremium.add(policyMapping.getPremium());
      }
    }
    return totalPremium;
  }

  private InsuredPerson getInsuredPerson(PolicyMapping policyMapping, Person person) {
    return InsuredPerson.builder()
        .id(person.getPersonId())
        .firstName(person.getFirstName())
        .secondName(person.getSecondName())
        .premium(policyMapping.getPremium())
        .build();
  }

  private PolicyModificationResponse buildPolicyModificationResponse(
      PolicyModificationRequest policyModificationRequest,
      List<InsuredPerson> insuredPersons,
      BigDecimal totalPremium) {
    return PolicyModificationResponse.builder()
        .policyId(policyModificationRequest.getPolicyId())
        .insuredPersons(insuredPersons)
        .effectiveDate(policyModificationRequest.getEffectiveDate())
        .totalPremium(totalPremium)
        .build();
  }

  private PolicyFetchResponse buildPolicyFetchResponse(
      Policy fetchedPolicy,
      List<InsuredPerson> insuredPersons,
      BigDecimal totalPremium,
      Date requestDate) {
    return PolicyFetchResponse.builder()
        .policyId(fetchedPolicy.getPolicyId())
        .insuredPersons(insuredPersons)
        .totalPremium(totalPremium)
        .requestDate(requestDate)
        .build();
  }
}
