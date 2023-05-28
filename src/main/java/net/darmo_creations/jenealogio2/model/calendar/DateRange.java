package net.darmo_creations.jenealogio2.model.calendar;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * This class represents date range.
 * The {@link #date()} attribute corresponds to {@link #startDate()}.
 *
 * @param startDate Range’s start date.
 * @param endDate   Range’s end date (included).
 */
public record DateRange(@NotNull LocalDateTime startDate,
                        @NotNull LocalDateTime endDate)
    implements CalendarDate {
  /**
   * Create a date range.
   *
   * @param startDate Range’s start date.
   * @param endDate   Range’s end date (included).
   */
  public DateRange(@NotNull LocalDateTime startDate, @NotNull LocalDateTime endDate) {
    this.startDate = Objects.requireNonNull(startDate);
    this.endDate = Objects.requireNonNull(endDate);
  }

  @Override
  public LocalDateTime date() {
    return this.startDate;
  }
}
