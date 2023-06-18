package net.darmo_creations.jenealogio2.model.datetime.calendar;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wrapper class that associates a {@link LocalDateTime} object to a {@link Calendar}.
 * <p>
 * Additional fields indicate which fields should be taken into account.
 *
 * @param iso8601Date A date-time object interpreted as being in the ISO-8601 calendar.
 * @param calendar    The date’s calendar for conversions.
 * @param isTimeSet   Whether to take into account the time in the first argument.
 */
public record CalendarDateTime(
    @NotNull LocalDateTime iso8601Date,
    @NotNull Calendar<?> calendar,
    boolean isTimeSet
) implements Comparable<CalendarDateTime> {
  private static final Pattern DATE_PATTERN =
      Pattern.compile("^(\\d{4,}-\\d{2}-\\d{2})(?:T(\\d{2}):(\\d{2}))?$");

  public CalendarDateTime(@NotNull LocalDateTime iso8601Date, @NotNull Calendar<?> calendar) {
    this(iso8601Date, calendar, true);
  }

  public CalendarDateTime(
      @NotNull LocalDateTime iso8601Date,
      @NotNull Calendar<?> calendar,
      boolean isTimeSet
  ) {
    LocalDateTime date = Objects.requireNonNull(iso8601Date).withSecond(0);
    if (!isTimeSet) {
      date = date.withHour(0).withMinute(0);
    }
    this.iso8601Date = date;
    this.calendar = Objects.requireNonNull(calendar);
    this.isTimeSet = isTimeSet;
  }

  /**
   * Convert the date returned by {@link #iso8601Date()} into a date in the associated calendar.
   */
  public CalendarSpecificDateTime getCalendarDateTime() {
    return this.calendar.convertDate(this.iso8601Date, this.isTimeSet);
  }

  @Override
  public int compareTo(@NotNull CalendarDateTime other) {
    return this.iso8601Date.compareTo(other.iso8601Date());
  }

  @Override
  public String toString() {
    String hour = this.formatField(this.iso8601Date.getHour(), this.isTimeSet);
    String minute = this.formatField(this.iso8601Date.getMinute(), this.isTimeSet);
    String date = this.iso8601Date.toLocalDate().toString();
    if (this.isTimeSet) {
      date += "T%s:%s".formatted(hour, minute);
    }
    return "%s;%s".formatted(date, this.calendar.name());
  }

  private String formatField(int field, boolean isSet) {
    return isSet ? ("%02d").formatted(field) : "#";
  }

  /**
   * Parse a string representing a {@link CalendarDateTime} object.
   *
   * @param dateString String to parse.
   * @return The corresponding date object.
   */
  public static CalendarDateTime parse(@NotNull String dateString) {
    String[] parts = dateString.split(";", 2);
    if (parts.length == 1) {
      parts = new String[] {parts[0], Calendar.GREGORIAN.name()};
    } else if (parts.length != 2) {
      throw new DateTimeParseException("Text %s could not be parsed.".formatted(dateString), dateString, 0);
    }
    Matcher matcher = DATE_PATTERN.matcher(parts[0]);
    if (!matcher.find()) {
      throw new DateTimeParseException("Text %s could not be parsed.".formatted(dateString), dateString, 0);
    }
    boolean isTimeSet = matcher.group(2) != null;
    int hour = 0, minute = 0;
    if (isTimeSet) {
      hour = Integer.parseInt(matcher.group(2));
      minute = Integer.parseInt(matcher.group(3));
    }
    LocalDateTime date = LocalDate.parse(matcher.group(1)).atTime(hour, minute);
    return new CalendarDateTime(date, Calendar.forName(parts[1]), isTimeSet);
  }
}
