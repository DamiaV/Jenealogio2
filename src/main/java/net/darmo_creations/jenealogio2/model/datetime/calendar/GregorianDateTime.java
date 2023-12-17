package net.darmo_creations.jenealogio2.model.datetime.calendar;

import org.jetbrains.annotations.*;

import java.time.*;

/**
 * This class represents a date-time in the gregorian calendar system.
 *
 * @see GregorianCalendar
 */
public final class GregorianDateTime extends CalendarSpecificDateTime {
  GregorianDateTime(
      @NotNull LocalDate date,
      Integer hour,
      Integer minute,
      @NotNull Calendar<GregorianDateTime> calendar
  ) {
    super(
        date.getYear(),
        date.getMonthValue(),
        date.getDayOfMonth(),
        hour,
        minute,
        calendar
    );
  }

  @Override
  public LocalDateTime toISO8601Date() {
    return LocalDateTime.of(
        this.year(),
        this.month(),
        this.dayOfMonth(),
        this.hour().orElse(0),
        this.minute().orElse(0)
    );
  }
}
