package net.darmo_creations.jenealogio2.ui;

import net.darmo_creations.jenealogio2.model.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Wrapper class containing information about the relation between a person and its parent’s index.
 * This class is used to create a person’s parent from the {@link GeneticFamilyTreePane}.
 *
 * @param child      Person that should be the child of the parent to create.
 * @param parentType The type of the parent to create.
 */
public record ChildInfo(@NotNull Person child, @NotNull ParentalRelationType parentType) {
  public ChildInfo {
    Objects.requireNonNull(child);
    Objects.requireNonNull(parentType);
  }
}
