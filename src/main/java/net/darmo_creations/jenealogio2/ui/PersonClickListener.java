package net.darmo_creations.jenealogio2.ui;

import javafx.scene.input.MouseButton;
import net.darmo_creations.jenealogio2.model.Person;
import org.jetbrains.annotations.NotNull;

/**
 * Interface representing a listener to clicks on nodes wrapping a {@link Person} object.
 */
public interface PersonClickListener {
  /**
   * Called when a node wrapping a {@link Person} object is clicked.
   *
   * @param person      The person that was clicked.
   * @param clickCount  Number of mouse clicks.
   * @param mouseButton Mouse button that was clicked.
   */
  void onClick(@NotNull Person person, int clickCount, @NotNull MouseButton mouseButton);
}