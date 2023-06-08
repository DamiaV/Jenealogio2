package net.darmo_creations.jenealogio2.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Format arguments are used by {@link StringUtils#format(String, FormatArg...)} method.
 * A format argument represents a single named value.
 * The argument’s value will be substituted to the placeholder with the argument’s name.
 *
 * @param name  Argument’s name.
 * @param value Argument’s value.
 */
public record FormatArg(@NotNull String name, Object value) {
  public FormatArg {
    Objects.requireNonNull(name);
  }
}
