package net.darmo_creations.jenealogio2.model.datetime.calendar;

import net.time4j.*;
import net.time4j.calendar.*;
import org.jetbrains.annotations.*;

import java.time.*;

/**
 * This class represents a date-time in the Thai solar calendar system.
 *
 * @see ThaiSolarCalendarSystem
 */
public final class ThaiSolarDateTime extends CalendarSpecificDateTime {
  ThaiSolarDateTime(
      @NotNull ThaiSolarCalendar date,
      Integer hours,
      Integer minutes,
      @NotNull ThaiSolarCalendarSystem calendar
  ) {
    super(
        ThaiSolarEra.BUDDHIST,
        date.get(ThaiSolarCalendar.YEAR_OF_ERA),
        date.get(ThaiSolarCalendar.MONTH_OF_YEAR).getValue(),
        date.get(ThaiSolarCalendar.DAY_OF_MONTH),
        hours,
        minutes,
        calendar
    );
  }

  @Override
  public LocalDateTime toISO8601Date() {
    final PlainDate date = ThaiSolarCalendar.of(ThaiSolarEra.BUDDHIST, this.year(), this.month(), this.dayOfMonth())
        .transform(PlainDate.axis());
    return LocalDate.of(date.getYear(), date.getMonth(), date.getDayOfMonth())
        .atTime(LocalTime.of(this.hour().orElse(0), this.minute().orElse(0)));
  }
}
