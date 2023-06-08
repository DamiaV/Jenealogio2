package net.darmo_creations.jenealogio2.ui;

import net.darmo_creations.jenealogio2.model.Person;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Wrapper class containing information about the relation between a person and its parent’s index.
 * This class is used to create a person’s parent from the {@link FamilyTreePane}.
 *
 * @param child       Person that should be the child of the parent to create.
 * @param parentIndex Index of the parent to create.
 */
public record ChildInfo(@NotNull Person child, int parentIndex) {
  public ChildInfo {
    Objects.requireNonNull(child);
    if (parentIndex < 0 || parentIndex > 1) {
      throw new IllegalArgumentException("parent index must be in [0, 1]");
    }
  }
}
