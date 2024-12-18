package net.darmo_creations.jenealogio2.config;

import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Enumeration of all available date formats.
 */
public enum DateFormat {
  DMY_SHORT_NAME("%d %b %y%E"),
  MDY_SHORT_NAME("%b %d %y%E"),
  DMY_FULL_NAME("%d %B %y%E"),
  MDY_FULL_NAME("%B %d %y%E"),
  DMY_SHORT_NAME_SUFFIX("%d%s %b %y%E"),
  MDY_SHORT_NAME_SUFFIX("%b %d%s %y%E"),
  DMY_FULL_NAME_SUFFIX("%d%s %B %y%E"),
  MDY_FULL_NAME_SUFFIX("%B %d%s %y%E"),
  ;

  private final String format;

  DateFormat(@NotNull String format) {
    this.format = Objects.requireNonNull(format);
  }

  /**
   * The format string accepted by {@link CalendarDateTimeFormatter}.
   */
  public String getFormat() {
    return this.format;
  }
}
