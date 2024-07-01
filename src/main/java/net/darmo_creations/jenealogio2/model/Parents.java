package net.darmo_creations.jenealogio2.model;

import org.jetbrains.annotations.*;

import java.util.*;

/**
 * This record represents the parents of a {@link Person}.
 *
 * @param parent1 The person’s parent 1.
 * @param parent2 The person’s parent 2.
 */
public record Parents(@NotNull Optional<Person> parent1, @NotNull Optional<Person> parent2) {
  public Parents {
    Objects.requireNonNull(parent1);
    Objects.requireNonNull(parent2);
  }
}
