package net.darmo_creations.jenealogio2.ui.components;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.themes.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class AnnotationsListView extends VBox
    implements PersonRequester, LifeEventRequester {
  private final Button removeButton = new Button();
  private final ListView<ObjectAnnotation> annotationsListView = new ListView<>();
  private final ErasableTextField noteField;
  private boolean internalNoteUpdate;

  private PersonRequestListener personRequestListener;
  private LifeEventRequestListener eventRequestListener;

  public AnnotationsListView(final @NotNull Config config) {
    super(5);
    final Language language = config.language();

    final HBox buttonsHBox = new HBox(5);

    final Button addPersonButton = new Button(
        language.translate("annotations_list.add_person"),
        config.theme().getIcon(Icon.ADD_PERSON_ANNOTATION, Icon.Size.SMALL)
    );
    addPersonButton.setOnAction(e -> this.onAddPerson());

    final Button addEventButton = new Button(
        language.translate("annotations_list.add_event"),
        config.theme().getIcon(Icon.ADD_EVENT_ANNOTATION, Icon.Size.SMALL)
    );
    addEventButton.setOnAction(e -> this.onAddEvent());

    this.removeButton.setText(language.translate("annotations_list.remove"));
    this.removeButton.setGraphic(config.theme().getIcon(Icon.REMOVE_ANNOTATION, Icon.Size.SMALL));
    this.removeButton.setOnAction(e -> this.onRemove());

    buttonsHBox.getChildren().addAll(
        new Spacer(Orientation.HORIZONTAL),
        addPersonButton,
        addEventButton,
        this.removeButton
    );

    VBox.setVgrow(this.annotationsListView, Priority.ALWAYS);
    this.annotationsListView.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue)
            -> this.onSelection());
    this.annotationsListView.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.DELETE)
        this.onRemove();
    });
    this.annotationsListView.setCellFactory(param -> new ObjectAnnotationListCell(config));

    this.noteField = new ErasableTextField(config);
    this.noteField.textField()
        .setPromptText(language.translate("annotations_list.note_field.prompt_text"));
    this.noteField.textField().textProperty()
        .addListener((observable, oldValue, newValue) -> {
          if (this.internalNoteUpdate) return;
          final var selectedItem = this.annotationsListView.getSelectionModel().getSelectedItem();
          if (selectedItem != null) {
            selectedItem.setNote(newValue);
            this.annotationsListView.refresh();
          }
        });

    this.getChildren().addAll(buttonsHBox, this.annotationsListView, this.noteField);

    this.onSelection();
  }

  /**
   * The annotations contained in this list.
   */
  public List<ObjectAnnotation> getAnnotations() {
    return new LinkedList<>(this.annotationsListView.getItems());
  }

  /**
   * Set the list of annotations to populate the list view.
   *
   * @param annotations List of relatives.
   */
  public void setAnnotations(final @NotNull Collection<ObjectAnnotation> annotations) {
    final var items = this.annotationsListView.getItems();
    items.clear();
    items.addAll(annotations);
    items.sort(null);
  }

  /**
   * Called when the selection in {@link #annotationsListView} changes.
   */
  private void onSelection() {
    final ObjectAnnotation selectedItem = this.annotationsListView.getSelectionModel().getSelectedItem();
    final boolean noSelection = selectedItem == null;
    this.removeButton.setDisable(noSelection);
    this.internalNoteUpdate = true;
    this.noteField.textField().setText(noSelection ? "" : selectedItem.note().orElse(""));
    this.noteField.textField().setDisable(noSelection);
    this.internalNoteUpdate = false;
  }

  /**
   * Called when the “Add Person” button is clicked.
   * Opens an dialog to choose a person.
   */
  private void onAddPerson() {
    if (this.personRequestListener == null) return;
    final var items = this.annotationsListView.getItems();
    final var exclusionList = items.stream()
        .filter(item -> item.object() instanceof Person)
        .map(item -> (Person) item.object()).toList();
    this.personRequestListener.onPersonRequest(exclusionList)
        .map(person -> new ObjectAnnotation(person, null))
        .ifPresent(annotation -> {
          if (!items.contains(annotation)) {
            items.add(annotation);
            items.sort(null);
          }
        });
  }

  /**
   * Called when the “Add Event” button is clicked.
   * Opens an dialog to choose an event.
   */
  private void onAddEvent() {
    if (this.eventRequestListener == null) return;
    final var items = this.annotationsListView.getItems();
    final var exclusionList = items.stream()
        .filter(item -> item.object() instanceof LifeEvent)
        .map(item -> (LifeEvent) item.object())
        .toList();
    this.eventRequestListener.onLifeEventRequest(exclusionList)
        .map(event -> new ObjectAnnotation(event, null))
        .ifPresent(annotation -> {
          if (!items.contains(annotation)) {
            items.add(annotation);
            items.sort(null);
          }
        });
  }

  /**
   * Called when the “Remove” action (button or keyboard key) is fired.
   */
  private void onRemove() {
    this.annotationsListView.getItems()
        .removeAll(new LinkedList<>(this.annotationsListView.getSelectionModel().getSelectedItems()));
  }

  @Override
  public void setPersonRequestListener(@NotNull PersonRequestListener listener) {
    this.personRequestListener = Objects.requireNonNull(listener);
  }

  @Override
  public void setLifeEventRequestListener(@NotNull LifeEventRequestListener listener) {
    this.eventRequestListener = Objects.requireNonNull(listener);
  }
}
