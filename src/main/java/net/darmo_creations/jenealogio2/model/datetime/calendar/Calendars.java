package net.darmo_creations.jenealogio2.model.datetime.calendar;

import net.time4j.calendar.*;

/**
 * List of all available calendars.
 */
public final class Calendars {
  /**
   * The Coptic calendar system.
   *
   * @see CopticCalendar
   */
  public static final CopticCalendarSystem COPTIC = new CopticCalendarSystem();
  /**
   * The Ethiopian calendar system.
   *
   * @see EthiopianCalendar
   */
  public static final EthiopianCalendarSystem ETHIOPIAN = new EthiopianCalendarSystem();
  /**
   * The French Republican calendar system.
   *
   * @see ca.rmen.lfrc.FrenchRevolutionaryCalendar
   */
  public static final FrenchRepublicanCalendar FRENCH_REPUBLICAN_CALENDAR = new FrenchRepublicanCalendar();
  /**
   * The French Republican calendar system with decimal time.
   *
   * @see ca.rmen.lfrc.FrenchRevolutionaryCalendar
   */
  public static final FrenchRepublicanDecimalCalendar FRENCH_REPUBLICAN_DECIMAL_CALENDAR = new FrenchRepublicanDecimalCalendar();
  /**
   * The gregorian calendar system.
   */
  public static final GregorianCalendarSystem GREGORIAN = new GregorianCalendarSystem();
  /**
   * The hebrew calendar system.
   *
   * @see HebrewCalendar
   */
  public static final HebrewCalendarSystem HEBREW = new HebrewCalendarSystem();
  /**
   * The indian calendar system.
   *
   * @see IndianCalendar
   */
  public static final IndianCalendarSystem INDIAN = new IndianCalendarSystem();
  /**
   * The Julian calendar system.
   *
   * @see JulianCalendar
   */
  public static final JulianCalendarSystem JULIAN = new JulianCalendarSystem();
  /**
   * The Minguo calendar system.
   *
   * @see MinguoCalendar
   */
  public static final MinguoCalendarSystem MINGUO = new MinguoCalendarSystem();
  /**
   * The Solar Hijri calendar system.
   *
   * @see PersianCalendar
   */
  public static final SolarHijriCalendarSystem SOLAR_HIJRI = new SolarHijriCalendarSystem();
  /**
   * The Thai solar calendar system.
   *
   * @see ThaiSolarCalendar
   */
  public static final ThaiSolarCalendarSystem THAI_SOLAR = new ThaiSolarCalendarSystem();

  private Calendars() {
  }
}
