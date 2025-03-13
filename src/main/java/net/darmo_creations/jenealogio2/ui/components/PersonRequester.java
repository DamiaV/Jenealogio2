package net.darmo_creations.jenealogio2.ui.components;

import net.darmo_creations.jenealogio2.model.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Objects implementing this interface may request {@link Person} objects from their listeners.
 */
public interface PersonRequester {
  /**
   * Set the {@link PersonRequestListener}. If one was already set prior, it is discarded.
   */
  void setPersonRequestListener(@NotNull PersonRequestListener listener);

  /**
   * Objects implementing this interface answer to a request for a person.
   */
  interface PersonRequestListener {
    /**
     * Called when a {@link PersonRequester} asks for a person.
     *
     * @param exclusionList List of persons to exclude from potential results.
     * @return A person.
     */
    Optional<Person> onPersonRequest(@NotNull List<Person> exclusionList);
  }
}
