package net.darmo_creations.jenealogio2.model;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A registry entry has an internal unique ID, a name and a boolean indicating
 * whether it is built-in or user-defined.
 */
public abstract class RegistryEntry {
  private final RegistryEntryKey key;

  protected RegistryEntry(@NotNull RegistryEntryKey key) {
    this.key = Objects.requireNonNull(key);
  }

  /**
   * The registry key of this entry. If this entry is in the default namespace,
   * its name will be used as the translation key in the GUI, otherwise it will be used as is.
   */
  public RegistryEntryKey key() {
    return this.key;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }
    RegistryEntry that = (RegistryEntry) o;
    return Objects.equals(this.key, that.key);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.key);
  }

  @Override
  public String toString() {
    return "RegistryEntry{%s}".formatted(this.key);
  }
}
