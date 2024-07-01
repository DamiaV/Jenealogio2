package net.darmo_creations.jenealogio2.model;

import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Simple class that associates a {@link LatLon} to an address.
 */
public final class Place {
  private final String address;
  private final LatLon latLon;

  public Place(@NotNull String address, LatLon latLon) {
    this.address = Objects.requireNonNull(address);
    this.latLon = latLon;
  }

  public String address() {
    return this.address;
  }

  public Optional<LatLon> latLon() {
    return Optional.ofNullable(this.latLon);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this)
      return true;
    if (obj == null || obj.getClass() != this.getClass())
      return false;
    var that = (Place) obj;
    return Objects.equals(this.address, that.address)
           && Objects.equals(this.latLon, that.latLon);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.address, this.latLon);
  }

  @Override
  public String toString() {
    return "Place[address=%s, latLon=%s]".formatted(this.address, this.latLon);
  }
}
