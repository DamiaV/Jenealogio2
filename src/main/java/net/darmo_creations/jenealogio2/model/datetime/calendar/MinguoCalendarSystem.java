package net.darmo_creations.jenealogio2.model.datetime.calendar;

import net.time4j.*;
import net.time4j.calendar.*;
import net.time4j.engine.*;
import org.jetbrains.annotations.*;

import java.time.*;
import java.util.*;

/**
 * The Minguo calendar system.
 *
 * @see EthiopianCalendar
 */
public final class MinguoCalendarSystem extends Calendar<MinguoDateTime> {
  public static final String NAME = "minguo";

  MinguoCalendarSystem() {
    super(NAME, 12, 24, 60,
        Arrays.stream(MinguoEra.values()).sorted(Comparator.reverseOrder()).toArray(MinguoEra[]::new));
  }

  @Override
  public MinguoDateTime getDate(@NotNull CalendarEra era, int year, int month, int day, Integer hour, Integer minute) {
    return new MinguoDateTime(
        MinguoCalendar.of((MinguoEra) era, year, month, day),
        hour,
        minute,
        this
    );
  }

  @Override
  public MinguoDateTime convertDate(@NotNull LocalDateTime date, boolean isTimeSet) {
    return new MinguoDateTime(
        PlainDate.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth())
            .transform(MinguoCalendar.axis()),
        isTimeSet ? date.getHour() : null,
        isTimeSet ? date.getMinute() : null,
        this
    );
  }
}
