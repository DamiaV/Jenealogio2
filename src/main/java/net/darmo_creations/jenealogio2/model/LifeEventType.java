package net.darmo_creations.jenealogio2.model;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * This class represents a type of {@link LifeEvent}.
 */
public final class LifeEventType extends RegistryEntry {
  private final Group group;
  private final boolean indicatesDeath;
  private final boolean indicatesUnion;
  private final int minActors;
  private final int maxActors;
  private final boolean unique;

  /**
   * Create a life event type.
   *
   * @param key            Type’s registry key.
   * @param group          Type’s group.
   * @param indicatesDeath Whether this type indicates the death of the associated event’s actors.
   * @param indicatesUnion Whether this type indicates the union (marriage, etc.) the associated event’s actors.
   * @param minActors      Minimum allowed number of actor allowed to be attached to the event.
   * @param maxActors      Maximum allowed number of actor allowed to be attached to the event.
   * @param unique         Whether events with this type can appear multiple times in an actor’s timeline.
   */
  LifeEventType(
      @NotNull RegistryEntryKey key,
      @NotNull Group group,
      boolean indicatesDeath,
      boolean indicatesUnion,
      int minActors,
      int maxActors,
      boolean unique
  ) {
    super(key);
    if (minActors <= 0) {
      throw new IllegalArgumentException("minActors must be > 0");
    }
    if (maxActors > 2) {
      throw new IllegalArgumentException("maxActors must be > 2");
    }
    if (minActors > maxActors) {
      throw new IllegalArgumentException("minActors > minActors");
    }
    this.group = Objects.requireNonNull(group);
    this.indicatesDeath = indicatesDeath;
    this.indicatesUnion = indicatesUnion;
    this.minActors = minActors;
    this.maxActors = maxActors;
    this.unique = unique;
  }

  /**
   * The group of this life event type.
   */
  public Group group() {
    return this.group;
  }

  /**
   * Whether this life event type indicates that the actors it is associated with are deceased.
   */
  public boolean indicatesDeath() {
    return this.indicatesDeath;
  }

  /**
   * Whether this life event type indicates that the actors it is associated with are in a union (marriage, etc.).
   */
  public boolean indicatesUnion() {
    return this.indicatesUnion;
  }

  /**
   * The minimum number of actors allowed for this event type.
   */
  public int minActors() {
    return this.minActors;
  }

  /**
   * The maximum number of actors allowed for this event type.
   */
  public int maxActors() {
    return this.maxActors;
  }

  /**
   * Indicate whether this event type may occur at most once in a person’s life.
   */
  public boolean isUnique() {
    return this.unique;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    LifeEventType that = (LifeEventType) o;
    return this.group == that.group;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), this.group);
  }

  @Override
  public String toString() {
    return "LifeEventType{key='%s', group=%s}".formatted(this.key(), this.group);
  }

  /**
   * Enumeration of available life event type groups.
   */
  public enum Group {
    LIFESPAN,
    ADMIN,
    DISTINCTION,
    EDUCATION,
    MEDICAL,
    MILITARY,
    RELATIONSHIP,
    RELIGION,

    OTHER
  }

  /**
   * Wrapper class used to declare new {@link LifeEventType} entries.
   *
   * @param group          Type’s group.
   * @param indicatesDeath Whether this type indicates the death of the associated event’s actors.
   * @param indicatesUnion Whether this type indicates the union (marriage, etc.) the associated event’s actors.
   * @param minActors      Minimum allowed number of actor allowed to be attached to the event.
   * @param maxActors      Maximum allowed number of actor allowed to be attached to the event.
   * @param isUnique       Whether events with this type can appear multiple times in an actor’s timeline.
   */
  public record RegistryArgs(
      @NotNull Group group,
      boolean indicatesDeath,
      boolean indicatesUnion,
      int minActors,
      int maxActors,
      boolean isUnique
  ) {
    /**
     * Create a new registry wrapper with an minimum and maximum number of actors of 1 and no unicity constraint.
     *
     * @param group          Type’s group.
     * @param indicatesDeath Whether this type indicates the death of the associated event’s actors.
     * @param indicatesUnion Whether events with this type indicate the union (marriage, etc.) the associated event’s actors.
     */
    public RegistryArgs(@NotNull Group group, boolean indicatesDeath, boolean indicatesUnion) {
      this(group, indicatesDeath, indicatesUnion, 1, 1, false);
    }

    /**
     * Create a new registry wrapper with an minimum and maximum number of actors of 1.
     *
     * @param group          Type’s group.
     * @param indicatesDeath Whether this type indicates the death of the associated event’s actors.
     * @param indicatesUnion Whether events with this type indicate the union (marriage, etc.) the associated event’s actors.
     * @param isUnique       Whether events with the created type can appear multiple times in an actor’s timeline.
     */
    public RegistryArgs(@NotNull Group group, boolean indicatesDeath, boolean indicatesUnion, boolean isUnique) {
      this(group, indicatesDeath, indicatesUnion, 1, 1, isUnique);
    }
  }
}
