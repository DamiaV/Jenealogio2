package net.darmo_creations.jenealogio2.model.datetime.calendar;

import net.time4j.*;
import net.time4j.calendar.*;
import org.jetbrains.annotations.*;

import java.time.*;

/**
 * This class represents a date-time in the Solar Hijri calendar system.
 *
 * @see SolarHijriCalendarSystem
 */
public final class SolarHijriDateTime extends CalendarSpecificDateTime {
  SolarHijriDateTime(
      @NotNull PersianCalendar date,
      Integer hours,
      Integer minutes,
      @NotNull SolarHijriCalendarSystem calendar
  ) {
    super(
        date.get(PersianCalendar.YEAR_OF_ERA),
        date.get(PersianCalendar.MONTH_OF_YEAR).getValue(),
        date.get(PersianCalendar.DAY_OF_MONTH),
        hours,
        minutes,
        calendar
    );
  }

  @Override
  public LocalDateTime toISO8601Date() {
    final PlainDate date = PersianCalendar.of(this.year(), this.month(), this.dayOfMonth())
        .transform(PlainDate.axis());
    return LocalDate.of(date.getYear(), date.getMonth(), date.getDayOfMonth())
        .atTime(LocalTime.of(this.hour().orElse(0), this.minute().orElse(0)));
  }
}
