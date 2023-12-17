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
      @NotNull Calendar<FrenchRepublicanDateTime> calendar
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
    var date = new FrenchRevolutionaryCalendarDate(
        Locale.getDefault(), this.year(), this.month(), this.dayOfMonth(), 0, 0, 0);
    GregorianCalendar gd = FrenchRepublicanCalendar.CAL.getDate(date);
    // Converted gregorian date is in UTC, add 1h to account for France’s timezone offset
    LocalDate d = LocalDate.ofInstant(gd.toInstant(), ZoneId.of("+01:00"));
    return d.atTime(this.hour().orElse(0), this.minute().orElse(0));
  }
}
