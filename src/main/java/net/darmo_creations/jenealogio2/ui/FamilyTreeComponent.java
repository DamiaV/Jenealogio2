package net.darmo_creations.jenealogio2.ui;

import javafx.scene.layout.AnchorPane;
import net.darmo_creations.jenealogio2.model.FamilyTree;
import net.darmo_creations.jenealogio2.model.Person;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * A JavaFX component that shows data from a family tree.
 */
public abstract class FamilyTreeComponent extends AnchorPane {
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
   * Select the widget corresponding to the given person.
   * <p>
   * If null, current selection will be cleared.
   *
   * @param person A person object.
   */
  public final void selectPerson(Person person) {
    if (person == null) {
      this.deselectAll();
    } else {
      this.select(person);
    }
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
  protected abstract void deselectAll();

  /**
   * Select the component corresponding to the given person object.
   *
   * @param person A person object.
   */
  protected abstract void select(@NotNull Person person);

  /**
   * The list of all listeners to person components click.
   */
  public final List<PersonClickListener> personClickListeners() {
    return this.personClickListeners;
  }

  /**
   * Fire a person component click event.
   *
   * @param person     The person object wrapped by the component.
   * @param clickCount Number of clicks.
   */
  protected final void firePersonClickEvent(Person person, int clickCount) {
    this.personClickListeners.forEach(listener -> listener.onClick(person, clickCount));
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
   * Interface representing a listener to person component clicks.
   */
  public interface PersonClickListener {
    /**
     * Called when a person component is clicked.
     *
     * @param person     The person object wrapped by the clicked component.
     * @param clickCount Number of clicks.
     */
    void onClick(Person person, int clickCount);
  }

  public interface NewParentClickListener {
    void onClick(@NotNull ChildInfo childInfo);
  }
}
