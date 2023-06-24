package net.darmo_creations.jenealogio2.model;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A family tree has a name and contains a set of persons that belong to it.
 * Trees also have a root which is the person diplayed by default.
 */
public class FamilyTree {
  private final Set<Person> persons = new HashSet<>();
  private final Set<LifeEvent> lifeEvents = new HashSet<>();
  private String name;
  private Person root;

  /**
   * Create a new family tree.
   *
   * @param name Tree’s name.
   */
  public FamilyTree(@NotNull String name) {
    this.setName(name);
  }

  /**
   * Tree’s name.
   */
  public String name() {
    return this.name;
  }

  /**
   * Set tree’s name.
   *
   * @param name The new name.
   */
  public void setName(@NotNull String name) {
    this.name = Objects.requireNonNull(name);
  }

  /**
   * A copy of this tree’s members set.
   */
  public Set<Person> persons() {
    return new HashSet<>(this.persons);
  }

  /**
   * A copy of this tree’s life events set.
   */
  public Set<LifeEvent> lifeEvents() {
    return new HashSet<>(this.lifeEvents);
  }

  /**
   * Add a person to this tree.
   * If this tree has no root yet, the passed person will become it.
   *
   * @param person The person to add.
   */
  public void addPerson(@NotNull Person person) {
    if (this.persons.isEmpty()) {
      this.root = person;
    }
    this.persons.add(person);
  }

  /**
   * Remove the given person from this tree.
   *
   * @param person The person to remove.
   * @throws IllegalArgumentException If the passed person is this tree’s root.
   */
  public void removePerson(Person person) {
    if (person == this.root) {
      throw new IllegalArgumentException("cannot delete root");
    }
    person.setParent(0, null);
    person.setParent(1, null);
    for (LifeEvent lifeEvent : person.lifeEvents()) {
      this.removeActorFromLifeEvent(lifeEvent, person);
      lifeEvent.removeWitness(person);
      this.lifeEvents.remove(lifeEvent);
    }
    for (Person child : person.children()) {
      child.removeParent(person);
    }
    for (Person.RelativeType type : Person.RelativeType.values()) {
      for (Person nonBiologicalChild : person.nonBiologicalChildren(type)) {
        nonBiologicalChild.removeRelative(person, type);
      }
      for (Person relative : person.getRelatives(type)) {
        person.removeRelative(relative, type);
      }
    }
    this.persons.remove(person);
  }

  /**
   * Set the actors of the given life event. The event is added to this tree’s events set.
   *
   * @param lifeEvent The event to set actors of.
   * @param actors    Persons to set as actors.
   */
  public void setLifeEventActors(@NotNull LifeEvent lifeEvent, final @NotNull Set<Person> actors) {
    this.lifeEvents.add(lifeEvent);
    lifeEvent.setActors(actors);
  }

  /**
   * Remove an actor from a life event. If the event is at its minimum allowed number of actors,
   * all actors and witnesses are detached from the event and the event itself is removed from this tree’s events set.
   *
   * @param lifeEvent Life event to remove the actor from.
   * @param actor     Actor to remove.
   */
  public void removeActorFromLifeEvent(@NotNull LifeEvent lifeEvent, @NotNull Person actor) {
    if (!lifeEvent.hasActor(actor)) {
      return;
    }
    if (lifeEvent.actors().size() <= lifeEvent.type().minActors()) {
      lifeEvent.actors().forEach(a -> a.removeLifeEvent(lifeEvent));
      lifeEvent.witnesses().forEach(w -> w.removeLifeEvent(lifeEvent));
      actor.removeLifeEvent(lifeEvent);
      this.lifeEvents.remove(lifeEvent);
    } else {
      lifeEvent.removeActor(actor);
    }
  }

  /**
   * Add a witness to a life event. The event is added to this tree’s events set.
   *
   * @param lifeEvent Life event to add the witness to.
   * @param witness   The witness to add.
   */
  public void addWitnessToLifeEvent(@NotNull LifeEvent lifeEvent, @NotNull Person witness) {
    this.lifeEvents.add(lifeEvent);
    lifeEvent.addWitness(witness);
  }

  /**
   * Remove a witness from a life event.
   *
   * @param lifeEvent Life event to remove the witness from.
   * @param witness   The witness to remove.
   */
  public void removeWitnessFromLifeEvent(@NotNull LifeEvent lifeEvent, @NotNull Person witness) {
    lifeEvent.removeWitness(witness);
  }

  /**
   * Indicate whether the given person is this tree’s root.
   *
   * @param person Person to check.
   * @return True if the given person is this tree’s root, false otherwise.
   */
  public boolean isRoot(final Person person) {
    return this.root == person;
  }

  /**
   * This tree’s root. Will only be empty if this tree has no member.
   */
  public Optional<Person> root() {
    return Optional.ofNullable(this.root);
  }

  /**
   * Set this tree’s root.
   *
   * @param root Person to be set as root.
   * @throws NoSuchElementException If the passed person is not a member of this tree.
   */
  public void setRoot(@NotNull Person root) {
    if (!this.persons.contains(root)) {
      throw new NoSuchElementException("Person %s is not in this family tree".formatted(root));
    }
    this.root = Objects.requireNonNull(root);
  }
}
