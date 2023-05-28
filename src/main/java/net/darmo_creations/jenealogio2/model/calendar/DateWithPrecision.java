package net.darmo_creations.jenealogio2.model.calendar;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * This class represent a single date with some precision.
 *
 * @param date      The date.
 * @param precision Date’s precision.
 */
public record DateWithPrecision(@NotNull LocalDateTime date,
                                @NotNull DatePrecision precision)
    implements CalendarDate {
  /**
   * Create a date with some precision.
   *
   * @param date      The date.
   * @param precision Date’s precision.
   */
  public DateWithPrecision(LocalDateTime date, DatePrecision precision) {
    this.date = Objects.requireNonNull(date);
    this.precision = Objects.requireNonNull(precision);
  }
}
