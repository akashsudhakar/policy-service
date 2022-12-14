package com.embea.policy.services;

import com.embea.policy.dto.Person;
import com.embea.policy.model.InsuredPerson;
import com.embea.policy.repository.PersonRepo;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@AllArgsConstructor
public class PersonService {

  private final PersonRepo personRepo;

  /**
   * Store person details to database
   *
   * @param insuredPerson Insured person
   * @return Created person object
   */
  public Person storePersonEntry(InsuredPerson insuredPerson) {
    Person createdPerson = personRepo.save(createPersonObject(insuredPerson));
    log.debug("Person created with Id {}", createdPerson.getPersonId());
    return createdPerson;
  }

  /**
   * Find person using person id
   *
   * @param personId Person id
   * @return Fetched person object
   */
  public Person getPerson(Long personId) {
    return personRepo.findById(personId).orElse(null);
  }

  private Person createPersonObject(InsuredPerson insuredPerson) {
    return Person.builder()
        .firstName(insuredPerson.getFirstName())
        .secondName(insuredPerson.getSecondName())
        .build();
  }
}
