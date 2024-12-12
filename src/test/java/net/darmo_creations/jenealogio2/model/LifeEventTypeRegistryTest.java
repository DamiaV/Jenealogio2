package net.darmo_creations.jenealogio2.model;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class LifeEventTypeRegistryTest {
  private LifeEventTypeRegistry registry;

  @BeforeEach
  void setUp() {
    this.registry = new LifeEventTypeRegistry();
  }

  @Test
  void serializableEntriesReturnsNoUnchangedBuiltins() {
    assertTrue(this.registry.serializableEntries().isEmpty());
  }

  @Test
  void serializableEntriesReturnsUserDefined() {
    final var key = new RegistryEntryKey(Registry.USER_NS, "test");
    this.registry.registerEntry(key, "test", new LifeEventTypeRegistry.RegistryArgs(LifeEventType.Group.OTHER, false, false));
    final var genders = this.registry.serializableEntries();
    assertEquals(1, genders.size());
    assertEquals(key, genders.get(0).key());
  }
}