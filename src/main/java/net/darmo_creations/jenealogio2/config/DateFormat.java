package net.darmo_creations.jenealogio2.config;

import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Enumeration of all available date formats.
 */
public enum DateFormat {
  DMY_SHORT_NAME("%d %b %y"),
  MDY_SHORT_NAME("%b %d %y"),
  DMY_FULL_NAME("%d %B %y"),
  MDY_FULL_NAME("%B %d %y"),
  DMY_SHORT_NAME_SUFFIX("%d%s %b %y"),
  MDY_SHORT_NAME_SUFFIX("%b %d%s %y"),
  DMY_FULL_NAME_SUFFIX("%d%s %B %y"),
  MDY_FULL_NAME_SUFFIX("%B %d%s %y"),
//  DMY_PAD("%D/%m/%y"),
//  MDY_PAD("%m/%D/%y"),
//  DMY("%d/%m/%y"),
//  MDY("%n/%D/%y"),
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
