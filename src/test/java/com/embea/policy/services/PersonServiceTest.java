package com.embea.policy.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.embea.policy.dto.Person;
import com.embea.policy.model.InsuredPerson;
import com.embea.policy.repository.PersonRepo;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

  private static final String FIRST_NAME = "Jane";
  private static final String SECOND_NAME = "Jackson";
  private static final Long PERSON_ID = 1L;

  @Mock private PersonRepo personRepo;

  @InjectMocks private PersonService personService;

  @Mock private Person mockPerson;

  @Captor private ArgumentCaptor<Person> personArgumentCaptor;

  @Test
  @DisplayName(
      "Given valid insured person object "
          + "When we try to insert person into database "
          + "Then it returns inserted Person object.")
  void testStorePersonEntryWithValidValues() {
    InsuredPerson insuredPerson = createInsuredPerson();
    doReturn(mockPerson).when(personRepo).save(any(Person.class));

    Person storePersonEntry = personService.storePersonEntry(insuredPerson);
    assertEquals(mockPerson, storePersonEntry);
    verify(personRepo).save(personArgumentCaptor.capture());
    Person captorValue = personArgumentCaptor.getValue();
    validatePersonObject(captorValue);
  }

  @Test
  @DisplayName(
      "Given invalid insured person object "
          + "When we try to insert person into database "
          + "Then throws IllegalArgumentException back to the caller.")
  void testStorePersonEntryWithInvalidValues() {
    InsuredPerson insuredPerson = createInsuredPerson();
    doThrow(IllegalArgumentException.class).when(personRepo).save(any(Person.class));

    assertThrows(
        IllegalArgumentException.class, () -> personService.storePersonEntry(insuredPerson));

    verify(personRepo).save(personArgumentCaptor.capture());
    Person captorValue = personArgumentCaptor.getValue();
    validatePersonObject(captorValue);
  }

  @Test
  @DisplayName(
      "Given valid person id "
          + "When we try to fetch person from database "
          + "Then it executes successfully and returns saved person.")
  void testGetPersonWithValidValues() {
    doReturn(Optional.of(mockPerson)).when(personRepo).findById(PERSON_ID);

    Person person = personService.getPerson(PERSON_ID);

    assertEquals(mockPerson, person);
    verify(personRepo).findById(PERSON_ID);
  }

  @Test
  @DisplayName(
      "Given invalid person id "
          + "When we try to fetch person from database "
          + "Then it executes successfully, but does not return any Person.")
  void testGetPolicyDoesNotReturnsPolicy() {
    doReturn(Optional.empty()).when(personRepo).findById(PERSON_ID);

    Person person = personService.getPerson(PERSON_ID);

    assertNull(person);
    verify(personRepo).findById(PERSON_ID);
  }

  @Test
  @DisplayName(
      "Given null person id "
          + "When we try to fetch person from database "
          + "Then throws IllegalArgumentException back to the caller.")
  void testGetPolicyThrowsException() {
    doThrow(IllegalArgumentException.class).when(personRepo).findById(null);

    assertThrows(IllegalArgumentException.class, () -> personService.getPerson(null));

    verify(personRepo).findById(null);
  }

  private InsuredPerson createInsuredPerson() {
    return InsuredPerson.builder().firstName(FIRST_NAME).secondName(SECOND_NAME).build();
  }

  private void validatePersonObject(Person captorValue) {
    assertEquals(FIRST_NAME, captorValue.getFirstName());
    assertEquals(SECOND_NAME, captorValue.getSecondName());
  }
}
