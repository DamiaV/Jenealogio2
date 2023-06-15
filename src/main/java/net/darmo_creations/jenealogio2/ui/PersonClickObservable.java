package net.darmo_creations.jenealogio2.ui;

import javafx.scene.input.MouseButton;
import net.darmo_creations.jenealogio2.model.Person;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Marks a node as containing nodes wrapping {@link Person} objects that can be clicked.
 */
public interface PersonClickObservable {
  /**
   * Return the list of all listeners to person click events.
   */
  List<PersonClickListener> getPersonClickListeners();

  /**
   * Fire a click event for the given person.
   *
   * @param person      Clicked person object.
   * @param clickCount  Number of clicks.
   * @param mouseButton Clicked mouse button.
   */
  default void firePersonClickEvent(@NotNull Person person, int clickCount, @NotNull MouseButton mouseButton) {
    for (PersonClickListener personClickListener : this.getPersonClickListeners()) {
      personClickListener.onClick(person, clickCount, mouseButton);
    }
  }
}
