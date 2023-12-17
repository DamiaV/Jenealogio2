package net.darmo_creations.jenealogio2.model.datetime.calendar;

import org.jetbrains.annotations.*;

import java.time.*;
import java.util.*;

/**
 * This class represents a date-time specific to a calendar system.
 *
 * @see Calendar
 */
public abstract sealed class CalendarSpecificDateTime implements Comparable<CalendarSpecificDateTime>
    permits CopticDateTime, EthiopianDateTime, FrenchRepublicanDecimalDateTime, FrenchRepublicanDateTime,
    GregorianDateTime, JulianDateTime {
  private final Calendar<?> calendar;
  private final int year;
  private final int month;
  private final int day;
  private final Integer hour;
  private final Integer minute;
  private final boolean isTimeSet;

  /**
   * Create a calendar-specific date-time object. Only hours’ and minutes’ bounds are checked,
   * year, month and day MUST be checked by sub-classes.
   */
  protected CalendarSpecificDateTime(
      int year,
      int month,
      int day,
      Integer hour,
      Integer minute,
      @NotNull Calendar<?> calendar
  ) {
    this.year = year;
    this.month = month;
    this.day = day;
    this.calendar = Objects.requireNonNull(calendar);
    this.isTimeSet = hour != null && minute != null;
    if (this.isTimeSet && (hour < 0 || hour >= this.calendar.hoursInDay())) {
      throw new IllegalArgumentException(
          "Hour out of range: expected [0, %d[, got %d".formatted(this.calendar.hoursInDay(), hour));
    }
    this.hour = this.isTimeSet ? hour : null;
    if (this.isTimeSet && (minute < 0 || minute >= this.calendar.minutesInHour())) {
      throw new IllegalArgumentException(
          "Minute out of range: expected [0, %d[, got %d".formatted(this.calendar.minutesInHour(), minute));
    }
    this.minute = this.isTimeSet ? minute : null;
  }

  /**
   * The calender system this date is defined in.
   */
  public final Calendar<?> calendar() {
    return this.calendar;
  }

  /**
   * This date’s year.
   */
  public final int year() {
    return this.year;
  }

  /**
   * This date’s month value.
   */
  public final int month() {
    return this.month;
  }

  /**
   * This date’s day of month.
   */
  public final int dayOfMonth() {
    return this.day;
  }

  /**
   * This date’s hour.
   */
  public final Optional<Integer> hour() {
    return Optional.ofNullable(this.hour);
  }

  /**
   * This date’s minute.
   */
  public final Optional<Integer> minute() {
    return Optional.ofNullable(this.minute);
  }

  /**
   * Indicate whether the hour and minute values are available.
   */
  public final boolean isTimeSet() {
    return this.isTimeSet;
  }

  /**
   * Convert this date into an ISO-8601 date.
   */
  public abstract LocalDateTime toISO8601Date();

  @Override
  public final int compareTo(final @NotNull CalendarSpecificDateTime o) {
    return this.toISO8601Date().compareTo(o.toISO8601Date());
  }

  @Override
  public final String toString() {
    // FIXME negative years
    String date = "%04d-%02d-%02d".formatted(this.year, this.month, this.day);
    String hour = this.hour != null ? "%02d".formatted(this.hour) : "#";
    String minute = this.minute != null ? "%02d".formatted(this.minute) : "#";
    if (this.isTimeSet) {
      date += "T%s:%s".formatted(hour, minute);
    }
    return date;
  }
}
