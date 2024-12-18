package net.darmo_creations.jenealogio2.model.datetime.calendar;

import net.time4j.*;
import net.time4j.calendar.*;
import net.time4j.engine.*;
import org.jetbrains.annotations.*;

import java.time.*;
import java.util.*;

/**
 * The Ethiopian calendar system.
 *
 * @see EthiopianCalendar
 */
public final class EthiopianCalendarSystem extends Calendar<EthiopianDateTime> {
  public static final String NAME = "ethiopian";

  EthiopianCalendarSystem() {
    super(NAME, 13, 24, 60,
        Arrays.stream(EthiopianEra.values()).sorted(Comparator.reverseOrder()).toArray(EthiopianEra[]::new));
  }

  @Override
  public EthiopianDateTime getDate(@NotNull CalendarEra era, int year, int month, int day, Integer hour, Integer minute) {
    return new EthiopianDateTime(
        EthiopianCalendar.of((EthiopianEra) era, year, month, day),
        hour,
        minute,
        this
    );
  }

  @Override
  public EthiopianDateTime convertDate(@NotNull LocalDateTime date, boolean isTimeSet) {
    return new EthiopianDateTime(
        PlainDate.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth())
            .transform(EthiopianCalendar.axis()),
        isTimeSet ? date.getHour() : null,
        isTimeSet ? date.getMinute() : null,
        this
    );
  }
}
