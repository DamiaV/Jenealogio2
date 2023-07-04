package net.darmo_creations.jenealogio2.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * This class represents the gender of a {@link Person}.
 */
public final class Gender extends RegistryEntry {
  public static final String MISSING_COLOR = "#808080";

  private final String defaultColor;
  private String color;

  /**
   * Create a new gender object.
   *
   * @param key             Gender’s registry key.
   * @param userDefinedName Entry’s display text if not builtin.
   * @param color           Color to use in the GUI. Must be hex CSS color code.
   */
  Gender(@NotNull RegistryEntryKey key, String userDefinedName, @NotNull String color) {
    super(key, userDefinedName);
    this.defaultColor = key.isBuiltin() ? color : null;
    this.setColor(color);
  }

  /**
   * The default color, as set when registered. Null for non-builtin entries.
   */
  public @Nullable String defaultColor() {
    return this.defaultColor;
  }

  /**
   * The color to use in the GUI.
   */
  public String color() {
    return this.color;
  }

  /**
   * Set the color to use in the GUI.
   *
   * @param color A color.
   */
  public void setColor(@NotNull String color) {
    this.color = Objects.requireNonNull(color);
  }

  @Override
  public RegistryEntry withKey(@NotNull RegistryEntryKey key) {
    return new Gender(key, this.userDefinedName(), this.color);
  }

  @Override
  public String toString() {
    return "Gender{key=%s, color='%s'}".formatted(this.key(), this.color);
  }
}
