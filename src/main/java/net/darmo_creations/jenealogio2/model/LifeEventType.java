package net.darmo_creations.jenealogio2.model;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class LifeEventType extends RegistryEntry {
  private final Group group;
  private final boolean indicatesDeath;
  private final int minActors;
  private final int maxActors;
  private final boolean unique;

  LifeEventType(@NotNull RegistryEntryKey key, @NotNull Group group, boolean indicatesDeath, int minActors, int maxActors, boolean unique) {
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

  public record RegistryArgs(
      @NotNull Group group, boolean indicatesDeath, int minActors, int maxActors, boolean isUnique) {
    public RegistryArgs(@NotNull Group group, boolean indicatesDeath) {
      this(group, indicatesDeath, 1, 1, false);
    }

    public RegistryArgs(@NotNull Group group, boolean indicatesDeath, boolean isUnique) {
      this(group, indicatesDeath, 1, 1, isUnique);
    }
  }
}
