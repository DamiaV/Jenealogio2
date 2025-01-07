package net.darmo_creations.jenealogio2.ui.components;

import javafx.collections.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.themes.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * JavaFX component that presents a form to edit the parents of a person.
 */
public class ParentsListView extends VBox implements PersonRequester {
  private final Button removeRelativeButton;
  private final ListView<Person> personsListView = new ListView<>();

  private final ParentalRelationType relationType;
  private final Button addWitnessButton;

  private PersonRequestListener personRequestListener;
  private final Set<UpdateListener> updateListeners = new HashSet<>();

  /**
   * Create a new component.
   *
   * @param config The app’s config.
   */
  public ParentsListView(final @NotNull Config config, @NotNull ParentalRelationType relationType) {
    super(5);
    this.relationType = relationType;
    final Language language = config.language();
    final Theme theme = config.theme();

    final HBox buttonsHBox = new HBox(5);

    this.addWitnessButton = new Button(language.translate("parents_list.add"),
        theme.getIcon(Icon.ADD_PARENT, Icon.Size.SMALL));
    this.addWitnessButton.setOnAction(event -> this.onAdd());
    this.removeRelativeButton = new Button(language.translate("parents_list.remove"),
        theme.getIcon(Icon.REMOVE_PARENT, Icon.Size.SMALL));
    this.removeRelativeButton.setOnAction(event -> this.onRemove());
    this.removeRelativeButton.setDisable(true);
    final Pane spacer = new Pane();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    buttonsHBox.getChildren().addAll(spacer, this.addWitnessButton, this.removeRelativeButton);

    VBox.setVgrow(this.personsListView, Priority.ALWAYS);
    this.personsListView.setPrefHeight(50);
    this.personsListView.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> this.onSelection());
    this.personsListView.getItems()
        .addListener((ListChangeListener<? super Person>) c -> this.onListChange());
    this.personsListView.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.DELETE)
        this.onRemove();
    });

    this.getChildren().addAll(buttonsHBox, this.personsListView);
  }

  private void onListChange() {
    final var maxCount = this.relationType.maxParentsCount();
    this.addWitnessButton.setDisable(
        maxCount.isPresent() && this.personsListView.getItems().size() >= maxCount.get());
    this.updateListeners.forEach(UpdateListener::onUpdate);
  }

  /**
   * Called when the selection in {@link #personsListView} changes.
   */
  private void onSelection() {
    this.removeRelativeButton.setDisable(
        this.personsListView.getSelectionModel().getSelectedItems().isEmpty());
  }

  /**
   * Called when the “Add” button is clicked.
   * Opens an dialog to choose a person.
   */
  private void onAdd() {
    this.personRequestListener.onPersonRequest(new LinkedList<>(this.personsListView.getItems()))
        .ifPresent(person -> {
          if (!this.personsListView.getItems().contains(person))
            this.personsListView.getItems().add(person);
        });
  }

  /**
   * Called when the “Remove” action (button or keyboard key) is fired.
   */
  private void onRemove() {
    this.personsListView.getItems()
        .removeAll(new LinkedList<>(this.personsListView.getSelectionModel().getSelectedItems()));
  }

  /**
   * The persons contained in this list.
   */
  public List<Person> getPersons() {
    return new LinkedList<>(this.personsListView.getItems());
  }

  /**
   * Set the list of relatives to populate the list view.
   *
   * @param persons List of relatives.
   * @throws IllegalArgumentException If more persons are passed than allowed by this view’s {@link ParentalRelationType}.
   */
  public void setPersons(final @NotNull Set<Person> persons) {
    final var maxCount = this.relationType.maxParentsCount();
    if (maxCount.isPresent() && persons.size() > maxCount.get())
      throw new IllegalArgumentException("Too many persons, expected %d, got %d"
          .formatted(maxCount.get(), persons.size()));
    this.personsListView.getItems().clear();
    this.personsListView.getItems().addAll(persons.stream()
        .sorted(Person.birthDateThenNameComparator(false))
        .toList());
  }

  @Override
  public void setPersonRequestListener(@NotNull PersonRequestListener listener) {
    this.personRequestListener = listener;
  }

  public void addUpdateListener(@NotNull UpdateListener listener) {
    this.updateListeners.add(listener);
  }

  public interface UpdateListener {
    void onUpdate();
  }
}
