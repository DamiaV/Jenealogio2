package net.darmo_creations.jenealogio2.ui;

import net.darmo_creations.jenealogio2.model.Person;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record ChildInfo(@NotNull Person child, int parentIndex) {
  public ChildInfo {
    Objects.requireNonNull(child);
    if (parentIndex < 0 || parentIndex > 1) {
      throw new IllegalArgumentException("parent index must be in [0, 1]");
    }
  }
}
