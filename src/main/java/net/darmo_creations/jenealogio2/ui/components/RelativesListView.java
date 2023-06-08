package net.darmo_creations.jenealogio2.ui.components;

import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import net.darmo_creations.jenealogio2.App;
import net.darmo_creations.jenealogio2.config.Language;
import net.darmo_creations.jenealogio2.model.Person;
import net.darmo_creations.jenealogio2.themes.Icon;
import net.darmo_creations.jenealogio2.themes.Theme;
import net.darmo_creations.jenealogio2.ui.dialogs.Alerts;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * JavaFX component that presents a form to edit the relatives of a person.
 */
public class RelativesListView extends VBox {
  private final Button removeRelativeButton;
  private final ListView<Person> personsListView = new ListView<>();

  private Collection<Person> persons;

  /**
   * Create a new component.
   */
  public RelativesListView() {
    super(4);
    Language language = App.config().language();
    Theme theme = App.config().theme();

    HBox buttonsHBox = new HBox(4);

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
   * Opens an alert dialog to choose a person.
   */
  private void onAdd() {
    List<Person> potentialRelatives = this.persons.stream()
        .filter(p -> !this.personsListView.getItems().contains(p))
        .toList();
    // FIXME empty choices -> disable button
    Optional<Person> result = Alerts.chooser(
        "alert.choose_relative.header", "alert.choose_relative.title", potentialRelatives);
    result.ifPresent(person -> this.personsListView.getItems().add(person));
  }

  /**
   * Called when the remove relative action (button or keyboard key) is fired.
   */
  private void onRemove() {
    this.personsListView.getItems()
        .removeAll(new LinkedList<>(this.personsListView.getSelectionModel().getSelectedItems()));
  }

  /**
   * Set the list of relatives to choose from.
   *
   * @param persons List of potential relatives.
   */
  public void setPotentialRelatives(final @NotNull Collection<Person> persons) {
    this.persons = persons.stream()
        .sorted(Person.lastThenFirstNamesComparator())
        .toList();
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
}
