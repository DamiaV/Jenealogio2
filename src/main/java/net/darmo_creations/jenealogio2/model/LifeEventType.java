package net.darmo_creations.jenealogio2.model;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * This class represents a type of {@link LifeEvent}.
 */
public final class LifeEventType extends RegistryEntry {
  private Group group;
  private boolean indicatesDeath;
  private boolean indicatesUnion;
  private int minActors;
  private int maxActors;
  private boolean unique;

  private boolean init;

  /**
   * Create a life event type.
   *
   * @param key             Type’s registry key.
   * @param userDefinedName Entry’s display text if not builtin.
   * @param group           Type’s group.
   * @param indicatesDeath  Whether this type indicates the death of the associated event’s actors.
   * @param indicatesUnion  Whether this type indicates the union (marriage, etc.) the associated event’s actors.
   * @param minActors       Minimum allowed number of actor allowed to be attached to the event.
   * @param maxActors       Maximum allowed number of actor allowed to be attached to the event.
   * @param unique          Whether events with this type can appear multiple times in an actor’s timeline.
   */
  LifeEventType(
      @NotNull RegistryEntryKey key,
      String userDefinedName,
      @NotNull Group group,
      boolean indicatesDeath,
      boolean indicatesUnion,
      int minActors,
      int maxActors,
      boolean unique
  ) {
    super(key, userDefinedName);
    if (indicatesUnion && minActors == 1) {
      throw new IllegalArgumentException("at least 2 actors required for indicatesUnion to be true");
    }
    this.init = true;
    this.setGroup(group);
    this.setIndicatesDeath(indicatesDeath);
    this.setIndicatesUnion(indicatesUnion);
    this.setMaxActors(maxActors);
    this.setMinActors(minActors);
    this.setUnique(unique);
    this.init = false;
  }

  /**
   * The group of this life event type.
   */
  public Group group() {
    return this.group;
  }

  /**
   * Set the group of this life event type. Only works for user-defined entries.
   *
   * @param group A group.
   */
  public void setGroup(@NotNull Group group) {
    this.ensureNotBuiltin("group");
    this.group = Objects.requireNonNull(group);
  }

  /**
   * Whether this life event type indicates that the actors it is associated with are deceased.
   */
  public boolean indicatesDeath() {
    return this.indicatesDeath;
  }

  /**
   * Set whether this life event type indicates that the actors it is associated with are deceased.
   * Only works for user-defined entries.
   *
   * @param indicatesDeath True to indicate death, false otherwise.
   */
  public void setIndicatesDeath(boolean indicatesDeath) {
    this.ensureNotBuiltin("indicatesDeath");
    this.indicatesDeath = indicatesDeath;
  }

  /**
   * Whether this life event type indicates that the actors it is associated with are in a union (marriage, etc.).
   */
  public boolean indicatesUnion() {
    return this.indicatesUnion;
  }

  /**
   * Set whether this life event type indicates that the actors it is associated with are in a union (marriage, etc.).
   * Only works for user-defined entries.
   *
   * @param indicatesUnion True to indicate a union, false otherwise.
   */
  public void setIndicatesUnion(boolean indicatesUnion) {
    this.ensureNotBuiltin("indicatesUnion");
    this.indicatesUnion = indicatesUnion;
  }

  /**
   * The minimum number of actors allowed for this event type.
   */
  public int minActors() {
    return this.minActors;
  }

  /**
   * Set the minimum number of actors allowed for this event type. Only works for user-defined entries.
   *
   * @param minActors The minimum number of actors.
   */
  public void setMinActors(int minActors) {
    this.ensureNotBuiltin("minActors");
    if (minActors < 1) {
      throw new IllegalArgumentException("expected > 0, got " + minActors);
    }
    if (minActors > this.maxActors) {
      throw new IllegalArgumentException("minActors cannot be greater than maxActors");
    }
    this.minActors = minActors;
  }

  /**
   * The maximum number of actors allowed for this event type.
   */
  public int maxActors() {
    return this.maxActors;
  }

  /**
   * Set the maximum number of actors allowed for this event type. Only works for user-defined entries.
   *
   * @param maxActors The maximum number of actors.
   */
  public void setMaxActors(int maxActors) {
    this.ensureNotBuiltin("maxActors");
    if (maxActors > 2) {
      throw new IllegalArgumentException("maxActors must be > 2");
    }
    if (maxActors < this.minActors) {
      throw new IllegalArgumentException("maxActors cannot be less than minActors");
    }
    this.maxActors = maxActors;
  }

  /**
   * Indicate whether this event type may occur at most once in a person’s life.
   */
  public boolean isUnique() {
    return this.unique;
  }

  /**
   * Set whether this event type may occur at most once in a person’s life. Only works for user-defined entries.
   *
   * @param unique True to set as unique, false otherwise.
   */
  public void setUnique(boolean unique) {
    this.ensureNotBuiltin("unique");
    this.unique = unique;
  }

  @Override
  protected void ensureNotBuiltin(@NotNull String property) {
    if (!this.init) {
      super.ensureNotBuiltin(property);
    }
  }

  @Override
  public String toString() {
    return "LifeEventType{key=%s, group=%s, indicatesDeath=%s, indicatesUnion=%s, minActors=%d, maxActors=%d, unique=%s}"
        .formatted(this.key(), this.group, this.indicatesDeath, this.indicatesUnion, this.minActors, this.maxActors, this.unique);
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
