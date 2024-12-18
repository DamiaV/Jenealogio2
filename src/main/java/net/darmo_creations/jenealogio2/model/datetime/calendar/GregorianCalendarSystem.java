package net.darmo_creations.jenealogio2.model.datetime.calendar;

import net.time4j.engine.*;
import org.jetbrains.annotations.*;

import java.time.*;

/**
 * The gregorian calendar system.
 */
public final class GregorianCalendarSystem extends Calendar<GregorianDateTime> {
  public static final String NAME = "gregorian";

  GregorianCalendarSystem() {
    super(NAME, 12, 24, 60);
  }

  @Override
  public GregorianDateTime getDate(CalendarEra era, int year, int month, int day, Integer hour, Integer minute) {
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
}
