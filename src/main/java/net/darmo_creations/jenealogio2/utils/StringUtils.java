package net.darmo_creations.jenealogio2.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class StringUtils {
  /**
   * Strip a string of all leading and trailing whitespace. If the string is null, is empty,
   * or only contains whitespace, null is returned.
   *
   * @param s A string.
   * @return The same string stripped of all leading and trailing whitespace or an empty optional
   * if it was null, empty, or all whitespace.
   */
  public static Optional<String> stripNullable(String s) {
    return s == null || s.isBlank() ? Optional.empty() : Optional.of(s.strip());
  }

  public static String format(@NotNull String pattern, final @NotNull Map<String, Object> args) {
    for (var e : args.entrySet()) {
      pattern = pattern.replace("{" + e.getKey() + "}", Objects.toString(e.getValue()));
    }
    return pattern;
  }

  private StringUtils() {
  }
}
