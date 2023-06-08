package net.darmo_creations.jenealogio2.model;

import net.darmo_creations.jenealogio2.model.calendar.CalendarDate;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A life event is an event a person may live through during their life or be associated with posthumously.
 * <p>
 * Life events have actors and witnesses. An actor is a person that is directly concerned by the event while a witness
 * is a person that participated in a lesser degree. For instance, in a birth event, the actor is the person being born,
 * and the witnesses may be the parents or other relatives.
 * <p>
 * {@link LifeEvent} objects are comparable. Their natural ordering
 * is based on the {@link CalendarDate#compareTo(CalendarDate)} method.
 */
public class LifeEvent extends GenealogyObject<LifeEvent> implements Comparable<LifeEvent> {
  private final Set<Person> actors = new HashSet<>();
  private final Set<Person> witnesses = new HashSet<>();
  private CalendarDate date;
  private LifeEventType type;
  private String place;

  /**
   * Create a new life event.
   *
   * @param actor Event’s actor.
   * @param date  Event’s date.
   * @param type  Event’s type.
   */
  public LifeEvent(@NotNull Person actor, @NotNull CalendarDate date, @NotNull LifeEventType type) {
    this.date = Objects.requireNonNull(date);
    this.type = Objects.requireNonNull(type);
    this.addActor(actor);
  }

  /**
   * This event’s date.
   */
  public CalendarDate date() {
    return this.date;
  }

  /**
   * Set this event’s date.
   *
   * @param date The date.
   * @return This object.
   */
  public LifeEvent setDate(@NotNull CalendarDate date) {
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
  public Optional<String> place() {
    return Optional.ofNullable(this.place);
  }

  /**
   * Set this event’s location.
   *
   * @param place The location.
   * @return This object.
   */
  public LifeEvent setPlace(String place) {
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
   * Add an actor to this event.
   *
   * @param actor The actor to add.
   * @throws IllegalArgumentException If the actor is already a witness of this event
   *                                  or if this event cannot accept any more actors.
   */
  public void addActor(final @NotNull Person actor) {
    Objects.requireNonNull(actor);
    if (this.hasWitness(actor)) {
      throw new IllegalArgumentException("same person cannot be both actor and witness of same event");
    }
    int max = this.type.maxActors();
    if (this.actors.size() == max && !this.hasActor(actor)) {
      throw new IllegalArgumentException("cannot add more than %d actor(s) to life event with type %s"
          .formatted(max, this.type.key()));
    }
    this.actors.add(actor);
  }

  /**
   * Remove an actor from this event.
   *
   * @param actor The actor to remove.
   * @throws IllegalArgumentException If the number of actors is already at the allowed minimum.
   */
  public void removeActor(final Person actor) {
    if (this.actors.size() == this.type.minActors() && this.hasActor(actor)) {
      throw new IllegalStateException("cannot remove any more actors");
    }
    this.actors.remove(actor);
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
   * Add a witness to this event.
   *
   * @param witness The witness to add.
   * @throws IllegalArgumentException If the person is already an actor of this event.
   */
  public void addWitness(final @NotNull Person witness) {
    Objects.requireNonNull(witness);
    if (this.hasActor(witness)) {
      throw new IllegalArgumentException("same person cannot be both witness and actor of same event");
    }
    this.witnesses.add(witness);
  }

  /**
   * Remove a witness from this event.
   *
   * @param witness The witness to remove.
   */
  public void removeWitness(final Person witness) {
    this.witnesses.remove(witness);
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
