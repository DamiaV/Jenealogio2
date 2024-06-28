package net.darmo_creations.jenealogio2.ui.components;

import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.themes.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * JavaFX component that presents a form to edit the relatives of a person.
 */
public class RelativesListView extends VBox implements PersonRequester {
  private final Button removeRelativeButton;
  private final ListView<Person> personsListView = new ListView<>();

  private PersonRequestListener listener;

  /**
   * Create a new component.
   *
   * @param config The app’s config.
   */
  public RelativesListView(final @NotNull Config config) {
    super(5);
    Language language = config.language();
    Theme theme = config.theme();

    HBox buttonsHBox = new HBox(5);

    Button addWitnessButton = new Button(language.translate("relatives_list.add"),
        theme.getIcon(Icon.ADD_PARENT, Icon.Size.SMALL));
    addWitnessButton.setOnAction(event -> this.onAdd());
    this.removeRelativeButton = new Button(language.translate("relatives_list.remove"),
        theme.getIcon(Icon.REMOVE_PARENT, Icon.Size.SMALL));
    this.removeRelativeButton.setOnAction(event -> this.onRemove());
    this.removeRelativeButton.setDisable(true);
    Pane spacer = new Pane();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    buttonsHBox.getChildren().addAll(spacer, addWitnessButton, this.removeRelativeButton);

    VBox.setVgrow(this.personsListView, Priority.ALWAYS);
    this.personsListView.setPrefHeight(50);
    this.personsListView.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> this.onSelection());
    this.personsListView.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.DELETE) {
        this.onRemove();
      }
    });

    this.getChildren().addAll(buttonsHBox, this.personsListView);
  }

  /**
   * Called when the selection in {@link #personsListView} changes.
   */
  private void onSelection() {
    this.removeRelativeButton.setDisable(this.personsListView.getSelectionModel().getSelectedItems().isEmpty());
  }

  /**
   * Called when the add relative button is clicked.
   * Opens an dialog to choose a person.
   */
  private void onAdd() {
    this.listener.onPersonRequest(new LinkedList<>(this.personsListView.getItems()))
        .ifPresent(person -> this.personsListView.getItems().add(person));
  }

  /**
   * Called when the remove relative action (button or keyboard key) is fired.
   */
  private void onRemove() {
    this.personsListView.getItems()
        .removeAll(new LinkedList<>(this.personsListView.getSelectionModel().getSelectedItems()));
  }

  /**
   * The persons selected to be relatives of a person.
   */
  public List<Person> getPersons() {
    return new LinkedList<>(this.personsListView.getItems());
  }

  /**
   * Set the list of relatives to populate the list view.
   *
   * @param persons List of relatives.
   */
  public void setPersons(final @NotNull Collection<Person> persons) {
    this.personsListView.getItems().clear();
    this.personsListView.getItems().addAll(persons.stream().sorted().toList());
  }

  @Override
  public void setPersonRequestListener(@NotNull PersonRequestListener listener) {
    this.listener = listener;
  }
}
