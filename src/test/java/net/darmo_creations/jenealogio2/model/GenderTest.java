package net.darmo_creations.jenealogio2.model;

import javafx.scene.image.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("DataFlowIssue")
class GenderTest {
  private Gender g1, g2;

  @BeforeEach
  void setUp() {
    this.g1 = new Gender(new RegistryEntryKey("builtin", "test"), null, null);
    this.g2 = new Gender(new RegistryEntryKey("user", "test"), "Test", new Image("/test.png"));
  }

  @Test
  void testDefaultColorBuiltin() {
    assertEquals("#ff0000", this.g1.defaultColor());
  }

  @Test
  void testDefaultColorNotBuiltin() {
    assertNull(this.g2.defaultColor());
  }

  @Test
  void testColorBuiltin() {
    assertEquals("#ff0000", this.g1.color());
  }

  @Test
  void testColorNotBuiltin() {
    assertEquals("#00ff00", this.g2.color());
  }

  @Test
  void testSetColorNullThrowsError() {
    assertThrows(NullPointerException.class, () -> this.g1.setColor(null));
  }

  @Test
  void testSetColorDoesntChangeDefaultColorBuiltin() {
    this.g1.setColor("#0000ff");
    assertEquals("#ff0000", this.g1.defaultColor());
  }

  @Test
  void testSetColorDoesntChangeDefaultColorNotBuiltin() {
    this.g2.setColor("#0000ff");
    assertNull(this.g2.defaultColor());
  }

  @Test
  void testSetColorBuiltin() {
    this.g1.setColor("#0000ff");
    assertEquals("#0000ff", this.g1.color());
  }

  @Test
  void testSetColorNotBuiltin() {
    this.g2.setColor("#0000ff");
    assertEquals("#0000ff", this.g2.color());
  }
}