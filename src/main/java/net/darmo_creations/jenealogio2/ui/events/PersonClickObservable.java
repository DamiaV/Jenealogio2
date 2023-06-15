package net.darmo_creations.jenealogio2.ui.events;

import net.darmo_creations.jenealogio2.model.Person;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Marks a node as containing nodes wrapping {@link Person} objects that can be clicked.
 */
public interface PersonClickObservable {
  /**
   * Return the list of all listeners to person click events.
   */
  List<PersonClickListener> personClickListeners();

  /**
   * Fire a click event for the given person.
   *
   * @param event The event to fire.
   */
  default void firePersonClickEvent(@NotNull PersonClickEvent event) {
    Objects.requireNonNull(event);
    for (PersonClickListener personClickListener : this.personClickListeners()) {
      personClickListener.onClick(event);
    }
  }
}
