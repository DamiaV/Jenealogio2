package net.darmo_creations.jenealogio2.utils;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("DataFlowIssue")
class FormatArgTest {
  @Test
  void testNameNullThrowsError() {
    assertThrows(NullPointerException.class, () -> new FormatArg(null, ""));
  }
}