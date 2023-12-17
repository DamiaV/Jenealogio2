package net.darmo_creations.jenealogio2.model.datetime.calendar;

import ca.rmen.lfrc.*;
import org.jetbrains.annotations.*;

import java.time.*;
import java.util.GregorianCalendar;
import java.util.*;

/**
 * This class represents a date-time in the French republican/revolutionary calendar system with decimal time.
 * <p>
 * There may be some small discrepancies when converting to and from decimal time.
 *
 * @see FrenchRepublicanDecimalCalendar
 */
public final class FrenchRepublicanDecimalDateTime extends CalendarSpecificDateTime {
  public static final int HOURS_IN_DAY = 10;
  public static final int MINUTES_IN_HOUR = 100;

  // Seconds are kept for more accurate conversions to LocalDateTime
  private final int seconds;

  FrenchRepublicanDecimalDateTime(@NotNull FrenchRevolutionaryCalendarDate date, boolean isTimeSet) {
    super(
        date.year,
        date.month,
        date.dayOfMonth,
        isTimeSet ? date.hour : 0,
        isTimeSet ? date.minute : 0
    );
    this.seconds = isTimeSet ? date.second : 0;
  }

  @Override
  public LocalDateTime toISO8601Date() {
    var date = new FrenchRevolutionaryCalendarDate(
        Locale.getDefault(), this.year(), this.month(), this.dayOfMonth(),
        this.hour().orElse(0), this.minute().orElse(0), this.seconds);
    GregorianCalendar gd = FrenchRepublicanCalendar.CAL.getDate(date);
    // Converted gregorian date is in UTC, add 1h to account for France’s timezone offset
    return LocalDateTime.ofInstant(gd.toInstant(), ZoneId.of("+01:00"));
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
