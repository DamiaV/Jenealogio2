package net.darmo_creations.jenealogio2.model.datetime;

import net.darmo_creations.jenealogio2.model.datetime.calendar.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * This class represents an alternative between two dates.
 * The {@link #date()} attribute corresponds to the first element of {@link #dates()}, i.e. the earliest date.
 *
 * @param dates The list of dates. Must not be empty and not contain any null elements.
 *              The passed list object is copied and this copy is then sorted.
 */
public record DateTimeAlternative(@NotNull List<@NotNull CalendarSpecificDateTime> dates)
    implements DateTime {
  public static final int MAX_DATES = 5;

  public DateTimeAlternative(@NotNull List<@NotNull CalendarSpecificDateTime> dates) {
    if (dates.size() < 2)
      throw new IllegalArgumentException("Too few dates: expected at least 2 dates, got %d".formatted(dates.size()));
    if (dates.size() > MAX_DATES)
      throw new IllegalArgumentException("List cannot contain more than %d dates".formatted(MAX_DATES));
    //noinspection ConstantValue
    if (dates.stream().anyMatch(Objects::isNull))
      throw new IllegalArgumentException("List cannot contain null elements");
    this.dates = dates.stream().sorted().toList();
  }

  @Override
  @Unmodifiable
  public CalendarSpecificDateTime date() {
    return this.dates.get(0);
  }
}
