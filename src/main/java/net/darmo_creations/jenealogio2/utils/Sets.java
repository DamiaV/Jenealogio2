package net.darmo_creations.jenealogio2.utils;

import org.jetbrains.annotations.*;

import java.util.*;
import java.util.stream.*;

/**
 * This class provides utility methods to manipulate {@link Set}s.
 */
public final class Sets {
  /**
   * Merge the {@link Set} values of a map into a single {@link Set}.
   *
   * @param map The map to merge the value {@link Set}s of.
   * @param <T> Type of elements.
   * @return A new {@link Set} containing the union of all value {@link Set}s from the map.
   */
  @Contract("_ -> new")
  public static <T> Set<T> merge(final @NotNull Map<?, Set<T>> map) {
    return map.values().stream()
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
  }

  /**
   * Compute the difference {@code a \ b}.
   *
   * @param a   A set.
   * @param b   Another set.
   * @param <T> Type of elements.
   * @return A new set containing the result of {@code a \ b}.
   */
  @Contract("_, _ -> new")
  public static <T> Set<T> difference(final @NotNull Set<T> a, final @NotNull Set<T> b) {
    final Set<T> result = new HashSet<>(a);
    result.removeAll(b);
    return result;
  }

  /**
   * Compute the set union of the given sets.
   * If no sets are provided, an empty set is returned.
   *
   * @param sets The sets to get the union of.
   * @param <T>  Type of elements.
   * @return A new set containing the result of the sets’ union.
   */
  @SafeVarargs
  @Contract("_ -> new")
  public static <T> Set<T> union(final @NotNull Set<T>... sets) {
    final Set<T> result = new HashSet<>();
    for (final Set<T> set : sets) result.addAll(set);
    return result;
  }

  private Sets() {
  }
}
