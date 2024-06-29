package net.darmo_creations.jenealogio2.model;

import org.jetbrains.annotations.*;

import java.util.*;

/**
 * A registry entry has an internal unique key in a {@link Registry}.
 */
public abstract class RegistryEntry {
  private final RegistryEntryKey key;
  private String userDefinedName;

  protected RegistryEntry(@NotNull RegistryEntryKey key, String userDefinedName) {
    this.key = Objects.requireNonNull(key);
    if (!this.isBuiltin() && (userDefinedName == null || userDefinedName.isEmpty())) {
      throw new IllegalArgumentException("empty label for key '%s'".formatted(key));
    }
    this.userDefinedName = userDefinedName;
  }

  /**
   * The registry key of this entry. If this entry is in the default namespace,
   * its name will be used as the translation key in the GUI, otherwise it will be used as is.
   */
  public RegistryEntryKey key() {
    return this.key;
  }

  /**
   * Display name of this entry. Null for builtin entries.
   */
  public @Nullable String userDefinedName() {
    return this.userDefinedName;
  }

  /**
   * Set the display name of this entry.
   *
   * @param userDefinedName The new name.
   * @throws UnsupportedOperationException If this entry is built-in.
   */
  public void setUserDefinedName(@NotNull String userDefinedName) {
    this.ensureNotBuiltin("userDefinedName");
    this.userDefinedName = Objects.requireNonNull(userDefinedName);
  }

  /**
   * Whether this entry’s key is in the “builtin” namespace.
   *
   * @see Registry#BUILTIN_NS
   */
  public boolean isBuiltin() {
    return this.key.isBuiltin();
  }

  protected void ensureNotBuiltin(@NotNull String property) {
    if (this.isBuiltin())
      throw new UnsupportedOperationException("Cannot modify property %s of builtin regitry entry.".formatted(property));
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
    return "RegistryEntry{key=%s}".formatted(this.key);
  }
}
