package net.darmo_creations.jenealogio2.utils;

import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.datetime.calendar.Calendar;
import net.darmo_creations.jenealogio2.model.datetime.calendar.*;
import org.jetbrains.annotations.*;

import java.time.*;
import java.util.*;

/**
 * Custom date-time formatter.
 * <p>
 * Available format tags:
 * <li>{@code %d}: Day of the month as a non-padded decimal number.</li>
 * <li>{@code %D}: Day of the month as a zero-padded decimal number.</li>
 * <li>{@code %b}: Month as language’s abbreviated name.</li>
 * <li>{@code %B}: Month as language’s full name.</li>
 * <li>{@code %n}: Month as a non-padded decimal number.</li>
 * <li>{@code %m}: Month as a zero-padded decimal number.</li>
 * <li>{@code %y}: Year as a 4-digit zero-padded decimal number.</li>
 * <li>{@code %H}: Hour (24-hour clock) as a zero-padded decimal number.</li>
 * <li>{@code %h}: Hour (12-hour clock) as a zero-padded decimal number.</li>
 * <li>{@code %I}: Hour (24-hour clock) as a non-padded decimal number.</li>
 * <li>{@code %i}: Hour (12-hour clock) as a non-padded decimal number.</li>
 * <li>{@code %M}: Minute as a zero-padded decimal number.</li>
 * <li>{@code %p}: Language’s equivalent of either AM or PM.</li>
 * <li>{@code %s}: Language’s equivalent of <em>st</em>, <em>th</em>, etc.</li>
 */
public class CalendarDateTimeFormatter {
  private final Language language;
  private final List<Token> tokens = new LinkedList<>();

  public CalendarDateTimeFormatter(@NotNull Language language, @NotNull String pattern) {
    this.language = Objects.requireNonNull(language);
    this.parsePattern(pattern);
  }

  private void parsePattern(@NotNull String pattern) {
    StringBuilder sb = new StringBuilder();
    boolean percent = false;
    for (char c : pattern.toCharArray()) {
      if (percent) {
        if (!sb.isEmpty()) {
          try {
            this.tokens.add(new LiteraToken(sb.toString()));
          } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid format tag %%%c in pattern \"%s\"".formatted(c, pattern), e);
          }
          sb.setLength(0);
        }
        this.tokens.add(new FormatTagToken(FormatTag.fromChar(c)));
        percent = false;
      } else {
        if (c == '%') {
          percent = true;
        } else {
          sb.append(c);
        }
      }
    }
    if (percent) {
      throw new IllegalArgumentException("Incomplete format tag in pattern \"%s\"".formatted(pattern));
    }
    if (!sb.isEmpty()) {
      this.tokens.add(new LiteraToken(sb.toString()));
    }
  }

  /**
   * Format a {@link LocalDateTime} object.
   *
   * @param dateTime The date to format.
   * @return The formatted date.
   */
  public String format(@NotNull LocalDateTime dateTime) {
    return this.format(
        dateTime.getYear(),
        dateTime.getMonthValue(),
        dateTime.getDayOfMonth(),
        dateTime.getHour(),
        dateTime.getMinute(),
        Calendars.GREGORIAN
    );
  }

  /**
   * Format a {@link LocalDateTime} object.
   *
   * @param dateTime The date to format.
   * @return The formatted date.
   */
  public String format(@NotNull CalendarSpecificDateTime dateTime) {
    return this.format(
        dateTime.year(),
        dateTime.month(),
        dateTime.dayOfMonth(),
        dateTime.hour().orElse(0),
        dateTime.minute().orElse(0),
        dateTime.calendar()
    );
  }

  private String format(int y, int m, int d, int h, int min, Calendar<?> calendar) {
    StringBuilder sb = new StringBuilder();
    for (Token token : this.tokens) {
      token.apply(sb, y, m, d, h, min, calendar);
    }
    return sb.toString();
  }

  private interface Token {
    void apply(@NotNull StringBuilder sb, int y, int m, int d, int h, int min, @NotNull Calendar<?> calendar);
  }

  private record LiteraToken(@NotNull String text) implements Token {
    private LiteraToken {
      Objects.requireNonNull(text);
    }

    @Override
    public void apply(@NotNull StringBuilder sb, int y, int m, int d, int h, int min, @NotNull Calendar<?> calendar) {
      sb.append(this.text);
    }
  }

  private class FormatTagToken implements Token {
    private final FormatTag type;

    private FormatTagToken(@NotNull FormatTag type) {
      this.type = Objects.requireNonNull(type);
    }

    @Override
    public void apply(@NotNull StringBuilder sb, int y, int m, int d, int h, int min, @NotNull Calendar<?> calendar) {
      Language lang = CalendarDateTimeFormatter.this.language;
      switch (this.type) {
        case DAY -> sb.append(d);
        case DAY_PADDED -> sb.append("%02d".formatted(d));
        case MONTH_NAME_ABBR -> sb.append(calendar.getMonthName(lang, m, true));
        case MONTH_NAME -> sb.append(calendar.getMonthName(lang, m, false));
        case MONTH -> sb.append(m);
        case MONTH_PADDED -> sb.append("%02d".formatted(m));
        case YEAR -> sb.append(y);
        case HOUR_24 -> sb.append(h);
        case HOUR_12 -> {
          if (calendar.hoursInDay() == 24) {
            sb.append(h % 12 == 0 ? 12 : h % 12);
          } else {
            throw new IllegalArgumentException(
                "Cannot use %s tag with non-24h calendar: %s".formatted(FormatTag.HOUR_12, calendar.name()));
          }
        }
        case HOUR_24_PADDED -> sb.append("%02d".formatted(h));
        case HOUR_12_PADDED -> {
          if (calendar.hoursInDay() == 24) {
            sb.append("%02d".formatted(h % 12 == 0 ? 12 : h % 12));
          } else {
            throw new IllegalArgumentException(
                "Cannot use %s tag with non-24h calendar: %s".formatted(FormatTag.HOUR_12_PADDED, calendar.name()));
          }
        }
        case MINUTE -> sb.append("%02d".formatted(min));
        case AMPM -> {
          if (calendar.hoursInDay() == 24) {
            int noon = calendar.hoursInDay() / 2;
            sb.append(lang.translate(h < noon ? "calendar.am" : "calendar.pm"));
          } else {
            throw new IllegalArgumentException(
                "Cannot use %s tag with non-24h calendar: %s".formatted(FormatTag.AMPM, calendar.name()));
          }
        }
        case SUFFIX -> lang.getDaySuffix(d).ifPresent(sb::append);
        case PERCENT -> sb.append('%');
      }
    }
  }

  private enum FormatTag {
    DAY('d'),
    DAY_PADDED('D'),
    MONTH_NAME_ABBR('b'),
    MONTH_NAME('B'),
    MONTH('n'),
    MONTH_PADDED('m'),
    YEAR('y'),
    HOUR_24_PADDED('H'),
    HOUR_12_PADDED('h'),
    HOUR_24('I'),
    HOUR_12('i'),
    MINUTE('M'),
    AMPM('p'),
    SUFFIX('s'),
    PERCENT('%'),
    ;

    private final char tag;

    FormatTag(char tag) {
      this.tag = tag;
    }

    public char tag() {
      return this.tag;
    }

    public static FormatTag fromChar(char c) {
      return Arrays.stream(values())
          .filter(t -> t.tag() == c)
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException("Invalid format tag: %" + c));
    }

    @Override
    public String toString() {
      return "%" + this.tag;
    }
  }
}
