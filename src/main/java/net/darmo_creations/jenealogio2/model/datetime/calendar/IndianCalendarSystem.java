package net.darmo_creations.jenealogio2.model.datetime.calendar;

import net.time4j.*;
import net.time4j.calendar.*;
import net.time4j.engine.*;
import org.jetbrains.annotations.*;

import java.time.*;

/**
 * The indian calendar system.
 *
 * @see IndianCalendar
 */
public final class IndianCalendarSystem extends Calendar<IndianDateTime> {
  public static final String NAME = "indian";

  IndianCalendarSystem() {
    super(NAME, 12, 24, 60);
  }

  @Override
  public IndianDateTime getDate(CalendarEra era, int year, int month, int day, Integer hour, Integer minute) {
    return new IndianDateTime(
        IndianCalendar.of(year, month, day),
        hour,
        minute,
        this
    );
  }

  @Override
  public IndianDateTime convertDate(@NotNull LocalDateTime date, boolean isTimeSet) {
    return new IndianDateTime(
        PlainDate.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth())
            .transform(IndianCalendar.axis()),
        isTimeSet ? date.getHour() : null,
        isTimeSet ? date.getMinute() : null,
        this
    );
  }
}
