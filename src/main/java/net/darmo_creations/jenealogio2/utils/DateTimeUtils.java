package net.darmo_creations.jenealogio2.utils;

import net.darmo_creations.jenealogio2.App;
import net.darmo_creations.jenealogio2.config.Config;
import net.darmo_creations.jenealogio2.config.Language;
import net.darmo_creations.jenealogio2.model.datetime.DateTime;
import net.darmo_creations.jenealogio2.model.datetime.DateTimeAlternative;
import net.darmo_creations.jenealogio2.model.datetime.DateTimeRange;
import net.darmo_creations.jenealogio2.model.datetime.DateTimeWithPrecision;
import net.darmo_creations.jenealogio2.model.datetime.calendar.CalendarDateTime;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.function.Function;

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
   * @param date Date to format.
   * @return The formatted date.
   */
  public static String formatDateTime(@NotNull DateTime date) {
    Objects.requireNonNull(date);
    Config config = App.config();
    Language language = config.language();
    String dateFormat = config.dateFormat().getFormat();
    String timeFormat = config.timeFormat().getFormat();
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("%s %s".formatted(dateFormat, timeFormat));
    DateTimeFormatter dateFormatterNoHour = DateTimeFormatter.ofPattern(dateFormat);
    Function<CalendarDateTime, String> formatter =
        d -> (d.isTimeSet() ? dateFormatter : dateFormatterNoHour).format(d.iso8601Date());

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
      return language.translate(
          "date_format.alternative",
          new FormatArg("date1", formatter.apply(d.earliestDate())),
          new FormatArg("date2", formatter.apply(d.latestDate()))
      );
    }

    throw new IllegalArgumentException("Unsupported date type: " + date.getClass());
  }

  private DateTimeUtils() {
  }
}
