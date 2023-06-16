package net.darmo_creations.jenealogio2.model.datetime.calendar;

import org.jetbrains.annotations.NotNull;
import org.threeten.extra.chrono.JulianDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;

/**
 * This class represents a date-time in the julian calendar system.
 *
 * @see JulianCalendar
 */
public final class JulianDateTime extends CalendarSpecificDateTime {
  public static final int HOURS_IN_DAY = 24;
  public static final int MINUTES_IN_HOUR = 60;

  JulianDateTime(@NotNull JulianDate date, int hours, int minutes) {
    super(
        date.get(ChronoField.YEAR),
        date.get(ChronoField.MONTH_OF_YEAR),
        date.get(ChronoField.DAY_OF_MONTH),
        hours,
        minutes
    );
  }

  @Override
  public LocalDateTime toISO8601Date() {
    return LocalDate.ofEpochDay(JulianDate.of(this.year(), this.month(), this.dayOfMonth()).toEpochDay())
        .atTime(LocalTime.of(this.hour(), this.minute()));
  }

  @Override
  protected int hoursInDay() {
    return HOURS_IN_DAY;
  }

  @Override
  protected int minutesInHour() {
    return MINUTES_IN_HOUR;
  }
}
