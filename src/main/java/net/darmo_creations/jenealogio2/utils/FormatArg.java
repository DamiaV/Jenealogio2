package net.darmo_creations.jenealogio2.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record FormatArg(@NotNull String name, Object value) {
  public FormatArg {
    Objects.requireNonNull(name);
  }
}
