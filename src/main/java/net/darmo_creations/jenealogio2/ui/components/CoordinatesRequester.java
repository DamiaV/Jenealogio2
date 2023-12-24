package net.darmo_creations.jenealogio2.ui.components;

import net.darmo_creations.jenealogio2.model.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Objects implementing this interface may request {@link LatLon} objects to their listeners.
 */
public interface CoordinatesRequester {
  /**
   * Set the {@link CoordinatesRequestListener}. If one was already set prior, it is discarded.
   */
  void setCoordinatesRequestListener(@NotNull CoordinatesRequestListener listener);

  /**
   * Objects implementing this interface answer to a request for a coordinate.
   */
  interface CoordinatesRequestListener {
    /**
     * Called when a {@link CoordinatesRequester} asks for a person.
     *
     * @return A coordinate.
     */
    Optional<LatLon> onCoordinatesRequest();
  }
}
