package net.darmo_creations.jenealogio2.ui.events;

import net.darmo_creations.jenealogio2.model.*;
import org.jetbrains.annotations.*;

/**
 * Interface representing a listener to clicks on nodes wrapping a {@link LifeEvent} object.
 */
public interface LifeEventClickListener {
  /**
   * Called when a node wrapping a {@link LifeEvent} object is clicked.
   *
   * @param event The event that was fired.
   */
  void onClick(@NotNull LifeEventClickEvent event);
}