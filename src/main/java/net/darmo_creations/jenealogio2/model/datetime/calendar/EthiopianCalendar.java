package net.darmo_creations.jenealogio2.model.datetime.calendar;

import org.jetbrains.annotations.NotNull;
import org.threeten.extra.chrono.EthiopicDate;

import java.time.LocalDateTime;

/**
 * The ethiopian calendar system.
 *
 * @see org.threeten.extra.chrono.EthiopicChronology
 */
public final class EthiopianCalendar implements Calendar<EthiopianDateTime> {
  public static final String NAME = "ethiopian";

  @Override
  public EthiopianDateTime getDate(int year, int month, int day, int hour, int minute) {
    return new EthiopianDateTime(EthiopicDate.of(year, month, day), hour, minute);
  }

  @Override
  public EthiopianDateTime convertDate(@NotNull LocalDateTime date) {
    return new EthiopianDateTime(EthiopicDate.from(date), date.getHour(), date.getMinute());
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
