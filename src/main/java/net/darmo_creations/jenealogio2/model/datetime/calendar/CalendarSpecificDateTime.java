package net.darmo_creations.jenealogio2.model.datetime.calendar;

import java.time.*;
import java.util.*;

/**
 * This class represents a date-time object specific to a calendar system.
 *
 * @see Calendar
 */
public abstract sealed class CalendarSpecificDateTime
    permits CopticDateTime, EthiopianDateTime, FrenchRepublicanDecimalDateTime, FrenchRepublicanDateTime,
    GregorianDateTime, JulianDateTime {
  private final int year;
  private final int month;
  private final int day;
  private final Integer hour;
  private final Integer minute;

  /**
   * Create a non-standard date-time object. Only hours’ and minutes’ bounds are checked,
   * year, month and day MUST be checked by sub-classes.
   */
  protected CalendarSpecificDateTime(int year, int month, int day, Integer hour, Integer minute) {
    this.year = year;
    this.month = month;
    this.day = day;
    boolean isTimeSet = hour != null && minute != null;
    if (isTimeSet && (hour < 0 || hour >= this.hoursInDay())) {
      throw new IllegalArgumentException("Hour out of range: expected [0, %d[, got %d".formatted(this.hoursInDay(), hour));
    }
    this.hour = isTimeSet ? hour : null;
    if (isTimeSet && (minute < 0 || minute >= this.minutesInHour())) {
      throw new IllegalArgumentException("Minute out of range: expected [0, %d[, got %d".formatted(this.minutesInHour(), minute));
    }
    this.minute = isTimeSet ? minute : null;
  }

  /**
   * The number of hours in an hour.
   * Used by the constructor to check bounds.
   */
  protected abstract int hoursInDay();

  /**
   * The number of minutes in an hour.
   * Used by the constructor to check bounds.
   */
  protected abstract int minutesInHour();

  /**
   * Convert this date into an ISO-8601 date.
   */
  public abstract LocalDateTime toISO8601Date();

  /**
   * This date’s year.
   */
  public int year() {
    return this.year;
  }

  /**
   * This date’s month value.
   */
  public int month() {
    return this.month;
  }

  /**
   * This date’s day of month.
   */
  public int dayOfMonth() {
    return this.day;
  }

  /**
   * This date’s hour.
   */
  public Optional<Integer> hour() {
    return Optional.ofNullable(this.hour);
  }

  /**
   * This date’s minute.
   */
  public Optional<Integer> minute() {
    return Optional.ofNullable(this.minute);
  }
}
