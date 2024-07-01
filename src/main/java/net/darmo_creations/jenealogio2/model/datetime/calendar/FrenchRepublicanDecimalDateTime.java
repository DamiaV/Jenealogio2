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
  // Seconds are kept for more accurate conversions to LocalDateTime
  private final int seconds;

  FrenchRepublicanDecimalDateTime(
      @NotNull FrenchRevolutionaryCalendarDate date,
      boolean isTimeSet,
      @NotNull Calendar<FrenchRepublicanDecimalDateTime> calendar
  ) {
    super(
        date.year,
        date.month,
        date.dayOfMonth,
        isTimeSet ? date.hour : 0,
        isTimeSet ? date.minute : 0,
        calendar
    );
    this.seconds = isTimeSet ? date.second : 0;
  }

  @Override
  public LocalDateTime toISO8601Date() {
    var date = new FrenchRevolutionaryCalendarDate(
        Locale.getDefault(), this.year(), this.month(), this.dayOfMonth(),
        this.hour().orElse(0), this.minute().orElse(0), this.seconds);
    GregorianCalendar gd = FrenchRepublicanCalendar.CAL.getDate(date);
    // Converted gregorian date is in UTC, convert it to France’s timezone
    return LocalDateTime.ofInstant(gd.toInstant(), ZoneId.of("Europe/Paris"));
  }
}
