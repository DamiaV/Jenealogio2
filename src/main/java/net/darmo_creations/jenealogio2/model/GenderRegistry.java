package net.darmo_creations.jenealogio2.model;

/**
 * Registry to manager {@link Gender}s.
 */
public final class GenderRegistry extends Registry<Gender, String> {
  /**
   * Create a new {@link Gender} registry.
   */
  GenderRegistry() {
    super(
        "genders",
        Gender::new,

        new BuiltinEntry<>("agender", "#000000"),
        new BuiltinEntry<>("female", "#ee8434"),
        new BuiltinEntry<>("gender_fluid", "#bf17d5"),
        new BuiltinEntry<>("male", "#00b69e"),
        new BuiltinEntry<>("non_binary", "#fff430")
    );
  }
}
