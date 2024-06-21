package net.darmo_creations.jenealogio2.model.datetime;

import net.darmo_creations.jenealogio2.model.datetime.calendar.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeAlternativeTest {
  @Test
  void dateIsEarliestDate() {
    var date = new DateTimeAlternative(
        Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0),
        Calendar.forName(GregorianCalendar.NAME).getDate(2024, 2, 1, 0, 0));
    assertEquals(date.earliestDate(), date.date());
  }

  @Test
  void latestBeforeEarliestThrows() {
    assertThrows(IllegalArgumentException.class, () -> new DateTimeAlternative(
        Calendar.forName(GregorianCalendar.NAME).getDate(2024, 2, 1, 0, 0),
        Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0))
    );
  }

  @Test
  void earliestEqualsLatestThrows() {
    assertThrows(IllegalArgumentException.class, () -> new DateTimeAlternative(
        Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0),
        Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0))
    );
  }
}