package net.darmo_creations.jenealogio2.utils;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtils {
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static final DateTimeFormatter FILE_NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss");

  public static String format(@NotNull LocalDateTime dateTime) {
    return dateTime.format(FORMATTER);
  }

  public static String formatFileName(@NotNull LocalDateTime dateTime) {
    return dateTime.format(FILE_NAME_FORMATTER);
  }

  private DateTimeUtils() {
  }
}
