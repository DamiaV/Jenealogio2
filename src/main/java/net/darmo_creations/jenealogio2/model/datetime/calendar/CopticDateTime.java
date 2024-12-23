package net.darmo_creations.jenealogio2.model.datetime.calendar;

import net.time4j.*;
import net.time4j.calendar.*;
import org.jetbrains.annotations.*;

import java.time.*;

/**
 * This class represents a date-time in the Coptic calendar system.
 *
 * @see CopticCalendarSystem
 */
public final class CopticDateTime extends CalendarSpecificDateTime {
  CopticDateTime(
      @NotNull CopticCalendar date,
      Integer hours,
      Integer minutes,
      @NotNull CopticCalendarSystem calendar
  ) {
    super(
        date.get(CopticCalendar.YEAR_OF_ERA),
        date.get(CopticCalendar.MONTH_OF_YEAR).getValue(),
        date.get(CopticCalendar.DAY_OF_MONTH),
        hours,
        minutes,
        calendar
    );
  }

  @Override
  public LocalDateTime toISO8601Date() {
    final PlainDate date = CopticCalendar.of(this.year(), this.month(), this.dayOfMonth())
        .transform(PlainDate.axis());
    return LocalDate.of(date.getYear(), date.getMonth(), date.getDayOfMonth())
        .atTime(LocalTime.of(this.hour().orElse(0), this.minute().orElse(0)));
  }
}
