package net.darmo_creations.jenealogio2.model.datetime;

import net.darmo_creations.jenealogio2.model.datetime.calendar.Calendar;
import net.darmo_creations.jenealogio2.model.datetime.calendar.*;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeAlternativeTest {
  @Test
  void dateIsFirstDate() {
    final var date = new DateTimeAlternative(new ArrayList<>(List.of(
        Calendar.forName(GregorianCalendarSystem.NAME).getDate(null, 2024, 1, 1, 0, 0),
        Calendar.forName(GregorianCalendarSystem.NAME).getDate(null, 2024, 2, 1, 0, 0))));
    assertEquals(date.dates().get(0), date.date());
  }

  @Test
  void emptyListThrows() {
    assertThrows(IllegalArgumentException.class, () -> new DateTimeAlternative(new ArrayList<>()));
  }

  @Test
  void nullElementThrows() {
    //noinspection DataFlowIssue
    assertThrows(NullPointerException.class, () -> new DateTimeAlternative(new ArrayList<>(List.of(
        Calendar.forName(GregorianCalendarSystem.NAME).getDate(null, 2024, 1, 1, 0, 0),
        null
    ))));
  }

  @Test
  void tooFewDatesThrows() {
    assertThrows(IllegalArgumentException.class, () -> new DateTimeAlternative(new ArrayList<>(List.of(
        Calendar.forName(GregorianCalendarSystem.NAME).getDate(null, 2024, 1, 1, 0, 0)
    ))));
  }

  @Test
  void tooManyDatesThrows() {
    final List<CalendarSpecificDateTime> dates = new ArrayList<>();
    for (int i = 0; i < DateTimeAlternative.MAX_DATES + 1; i++) {
      dates.add(Calendar.forName(GregorianCalendarSystem.NAME).getDate(null, 2024, 1, 1, 0, i));
    }
    assertThrows(IllegalArgumentException.class, () -> new DateTimeAlternative(dates));
  }
}