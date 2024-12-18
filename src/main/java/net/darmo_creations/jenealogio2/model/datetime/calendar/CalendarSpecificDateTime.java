package net.darmo_creations.jenealogio2.model.datetime.calendar;

import net.time4j.engine.*;
import org.jetbrains.annotations.*;

import java.time.*;
import java.util.*;

/**
 * This class represents a date-time specific to a calendar system.
 *
 * @see Calendar
 */
public abstract class CalendarSpecificDateTime implements Comparable<CalendarSpecificDateTime> {
  private final Calendar<?> calendar;
  private final CalendarEra era;
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
    this(null, year, month, day, hour, minute, calendar);
  }

  /**
   * Create a calendar-specific date-time object. Only hours’ and minutes’ bounds are checked,
   * year, month and day MUST be checked by sub-classes.
   */
  protected CalendarSpecificDateTime(
      CalendarEra era,
      int year,
      int month,
      int day,
      Integer hour,
      Integer minute,
      @NotNull Calendar<?> calendar
  ) {
    this.era = era;
    this.year = year;
    this.month = month;
    this.day = day;
    this.calendar = Objects.requireNonNull(calendar);
    this.isTimeSet = hour != null && minute != null;
    if (this.isTimeSet && (hour < 0 || hour >= this.calendar.hoursInDay()))
      throw new IllegalArgumentException(
          "Hour out of range: expected [0, %d[, got %d".formatted(this.calendar.hoursInDay(), hour));
    this.hour = this.isTimeSet ? hour : null;
    if (this.isTimeSet && (minute < 0 || minute >= this.calendar.minutesInHour()))
      throw new IllegalArgumentException(
          "Minute out of range: expected [0, %d[, got %d".formatted(this.calendar.minutesInHour(), minute));
    this.minute = this.isTimeSet ? minute : null;
  }

  /**
   * The calender system this date is defined in.
   */
  public final Calendar<?> calendar() {
    return this.calendar;
  }

  /**
   * This date’s era, for calendar systems that require one.
   */
  public final Optional<CalendarEra> era() {
    return Optional.ofNullable(this.era);
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
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || this.getClass() != o.getClass())
      return false;
    final CalendarSpecificDateTime that = (CalendarSpecificDateTime) o;
    return Objects.equals(this.calendar, that.calendar) &&
        Objects.equals(this.era, that.era) &&
        this.year == that.year &&
        this.month == that.month &&
        this.day == that.day &&
        this.isTimeSet == that.isTimeSet &&
        Objects.equals(this.hour, that.hour) &&
        Objects.equals(this.minute, that.minute);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        this.calendar,
        this.era,
        this.year,
        this.month,
        this.day,
        this.hour,
        this.minute,
        this.isTimeSet
    );
  }

  @Override
  public final String toString() {
    String s;
    if (this.year >= 0)
      s = "%04d-%02d-%02d".formatted(this.year, this.month, this.day);
    else
      s = "-%04d-%02d-%02d".formatted(-this.year, this.month, this.day);
    final String hour = this.hour != null ? "%02d".formatted(this.hour) : "#";
    final String minute = this.minute != null ? "%02d".formatted(this.minute) : "#";
    if (this.isTimeSet)
      s += "T%s:%s".formatted(hour, minute);
    if (this.era != null)
      s += "E" + this.era.name().toLowerCase();
    return s;
  }
}
