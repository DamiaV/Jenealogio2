package net.darmo_creations.jenealogio2.model;

import org.jetbrains.annotations.NotNull;

/**
 * This class represents the gender of a {@link Person}.
 */
public final class Gender extends RegistryEntry {
  Gender(int id, @NotNull String name, boolean builtin) {
    super(id, name, builtin);
  }

  @Override
  public String toString() {
    return "Gender{id=%d, name='%s', builtin=%s}".formatted(this.id(), this.name(), this.isBuiltin());
  }
}
