package net.darmo_creations.jenealogio2.model.datetime.calendar;

import ca.rmen.lfrc.*;
import net.time4j.engine.*;
import org.jetbrains.annotations.*;

import java.time.*;
import java.util.*;

/**
 * The French republican/revolutionary calendar system with decimal time.
 * <p>
 * There may be some small discrepancies when converting to and from decimal time.
 *
 * @see ca.rmen.lfrc.FrenchRevolutionaryCalendar
 */
public final class FrenchRepublicanDecimalCalendar extends Calendar<FrenchRepublicanDecimalDateTime> {
  public static final String NAME = "french_republican_decimal";

  FrenchRepublicanDecimalCalendar() {
    super(NAME, 13, 10, 100);
  }

  @Override
  public FrenchRepublicanDecimalDateTime getDate(CalendarEra era, int year, int month, int day, Integer hour, Integer minute) {
    return new FrenchRepublicanDecimalDateTime(
        new FrenchRevolutionaryCalendarDate(
            Locale.getDefault(), year, month, day, hour != null ? hour : 0, minute != null ? minute : 0, 0),
        hour != null && minute != null,
        this
    );
  }

  @Override
  public FrenchRepublicanDecimalDateTime convertDate(@NotNull LocalDateTime date, boolean isTimeSet) {
    //noinspection MagicConstant
    final var cal = new GregorianCalendar(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth(),
        date.getHour(), date.getMinute());
    cal.setGregorianChange(new Date(Long.MIN_VALUE));
    return new FrenchRepublicanDecimalDateTime(
        FrenchRepublicanCalendar.CAL.getDate(cal),
        isTimeSet,
        this
    );
  }
}
