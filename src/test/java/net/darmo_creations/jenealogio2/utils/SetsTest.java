package net.darmo_creations.jenealogio2.utils;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SetsTest {
  @Test
  void merge() {
    assertEquals(Set.of(1, 2, 3, 4), Sets.merge(Map.of("a", Set.of(1, 2), "b", Set.of(3, 4))));
  }

  @Test
  void difference() {
    final Set<Integer> a = Set.of(1, 2, 3, 4);
    final Set<Integer> b = Set.of(1, 3, 5);
    assertEquals(Set.of(2, 4), Sets.difference(a, b));
  }

  @Test
  void difference_noIntersection() {
    final Set<Integer> a = Set.of(1, 2, 3, 4);
    final Set<Integer> b = Set.of(5, 6, 7, 8);
    assertEquals(Set.of(1, 2, 3, 4), Sets.difference(a, b));
  }

  @Test
  void difference_resultNotSame() {
    final Set<Integer> a = Set.of(1, 2, 3, 4);
    final Set<Integer> b = Set.of(5, 6, 7, 8);
    assertNotSame(a, Sets.difference(a, b));
    assertNotSame(b, Sets.difference(a, b));
  }
}