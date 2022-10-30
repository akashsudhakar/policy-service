package com.embea.policy.facade;

import com.embea.policy.dto.Person;
import com.embea.policy.dto.Policy;
import com.embea.policy.dto.PolicyMapping;
import com.embea.policy.exception.PolicyNotFoundException;
import com.embea.policy.model.*;
import com.embea.policy.services.PersonService;
import com.embea.policy.services.PolicyMappingService;
import com.embea.policy.services.PolicyService;
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

  public PolicyResponse createPolicy(PolicyCreationRequest policyCreationRequest) {
    log.info(
        String.format(
            "Going to create policy with start date %s", policyCreationRequest.getStartDate()));
    Policy createdPolicy = policyService.insertPolicy(policyCreationRequest);
    PolicyCreationResponse policyResponse =
        insertPersonAndMapping(policyCreationRequest, createdPolicy);
    createdPolicy.setTotalPremium(policyResponse.getTotalPremium());
    policyService.savePolicy(createdPolicy);

    return policyResponse.toBuilder()
        .policyId(createdPolicy.getPolicyId())
        .startDate(createdPolicy.getEffectiveDate())
        .build();
  }

  @Transactional
  public PolicyResponse modifyPolicy(PolicyModificationRequest policyModificationRequest) {
    String policyId = policyModificationRequest.getPolicyId();
    Policy savedPolicy = policyService.getPolicy(policyId);
    if (savedPolicy == null) {
      throw new PolicyNotFoundException(
          String.format("No policy found with id - %s", policyModificationRequest.getPolicyId()));
    } else {
      if (!policyModificationRequest.getEffectiveDate().equals(savedPolicy.getEffectiveDate())) {
        savedPolicy.setEffectiveDate(policyModificationRequest.getEffectiveDate());
        policyService.savePolicy(savedPolicy);
      }
      Set<InsuredPerson> insuredPersons = new TreeSet<>();
      List<Long> idsPresent = new ArrayList<>();
      Double totalPremium = 0.0;
      for (InsuredPerson insuredPerson : policyModificationRequest.getInsuredPersons()) {
        if (insuredPerson.getId() == null) {
          Person storePersonEntry = personService.storePersonEntry(insuredPerson);
          insuredPerson.setId(storePersonEntry.getPersonId());
          policyMappingService.storePolicyMapping(savedPolicy, insuredPerson);
        }
        insuredPersons.add(insuredPerson);
        idsPresent.add(insuredPerson.getId());
        totalPremium += insuredPerson.getPremium();
      }
      List<PolicyMapping> policyMappings = policyMappingService.findPersonsForPolicy(policyId);
      List<Long> idsToRemove = new ArrayList<>();
      for (PolicyMapping policyMapping : policyMappings) {
        if (!idsPresent.contains(policyMapping.getPersonId())) {
          idsToRemove.add(policyMapping.getPersonId());
        }
      }
      policyMappingService.deletePersonIdsForPolicy(idsToRemove, policyId);
      return buildPolicyModificationResponse(
          policyModificationRequest, insuredPersons, totalPremium);
    }
  }

  public PolicyResponse fetchPolicy(PolicyFetchRequest policyFetchRequest) {
    Policy fetchedPolicy = policyService.getPolicy(policyFetchRequest.getPolicyId());
    if (fetchedPolicy == null) {
      throw new PolicyNotFoundException(
          String.format("No policy found with id - %s", policyFetchRequest.getPolicyId()));
    } else {
      List<PolicyMapping> policyMappings =
          policyMappingService.findPersonsForPolicy(fetchedPolicy.getPolicyId());
      Set<InsuredPerson> insuredPersons = new TreeSet<>();
      Double totalPremium = populatePersonDetails(policyMappings, insuredPersons);
      return buildPolicyFetchResponse(
          fetchedPolicy,
          insuredPersons,
          totalPremium,
          policyFetchRequest.getRequestDate() != null
              ? policyFetchRequest.getRequestDate()
              : new Date(Instant.now().toEpochMilli()));
    }
  }

  private PolicyCreationResponse insertPersonAndMapping(
      PolicyCreationRequest policyCreationRequest, Policy createdPolicy) {
    Double totalPremium = 0.0;
    for (InsuredPerson insuredPerson : policyCreationRequest.getInsuredPersons()) {
      Person storedPerson = personService.storePersonEntry(insuredPerson);
      insuredPerson.setId(storedPerson.getPersonId());
      totalPremium += insuredPerson.getPremium();
      policyMappingService.storePolicyMapping(createdPolicy, insuredPerson);
    }
    return PolicyCreationResponse.builder()
        .insuredPersons(policyCreationRequest.getInsuredPersons())
        .totalPremium(totalPremium)
        .build();
  }

  private Double populatePersonDetails(
      List<PolicyMapping> policyMappings, Set<InsuredPerson> insuredPersons) {
    Double totalPremium = 0.0;
    for (PolicyMapping policyMapping : policyMappings) {
      Person person = personService.getPerson(policyMapping.getPersonId());
      if (person != null) {
        insuredPersons.add(
            InsuredPerson.builder()
                .id(person.getPersonId())
                .firstName(person.getFirstName())
                .secondName(person.getSecondName())
                .premium(policyMapping.getPremium())
                .build());
        totalPremium += policyMapping.getPremium();
      }
    }
    return totalPremium;
  }

  private PolicyModificationResponse buildPolicyModificationResponse(
      PolicyModificationRequest policyModificationRequest,
      Set<InsuredPerson> insuredPersons,
      Double totalPremium) {
    return PolicyModificationResponse.builder()
        .policyId(policyModificationRequest.getPolicyId())
        .insuredPersons(insuredPersons)
        .effectiveDate(policyModificationRequest.getEffectiveDate())
        .totalPremium(totalPremium)
        .build();
  }

  private PolicyFetchResponse buildPolicyFetchResponse(
      Policy fetchedPolicy,
      Set<InsuredPerson> insuredPersons,
      Double totalPremium,
      Date requestDate) {
    return PolicyFetchResponse.builder()
        .policyId(fetchedPolicy.getPolicyId())
        .insuredPersons(insuredPersons)
        .totalPremium(totalPremium)
        .requestDate(requestDate)
        .build();
  }
}
