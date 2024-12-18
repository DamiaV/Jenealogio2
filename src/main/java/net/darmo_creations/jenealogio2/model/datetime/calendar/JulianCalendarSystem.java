package net.darmo_creations.jenealogio2.model.datetime.calendar;

import net.time4j.*;
import net.time4j.calendar.*;
import net.time4j.engine.*;
import net.time4j.history.*;
import org.jetbrains.annotations.*;

import java.time.*;

/**
 * The Julian calendar system.
 *
 * @see JulianCalendar
 */
public final class JulianCalendarSystem extends Calendar<JulianDateTime> {
  public static final String NAME = "julian";

  JulianCalendarSystem() {
    super(NAME, 12, 24, 60, HistoricEra.AD, HistoricEra.BC);
  }

  @Override
  public JulianDateTime getDate(@NotNull CalendarEra era, int year, int month, int day, Integer hour, Integer minute) {
    return new JulianDateTime(
        JulianCalendar.of((HistoricEra) era, year, month, day),
        hour,
        minute,
        this
    );
  }

  @Override
  public JulianDateTime convertDate(@NotNull LocalDateTime date, boolean isTimeSet) {
    return new JulianDateTime(
        PlainDate.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth())
            .transform(JulianCalendar.axis()),
        isTimeSet ? date.getHour() : null,
        isTimeSet ? date.getMinute() : null,
        this
    );
  }
}
