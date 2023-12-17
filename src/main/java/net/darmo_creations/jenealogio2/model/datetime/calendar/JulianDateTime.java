package net.darmo_creations.jenealogio2.model.datetime.calendar;

import org.jetbrains.annotations.*;
import org.threeten.extra.chrono.*;

import java.time.*;
import java.time.temporal.*;

/**
 * This class represents a date-time in the julian calendar system.
 *
 * @see JulianCalendar
 */
public final class JulianDateTime extends CalendarSpecificDateTime {
  JulianDateTime(
      @NotNull JulianDate date,
      Integer hours,
      Integer minutes,
      @NotNull Calendar<JulianDateTime> calendar
  ) {
    super(
        date.get(ChronoField.YEAR),
        date.get(ChronoField.MONTH_OF_YEAR),
        date.get(ChronoField.DAY_OF_MONTH),
        hours,
        minutes,
        calendar
    );
  }

  @Override
  public LocalDateTime toISO8601Date() {
    return LocalDate.ofEpochDay(JulianDate.of(this.year(), this.month(), this.dayOfMonth()).toEpochDay())
        .atTime(LocalTime.of(this.hour().orElse(0), this.minute().orElse(0)));
  }
}
