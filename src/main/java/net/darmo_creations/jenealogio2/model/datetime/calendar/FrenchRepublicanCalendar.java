package net.darmo_creations.jenealogio2.model.datetime.calendar;

import ca.rmen.lfrc.*;
import org.jetbrains.annotations.*;

import java.time.*;
import java.util.GregorianCalendar;
import java.util.*;

/**
 * The French republican/revolutionary calendar system with conventional time.
 *
 * @see ca.rmen.lfrc.FrenchRevolutionaryCalendar
 */
public final class FrenchRepublicanCalendar extends Calendar<FrenchRepublicanDateTime> {
  public static final String NAME = "french_republican";

  /**
   * Internal calendar instance for conversions.
   * <p>
   * Uses Romme calculations for extended calendar.
   */
  static final FrenchRevolutionaryCalendar CAL =
      new FrenchRevolutionaryCalendar(Locale.getDefault(), FrenchRevolutionaryCalendar.CalculationMethod.ROMME);

  @Override
  public FrenchRepublicanDateTime getDate(int year, int month, int day, Integer hour, Integer minute) {
    return new FrenchRepublicanDateTime(
        new FrenchRevolutionaryCalendarDate(Locale.getDefault(), year, month, day, 0, 0, 0),
        hour,
        minute,
        this
    );
  }

  @Override
  public FrenchRepublicanDateTime convertDate(@NotNull LocalDateTime date, boolean isTimeSet) {
    //noinspection MagicConstant
    final var cal = new GregorianCalendar(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());
    cal.setGregorianChange(new Date(Long.MIN_VALUE));
    return new FrenchRepublicanDateTime(
        CAL.getDate(cal),
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

  FrenchRepublicanCalendar() {
  }
}
