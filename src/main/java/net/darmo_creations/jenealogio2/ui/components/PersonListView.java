package net.darmo_creations.jenealogio2.ui.components;

import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.themes.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * This compontent is an editable list of {@link Person} objects.
 * The sort order can either be automatic or manual.
 */
public class PersonListView extends VBox implements PersonRequester {
  protected final Button moveUpButton;
  protected final Button moveDownButton;
  protected final Button addButton;
  protected final Button removeButton;
  protected final ListView<Person> personsListView = new ListView<>();

  private final boolean autoSort;

  private PersonRequestListener personRequestListener;
  private final Set<UpdateListener> updateListeners = new HashSet<>();

  /**
   * Create a new component.
   *
   * @param config   The app’s config.
   * @param autoSort If true, the list will be sorted automatically;
   *                 otherwise, two buttons will be shown to allow for manual sorting.
   */
  public PersonListView(final @NotNull Config config, boolean autoSort) {
    super(5);
    this.autoSort = autoSort;
    final Language language = config.language();
    final Theme theme = config.theme();

    final HBox buttonsHBox = new HBox(5);

    this.moveUpButton = new Button(
        language.translate("persons_list.move_up"),
        theme.getIcon(Icon.MOVE_UP, Icon.Size.SMALL)
    );
    this.moveUpButton.setOnAction(e -> this.onMoveUp());
    this.moveDownButton = new Button(
        language.translate("persons_list.move_down"),
        theme.getIcon(Icon.MOVE_DOWN, Icon.Size.SMALL)
    );
    this.moveDownButton.setOnAction(e -> this.onMoveDown());
    this.addButton = new Button(
        language.translate("persons_list.add"),
        theme.getIcon(Icon.ADD_PERSON, Icon.Size.SMALL)
    );
    this.addButton.setOnAction(event -> this.onAdd());
    this.removeButton = new Button(
        language.translate("persons_list.remove"),
        theme.getIcon(Icon.REMOVE_PERSON, Icon.Size.SMALL)
    );
    this.removeButton.setOnAction(event -> this.onRemove());
    final var children = buttonsHBox.getChildren();
    children.add(new Spacer(Orientation.HORIZONTAL));
    if (!autoSort) children.addAll(this.moveUpButton, this.moveDownButton);
    children.addAll(this.addButton, this.removeButton);

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

    this.onSelection();
  }

  /**
   * The persons contained in this list.
   */
  @Contract("-> new")
  public List<Person> getPersons() {
    return new LinkedList<>(this.personsListView.getItems());
  }

  /**
   * Set the list of persons to put into the list view.
   *
   * @param persons A list of persons.
   */
  public void setPersons(final @NotNull Collection<Person> persons) {
    final var items = this.personsListView.getItems();
    items.clear();
    items.addAll(persons);
    if (this.autoSort) items.sort(Person.lastThenFirstNamesComparator());
  }

  @Override
  public void setPersonRequestListener(@NotNull PersonRequestListener listener) {
    this.personRequestListener = listener;
  }

  public void addUpdateListener(@NotNull UpdateListener listener) {
    this.updateListeners.add(listener);
  }

  protected void onListChange() {
    this.updateListeners.forEach(UpdateListener::onUpdate);
  }

  /**
   * Called when the selection in {@link #personsListView} changes.
   */
  protected void onSelection() {
    final Person selectedItem = this.personsListView.getSelectionModel().getSelectedItem();
    final int i = this.personsListView.getSelectionModel().getSelectedIndex();
    final boolean noSelection = selectedItem == null;
    this.moveUpButton.setDisable(noSelection || i == 0);
    this.moveDownButton.setDisable(noSelection || i == this.personsListView.getItems().size() - 1);
    this.removeButton.setDisable(noSelection);
  }

  protected void onMoveUp() {
    if (this.autoSort) return;
    final Person selectedItem = this.personsListView.getSelectionModel().getSelectedItem();
    final int i = this.personsListView.getSelectionModel().getSelectedIndex();
    if (selectedItem == null || i == 0) return;
    final var items = this.personsListView.getItems();
    final Person prev = items.get(i - 1);
    items.set(i, prev);
    items.set(i - 1, selectedItem);
    this.personsListView.getSelectionModel().select(selectedItem);
  }

  protected void onMoveDown() {
    if (this.autoSort) return;
    final Person selectedItem = this.personsListView.getSelectionModel().getSelectedItem();
    final int i = this.personsListView.getSelectionModel().getSelectedIndex();
    final var items = this.personsListView.getItems();
    if (selectedItem == null || i == items.size() - 1) return;
    final Person next = items.get(i + 1);
    items.set(i, next);
    items.set(i + 1, selectedItem);
    this.personsListView.getSelectionModel().select(selectedItem);
  }

  /**
   * Called when the “Add” button is clicked.
   * Opens an dialog to choose a person.
   */
  protected void onAdd() {
    if (this.personRequestListener == null) return;
    final var items = this.personsListView.getItems();
    this.personRequestListener.onPersonRequest(new LinkedList<>(items))
        .ifPresent(person -> {
          if (!items.contains(person)) {
            items.add(person);
            if (this.autoSort) items.sort(Person.lastThenFirstNamesComparator());
          }
        });
  }

  /**
   * Called when the “Remove” action (button or keyboard key) is fired.
   */
  protected void onRemove() {
    this.personsListView.getItems()
        .removeAll(new LinkedList<>(this.personsListView.getSelectionModel().getSelectedItems()));
  }

  public interface UpdateListener {
    void onUpdate();
  }
}
