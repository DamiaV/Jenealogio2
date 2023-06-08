package net.darmo_creations.jenealogio2.utils;

import org.jetbrains.annotations.NotNull;

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

  public static String format(@NotNull String pattern, final @NotNull FormatArg... args) {
    for (FormatArg e : args) {
      pattern = pattern.replace("{" + e.name() + "}", Objects.toString(e.value()));
    }
    return pattern;
  }

  /**
   * Replace by an underscore any illegal Windows path character
   *
   * @param path Path to sanitize.
   * @return The path with each illegal character replaced by an underscore.
   */
  public static String stripWindowsIllegalChars(String path) {
    return path.replaceAll("[\0<>:\"/|?*\\\\]", "_");
  }

  private StringUtils() {
  }
}
