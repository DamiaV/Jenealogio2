package net.darmo_creations.jenealogio2.model;

import javafx.scene.image.*;

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
        (key, label, args) -> new Gender(key, label, args.icon()),

        new BuiltinEntry<>("agender", new RegistryArgs()),
        new BuiltinEntry<>("female", new RegistryArgs()),
        new BuiltinEntry<>("gender_fluid", new RegistryArgs()),
        new BuiltinEntry<>("male", new RegistryArgs()),
        new BuiltinEntry<>("non_binary", new RegistryArgs())
    );
  }

  /**
   * Wrapper class used to declare new {@link Gender} entries.
   *
   * @param icon Icon to use in the GUI.
   */
  public record RegistryArgs(Image icon) {
    public RegistryArgs() {
      this(null);
    }
  }
}
