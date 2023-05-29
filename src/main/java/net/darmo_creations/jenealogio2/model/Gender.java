package net.darmo_creations.jenealogio2.model;

import org.jetbrains.annotations.NotNull;

/**
 * This class represents the gender of a {@link Person}.
 */
public final class Gender extends RegistryEntry {
  Gender(@NotNull RegistryEntryKey key) {
    super(key);
  }

  @Override
  public String toString() {
    return "Gender{key='%s'}".formatted(this.key());
  }
}
