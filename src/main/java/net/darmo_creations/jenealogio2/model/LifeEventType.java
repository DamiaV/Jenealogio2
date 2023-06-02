package net.darmo_creations.jenealogio2.model;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class LifeEventType extends RegistryEntry {
  private final Group group;
  private final boolean indicatesDeath;
  private final int maxActors;
  private final boolean unique;

  LifeEventType(@NotNull RegistryEntryKey key, @NotNull Group group, boolean indicatesDeath, int maxActors, boolean unique) {
    super(key);
    if (maxActors <= 0) {
      throw new IllegalArgumentException("maxActors must be > 0");
    }
    this.group = Objects.requireNonNull(group);
    this.indicatesDeath = indicatesDeath;
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
    ADMIN,
    DISTINCTION,
    EDUCATION,
    LIFESPAN,
    MEDICAL,
    MILITARY,
    RELATIONSHIP,
    RELIGION,

    OTHER
  }

  public record RegistryArgs(@NotNull Group group, boolean indicatesDeath, int maxActors, boolean isUnique) {
    public RegistryArgs {
      Objects.requireNonNull(group);
      if (maxActors <= 0) {
        throw new IllegalArgumentException("maxActors should be > 0");
      }
    }

    public RegistryArgs(@NotNull Group group, boolean indicatesDeath) {
      this(group, indicatesDeath, 1, false);
    }

    public RegistryArgs(@NotNull Group group, boolean indicatesDeath, boolean isUnique) {
      this(group, indicatesDeath, 1, isUnique);
    }
  }
}
