package net.darmo_creations.jenealogio2.model;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * This class represents the gender of a {@link Person}.
 */
public final class Gender extends RegistryEntry {
  public static final String MISSING_COLOR = "#808080";

  private final String color;

  /**
   * Create a new gender object.
   *
   * @param key   Gender’s registry key.
   * @param color Color to use in the GUI. Must be hex CSS color code.
   */
  Gender(@NotNull RegistryEntryKey key, @NotNull String color) {
    super(key);
    this.color = Objects.requireNonNull(color);
  }

  /**
   * The color to use in the GUI.
   */
  public String color() {
    return this.color;
  }

  @Override
  public String toString() {
    return "Gender{key='%s'}".formatted(this.key());
  }
}
