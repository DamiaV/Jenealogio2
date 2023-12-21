package net.darmo_creations.jenealogio2.ui.components;

import net.darmo_creations.jenealogio2.model.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Simple class that associates a {@link LatLon} to an address.
 *
 * @param address Place’s address.
 * @param latLon  Place’s coordinates. May be null.
 */
public record Place(@NotNull String address, LatLon latLon) {
  public Place {
    Objects.requireNonNull(address);
  }
}
