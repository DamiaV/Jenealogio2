package net.darmo_creations.jenealogio2.model.datetime.calendar;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

/**
 * This interface represents a calendar system.
 * Calendars can convert {@link LocalDateTime} objects into their custom {@link CalendarSpecificDateTime} equivalents.
 *
 * @param <D> Type of custom dates for this calendar.
 */
public sealed interface Calendar<D extends CalendarSpecificDateTime>
    permits CopticCalendar, EthiopianCalendar, FrenchRepublicanDecimalCalendar, FrenchRepublicanCalendar,
    GregorianCalendar, JulianCalendar {
  /**
   * The coptic calendar system.
   *
   * @see org.threeten.extra.chrono.CopticChronology
   */
  CopticCalendar COPTIC = new CopticCalendar();
  /**
   * The ethiopian calendar system.
   *
   * @see org.threeten.extra.chrono.EthiopicChronology
   */
  EthiopianCalendar ETHIOPIAN = new EthiopianCalendar();
  /**
   * The gregorian calendar system.
   */
  GregorianCalendar GREGORIAN = new GregorianCalendar();
  /**
   * The julian calendar system.
   *
   * @see org.threeten.extra.chrono.JulianChronology
   */
  JulianCalendar JULIAN = new JulianCalendar();
  /**
   * The julian calendar system.
   *
   * @see org.threeten.extra.chrono.JulianChronology
   */
  FrenchRepublicanCalendar FRENCH_REPUBLICAN_CALENDAR = new FrenchRepublicanCalendar();
  /**
   * The julian calendar system.
   *
   * @see org.threeten.extra.chrono.JulianChronology
   */
  FrenchRepublicanDecimalCalendar FRENCH_REPUBLICAN_DECIMAL_CALENDAR = new FrenchRepublicanDecimalCalendar();

  /**
   * Return the calendar instance for the given name.
   *
   * @param name Calendar name as returned by {@link #name()}.
   * @return The calendar instance.
   * @throws IllegalArgumentException If the name does not correspond to any calendar instance.
   */
  static Calendar<?> forName(@NotNull String name) {
    return switch (name) {
      case CopticCalendar.NAME -> COPTIC;
      case EthiopianCalendar.NAME -> ETHIOPIAN;
      case GregorianCalendar.NAME -> GREGORIAN;
      case JulianCalendar.NAME -> JULIAN;
      case FrenchRepublicanCalendar.NAME -> FRENCH_REPUBLICAN_CALENDAR;
      case FrenchRepublicanDecimalCalendar.NAME -> FRENCH_REPUBLICAN_DECIMAL_CALENDAR;
      default -> throw new IllegalArgumentException("Undefined calendar name: " + name);
    };
  }

  /**
   * Create a calendar-specific date.
   *
   * @param year   Date’s year.
   * @param month  Date’s month value.
   * @param day    Date’s day of month.
   * @param hour   Date’s hour. May be null.
   * @param minute Date’s minute. May be null.
   * @return A new calendar-specific date.
   */
  D getDate(int year, int month, int day, Integer hour, Integer minute);

  /**
   * Convert an ISO-8601 date into one specific to this calendar.
   *
   * @param date        Date to convert.
   * @param isTimeSet   Whether to take into account the time in the first argument
   * @return The equivalent calendar-specific date.
   */
  D convertDate(@NotNull LocalDateTime date, boolean isTimeSet);

  /**
   * Name of this calendar.
   */
  String name();

  /**
   * Number of months in a year of this calendar.
   */
  int lengthOfYearInMonths();
}
