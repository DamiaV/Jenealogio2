package net.darmo_creations.jenealogio2.ui.components;

import net.darmo_creations.jenealogio2.model.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Objects implementing this interface may request {@link LifeEvent} objects from their listeners.
 */
public interface LifeEventRequester {
  /**
   * Set the {@link LifeEventRequestListener}. If one was already set prior, it is discarded.
   */
  void setLifeEventRequestListener(@NotNull LifeEventRequestListener listener);

  /**
   * Objects implementing this interface answer to a request for an event.
   */
  interface LifeEventRequestListener {
    /**
     * Called when a {@link LifeEventRequester} asks for an event.
     *
     * @param exclusionList List of events to exclude from potential results.
     * @return An event.
     */
    Optional<LifeEvent> onLifeEventRequest(@NotNull List<LifeEvent> exclusionList);
  }
}
