package net.darmo_creations.jenealogio2.model.datetime.calendar;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * This class represents a date-time in the gregorian calendar system.
 *
 * @see GregorianCalendar
 */
public final class GregorianDateTime extends CalendarSpecificDateTime {
  public static final int HOURS_IN_DAY = 24;
  public static final int MINUTES_IN_HOUR = 60;

  GregorianDateTime(@NotNull LocalDate date, Integer hour, Integer minute) {
    super(
        date.getYear(),
        date.getMonthValue(),
        date.getDayOfMonth(),
        hour,
        minute
    );
  }

  @Override
  public LocalDateTime toISO8601Date() {
    return LocalDateTime.of(
        this.year(),
        this.month(),
        this.dayOfMonth(),
        this.hour().orElse(0),
        this.minute().orElse(0)
    );
  }

  @Override
  protected int hoursInDay() {
    return HOURS_IN_DAY;
  }

  @Override
  protected int minutesInHour() {
    return MINUTES_IN_HOUR;
  }
}
