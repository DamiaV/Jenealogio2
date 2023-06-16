package net.darmo_creations.jenealogio2.model.datetime;

import net.darmo_creations.jenealogio2.model.datetime.calendar.CalendarDateTime;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * This class represent a single date with some precision.
 *
 * @param date      The date.
 * @param precision Date’s precision.
 */
public record DateTimeWithPrecision(@NotNull CalendarDateTime date,
                                    @NotNull DateTimePrecision precision)
    implements DateTime {
  public DateTimeWithPrecision {
    Objects.requireNonNull(date);
    Objects.requireNonNull(precision);
  }
}
