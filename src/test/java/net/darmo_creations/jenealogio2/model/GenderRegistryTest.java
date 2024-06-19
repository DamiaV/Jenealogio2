package net.darmo_creations.jenealogio2.model;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class GenderRegistryTest {
  private GenderRegistry genderRegistry;

  @BeforeEach
  void setUp() {
    this.genderRegistry = new GenderRegistry();
  }

  @Test
  void serializableEntriesReturnsNoUnchangedBuiltins() {
    assertTrue(this.genderRegistry.serializableEntries().isEmpty());
  }

  @Test
  void serializableEntriesReturnsChangedBuiltins() {
    var key = new RegistryEntryKey(Registry.BUILTIN_NS, "female");
    this.genderRegistry.getEntry(key).setColor("#123456");
    var genders = this.genderRegistry.serializableEntries();
    assertEquals(1, genders.size());
    assertEquals(key, genders.get(0).key());
  }

  @Test
  void serializableEntriesReturnsUserDefined() {
    var key = new RegistryEntryKey(Registry.USER_NS, "test");
    this.genderRegistry.registerEntry(key, "test", new GenderRegistry.RegistryArgs("#123456"));
    var genders = this.genderRegistry.serializableEntries();
    assertEquals(1, genders.size());
    assertEquals(key, genders.get(0).key());
  }
}
