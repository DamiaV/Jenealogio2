package net.darmo_creations.jenealogio2.config;

import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Enumeration of all available time formats.
 */
public enum TimeFormat {
  H24_HMPAD("HH:mm"),
  H12_HMPAD("hh:mm a"),
  H24_MPAD("H:mm"),
  H12_MPAD("h:mm a"),
  H24("H:m"),
  H12("h:m a"),
  ;

  private final String format;

  TimeFormat(@NotNull String format) {
    this.format = Objects.requireNonNull(format);
  }

  /**
   * The format string accepted by {@link DateTimeFormatter#ofPattern(String)}.
   */
  public String getFormat() {
    return this.format;
  }
}
