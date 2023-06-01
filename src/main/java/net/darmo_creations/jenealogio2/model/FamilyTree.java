package net.darmo_creations.jenealogio2.model;

import net.darmo_creations.jenealogio2.model.calendar.DatePrecision;
import net.darmo_creations.jenealogio2.model.calendar.DateWithPrecision;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FamilyTree {
  private final Set<Person> persons = new HashSet<>();
  private final Set<LifeEvent> lifeEvents = new HashSet<>();

  public FamilyTree() {
    // TEMP
    Person person = new Person();
    person.setLegalFirstNames(List.of("John", "Jack"));
    person.setLegalLastName("Doe");
    person.setGender(Registries.GENDERS.getEntry(new RegistryEntryKey(Registry.BUILTIN_NS, "male")));
    person.setLifeStatus(LifeStatus.MAYBE_LIVING);
    person.addLifeEventAsActor(new LifeEvent(person, new DateWithPrecision(LocalDateTime.now(), DatePrecision.EXACT),
        Registries.LIFE_EVENT_TYPES.getEntry(new RegistryEntryKey(Registry.BUILTIN_NS, "birth"))));
    this.persons.add(person);

    Person person2 = new Person();
    person2.setLegalFirstNames(List.of("John"));
    person2.setLegalLastName("Yo");
    person2.setGender(Registries.GENDERS.getEntry(new RegistryEntryKey(Registry.BUILTIN_NS, "non_binary")));
    this.persons.add(person2);
  }

  public Set<Person> persons() {
    return this.persons;
  }

  public Set<LifeEvent> lifeEvents() {
    return this.lifeEvents;
  }
}
