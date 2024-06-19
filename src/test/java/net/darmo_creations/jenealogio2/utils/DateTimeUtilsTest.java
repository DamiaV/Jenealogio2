package net.darmo_creations.jenealogio2.utils;

import org.junit.jupiter.api.*;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeUtilsTest {
  @Test
  void testFormat() {
    assertEquals(
        "1234-05-06 07:08:09",
        DateTimeUtils.format(LocalDateTime.of(1234, 5, 6, 7, 8, 9))
    );
  }

  @Test
  void testFormatFileName() {
    assertEquals(
        "1234-05-06_07.08.09",
        DateTimeUtils.formatFileName(LocalDateTime.of(1234, 5, 6, 7, 8, 9))
    );
  }
}