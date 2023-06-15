package net.darmo_creations.jenealogio2.ui.events;

import net.darmo_creations.jenealogio2.model.Person;

/**
 * Interface for events fired whenever a node wrapping a {@link Person} object is clicked.
 */
public sealed interface PersonClickEvent
    permits PersonClickedEvent, DeselectPersonsEvent {
}
