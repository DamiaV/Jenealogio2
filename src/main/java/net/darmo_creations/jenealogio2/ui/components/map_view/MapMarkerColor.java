package net.darmo_creations.jenealogio2.ui.components.map_view;

import javafx.scene.image.*;
import net.darmo_creations.jenealogio2.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.util.*;

/**
 * Enumeration of all available marker colors for {@link MapView}.
 */
public enum MapMarkerColor {
  GREEN("green"),
  YELLOW_GREEN("yellow_green"),
  YELLOW("yellow"),
  ORANGE("orange"),
  RED("red"),
  BLUE("blue"),
  ;

  private final Image image;

  MapMarkerColor(@NotNull String color) {
    this.image = getMarkerIcon(color);
  }

  /**
   * The image associated to this marker icon.
   * <p>
   * May be null if the image could not be found.
   */
  public @Nullable Image image() {
    return this.image;
  }

  /**
   * Get the marker icon for the given icon as an {@link Image}.
   */
  private static @Nullable Image getMarkerIcon(@NotNull String color) {
    Objects.requireNonNull(color);
    final String iconName = "map_pin_" + color;
    final String path = "%s%s.png".formatted(App.IMAGES_PATH + "map/", iconName);
    try (final var stream = MapMarkerColor.class.getResourceAsStream(path)) {
      if (stream == null) {
        App.LOGGER.warn("Missing icon: " + iconName);
        return null;
      }
      return new Image(stream);
    } catch (final IOException e) {
      return null;
    }
  }
}