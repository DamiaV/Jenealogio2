package net.darmo_creations.jenealogio2.model.datetime;

import net.darmo_creations.jenealogio2.model.datetime.calendar.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * This class represents an alternative between two dates.
 * The {@link #date()} attribute corresponds to {@link #earliestDate()}.
 *
 * @param earliestDate The earliest date of the two.
 * @param latestDate   The latest date of the two.
 */
public record DateTimeAlternative(@NotNull CalendarSpecificDateTime earliestDate,
                                  @NotNull CalendarSpecificDateTime latestDate)
    implements DateTime {
  public DateTimeAlternative {
    if (earliestDate.toISO8601Date().isAfter(latestDate.toISO8601Date())) {
      throw new IllegalArgumentException("earliest date is after latest date");
    }
    if (earliestDate.toISO8601Date().equals(latestDate.toISO8601Date())) {
      throw new IllegalArgumentException("earliest date is equal to latest date");
    }
    Objects.requireNonNull(earliestDate);
    Objects.requireNonNull(latestDate);
  }

  @Override
  public CalendarSpecificDateTime date() {
    return this.earliestDate;
  }
}
