package net.darmo_creations.jenealogio2.config;

import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Enumeration of all available time formats.
 */
public enum TimeFormat {
  H24_HMPAD("%H:%M"),
  H12_HMPAD("%h:%M %p", H24_HMPAD),
  H24_HM("%I:%M"),
  H12_HM("%i:%M %p", H24_HM),
  ;

  private final String format;
  private final TimeFormat fullVersion;

  TimeFormat(@NotNull String format) {
    this.format = Objects.requireNonNull(format);
    this.fullVersion = this;
  }

  TimeFormat(@NotNull String format, TimeFormat fullVersion) {
    this.format = Objects.requireNonNull(format);
    this.fullVersion = fullVersion;
  }

  /**
   * The format string accepted by {@link CalendarDateTimeFormatter}.
   */
  public String getFormat() {
    return this.format;
  }

  /**
   * The full-day version of this format.
   */
  public TimeFormat getFullVersion() {
    return this.fullVersion;
  }
}
