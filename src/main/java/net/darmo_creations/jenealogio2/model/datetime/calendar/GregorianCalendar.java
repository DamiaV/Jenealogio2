package net.darmo_creations.jenealogio2.model.datetime.calendar;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * The gregorian calendar system.
 */
public final class GregorianCalendar implements Calendar<GregorianDateTime> {
  public static final String NAME = "gregorian";

  @Override
  public GregorianDateTime getDate(int year, int month, int day, Integer hour, Integer minute) {
    return new GregorianDateTime(
        LocalDate.of(year, month, day),
        hour,
        minute,
        this
    );
  }

  @Override
  public GregorianDateTime convertDate(@NotNull LocalDateTime date, boolean isTimeSet) {
    return new GregorianDateTime(
        date.toLocalDate(),
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
    return 12;
  }

  GregorianCalendar() {
  }
}
