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
    this.image = getMarkerIcon(Objects.requireNonNull(color));
  }

  /**
   * The image associated to this marker color.
   * <p>
   * May be null if the image could not be found.
   */
  public @Nullable Image image() {
    return this.image;
  }

  /**
   * Get the marker icon for the given color as an {@link Image}.
   */
  private static @Nullable Image getMarkerIcon(@NotNull String color) {
    Objects.requireNonNull(color);
    String iconName = "map_pin_" + color;
    String path = "%s%s.png".formatted(App.IMAGES_PATH + "map/", iconName);
    try (var stream = MapMarkerColor.class.getResourceAsStream(path)) {
      if (stream == null) {
        App.LOGGER.warn("Missing icon: " + iconName);
        return null;
      }
      return new Image(stream);
    } catch (IOException e) {
      return null;
    }
  }
}