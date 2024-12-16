package net.darmo_creations.jenealogio2.ui;

import javafx.scene.layout.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.ui.events.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * A JavaFX component that shows data from a family tree.
 */
public abstract class FamilyTreeComponent extends AnchorPane implements PersonClickObservable {
  private final List<PersonClickListener> personClickListeners = new LinkedList<>();
  private final List<NewParentClickListener> newParentClickListeners = new LinkedList<>();

  private FamilyTree familyTree;

  /**
   * The family tree wrapped by this component.
   */
  protected Optional<FamilyTree> familyTree() {
    return Optional.ofNullable(this.familyTree);
  }

  /**
   * Set the family tree this component has to display data from.
   *
   * @param familyTree A family tree.
   */
  public void setFamilyTree(FamilyTree familyTree) {
    this.familyTree = familyTree;
    this.refresh();
  }

  /**
   * Refresh the displayed data.
   */
  public abstract void refresh();

  /**
   * Get the currently selected person.
   */
  public abstract Optional<Person> getSelectedPerson();

  /**
   * Clear all person component selection.
   */
  public abstract void deselectAll();

  /**
   * Select the widget corresponding to the given person.
   *
   * @param person       A person object.
   * @param updateTarget Whether to update the targetted person (center the view around it).
   *                     The component may choose to update the target even if this argument is false
   *                     when the person is not currently visible.
   */
  public abstract void select(@NotNull Person person, boolean updateTarget);

  @Override
  public final List<PersonClickListener> personClickListeners() {
    return this.personClickListeners;
  }

  /**
   * The list of all listeners to new parent click.
   */
  public final List<NewParentClickListener> newParentClickListeners() {
    return this.newParentClickListeners;
  }
}
