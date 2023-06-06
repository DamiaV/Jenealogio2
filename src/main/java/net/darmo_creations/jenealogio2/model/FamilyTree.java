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
    Person root = new Person();
    root.setLegalFirstNames(List.of("C", "D"));
    root.setLegalLastName("B");
    root.setGender(Registries.GENDERS.getEntry(new RegistryEntryKey(Registry.BUILTIN_NS, "male")));
    root.setLifeStatus(LifeStatus.MAYBE_LIVING);
    LifeEvent birth = new LifeEvent(root, new DateWithPrecision(LocalDateTime.now(), DatePrecision.EXACT),
        Registries.LIFE_EVENT_TYPES.getEntry(new RegistryEntryKey(Registry.BUILTIN_NS, "birth")));
    root.addLifeEvent(birth);
    this.addPerson(root);

    Person rootParent1 = new Person();
    rootParent1.setLegalFirstNames(List.of("Y"));
    rootParent1.setLegalLastName("C");
    rootParent1.setGender(Registries.GENDERS.getEntry(new RegistryEntryKey(Registry.BUILTIN_NS, "non_binary")));
    this.addPerson(rootParent1);

    Person rootParent2 = new Person();
    rootParent2.setLegalFirstNames(List.of("A"));
    rootParent2.setLegalLastName("B");
    rootParent2.setGender(Registries.GENDERS.getEntry(new RegistryEntryKey(Registry.BUILTIN_NS, "female")));
    this.addPerson(rootParent2);

    root.setParent(0, rootParent1);
    root.setParent(1, rootParent2);
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
