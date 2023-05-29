package net.darmo_creations.jenealogio2.model;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record RegistryEntryKey(@NotNull String namespace, @NotNull String name) {
  public RegistryEntryKey {
    Objects.requireNonNull(namespace);
    Objects.requireNonNull(name);
  }

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
