package net.darmo_creations.jenealogio2.model.datetime.calendar;

import net.time4j.*;
import net.time4j.calendar.*;
import net.time4j.engine.*;
import org.jetbrains.annotations.*;

import java.time.*;

/**
 * The Coptic calendar system.
 *
 * @see CopticCalendar
 */
public final class CopticCalendarSystem extends Calendar<CopticDateTime> {
  public static final String NAME = "coptic";

  CopticCalendarSystem() {
    super(NAME, 13, 24, 60);
  }

  @Override
  public CopticDateTime getDate(CalendarEra era, int year, int month, int day, Integer hour, Integer minute) {
    return new CopticDateTime(
        CopticCalendar.of(year, month, day),
        hour,
        minute,
        this
    );
  }

  @Override
  public CopticDateTime convertDate(@NotNull LocalDateTime date, boolean isTimeSet) {
    return new CopticDateTime(
        PlainDate.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth())
            .transform(CopticCalendar.axis()),
        isTimeSet ? date.getHour() : null,
        isTimeSet ? date.getMinute() : null,
        this
    );
  }
}
