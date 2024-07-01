package net.darmo_creations.jenealogio2.model;

import java.util.*;

/**
 * This record represents the parents of a {@link Person}.
 */
public final class Parents {
  private final Person parent1;
  private final Person parent2;

  /**
   * Create a new {@link Parents} object.
   *
   * @param parent1 The parent 1.
   * @param parent2 The parent 2.
   */
  public Parents(Person parent1, Person parent2) {
    this.parent1 = parent1;
    this.parent2 = parent2;
  }

  /**
   * Create a new empty {@link Parents} object.
   */
  public Parents() {
    this(null, null);
  }

  public Optional<Person> parent1() {
    return Optional.ofNullable(this.parent1);
  }

  public Optional<Person> parent2() {
    return Optional.ofNullable(this.parent2);
  }

  /**
   * Indicate whether any parent is present.
   *
   * @return True if at least one parent is present.
   */
  public boolean anyPresent() {
    return this.parent1 != null || this.parent2 != null;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this)
      return true;
    if (obj == null || obj.getClass() != this.getClass())
      return false;
    final Parents that = (Parents) obj;
    return Objects.equals(this.parent1, that.parent1)
           && Objects.equals(this.parent2, that.parent2);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.parent1, this.parent2);
  }

  @Override
  public String toString() {
    return "Parents[parent1=%s, parent2=%s]".formatted(this.parent1, this.parent2);
  }
}
