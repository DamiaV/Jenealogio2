package net.darmo_creations.jenealogio2.model.datetime;

import net.darmo_creations.jenealogio2.model.datetime.calendar.CalendarDateTime;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * This class represents an alternative between two dates.
 * The {@link #date()} attribute corresponds to {@link #earliestDate()}.
 *
 * @param earliestDate The earliest date of the two.
 * @param latestDate   The latest date of the two.
 */
public record DateTimeAlternative(@NotNull CalendarDateTime earliestDate,
                                  @NotNull CalendarDateTime latestDate)
    implements DateTime {
  public DateTimeAlternative {
    if (earliestDate.iso8601Date().isAfter(latestDate.iso8601Date())) {
      throw new IllegalArgumentException("earliest date is after latest date");
    }
    Objects.requireNonNull(earliestDate);
    Objects.requireNonNull(latestDate);
  }

  @Override
  public CalendarDateTime date() {
    return this.earliestDate;
  }
}
