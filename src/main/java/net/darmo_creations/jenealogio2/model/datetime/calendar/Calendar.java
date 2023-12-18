package net.darmo_creations.jenealogio2.model.datetime.calendar;

import net.darmo_creations.jenealogio2.config.*;
import org.jetbrains.annotations.*;

import java.time.*;
import java.time.format.*;
import java.util.regex.*;

/**
 * This interface represents a calendar system.
 * Calendars can convert {@link LocalDateTime} objects into their custom {@link CalendarSpecificDateTime} equivalents.
 *
 * @param <D> Type of custom dates for this calendar.
 */
public sealed abstract class Calendar<D extends CalendarSpecificDateTime>
    permits CopticCalendar, EthiopianCalendar, FrenchRepublicanDecimalCalendar, FrenchRepublicanCalendar,
    GregorianCalendar, JulianCalendar {
  /**
   * Pattern used to deserialize date-strings: [-]YYYY-MM-DD[Thh:mm]
   */
  private static final Pattern DATE_PATTERN = Pattern.compile("^(-?\\d{4,})-(\\d{2})-(\\d{2})(?:T(\\d{2}):(\\d{2}))?$");

  /**
   * Return the calendar instance for the given name.
   *
   * @param name Calendar name as returned by {@link #name()}.
   * @return The calendar instance.
   * @throws IllegalArgumentException If the name does not correspond to any calendar instance.
   */
  public static Calendar<?> forName(@NotNull String name) {
    return switch (name) {
      case CopticCalendar.NAME -> Calendars.COPTIC;
      case EthiopianCalendar.NAME -> Calendars.ETHIOPIAN;
      case GregorianCalendar.NAME -> Calendars.GREGORIAN;
      case JulianCalendar.NAME -> Calendars.JULIAN;
      case FrenchRepublicanCalendar.NAME -> Calendars.FRENCH_REPUBLICAN_CALENDAR;
      case FrenchRepublicanDecimalCalendar.NAME -> Calendars.FRENCH_REPUBLICAN_DECIMAL_CALENDAR;
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
  public abstract D getDate(int year, int month, int day, Integer hour, Integer minute);

  /**
   * Convert an ISO-8601 date into one specific to this calendar.
   *
   * @param date      Date to convert.
   * @param isTimeSet Whether to take into account the time in the first argument
   * @return The equivalent calendar-specific date.
   */
  public abstract D convertDate(@NotNull LocalDateTime date, boolean isTimeSet);

  /**
   * Parse a {@code -?YYYY-MM-DDThh:mm} string into a date from this calendar system.
   *
   * @param dateString String to parse.
   * @return A date object.
   * @throws DateTimeParseException If the string does not represent a valid date from this calender system.
   * @throws NullPointerException   If the string is null.
   */
  public final D parse(@NotNull String dateString) throws DateTimeParseException {
    Matcher matcher = DATE_PATTERN.matcher(dateString);
    if (!matcher.find()) {
      throw new DateTimeParseException("String \"%s\" does not represent a valid date".formatted(dateString), dateString, 0);
    }
    int year = Integer.parseInt(matcher.group(1));
    int month = Integer.parseInt(matcher.group(2));
    int day = Integer.parseInt(matcher.group(3));
    Integer hour = null, minute = null;
    boolean isTimeSet = matcher.group(4) != null && matcher.group(5) != null;
    if (isTimeSet) {
      hour = Integer.parseInt(matcher.group(4));
      minute = Integer.parseInt(matcher.group(5));
    }
    return this.getDate(year, month, day, hour, minute);
  }

  /**
   * Name of this calendar.
   */
  public abstract String name();

  /**
   * Return the localized name of a month of this calendar.
   *
   * @param language  Language to use.
   * @param month     1-indexed number of the month.
   * @param shortForm Whether to return the month name’s shot form.
   * @return The month’s name.
   */
  public final String getMonthName(@NotNull Language language, int month, boolean shortForm) {
    String key = "calendar.%s.month.%d".formatted(this.name(), month);
    if (!shortForm) {
      return language.translate(key);
    }
    String shortKey = key + ".short";
    return language.translate(language.hasKey(shortKey) ? shortKey : key);
  }

  /**
   * Number of months in a year of this calendar.
   */
  public abstract int lengthOfYearInMonths();

  /**
   * The number of hours in an hour.
   */
  public int hoursInDay() {
    return 24;
  }

  /**
   * The number of minutes in an hour.
   */
  public int minutesInHour() {
    return 60;
  }
}
