package net.darmo_creations.jenealogio2.utils;

import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.datetime.*;
import net.darmo_creations.jenealogio2.model.datetime.calendar.*;
import org.jetbrains.annotations.*;

import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Class providing methods to handle objects from the {@link java.time} package.
 */
public final class DateTimeUtils {
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static final DateTimeFormatter FILE_NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss");

  /**
   * Format a {@link LocalDateTime} object using the default formatter.
   *
   * @param dateTime Object to format.
   * @return Formatted date string.
   */
  public static String format(@NotNull LocalDateTime dateTime) {
    return dateTime.format(FORMATTER);
  }

  /**
   * Format a {@link LocalDateTime} object using the file name formatter.
   *
   * @param dateTime Object to format.
   * @return The formatted date string, compatible as a file name.
   */
  public static String formatFileName(@NotNull LocalDateTime dateTime) {
    return dateTime.format(FILE_NAME_FORMATTER);
  }

  /**
   * Format a {@link DateTime} object according to the current app configuration.
   *
   * @param date       Date to format.
   * @param useIsoDate If true, the corresponding ISO date will be formatted instead of the calendar-specific date.
   * @param config     The app’s config.
   * @return The formatted date.
   */
  public static String formatDateTime(@NotNull DateTime date, boolean useIsoDate, final @NotNull Config config) {
    Objects.requireNonNull(date);
    Language language = config.language();
    String dateFormat = config.dateFormat().getFormat();
    TimeFormat tf = config.timeFormat();
    String timeFormat = tf.getFormat();
    String fullTimeFormat = tf.getFullVersion().getFormat();
    var dateFormatter = new CalendarDateTimeFormatter(language, "%s, %s".formatted(dateFormat, timeFormat));
    var fullDateFormatter = new CalendarDateTimeFormatter(language, "%s, %s".formatted(dateFormat, fullTimeFormat));
    var dateFormatterNoHour = new CalendarDateTimeFormatter(language, dateFormat);
    Function<CalendarSpecificDateTime, String> formatter = d -> {
      CalendarDateTimeFormatter f;
      if (d.isTimeSet()) {
        f = useIsoDate || d.calendar().hoursInDay() == 24 ? dateFormatter : fullDateFormatter;
      } else {
        f = dateFormatterNoHour;
      }
      return useIsoDate ? f.format(d.toISO8601Date()) : f.format(d);
    };

    if (date instanceof DateTimeWithPrecision d) {
      return language.translate(
          "date_format." + d.precision().name().toLowerCase(),
          new FormatArg("date", formatter.apply(d.date()))
      );
    }
    if (date instanceof DateTimeRange d) {
      return language.translate(
          "date_format.range",
          new FormatArg("date1", formatter.apply(d.startDate())),
          new FormatArg("date2", formatter.apply(d.endDate()))
      );
    }
    if (date instanceof DateTimeAlternative d) {
      String comma = language.translate("list_comma");
      String or = language.translate("list_or");
      String str = d.dates().stream().map(formatter).collect(Collectors.joining(comma));
      StringBuilder sb = new StringBuilder(str);
      // Replace last occurrence of "comma" by "or"
      sb.replace(str.lastIndexOf(comma), str.lastIndexOf(comma) + comma.length(), or);
      return sb.toString();
    }

    throw new IllegalArgumentException("Unsupported date type: " + date.getClass());
  }

  private DateTimeUtils() {
  }
}
