package net.darmo_creations.jenealogio2.model.datetime.calendar;

import net.time4j.*;
import net.time4j.calendar.*;
import org.jetbrains.annotations.*;

import java.time.*;

/**
 * This class represents a date-time in the hebraic calendar system.
 *
 * @see HebrewCalendarSystem
 */
public final class HebrewDateTime extends CalendarSpecificDateTime {
  HebrewDateTime(
      @NotNull HebrewCalendar date,
      Integer hours,
      Integer minutes,
      @NotNull HebrewCalendarSystem calendar
  ) {
    super(
        date.get(HebrewCalendar.YEAR_OF_ERA),
        date.get(HebrewCalendar.MONTH_OF_YEAR)
            .getCivilValue(HebrewCalendar.isLeapYear(date.get(HebrewCalendar.YEAR_OF_ERA))),
        date.get(HebrewCalendar.DAY_OF_MONTH),
        hours,
        minutes,
        calendar
    );
  }

  @Override
  public LocalDateTime toISO8601Date() {
    final boolean leapYear = HebrewCalendar.isLeapYear(this.year());
    final PlainDate date = HebrewCalendar.of(this.year(), HebrewMonth.valueOfCivil(this.month(), leapYear), this.dayOfMonth())
        .transform(PlainDate.axis());
    return LocalDate.of(date.getYear(), date.getMonth(), date.getDayOfMonth())
        .atTime(LocalTime.of(this.hour().orElse(0), this.minute().orElse(0)));
  }
}
