package net.darmo_creations.jenealogio2.model;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A registry entry has an internal unique ID, a name and a boolean indicating
 * whether it is built-in or user-defined.
 */
public abstract class RegistryEntry {
  private final int id;
  private final String name;
  private final boolean builtin;

  protected RegistryEntry(int id, @NotNull String name, boolean builtin) {
    this.id = id;
    this.name = name.strip();
    this.builtin = builtin;
  }

  /**
   * Internal ID.
   */
  public int id() {
    return this.id;
  }

  /**
   * The name of this entry. If builtin is true, it will be used as the translation key in the GUI,
   * otherwise it will be used as is.
   */
  public String name() {
    return this.name;
  }

  /**
   * Indicates whether this entry is built-in or defined by a user.
   */
  public boolean isBuiltin() {
    return this.builtin;
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
    return this.id == that.id && this.builtin == that.builtin && Objects.equals(this.name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.id, this.name, this.builtin);
  }

  @Override
  public String toString() {
    return "RegistryEntry{id=%d, name='%s', builtin=%s}".formatted(this.id, this.name, this.builtin);
  }
}
