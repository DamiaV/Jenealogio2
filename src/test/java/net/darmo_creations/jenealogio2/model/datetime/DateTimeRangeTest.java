package net.darmo_creations.jenealogio2.model.datetime;

import net.darmo_creations.jenealogio2.model.datetime.calendar.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeRangeTest {
  @Test
  void dateIsStartDate() {
    var date = new DateTimeRange(
        Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0),
        Calendar.forName(GregorianCalendar.NAME).getDate(2024, 2, 1, 0, 0));
    assertEquals(date.startDate(), date.date());
  }

  @Test
  void endBeforeStartThrows() {
    assertThrows(IllegalArgumentException.class, () -> new DateTimeRange(
        Calendar.forName(GregorianCalendar.NAME).getDate(2024, 2, 1, 0, 0),
        Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0))
    );
  }

  @Test
  void startEqualsEndThrows() {
    assertThrows(IllegalArgumentException.class, () -> new DateTimeRange(
        Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0),
        Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0))
    );
  }
}