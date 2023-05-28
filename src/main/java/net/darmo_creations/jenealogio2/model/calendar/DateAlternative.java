package net.darmo_creations.jenealogio2.model.calendar;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * This class represents an alternative between two dates.
 * The {@link #date()} attribute corresponds to {@link #earliestDate()}.
 *
 * @param earliestDate The earliest date of the two.
 * @param latestDate   The latest date of the two.
 */
public record DateAlternative(@NotNull LocalDateTime earliestDate,
                              @NotNull LocalDateTime latestDate)
    implements CalendarDate {
  /**
   * Create a date alternative.
   *
   * @param earliestDate The earliest date of the two.
   * @param latestDate   The latest date of the two.
   */
  public DateAlternative(@NotNull LocalDateTime earliestDate, @NotNull LocalDateTime latestDate) {
    if (earliestDate.isAfter(latestDate)) {
      throw new IllegalArgumentException("earliest date is after latest date");
    }
    this.earliestDate = Objects.requireNonNull(earliestDate);
    this.latestDate = Objects.requireNonNull(latestDate);
  }

  @Override
  public LocalDateTime date() {
    return this.earliestDate;
  }
}
