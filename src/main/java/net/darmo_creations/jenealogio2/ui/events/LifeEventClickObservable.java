package net.darmo_creations.jenealogio2.ui.events;

import net.darmo_creations.jenealogio2.model.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Marks a node as containing nodes wrapping {@link LifeEvent} objects that can be clicked.
 */
public interface LifeEventClickObservable {
  /**
   * Return the list of all listeners to event click events.
   */
  List<LifeEventClickListener> lifeEventClickListeners();

  /**
   * Fire a click event for the given event.
   *
   * @param event The event to fire.
   */
  default void fireLifeEventClickEvent(@NotNull LifeEventClickEvent event) {
    Objects.requireNonNull(event);
    this.lifeEventClickListeners().forEach(listener -> listener.onClick(event));
  }
}
