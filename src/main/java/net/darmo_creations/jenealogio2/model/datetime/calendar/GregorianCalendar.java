package net.darmo_creations.jenealogio2.model.datetime.calendar;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

/**
 * The gregorian calendar system.
 */
public final class GregorianCalendar implements Calendar<GregorianDateTime> {
  public static final String NAME = "gregorian";

  @Override
  public GregorianDateTime getDate(int year, int month, int day, int hour, int minute) {
    return new GregorianDateTime(LocalDateTime.of(year, month, day, hour, minute));
  }

  @Override
  public GregorianDateTime convertDate(@NotNull LocalDateTime date) {
    return new GregorianDateTime(date);
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
