package net.darmo_creations.jenealogio2.model.datetime.calendar;

/**
 * List of all available calendars.
 */
public final class Calendars { // TODO arabic and hebrew calendars
  /**
   * The coptic calendar system.
   *
   * @see org.threeten.extra.chrono.CopticChronology
   */
  public static final CopticCalendar COPTIC = new CopticCalendar();
  /**
   * The ethiopian calendar system.
   *
   * @see org.threeten.extra.chrono.EthiopicChronology
   */
  public static final EthiopianCalendar ETHIOPIAN = new EthiopianCalendar();
  /**
   * The gregorian calendar system.
   */
  public static final GregorianCalendar GREGORIAN = new GregorianCalendar();
  /**
   * The julian calendar system.
   *
   * @see org.threeten.extra.chrono.JulianChronology
   */
  public static final JulianCalendar JULIAN = new JulianCalendar();
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

  private Calendars() {
  }
}
