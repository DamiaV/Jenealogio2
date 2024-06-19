package net.darmo_creations.jenealogio2.model;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class LifeEventTypeTest {
  private LifeEventType l1;
  private LifeEventType l2;

  @BeforeEach
  void setUp() {
    this.l1 = new LifeEventType(new RegistryEntryKey(Registry.BUILTIN_NS, "test"), null, LifeEventType.Group.OTHER, false, true, 2, 2, false);
    this.l2 = new LifeEventType(new RegistryEntryKey(Registry.USER_NS, "test2"), "Test", LifeEventType.Group.ADMIN, true, false, 1, 1, true);
  }

  @Test
  void updateBuiltinThrowsError() {
    assertThrows(UnsupportedOperationException.class, () -> this.l1.setUserDefinedName("test"));
    assertThrows(UnsupportedOperationException.class, () -> this.l1.setGroup(LifeEventType.Group.ADMIN));
    assertThrows(UnsupportedOperationException.class, () -> this.l1.setIndicatesDeath(true));
    assertThrows(UnsupportedOperationException.class, () -> this.l1.setActorsNumber(1, 1, false));
    assertThrows(UnsupportedOperationException.class, () -> this.l1.setUnique(true));
  }

  @Test
  void setActorsNumberInvalidMinThrowsError() {
    assertThrows(IllegalArgumentException.class, () -> this.l2.setActorsNumber(0, 1, false));
  }

  @Test
  void setActorsNumberInvalidMaxThrowsError() {
    assertThrows(IllegalArgumentException.class, () -> this.l2.setActorsNumber(1, 3, false));
  }

  @Test
  void setActorsNumberInvalidMinMaxThrowsError() {
    assertThrows(IllegalArgumentException.class, () -> this.l2.setActorsNumber(2, 1, false));
  }

  @Test
  void setActorsNumberIsUnionButMin1ThrowsError() {
    assertThrows(IllegalArgumentException.class, () -> this.l2.setActorsNumber(1, 2, true));
  }

  @Test
  void setActorsNumberIsUnionButMax1ThrowsError() {
    assertThrows(IllegalArgumentException.class, () -> this.l2.setActorsNumber(1, 1, true));
  }
}