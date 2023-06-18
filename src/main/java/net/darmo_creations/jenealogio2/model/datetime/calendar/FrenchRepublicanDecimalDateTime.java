package net.darmo_creations.jenealogio2.model.datetime.calendar;

import ca.rmen.lfrc.FrenchRevolutionaryCalendarDate;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;

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
    return LocalDateTime.ofInstant(FrenchRepublicanCalendar.CAL.getDate(date).toInstant(), ZoneId.systemDefault());
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
