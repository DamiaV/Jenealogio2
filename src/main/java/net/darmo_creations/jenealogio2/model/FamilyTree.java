package net.darmo_creations.jenealogio2.model;

import net.darmo_creations.jenealogio2.model.calendar.DatePrecision;
import net.darmo_creations.jenealogio2.model.calendar.DateWithPrecision;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.*;

public class FamilyTree {
  private final Set<Person> persons = new HashSet<>();
  private Person root;

  public FamilyTree() {
    // TEMP
    Person person = new Person();
    person.setLegalFirstNames(List.of("John", "Jack"));
    person.setLegalLastName("Doe");
    person.setGender(Registries.GENDERS.getEntry(new RegistryEntryKey(Registry.BUILTIN_NS, "male")));
    person.setLifeStatus(LifeStatus.MAYBE_LIVING);
    LifeEvent birth = new LifeEvent(person, new DateWithPrecision(LocalDateTime.now(), DatePrecision.EXACT),
        Registries.LIFE_EVENT_TYPES.getEntry(new RegistryEntryKey(Registry.BUILTIN_NS, "birth")));
    person.addLifeEvent(birth);
    this.addPerson(person);

    Person person2 = new Person();
    person2.setLegalFirstNames(List.of("John"));
    person2.setLegalLastName("Yo");
    person2.setGender(Registries.GENDERS.getEntry(new RegistryEntryKey(Registry.BUILTIN_NS, "non_binary")));
    this.addPerson(person2);
  }

  public Set<Person> persons() {
    return new HashSet<>(this.persons);
  }

  public void addPerson(@NotNull Person person) {
    if (this.persons.isEmpty()) {
      this.root = person;
    }
    this.persons.add(person);
  }

  public void removePerson(Person person) {
    if (person == this.root) {
      throw new IllegalArgumentException("cannot delete root");
    }
    this.persons.remove(person);
  }

  public boolean isRoot(final Person person) {
    return this.root == person;
  }

  public Optional<Person> root() {
    return Optional.ofNullable(this.root);
  }

  public void setRoot(@NotNull Person root) {
    this.root = Objects.requireNonNull(root);
  }
}
