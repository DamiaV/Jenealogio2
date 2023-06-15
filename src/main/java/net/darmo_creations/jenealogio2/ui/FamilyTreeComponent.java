package net.darmo_creations.jenealogio2.ui;

import javafx.scene.layout.AnchorPane;
import net.darmo_creations.jenealogio2.model.FamilyTree;
import net.darmo_creations.jenealogio2.model.Person;
import net.darmo_creations.jenealogio2.ui.events.PersonClickListener;
import net.darmo_creations.jenealogio2.ui.events.PersonClickObservable;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

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
  protected abstract void refresh();

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
   */
  public abstract void select(@NotNull Person person, boolean updateTarget);

  @Override
  public List<PersonClickListener> personClickListeners() {
    return this.personClickListeners;
  }

  /**
   * The list of all listeners to new parent click.
   */
  public final List<NewParentClickListener> newParentClickListeners() {
    return this.newParentClickListeners;
  }

  /**
   * Fire a new parent click event.
   *
   * @param childInfo Information about the child of the parent to create.
   */
  protected final void fireNewParentClickEvent(@NotNull ChildInfo childInfo) {
    this.newParentClickListeners.forEach(listener -> listener.onClick(childInfo));
  }

  /**
   * A function that takes in some {@link ChildInfo} and is called when a parent should be created for a given person.
   */
  public interface NewParentClickListener {
    void onClick(@NotNull ChildInfo childInfo);
  }
}
