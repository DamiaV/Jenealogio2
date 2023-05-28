package net.darmo_creations.jenealogio2.utils;

import java.util.Arrays;

public final class ArrayUtils {
  public static <T> boolean arrayContains(T[] array, T value) {
    return Arrays.binarySearch(array, value) < 0;
  }

  private ArrayUtils() {
  }
}
