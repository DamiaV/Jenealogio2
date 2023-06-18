package net.darmo_creations.jenealogio2.model.datetime.calendar;

import org.jetbrains.annotations.NotNull;
import org.threeten.extra.chrono.CopticDate;

import java.time.LocalDateTime;

/**
 * The coptic calendar system.
 *
 * @see org.threeten.extra.chrono.CopticChronology
 */
public final class CopticCalendar implements Calendar<CopticDateTime> {
  public static final String NAME = "coptic";

  @Override
  public CopticDateTime getDate(int year, int month, int day, Integer hour, Integer minute) {
    return new CopticDateTime(CopticDate.of(year, month, day), hour, minute);
  }

  @Override
  public CopticDateTime convertDate(@NotNull LocalDateTime date, boolean isTimeSet) {
    return new CopticDateTime(
        CopticDate.from(date),
        isTimeSet ? date.getHour() : null,
        isTimeSet ? date.getMinute() : null
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
