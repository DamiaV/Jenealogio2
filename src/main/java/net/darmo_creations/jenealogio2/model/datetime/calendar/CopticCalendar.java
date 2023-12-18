package net.darmo_creations.jenealogio2.model.datetime.calendar;

import org.jetbrains.annotations.*;
import org.threeten.extra.chrono.*;

import java.time.*;

/**
 * The coptic calendar system.
 *
 * @see org.threeten.extra.chrono.CopticChronology
 */
public final class CopticCalendar extends Calendar<CopticDateTime> {
  public static final String NAME = "coptic";

  @Override
  public CopticDateTime getDate(int year, int month, int day, Integer hour, Integer minute) {
    return new CopticDateTime(
        CopticDate.of(year, month, day),
        hour,
        minute,
        this
    );
  }

  @Override
  public CopticDateTime convertDate(@NotNull LocalDateTime date, boolean isTimeSet) {
    return new CopticDateTime(
        CopticDate.from(date),
        isTimeSet ? date.getHour() : null,
        isTimeSet ? date.getMinute() : null,
        this
    );
  }

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public int lengthOfYearInMonths() {
    return 13;
  }

  CopticCalendar() {
  }
}
