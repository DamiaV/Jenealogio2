package net.darmo_creations.jenealogio2.model.datetime.calendar;

import ca.rmen.lfrc.*;
import org.jetbrains.annotations.*;

import java.time.*;
import java.util.GregorianCalendar;
import java.util.*;

/**
 * This class represents a date-time in the French republican/revolutionary calendar system with conventional time.
 *
 * @see FrenchRepublicanCalendar
 */
public final class FrenchRepublicanDateTime extends CalendarSpecificDateTime {
  FrenchRepublicanDateTime(
      @NotNull FrenchRevolutionaryCalendarDate date,
      Integer hours,
      Integer minutes,
      @NotNull FrenchRepublicanCalendar calendar
  ) {
    super(
        date.year,
        date.month,
        date.dayOfMonth,
        hours,
        minutes,
        calendar
    );
  }

  @Override
  public LocalDateTime toISO8601Date() {
    final var date = new FrenchRevolutionaryCalendarDate(
        Locale.getDefault(), this.year(), this.month(), this.dayOfMonth(), 0, 0, 0);
    final GregorianCalendar gd = FrenchRepublicanCalendar.CAL.getDate(date);
    // Converted gregorian date is in UTC, convert it to France’s timezone
    final LocalDate d = LocalDate.ofInstant(gd.toInstant(), ZoneId.of("Europe/Paris"));
    return d.atTime(this.hour().orElse(0), this.minute().orElse(0));
  }
}
