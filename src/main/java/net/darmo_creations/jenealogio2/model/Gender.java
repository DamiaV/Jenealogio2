package net.darmo_creations.jenealogio2.model;

import org.jetbrains.annotations.NotNull;

/**
 * This class represents the gender of a {@link Person}.
 */
public final class Gender extends RegistryEntry {
  public static final String MISSING_COLOR = "#808080";

  private final String color;

  Gender(@NotNull RegistryEntryKey key, @NotNull String color) {
    super(key);
    this.color = color;
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
