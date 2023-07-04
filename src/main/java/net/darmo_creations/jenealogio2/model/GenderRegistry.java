package net.darmo_creations.jenealogio2.model;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Registry to manager {@link Gender}s.
 */
public final class GenderRegistry extends Registry<Gender, GenderRegistry.RegistryArgs> {
  /**
   * Create a new {@link Gender} registry.
   */
  GenderRegistry() {
    super(
        "genders",
        (key, label, args) -> new Gender(key, label, args.color()),

        new BuiltinEntry<>("agender", new RegistryArgs("#000000")),
        new BuiltinEntry<>("female", new RegistryArgs("#ee8434")),
        new BuiltinEntry<>("gender_fluid", new RegistryArgs("#bf17d5")),
        new BuiltinEntry<>("male", new RegistryArgs("#00b69e")),
        new BuiltinEntry<>("non_binary", new RegistryArgs("#fff430"))
    );
  }

  @Override
  public List<Gender> serializableEntries() {
    return this.entries().stream()
        .filter(entry -> !entry.isBuiltin() || !entry.color().equals(entry.defaultColor()))
        .toList();
  }

  @Override
  public RegistryArgs getBuildArgs(@NotNull Gender entry) {
    return new RegistryArgs(entry.color());
  }

  /**
   * Wrapper class used to declare new {@link Gender} entries.
   *
   * @param color Color to use in the GUI. Must be hex CSS color code.
   */
  public record RegistryArgs(@NotNull String color) {
    public RegistryArgs {
      Objects.requireNonNull(color);
    }
  }
}
