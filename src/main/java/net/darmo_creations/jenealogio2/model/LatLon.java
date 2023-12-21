package net.darmo_creations.jenealogio2.model;

import org.jetbrains.annotations.*;

/**
 * This class represents a point on the Earth’s surface.
 *
 * @param lat Point’s latitude.
 * @param lon Point’s longitude.
 */
public record LatLon(double lat, double lon) {
  /**
   * Parses a string into a {@link LatLon} object.
   *
   * @param s Strint to parse. Must be in the format "{@code <lat>.<lon>}".
   * @return The corresponding {@link LatLon} object.
   */
  public static LatLon fromString(@NotNull String s) {
    String[] split = s.split(",", 2);
    if (split.length != 2) {
      throw new IllegalArgumentException("Could not parse LatLon string: " + s);
    }
    return new LatLon(Double.parseDouble(split[0]), Double.parseDouble(split[1]));
  }

  @Override
  public String toString() {
    return "%f,%f".formatted(this.lat, this.lon);
  }
}
