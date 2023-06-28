package net.darmo_creations.jenealogio2.ui.events;

import net.darmo_creations.jenealogio2.model.Person;
import net.darmo_creations.jenealogio2.ui.ChildInfo;
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
   * The list of all listeners to new parent click.
   */
  List<NewParentClickListener> newParentClickListeners();

  /**
   * Fire a click event for the given person.
   *
   * @param event The event to fire.
   */
  default void firePersonClickEvent(@NotNull PersonClickEvent event) {
    Objects.requireNonNull(event);
    this.personClickListeners().forEach(listener -> listener.onClick(event));
  }

  /**
   * Fire a new parent click event.
   *
   * @param childInfo Information about the children of the parent to create.
   */
  default void fireNewParentClickEvent(@NotNull List<ChildInfo> childInfo) {
    this.newParentClickListeners().forEach(listener -> listener.onClick(childInfo));
  }
}
