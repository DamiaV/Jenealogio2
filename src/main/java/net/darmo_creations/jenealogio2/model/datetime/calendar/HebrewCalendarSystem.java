package net.darmo_creations.jenealogio2.model.datetime.calendar;

import net.time4j.*;
import net.time4j.calendar.*;
import net.time4j.engine.*;
import org.jetbrains.annotations.*;

import java.time.*;

/**
 * The hebrew calendar system.
 *
 * @see HebrewCalendar
 */
public final class HebrewCalendarSystem extends Calendar<HebrewDateTime> {
  public static final String NAME = "hebrew";

  HebrewCalendarSystem() {
    super(NAME, 13, 24, 60);
  }

  @Override
  public HebrewDateTime getDate(CalendarEra era, int year, int month, int day, Integer hour, Integer minute) {
    final boolean leapYear = HebrewCalendar.isLeapYear(year);
    return new HebrewDateTime(
        HebrewCalendar.of(year, HebrewMonth.valueOfCivil(month, leapYear), day),
        hour,
        minute,
        this
    );
  }

  @Override
  public HebrewDateTime convertDate(@NotNull LocalDateTime date, boolean isTimeSet) {
    return new HebrewDateTime(
        PlainDate.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth())
            .transform(HebrewCalendar.axis()),
        isTimeSet ? date.getHour() : null,
        isTimeSet ? date.getMinute() : null,
        this
    );
  }
}
