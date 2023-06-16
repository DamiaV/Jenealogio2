package net.darmo_creations.jenealogio2.model.datetime.calendar;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Simple wrapper class that associates a {@link LocalDateTime} object to a {@link Calendar}.
 *
 * @param iso8601Date A date-time object interpreted as being in the ISO-8601 calendar.
 * @param calendar    The date’s calendar for conversions.
 */
public record CalendarDateTime
    (@NotNull LocalDateTime iso8601Date, @NotNull Calendar<?> calendar)
    implements Comparable<CalendarDateTime> {
  public CalendarDateTime {
    Objects.requireNonNull(iso8601Date);
    Objects.requireNonNull(calendar);
  }

  @Override
  public int compareTo(@NotNull CalendarDateTime other) {
    return this.iso8601Date.compareTo(other.iso8601Date());
  }

  @Override
  public String toString() {
    return "%s;%s".formatted(this.iso8601Date, this.calendar.name());
  }

  /**
   * Parse a string representing a {@link CalendarDateTime} object.
   *
   * @param dateString String to parse.
   * @return The corresponding date object.
   */
  public static CalendarDateTime parse(@NotNull String dateString) {
    String[] parts = dateString.split(";", 2);
    if (parts.length == 1) {
      parts = new String[] {parts[0], Calendar.GREGORIAN.name()};
    } else if (parts.length != 2) {
      throw new IllegalArgumentException("Could not parse calendar date: " + dateString);
    }
    return new CalendarDateTime(LocalDateTime.parse(parts[0]), Calendar.forName(parts[1]));
  }
}
