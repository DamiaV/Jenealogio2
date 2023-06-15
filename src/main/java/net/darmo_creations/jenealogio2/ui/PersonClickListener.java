package net.darmo_creations.jenealogio2.ui;

import net.darmo_creations.jenealogio2.model.Person;
import org.jetbrains.annotations.NotNull;

/**
 * Interface representing a listener to clicks on nodes wrapping a {@link Person} object.
 */
public interface PersonClickListener {
  /**
   * Called when a node wrapping a {@link Person} object is clicked.
   *
   * @param event The event that was fired.
   */
  void onClick(@NotNull PersonClickEvent event);
}