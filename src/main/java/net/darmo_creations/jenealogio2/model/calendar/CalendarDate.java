package net.darmo_creations.jenealogio2.model.calendar;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

/**
 * A calendar date is either a date with a precision, a date alternative or a date range.
 * <p>
 * Instances of this interface are comparable.
 * Their natural ordering is based on the value returned by {@link #date()}.
 */
public sealed interface CalendarDate extends Comparable<CalendarDate>
    permits DateAlternative, DateRange, DateWithPrecision {
  /**
   * The date or earliest date this object represents.
   */
  LocalDateTime date();

  /**
   * Compare this calendar date to another one using their {@link #date()} attribute.
   *
   * @param other A calendar date.
   * @return The comparator value, negative if {@code this.date()} is less than {@code other.date()},
   * positive if greater, 0 if equal.
   */
  @Override
  default int compareTo(@NotNull CalendarDate other) {
    return this.date().compareTo(other.date());
  }
}
