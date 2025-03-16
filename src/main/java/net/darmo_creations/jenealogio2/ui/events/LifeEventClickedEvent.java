package net.darmo_creations.jenealogio2.ui.events;

import net.darmo_creations.jenealogio2.model.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * This event indicates that a node wrapping the given {@link LifeEvent} object should be selected.
 *
 * @param lifeEvent The event to select.
 */
public record LifeEventClickedEvent(@NotNull LifeEvent lifeEvent)
    implements LifeEventClickEvent {
  public LifeEventClickedEvent {
    Objects.requireNonNull(lifeEvent);
  }
}
