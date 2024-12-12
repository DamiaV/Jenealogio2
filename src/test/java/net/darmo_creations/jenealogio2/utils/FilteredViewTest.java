package net.darmo_creations.jenealogio2.utils;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FilteredViewTest {
  private List<Integer> ref;
  private FilteredView<Integer, Integer> view;

  @BeforeEach
  void setUp() {
    this.ref = new LinkedList<>(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
    this.view = new FilteredView<>(this.ref, i -> i > 4);
  }

  @Test
  void size() {
    assertEquals(5, this.view.size());
  }

  @Test
  void isEmpty() {
    assertFalse(this.view.isEmpty());
  }

  @ParameterizedTest
  @ValueSource(ints = {5, 6, 7, 8, 9})
  void contains(int i) {
    assertTrue(this.view.contains(i));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 1, 2, 3, 4})
  void containsNo(int i) {
    assertFalse(this.view.contains(i));
  }

  @Test
  void iterator() {
    int j = 5;
    for (final Integer i : this.view)
      assertEquals(j++, i);
    assertEquals(10, j);
  }

  @Test
  void toArray() {
    assertArrayEquals(new Object[] {5, 6, 7, 8, 9}, this.view.toArray());
  }

  @Test
  void toArray2() {
    assertArrayEquals(new Integer[] {5, 6, 7, 8, 9}, this.view.toArray(new Integer[0]));
  }

  @Test
  void toArrayUpdatesArrayIfBigEnough() {
    final Integer[] array = new Integer[5];
    this.view.toArray(array);
    assertArrayEquals(new Integer[] {5, 6, 7, 8, 9}, array);
  }

  @Test
  void addThrows() {
    assertThrows(UnsupportedOperationException.class, () -> this.view.add(1));
  }

  @Test
  void removeThrows() {
    assertThrows(UnsupportedOperationException.class, () -> this.view.remove(1));
  }

  @Test
  void containsAll() {
    assertTrue(this.view.containsAll(List.of(5, 6, 7, 8, 9)));
  }

  @Test
  void addAllThrows() {
    assertThrows(UnsupportedOperationException.class, () -> this.view.addAll(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)));
  }

  @Test
  void removeAllThrows() {
    assertThrows(UnsupportedOperationException.class, () -> this.view.removeAll(List.of(1)));
  }

  @Test
  void retainAllThrows() {
    assertThrows(UnsupportedOperationException.class, () -> this.view.retainAll(List.of(1)));
  }

  @Test
  void clearThrows() {
    assertThrows(UnsupportedOperationException.class, () -> this.view.clear());
  }

  @Test
  void viewReflectsChanges() {
    this.ref.add(10);
    assertTrue(this.view.contains(10));
  }
}