package net.darmo_creations.jenealogio2.model.datetime.calendar;

import net.darmo_creations.jenealogio2.config.*;
import net.time4j.engine.*;
import org.jetbrains.annotations.*;

import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.regex.*;

/**
 * This interface represents a calendar system.
 * Calendars can convert {@link LocalDateTime} objects into their custom {@link CalendarSpecificDateTime} equivalents.
 *
 * @param <D> Type of custom dates for this calendar.
 */
public abstract class Calendar<D extends CalendarSpecificDateTime> {
  /**
   * Pattern used to deserialize date-strings: [-]YYYY-MM-DD[Thh:mm]
   */
  private static final Pattern DATE_PATTERN = Pattern.compile(
      "^(?<year>-?\\d{4,})-(?<month>\\d{2})-(?<dayOfMonth>\\d{2})(?:T(?<hours>\\d{2}):(?<minutes>\\d{2}))?(?:E(?<era>[a-z_]+))?$");

  /**
   * Return the calendar instance for the given name.
   *
   * @param name Calendar name as returned by {@link #name()}.
   * @return The calendar instance.
   * @throws IllegalArgumentException If the name does not correspond to any calendar instance.
   */
  public static Calendar<?> forName(@NotNull String name) {
    return switch (name) {
      case CopticCalendarSystem.NAME -> Calendars.COPTIC;
      case EthiopianCalendarSystem.NAME -> Calendars.ETHIOPIAN;
      case FrenchRepublicanCalendar.NAME -> Calendars.FRENCH_REPUBLICAN_CALENDAR;
      case FrenchRepublicanDecimalCalendar.NAME -> Calendars.FRENCH_REPUBLICAN_DECIMAL_CALENDAR;
      case GregorianCalendarSystem.NAME -> Calendars.GREGORIAN;
      case HebrewCalendarSystem.NAME -> Calendars.HEBREW;
      case IndianCalendarSystem.NAME -> Calendars.INDIAN;
      case JulianCalendarSystem.NAME -> Calendars.JULIAN;
      case MinguoCalendarSystem.NAME -> Calendars.MINGUO;
      case SolarHijriCalendarSystem.NAME -> Calendars.SOLAR_HIJRI;
      case ThaiSolarCalendarSystem.NAME -> Calendars.THAI_SOLAR;
      default -> throw new IllegalArgumentException("Undefined calendar name: " + name);
    };
  }

  private final String name;
  private final int monthsInYear;
  private final int hoursInDay;
  private final int minutesInHour;
  @Unmodifiable
  private final List<CalendarEra> eras;

  protected Calendar(String name, int monthsInYear, int hoursInDay, int minutesInHour, final @NotNull CalendarEra... eras) {
    this.name = name;
    this.monthsInYear = monthsInYear;
    this.hoursInDay = hoursInDay;
    this.minutesInHour = minutesInHour;
    this.eras = List.of(eras);
  }

  /**
   * Create a calendar-specific date.
   *
   * @param era    Date’s era. May be null if this calendar does not feature eras.
   * @param year   Date’s year.
   * @param month  Date’s month value.
   * @param day    Date’s day of month.
   * @param hour   Date’s hour. May be null.
   * @param minute Date’s minute. May be null.
   * @return A new calendar-specific date.
   */
  public abstract D getDate(CalendarEra era, int year, int month, int day, Integer hour, Integer minute);

  /**
   * Convert an ISO-8601 date into one specific to this calendar.
   *
   * @param date      Date to convert.
   * @param isTimeSet Whether to take into account the time in the first argument
   * @return The equivalent calendar-specific date.
   */
  public abstract D convertDate(@NotNull LocalDateTime date, boolean isTimeSet);

  /**
   * Parse a {@code -?YYYY-MM-DDThh:mmEera} string into a date from this calendar system.
   *
   * @param dateString String to parse.
   * @return A date object.
   * @throws DateTimeParseException If the string does not represent a valid date from this calender system.
   * @throws NullPointerException   If the string is null.
   */
  public final D parse(@NotNull String dateString) throws DateTimeParseException {
    final Matcher matcher = DATE_PATTERN.matcher(dateString);
    if (!matcher.find())
      throw new DateTimeParseException("String \"%s\" does not represent a valid date".formatted(dateString), dateString, 0);
    final int year = Integer.parseInt(matcher.group("year"));
    final int month = Integer.parseInt(matcher.group("month"));
    final int day = Integer.parseInt(matcher.group("dayOfMonth"));
    Integer hour = null, minute = null;
    if (matcher.group("hours") != null && matcher.group("minutes") != null) {
      hour = Integer.parseInt(matcher.group("hours"));
      minute = Integer.parseInt(matcher.group("minutes"));
    }
    final String eraString = matcher.group("era");
    CalendarEra era = null;
    if (eraString != null)
      era = this.eras.stream()
          .filter(e -> e.name().equals(eraString.toUpperCase()))
          .findFirst()
          .orElseThrow(() -> new DateTimeParseException("Invalid era \"%s\"".formatted(eraString), dateString, 0));
    return this.getDate(era, year, month, day, hour, minute);
  }

  /**
   * Name of this calendar.
   */
  public final String name() {
    return this.name;
  }

  /**
   * Number of months in a year of this calendar.
   */
  public final int lengthOfYearInMonths() {
    return this.monthsInYear;
  }

  /**
   * The number of hours in an hour.
   */
  public final int hoursInDay() {
    return this.hoursInDay;
  }

  /**
   * The number of minutes in an hour.
   */
  public final int minutesInHour() {
    return this.minutesInHour;
  }

  /**
   * The eras supported by this calendar. May be empty.
   * The returned list is unmodifiable.
   */
  @Unmodifiable
  public final List<CalendarEra> eras() {
    return this.eras;
  }

  /**
   * Return the localized name of a month of this calendar.
   *
   * @param language  Language to use.
   * @param month     1-indexed number of the month.
   * @param shortForm Whether to return the month name’s short form.
   * @return The month’s name.
   */
  public final String getMonthName(@NotNull Language language, int month, boolean shortForm) {
    final String key = "calendar.%s.month.%d".formatted(this.name(), month);
    if (!shortForm)
      return language.translate(key);
    final String shortKey = key + ".short";
    return language.translate(language.hasKey(shortKey) ? shortKey : key);
  }

  /**
   * Return the localized name of an era of this calendar.
   *
   * @param language  Language to use.
   * @param era       A {@link CalendarEra}.
   * @param shortForm Whether to return the era’s short form.
   * @return The era’s name.
   */
  public final String getEraName(@NotNull Language language, @NotNull CalendarEra era, boolean shortForm) {
    final String key = "calendar.%s.era.%s".formatted(this.name(), era.name().toLowerCase());
    if (!shortForm)
      return language.translate(key);
    final String shortKey = key + ".short";
    return language.translate(language.hasKey(shortKey) ? shortKey : key);
  }
}
