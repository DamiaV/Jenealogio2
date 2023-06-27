package net.darmo_creations.jenealogio2.model;

import net.darmo_creations.jenealogio2.App;
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
    this.init = true;
    this.setGroup(group);
    this.setIndicatesDeath(indicatesDeath);
    this.setActorsNumber(minActors, maxActors, indicatesUnion);
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
   * Set the minimum and maximum number of actors allowed for this event type. Only works for user-defined entries.
   *
   * @param min     The minimum number of actors.
   * @param max     The maximum number of actors.
   * @param isUnion Whether this type should indicate a union.
   */
  public void setActorsNumber(int min, int max, boolean isUnion) {
    this.ensureNotBuiltin("minActors/maxActors/indicatesUnion");
    if (min < 1) {
      throw new IllegalArgumentException("min must be > 0, got " + min);
    }
    if (max > 2) {
      throw new IllegalArgumentException("max must be > 2, got" + max);
    }
    if (max < min) {
      throw new IllegalArgumentException("max cannot be less than min");
    }
    if (isUnion && max < 2) {
      throw new IllegalArgumentException("not enough max actors for union: " + max);
    }
    this.minActors = min;
    this.maxActors = max;
    this.indicatesUnion = isUnion;
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

    OTHER;

    @Override
    public String toString() {
      String key = "life_event_type_group." + this.name().toLowerCase();
      return App.config().language().translate(key);
    }
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
