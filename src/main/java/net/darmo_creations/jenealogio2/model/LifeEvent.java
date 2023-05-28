package net.darmo_creations.jenealogio2.model;

import net.darmo_creations.jenealogio2.model.calendar.CalendarDate;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A life event is an event a person may live through during their life or be associated with posthumously.
 * <p>
 * Life events have actors and witnesses. An actor is a person that is directly concerned by the event while a witness
 * is a person that participated in a lesser degree. For instance, in a birth event, the actor is the person being born,
 * and the witnesses may be the parents or other relatives.
 */
public class LifeEvent extends GenealogyObject<LifeEvent> implements Comparable<LifeEvent> {
  private final Set<Person> actors = new HashSet<>();
  private final Set<Person> witnesses = new HashSet<>();
  private CalendarDate date;
  private LifeEventType type;

  public LifeEvent(@NotNull Person actor, @NotNull CalendarDate date, @NotNull LifeEventType type) {
    this.date = Objects.requireNonNull(date);
    this.type = Objects.requireNonNull(type);
    this.addActor(actor);
  }

  public CalendarDate date() {
    return this.date;
  }

  public LifeEvent setDate(@NotNull CalendarDate date) {
    this.date = Objects.requireNonNull(date);
    return this;
  }

  public LifeEventType type() {
    return this.type;
  }

  public void setType(@NotNull LifeEventType type) {
    if (type.maxActors() < this.actors.size()) {
      throw new IllegalArgumentException("new life event accepts too few actors");
    }
    this.type = type;
  }

  public int maxActors() {
    return this.type.maxActors();
  }

  public Set<Person> actors() {
    return new HashSet<>(this.actors);
  }

  public void addActor(@NotNull Person actor) {
    Objects.requireNonNull(actor);
    if (this.hasWitness(actor)) {
      throw new IllegalArgumentException("same person cannot be both actor and witness of same event");
    }
    int max = this.maxActors();
    if (this.actors.size() == max && !this.hasActor(actor)) {
      throw new IllegalArgumentException("cannot add more than %d actor(s) to life event with type %s"
          .formatted(max, this.type.name()));
    }
    this.actors.add(actor);
    if (actor.getLifeEventsAsActor().contains(this)) {
      actor.addLifeEventAsActor(this);
    }
  }

  public void removeActor(@NotNull Person actor) {
    Objects.requireNonNull(actor);
    if (this.actors.size() == 1 && this.hasActor(actor)) {
      throw new IllegalStateException("cannot remove only actor");
    }
    this.actors.remove(actor);
    if (actor.getLifeEventsAsActor().contains(this)) {
      actor.removeLifeEventAsActor(this);
    }
  }

  public boolean hasActor(final Person person) {
    return this.actors.contains(person);
  }

  public Set<Person> witnesses() {
    return new HashSet<>(this.witnesses);
  }

  public void addWitness(@NotNull Person witness) {
    Objects.requireNonNull(witness);
    if (this.hasActor(witness)) {
      throw new IllegalArgumentException("same person cannot be both witness and actor of same event");
    }
    this.witnesses.add(witness);
    if (witness.getLifeEventsAsWitness().contains(this)) {
      witness.addLifeEventAsWitness(this);
    }
  }

  public void removeWitness(@NotNull Person witness) {
    Objects.requireNonNull(witness);
    this.witnesses.remove(witness);
    if (witness.getLifeEventsAsWitness().contains(this)) {
      witness.removeLifeEventAsWitness(this);
    }
  }

  public boolean hasWitness(final Person person) {
    return this.witnesses.contains(person);
  }

  @Override
  public int compareTo(final @NotNull LifeEvent lifeEvent) {
    return this.date.compareTo(lifeEvent.date());
  }
}
