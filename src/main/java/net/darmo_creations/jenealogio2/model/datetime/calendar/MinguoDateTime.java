package net.darmo_creations.jenealogio2.model.datetime.calendar;

import net.time4j.*;
import net.time4j.calendar.*;
import org.jetbrains.annotations.*;

import java.time.*;

/**
 * This class represents a date-time in the Minguo calendar system.
 *
 * @see MinguoCalendarSystem
 */
public final class MinguoDateTime extends CalendarSpecificDateTime {
  MinguoDateTime(
      @NotNull MinguoCalendar date,
      Integer hours,
      Integer minutes,
      @NotNull MinguoCalendarSystem calendar
  ) {
    super(
        date.get(MinguoCalendar.ERA),
        date.get(MinguoCalendar.YEAR_OF_ERA),
        date.get(MinguoCalendar.MONTH_OF_YEAR).getValue(),
        date.get(MinguoCalendar.DAY_OF_MONTH),
        hours,
        minutes,
        calendar
    );
  }

  @Override
  public LocalDateTime toISO8601Date() {
    final PlainDate date = MinguoCalendar.of((MinguoEra) this.era().orElseThrow(), this.year(), this.month(), this.dayOfMonth())
        .transform(PlainDate.axis());
    return LocalDate.of(date.getYear(), date.getMonth(), date.getDayOfMonth())
        .atTime(LocalTime.of(this.hour().orElse(0), this.minute().orElse(0)));
  }
}
