package net.darmo_creations.jenealogio2.model.datetime;

import net.darmo_creations.jenealogio2.model.datetime.calendar.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * This class represent a single date with some precision.
 *
 * @param date      The date.
 * @param precision Date’s precision.
 */
public record DateTimeWithPrecision(@NotNull CalendarSpecificDateTime date,
                                    @NotNull DateTimePrecision precision)
    implements DateTime {
  public DateTimeWithPrecision {
    Objects.requireNonNull(date);
    Objects.requireNonNull(precision);
  }
}
