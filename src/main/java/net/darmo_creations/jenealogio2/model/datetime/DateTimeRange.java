package net.darmo_creations.jenealogio2.model.datetime;

import net.darmo_creations.jenealogio2.model.datetime.calendar.CalendarDateTime;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * This class represents a date range.
 * The {@link #date()} attribute corresponds to {@link #startDate()}.
 *
 * @param startDate Range’s start date.
 * @param endDate   Range’s end date (included).
 */
public record DateTimeRange(@NotNull CalendarDateTime startDate,
                            @NotNull CalendarDateTime endDate)
    implements DateTime {
  public DateTimeRange {
    if (startDate.iso8601Date().isAfter(endDate.iso8601Date())) {
      throw new IllegalArgumentException("start date is after end date");
    }
    Objects.requireNonNull(startDate);
    Objects.requireNonNull(endDate);
  }

  @Override
  public CalendarDateTime date() {
    return this.startDate;
  }
}
