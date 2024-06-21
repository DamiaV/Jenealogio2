package net.darmo_creations.jenealogio2.model;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class LatLonTest {
  @Test
  void fromStringInt() {
    assertEquals(new LatLon(1, 2), LatLon.fromString("1,2"));
  }

  @Test
  void fromStringFloat() {
    assertEquals(new LatLon(1.6, 2.4), LatLon.fromString("1.6,2.4"));
  }

  @Test
  void testToString() {
    assertEquals("4.0,5.6", new LatLon(4, 5.6).toString());
  }
}