package net.darmo_creations.jenealogio2.model.datetime.calendar;

import net.time4j.*;
import net.time4j.calendar.*;
import net.time4j.history.*;
import org.jetbrains.annotations.*;

import java.time.*;

/**
 * This class represents a date-time in the Julian calendar system.
 *
 * @see JulianCalendarSystem
 */
public final class JulianDateTime extends CalendarSpecificDateTime {
  JulianDateTime(
      @NotNull JulianCalendar date,
      Integer hours,
      Integer minutes,
      @NotNull JulianCalendarSystem calendar
  ) {
    super(
        date.get(JulianCalendar.ERA),
        date.get(JulianCalendar.YEAR_OF_ERA),
        date.get(JulianCalendar.MONTH_OF_YEAR),
        date.get(JulianCalendar.DAY_OF_MONTH),
        hours,
        minutes,
        calendar
    );
  }

  @Override
  public LocalDateTime toISO8601Date() {
    final PlainDate date = JulianCalendar.of((HistoricEra) this.era().orElseThrow(), this.year(), this.month(), this.dayOfMonth())
        .transform(PlainDate.axis());
    return LocalDate.of(date.getYear(), date.getMonth(), date.getDayOfMonth())
        .atTime(LocalTime.of(this.hour().orElse(0), this.minute().orElse(0)));
  }
}
