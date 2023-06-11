package net.darmo_creations.jenealogio2.config;

import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Enumeration of all available date formats.
 */
public enum DateFormat {
  DMY_PAD("dd/MM/yyyy"),
  MDY_PAD("MM/dd/yyyy"),
  DMY("d/M/yyyy"),
  MDY("M/d/yyyy"),
  ;

  private final String format;

  DateFormat(@NotNull String format) {
    this.format = Objects.requireNonNull(format);
  }

  /**
   * The format string accepted by {@link DateTimeFormatter#ofPattern(String)}.
   */
  public String getFormat() {
    return this.format;
  }
}
