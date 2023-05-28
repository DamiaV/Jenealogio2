package net.darmo_creations.jenealogio2.model;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class LifeEventType extends RegistryEntry {
  private final Group group;
  private final boolean indicatesDeath;
  private final int maxActors;

  LifeEventType(int id, @NotNull String name, boolean builtin, @NotNull Group group, boolean indicatesDeath, int maxActors) {
    super(id, name, builtin);
    if (maxActors <= 0) {
      throw new IllegalArgumentException("maxActors must be > 0");
    }
    this.group = Objects.requireNonNull(group);
    this.indicatesDeath = indicatesDeath;
    this.maxActors = maxActors;
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
    return "LifeEventType{id=%d, name=%s, builtin=%s, group=%s}"
        .formatted(this.id(), this.name(), this.isBuiltin(), this.group);
  }

  public enum Group {
    ADMIN,
    DISTINCTION,
    EDUCATION,
    FAMILY,
    LIFESPAN,
    MEDICAL,
    MILITARY,
    RELIGION,

    OTHER
  }

  public record RegistryArgs(Group group, boolean indicatesDeath, int maxActors) {
    public RegistryArgs {
      if (maxActors <= 0) {
        throw new IllegalArgumentException("maxActors should be > 0");
      }
    }

    public RegistryArgs(Group group, boolean indicatesDeath) {
      this(group, indicatesDeath, 1);
    }
  }
}
