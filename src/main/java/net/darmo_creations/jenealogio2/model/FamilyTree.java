package net.darmo_creations.jenealogio2.model;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class FamilyTree {
  private final Set<Person> persons = new HashSet<>();
  private String name;
  private Person root;

  public FamilyTree(@NotNull String name) {
    this.setName(name);
  }

  public String name() {
    return this.name;
  }

  public void setName(@NotNull String name) {
    this.name = Objects.requireNonNull(name);
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
    if (!this.persons.contains(root)) {
      throw new IllegalArgumentException("Person %s is not in this family tree".formatted(root));
    }
    this.root = Objects.requireNonNull(root);
  }
}
