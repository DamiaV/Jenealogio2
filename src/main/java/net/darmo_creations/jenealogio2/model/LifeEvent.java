package net.darmo_creations.jenealogio2.model;

import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.datetime.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.stream.*;

/**
 * A life event is an event a person may live through during their life or be associated with posthumously.
 * <p>
 * Life events have actors and witnesses. An actor is a person that is directly concerned by the event while a witness
 * is a person that participated in a lesser degree. For instance, in a birth event, the actor is the person being born,
 * and the witnesses may be the parents or other relatives.
 * <p>
 * {@link LifeEvent} objects are comparable. Their natural ordering
 * is based on the {@link DateTime#compareTo(DateTime)} method.
 */
public class LifeEvent extends GenealogyObject<LifeEvent> implements Comparable<LifeEvent> {
  private final Set<Person> actors = new HashSet<>();
  private final Set<Person> witnesses = new HashSet<>();
  private DateTime date;
  private LifeEventType type;
  private Place place;

  /**
   * Create a new life event.
   *
   * @param date Event’s date.
   * @param type Event’s type.
   */
  public LifeEvent(@NotNull DateTime date, @NotNull LifeEventType type) {
    this.date = Objects.requireNonNull(date);
    this.type = Objects.requireNonNull(type);
  }

  @Override
  public String name(@NotNull Language language) {
    String name = language.translate("life_event_types." + this.type.key().name());
    String actorsNames = this.actors.stream().map(a -> a.name(language)).collect(Collectors.joining(", "));
    return "%s (%s)".formatted(name, actorsNames);
  }

  /**
   * This event’s date.
   */
  public DateTime date() {
    return this.date;
  }

  /**
   * Set this event’s date.
   *
   * @param date The date.
   * @return This object.
   */
  public LifeEvent setDate(@NotNull DateTime date) {
    this.date = Objects.requireNonNull(date);
    return this;
  }

  /**
   * This event’s type.
   */
  public LifeEventType type() {
    return this.type;
  }

  /**
   * Set this event’s type.
   *
   * @param type The type.
   * @return This object.
   */
  public LifeEvent setType(@NotNull LifeEventType type) {
    this.type = Objects.requireNonNull(type);
    return this;
  }

  /**
   * This event’s location.
   */
  public Optional<Place> place() {
    return Optional.ofNullable(this.place);
  }

  /**
   * Set this event’s location.
   *
   * @param place The location.
   * @return This object.
   */
  public LifeEvent setPlace(Place place) {
    this.place = place;
    return this;
  }

  /**
   * A copy of this event’s actors.
   */
  public Set<Person> actors() {
    return new HashSet<>(this.actors);
  }

  /**
   * Replace all current actors by the given ones. Updates the {@link Person} object.
   *
   * @param actors Persons to set as actors of this life event.
   * @throws IllegalArgumentException If the number of new actors is not within the allowed bounds
   *                                  or if any of the actors are witnesses.
   */
  void setActors(final @NotNull Set<Person> actors) {
    if (actors.size() < this.type.minActors() || actors.size() > this.type.maxActors()) {
      throw new IllegalArgumentException("invalid actors number: expected between %d and %d, got %d"
          .formatted(this.type.minActors(), this.type.maxActors(), actors.size()));
    }
    if (actors.stream().anyMatch(this::hasWitness)) {
      throw new IllegalArgumentException("same person cannot be both witness and actor of same event");
    }
    // Dissociate current actors
    this.actors.forEach(p -> p.removeLifeEvent(this));
    this.actors.clear();
    this.actors.addAll(actors);
    // Associate new actors
    actors.forEach(actor -> actor.addLifeEvent(this));
  }

  /**
   * Remove an actor from this event. Updates the {@link Person} object.
   *
   * @param actor The actor to remove.
   * @throws IllegalArgumentException If the number of actors is already at the allowed minimum.
   */
  void removeActor(final @NotNull Person actor) {
    if (this.actors.size() == this.type.minActors() && this.hasActor(actor)) {
      throw new IllegalStateException("cannot remove any more actors");
    }
    this.actors.remove(actor);
    actor.removeLifeEvent(this);
  }

  /**
   * Indicate whether a person is an actor of this event.
   *
   * @param person Person to check.
   * @return True if the person is an actor of this event, false otherwise.
   */
  public boolean hasActor(final Person person) {
    return this.actors.contains(person);
  }

  /**
   * A copy of this event’s witnesses.
   */
  public Set<Person> witnesses() {
    return new HashSet<>(this.witnesses);
  }

  /**
   * Add a witness to this event. Updates the {@link Person} object.
   *
   * @param witness The witness to add.
   * @throws IllegalArgumentException If the person is already an actor of this event.
   */
  void addWitness(final @NotNull Person witness) {
    Objects.requireNonNull(witness);
    if (this.hasActor(witness)) {
      throw new IllegalArgumentException("same person cannot be both witness and actor of same event");
    }
    this.witnesses.add(witness);
    witness.addLifeEvent(this);
  }

  /**
   * Remove a witness from this event. Updates the {@link Person} object.
   *
   * @param witness The witness to remove.
   */
  void removeWitness(final Person witness) {
    if (!this.hasWitness(witness)) {
      return;
    }
    this.witnesses.remove(witness);
    witness.removeLifeEvent(this);
  }

  /**
   * Indicate whether a person is a witness of this event.
   *
   * @param person Person to check.
   * @return True if the person is a witness of this event, false otherwise.
   */
  public boolean hasWitness(final Person person) {
    return this.witnesses.contains(person);
  }

  @Override
  public int compareTo(final @NotNull LifeEvent lifeEvent) {
    return this.date.compareTo(lifeEvent.date());
  }
}
