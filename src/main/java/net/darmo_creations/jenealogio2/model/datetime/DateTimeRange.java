package net.darmo_creations.jenealogio2.model.datetime;

import net.darmo_creations.jenealogio2.model.datetime.calendar.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * This class represents a date range.
 * The {@link #date()} attribute corresponds to {@link #startDate()}.
 *
 * @param startDate Range’s start date.
 * @param endDate   Range’s end date (included).
 */
public record DateTimeRange(@NotNull CalendarSpecificDateTime startDate,
                            @NotNull CalendarSpecificDateTime endDate)
    implements DateTime {
  public DateTimeRange {
    if (startDate.toISO8601Date().isAfter(endDate.toISO8601Date()))
      throw new IllegalArgumentException("start date is after end date");
    if (startDate.toISO8601Date().equals(endDate.toISO8601Date()))
      throw new IllegalArgumentException("start date is equal to end date");
    Objects.requireNonNull(startDate);
    Objects.requireNonNull(endDate);
  }

  @Override
  public CalendarSpecificDateTime date() {
    return this.startDate;
  }
}
