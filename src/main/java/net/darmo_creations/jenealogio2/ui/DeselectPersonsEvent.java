package net.darmo_creations.jenealogio2.ui;

import net.darmo_creations.jenealogio2.model.Person;

/**
 * This event indicates that the all selected nodes wrapping a {@link Person} object should be deselected.
 */
public record DeselectPersonsEvent() implements PersonClickEvent {
}
