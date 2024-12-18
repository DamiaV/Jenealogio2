package net.darmo_creations.jenealogio2.model.datetime.calendar;

import net.time4j.*;
import net.time4j.calendar.*;
import org.jetbrains.annotations.*;

import java.time.*;

/**
 * This class represents a date-time in the Ethiopian calendar system.
 *
 * @see EthiopianCalendarSystem
 */
public final class EthiopianDateTime extends CalendarSpecificDateTime {
  EthiopianDateTime(
      @NotNull EthiopianCalendar date,
      Integer hours,
      Integer minutes,
      @NotNull EthiopianCalendarSystem calendar
  ) {
    super(
        date.get(EthiopianCalendar.ERA),
        date.get(EthiopianCalendar.YEAR_OF_ERA),
        date.get(EthiopianCalendar.MONTH_OF_YEAR).getValue(),
        date.get(EthiopianCalendar.DAY_OF_MONTH),
        hours,
        minutes,
        calendar
    );
  }

  @Override
  public LocalDateTime toISO8601Date() {
    final PlainDate date = EthiopianCalendar.of((EthiopianEra) this.era().orElseThrow(), this.year(), this.month(), this.dayOfMonth())
        .transform(PlainDate.axis());
    return LocalDate.of(date.getYear(), date.getMonth(), date.getDayOfMonth())
        .atTime(LocalTime.of(this.hour().orElse(0), this.minute().orElse(0)));
  }
}
