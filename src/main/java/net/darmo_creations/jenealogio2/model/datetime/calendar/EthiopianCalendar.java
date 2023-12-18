package net.darmo_creations.jenealogio2.model.datetime.calendar;

import org.jetbrains.annotations.*;
import org.threeten.extra.chrono.*;

import java.time.*;

/**
 * The ethiopian calendar system.
 *
 * @see org.threeten.extra.chrono.EthiopicChronology
 */
public final class EthiopianCalendar extends Calendar<EthiopianDateTime> {
  public static final String NAME = "ethiopian";

  @Override
  public EthiopianDateTime getDate(int year, int month, int day, Integer hour, Integer minute) {
    return new EthiopianDateTime(
        EthiopicDate.of(year, month, day),
        hour,
        minute,
        this
    );
  }

  @Override
  public EthiopianDateTime convertDate(@NotNull LocalDateTime date, boolean isTimeSet) {
    return new EthiopianDateTime(
        EthiopicDate.from(date),
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

  EthiopianCalendar() {
  }
}
