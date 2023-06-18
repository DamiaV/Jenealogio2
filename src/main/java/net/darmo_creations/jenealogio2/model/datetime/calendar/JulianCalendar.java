package net.darmo_creations.jenealogio2.model.datetime.calendar;

import org.jetbrains.annotations.NotNull;
import org.threeten.extra.chrono.JulianDate;

import java.time.LocalDateTime;

/**
 * The julian calendar system.
 *
 * @see org.threeten.extra.chrono.JulianChronology
 */
public final class JulianCalendar implements Calendar<JulianDateTime> {
  public static final String NAME = "julian";

  @Override
  public JulianDateTime getDate(int year, int month, int day, Integer hour, Integer minute) {
    return new JulianDateTime(JulianDate.of(year, month, day), hour, minute);
  }

  @Override
  public JulianDateTime convertDate(@NotNull LocalDateTime date, boolean isTimeSet) {
    return new JulianDateTime(
        JulianDate.from(date),
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
    return 12;
  }

  JulianCalendar() {
  }
}
