package net.darmo_creations.jenealogio2.model.datetime.calendar;

import net.time4j.*;
import net.time4j.calendar.*;
import net.time4j.engine.*;
import org.jetbrains.annotations.*;

import java.time.*;

/**
 * The Thai solar calendar system.
 *
 * @see ThaiSolarCalendar
 */
public final class ThaiSolarCalendarSystem extends Calendar<ThaiSolarDateTime> {
  public static final String NAME = "thai_solar";

  ThaiSolarCalendarSystem() {
    super(NAME, 12, 24, 60, ThaiSolarEra.BUDDHIST);
  }

  @Override
  public ThaiSolarDateTime getDate(@NotNull CalendarEra era, int year, int month, int day, Integer hour, Integer minute) {
    return new ThaiSolarDateTime(
        ThaiSolarCalendar.of(ThaiSolarEra.BUDDHIST, year, month, day),
        hour,
        minute,
        this
    );
  }

  @Override
  public ThaiSolarDateTime convertDate(@NotNull LocalDateTime date, boolean isTimeSet) {
    return new ThaiSolarDateTime(
        PlainDate.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth())
            .transform(ThaiSolarCalendar.axis()),
        isTimeSet ? date.getHour() : null,
        isTimeSet ? date.getMinute() : null,
        this
    );
  }
}
