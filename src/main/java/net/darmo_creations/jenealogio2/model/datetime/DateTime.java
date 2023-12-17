package net.darmo_creations.jenealogio2.model.datetime;

import net.darmo_creations.jenealogio2.model.datetime.calendar.*;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link DateTime} object is either a {@link DateTimeWithPrecision}, a {@link DateTimeAlternative} or a {@link DateTimeRange}.
 * <p>
 * Instances of this interface are comparable.
 * Their natural ordering is based on the value returned by {@link #date()}.
 */
public sealed interface DateTime extends Comparable<DateTime>
    permits DateTimeAlternative, DateTimeRange, DateTimeWithPrecision {
  /**
   * The date or earliest date this object represents.
   */
  CalendarSpecificDateTime date();

  /**
   * Compare this date to another one using their {@link #date()} attribute.
   *
   * @param other A date.
   * @return The comparator value, negative if {@code this.date()} is less than {@code other.date()},
   * positive if greater, 0 if equal.
   */
  @Override
  default int compareTo(@NotNull DateTime other) {
    return this.date().compareTo(other.date());
  }
}
