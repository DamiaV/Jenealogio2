package net.darmo_creations.jenealogio2.ui.events;

import net.darmo_creations.jenealogio2.model.*;

/**
 * Interface for events fired whenever a node wrapping a {@link LifeEvent} object is clicked.
 */
public sealed interface LifeEventClickEvent
    permits LifeEventClickedEvent {
}
