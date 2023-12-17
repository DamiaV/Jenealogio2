package net.darmo_creations.jenealogio2.model.datetime.calendar;

import ca.rmen.lfrc.FrenchRevolutionaryCalendar;
import ca.rmen.lfrc.FrenchRevolutionaryCalendarDate;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * The French republican/revolutionary calendar system with conventional time.
 *
 * @see ca.rmen.lfrc.FrenchRevolutionaryCalendar
 */
public final class FrenchRepublicanCalendar implements Calendar<FrenchRepublicanDateTime> {
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
    var date = new FrenchRevolutionaryCalendarDate(Locale.getDefault(), year, month, day, 0, 0, 0);
    return new FrenchRepublicanDateTime(date, hour, minute);
  }

  @Override
  public FrenchRepublicanDateTime convertDate(@NotNull LocalDateTime date, boolean isTimeSet) {
    //noinspection MagicConstant
    var cal = new GregorianCalendar(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());
    cal.setGregorianChange(new Date(Long.MIN_VALUE));
    return new FrenchRepublicanDateTime(
        CAL.getDate(cal),
        isTimeSet ? date.getHour() : 0,
        isTimeSet ? date.getMinute() : 0
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
