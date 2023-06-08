package net.darmo_creations.jenealogio2.model;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * This class represents the key of a {@link RegistryEntry} in a {@link Registry}.
 *
 * @param namespace Key’s namespace.
 * @param name      Key’s name.
 */
public record RegistryEntryKey(@NotNull String namespace, @NotNull String name) {
  public RegistryEntryKey {
    Objects.requireNonNull(namespace);
    Objects.requireNonNull(name);
  }

  /**
   * Create a key from a string of the form {@code namespace:name}.
   *
   * @param key String from which to extract the namespace and name.
   * @throws IllegalArgumentException If no namespace could be found.
   */
  public RegistryEntryKey(@NotNull String key) {
    this(splitKey(key));
  }

  // Convenience constructor to avoid calling splitKey() method twice in above constructor
  private RegistryEntryKey(@NotNull String[] key) {
    this(key[0], key[1]);
  }

  /**
   * Extract namespace and name from a string.
   *
   * @param key String to split.
   * @return An array of 2 elements containing the namespace and name (in that order).
   * @throws IllegalArgumentException If no namespace could be found.
   */
  private static String[] splitKey(@NotNull String key) {
    if (!key.contains(":")) {
      throw new IllegalArgumentException("Missing namespace");
    }
    return key.split(":", 2);
  }

  /**
   * This key’s full name in the form {@code namespace:name}.
   */
  public String fullName() {
    return this.namespace + ":" + this.name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }
    RegistryEntryKey that = (RegistryEntryKey) o;
    return Objects.equals(this.namespace, that.namespace) && Objects.equals(this.name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.namespace, this.name);
  }

  @Override
  public String toString() {
    return "RegistryEntryKey{%s}".formatted(this.fullName());
  }
}
