package net.darmo_creations.jenealogio2.ui.components;

import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * JavaFX component that presents a form to edit the parents of a person.
 */
public class ParentsListView extends PersonListView {
  private final ParentalRelationType relationType;

  /**
   * Create a new component.
   *
   * @param config The app’s config.
   */
  public ParentsListView(final @NotNull Config config, @NotNull ParentalRelationType relationType) {
    super(config, true);
    this.relationType = relationType;
  }

  @Override
  protected void onListChange() {
    final int maxCount = this.relationType.maxParentsCount().orElse(Integer.MAX_VALUE);
    this.addButton.setDisable(this.personsListView.getItems().size() >= maxCount);
    super.onListChange();
  }

  /**
   * Set the list of relatives to populate the list view.
   *
   * @param persons List of relatives.
   * @throws IllegalArgumentException If more persons are passed than allowed by this view’s {@link ParentalRelationType}.
   */
  public void setPersons(final @NotNull Collection<Person> persons) {
    final var maxCount = this.relationType.maxParentsCount();
    if (maxCount.isPresent() && persons.size() > maxCount.get())
      throw new IllegalArgumentException("Too many persons, expected %d, got %d"
          .formatted(maxCount.get(), persons.size()));
    super.setPersons(persons);
  }
}
