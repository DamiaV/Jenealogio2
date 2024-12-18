package net.darmo_creations.jenealogio2.model.datetime.calendar;

import net.time4j.*;
import net.time4j.calendar.*;
import org.jetbrains.annotations.*;

import java.time.*;

/**
 * This class represents a date-time in the indian calendar system.
 *
 * @see IndianCalendarSystem
 */
public final class IndianDateTime extends CalendarSpecificDateTime {
  IndianDateTime(
      @NotNull IndianCalendar date,
      Integer hours,
      Integer minutes,
      @NotNull IndianCalendarSystem calendar
  ) {
    super(
        date.get(IndianCalendar.YEAR_OF_ERA),
        date.get(IndianCalendar.MONTH_OF_YEAR).getValue(),
        date.get(IndianCalendar.DAY_OF_MONTH),
        hours,
        minutes,
        calendar
    );
  }

  @Override
  public LocalDateTime toISO8601Date() {
    final PlainDate date = IndianCalendar.of(this.year(), this.month(), this.dayOfMonth())
        .transform(PlainDate.axis());
    return LocalDate.of(date.getYear(), date.getMonth(), date.getDayOfMonth())
        .atTime(LocalTime.of(this.hour().orElse(0), this.minute().orElse(0)));
  }
}
