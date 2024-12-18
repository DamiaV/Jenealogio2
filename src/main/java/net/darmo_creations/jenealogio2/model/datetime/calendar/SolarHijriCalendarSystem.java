package net.darmo_creations.jenealogio2.model.datetime.calendar;

import net.time4j.*;
import net.time4j.calendar.*;
import net.time4j.engine.*;
import org.jetbrains.annotations.*;

import java.time.*;

/**
 * The Solar Hijri calendar system.
 *
 * @see PersianCalendar
 */
public final class SolarHijriCalendarSystem extends Calendar<SolarHijriDateTime> {
  public static final String NAME = "solar_hijri";

  SolarHijriCalendarSystem() {
    super(NAME, 12, 24, 60);
  }

  @Override
  public SolarHijriDateTime getDate(CalendarEra era, int year, int month, int day, Integer hour, Integer minute) {
    return new SolarHijriDateTime(
        PersianCalendar.of(year, month, day),
        hour,
        minute,
        this
    );
  }

  @Override
  public SolarHijriDateTime convertDate(@NotNull LocalDateTime date, boolean isTimeSet) {
    return new SolarHijriDateTime(
        PlainDate.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth())
            .transform(PersianCalendar.axis()),
        isTimeSet ? date.getHour() : null,
        isTimeSet ? date.getMinute() : null,
        this
    );
  }
}
