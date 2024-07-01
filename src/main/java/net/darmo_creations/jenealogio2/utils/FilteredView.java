package net.darmo_creations.jenealogio2.utils;

import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.*;

/**
 * This class offers an unmodifiable filtered view for a given {@link Collection}.
 *
 * @param <T> Type of the elements of the original collection.
 * @param <U> Type of the elements that match the predicate.
 */
public class FilteredView<T, U extends T> implements Collection<U> {
  private final Collection<T> collection;
  private final Predicate<T> predicate;

  /**
   * Create a filtered view for the given collection.
   *
   * @param collection The collection to wrap.
   * @param predicate  A predicate.
   */
  public FilteredView(final @NotNull Collection<T> collection, @NotNull Predicate<T> predicate) {
    this.collection = Objects.requireNonNull(collection);
    this.predicate = Objects.requireNonNull(predicate);
  }

  @Override
  public int size() {
    return (int) this.collection.stream()
        .filter(this.predicate)
        .count();
  }

  @Override
  public boolean isEmpty() {
    return this.collection.stream()
        .noneMatch(this.predicate);
  }

  @Override
  public boolean contains(Object o) {
    return this.collection.stream()
        .anyMatch(this.predicate.and(Predicate.isEqual(o)));
  }

  @Override
  public Iterator<U> iterator() {
    //noinspection unchecked
    return this.collection.stream()
        .filter(this.predicate)
        .map(o -> (U) o)
        .iterator();
  }

  @Override
  public Object[] toArray() {
    return this.collection.stream()
        .filter(this.predicate)
        .toArray();
  }

  @Override
  public <T1> T1[] toArray(@NotNull T1[] a) {
    return this.collection.stream()
        .filter(this.predicate)
        .toList()
        .toArray(a);
  }

  @Override
  public boolean add(U t) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean remove(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsAll(@NotNull Collection<?> c) {
    return this.collection.stream()
        .filter(this.predicate)
        .allMatch(c::contains);
  }

  @Override
  public boolean addAll(@NotNull Collection<? extends U> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAll(@NotNull Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean retainAll(@NotNull Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }
}
