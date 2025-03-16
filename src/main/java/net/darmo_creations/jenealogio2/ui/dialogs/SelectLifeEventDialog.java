package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.collections.*;
import javafx.collections.transformation.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.ui.components.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * A dialog used to select a life event from the given {@link FamilyTree} object.
 */
public class SelectLifeEventDialog extends DialogBase<LifeEvent> {
  private final ErasableTextField personFilterTextInput;
  private final ListView<SelectPersonDialog.PersonView> personListView = new ListView<>();
  private final ObservableList<SelectPersonDialog.PersonView> personList = FXCollections.observableArrayList();

  private final ErasableTextField eventFilterTextInput;
  private final ListView<LifeEventView> eventListView = new ListView<>();
  private final ObservableList<LifeEventView> eventList = FXCollections.observableArrayList();

  private Collection<LifeEvent> exclusionList;

  public SelectLifeEventDialog(final @NotNull Config config) {
    super(
        config,
        "select_event",
        true,
        ButtonTypes.OK,
        ButtonTypes.CANCEL
    );

    this.personFilterTextInput = new ErasableTextField(config);
    this.eventFilterTextInput = new ErasableTextField(config);
    final Node personsListPanel = this.setupPersonsList();
    final Node eventsListPanel = this.setupLifeEventsList();

    HBox.setHgrow(personsListPanel, Priority.ALWAYS);
    HBox.setHgrow(eventsListPanel, Priority.ALWAYS);
    final SplitPane content = new SplitPane(personsListPanel, eventsListPanel);
    content.setOrientation(Orientation.HORIZONTAL);
    content.setDividerPositions(0.5);
    content.setPrefWidth(800);
    content.setPrefHeight(400);
    this.getDialogPane().setContent(content);

    this.getDialogPane().addEventHandler(KeyEvent.KEY_PRESSED, event -> {
      if (!this.personFilterTextInput.textField().isFocused())
        this.personFilterTextInput.textField().requestFocus();
    });

    final Stage stage = this.stage();
    stage.setMinWidth(400);
    stage.setMinHeight(400);

    this.setResultConverter(buttonType -> {
      if (!buttonType.getButtonData().isCancelButton())
        return this.eventListView
            .getSelectionModel()
            .getSelectedItems()
            .stream()
            .map(LifeEventView::lifeEvent)
            .findFirst()
            .orElse(null);
      return null;
    });
  }

  private Node setupPersonsList() {
    final Language language = this.config.language();

    HBox.setHgrow(this.personFilterTextInput, Priority.ALWAYS);
    this.personFilterTextInput.textField().setPromptText(language.translate("dialog.select_event.persons_filter"));
    final FilteredList<SelectPersonDialog.PersonView> filteredList = new FilteredList<>(this.personList, data -> true);
    this.personListView.setItems(filteredList);
    this.personFilterTextInput.textField().textProperty().addListener(
        (observable, oldValue, newValue) ->
            filteredList.setPredicate(pictureView -> {
              if (newValue == null || newValue.isEmpty())
                return true;
              return pictureView.person().matchesName(newValue, language);
            })
    );

    this.personListView.getSelectionModel().selectedItemProperty().addListener(
        (observable, oldValue, newValue) -> this.onPersonSelection(newValue));
    VBox.setVgrow(this.personListView, Priority.ALWAYS);

    return new VBox(
        5,
        new HBox(
            5,
            new Label(language.translate("dialog.select_event.persons_list")),
            this.personFilterTextInput
        ),
        this.personListView
    );
  }

  private Node setupLifeEventsList() {
    final Language language = this.config.language();

    HBox.setHgrow(this.eventFilterTextInput, Priority.ALWAYS);
    this.eventFilterTextInput.textField().setPromptText(language.translate("dialog.select_event.events_filter"));
    final FilteredList<LifeEventView> filteredList = new FilteredList<>(this.eventList, data -> true);
    this.eventListView.setItems(filteredList);
    this.eventFilterTextInput.textField().textProperty().addListener(
        (observable, oldValue, newValue) ->
            filteredList.setPredicate(pictureView -> {
              if (newValue == null || newValue.isEmpty())
                return true;
              return pictureView.getLabelText().toLowerCase(language.locale())
                  .contains(newValue.toLowerCase(language.locale()));
            })
    );

    this.eventListView.getSelectionModel().selectedItemProperty().addListener(
        (observable, oldValue, newValue) -> this.updateButtons());
    this.eventListView.setOnMouseClicked(this::onEventsListClicked);
    VBox.setVgrow(this.eventListView, Priority.ALWAYS);

    return new VBox(
        5,
        new HBox(
            5,
            new Label(language.translate("dialog.select_event.events_list")),
            this.eventFilterTextInput
        ),
        this.eventListView
    );
  }

  /**
   * Update the list of persons with the ones from the given tree, ignoring any that appear in the exclusion list.
   *
   * @param tree          Tree to pull persons from.
   * @param exclusionList List of life events to exclude from the list.
   */
  public void updatePersonList(final @NotNull FamilyTree tree, final @NotNull Collection<LifeEvent> exclusionList) {
    this.exclusionList = exclusionList;
    this.personFilterTextInput.textField().setText(null);
    this.eventFilterTextInput.textField().setText(null);
    this.personList.clear();
    this.eventList.clear();
    tree.persons().stream()
        .filter(p -> p.getLifeEventsAsActor().stream().anyMatch(e -> !exclusionList.contains(e)))
        .forEach(p -> this.personList.add(new SelectPersonDialog.PersonView(p, this.config)));
    this.personList.sort(
        (p1, p2) -> Person.lastThenFirstNamesComparator().compare(p1.person(), p2.person()));
  }

  private void onPersonSelection(final SelectPersonDialog.PersonView personView) {
    if (personView == null) return;
    this.eventList.clear();
    personView.person().getLifeEventsAsActor()
        .stream()
        .filter(e -> !this.exclusionList.contains(e))
        .forEach(e -> this.eventList.add(new LifeEventView(e, this.config)));
    this.eventList.sort(Comparator.comparing(LifeEventView::lifeEvent));
  }

  /**
   * Enable double-click on an event to select it.
   */
  private void onEventsListClicked(final @NotNull MouseEvent event) {
    if (event.getClickCount() > 1)
      ((Button) this.getDialogPane().lookupButton(ButtonTypes.OK)).fire();
  }

  private void updateButtons() {
    final boolean noSelection = this.eventListView.getSelectionModel().getSelectedItems().isEmpty();
    this.getDialogPane().lookupButton(ButtonTypes.OK).setDisable(noSelection);
  }

  /**
   * Simple widget that shows the type, date, and main picture of a given event.
   */
  private static class LifeEventView extends HBox {
    private static final int IMAGE_SIZE = 100;

    private final Label nameLabel;
    private final LifeEvent event;

    public LifeEventView(final @NotNull LifeEvent event, final @NotNull Config config) {
      super(5);
      this.event = event;
      final Language language = config.language();

      final ImageView imageView = new ImageView(
          event.mainPicture().flatMap(Picture::image).orElse(PersonWidget.DEFAULT_IMAGE));
      imageView.setPreserveRatio(true);
      imageView.setFitWidth(IMAGE_SIZE);
      imageView.setFitHeight(IMAGE_SIZE);
      String eventName;
      if (event.type().isBuiltin())
        eventName = language.translate("life_event_types." + event.type().key().name());
      else
        eventName = Objects.requireNonNull(event.type().userDefinedName());
      final List<Person> actors = event.actors()
          .stream()
          .sorted(Person.lastThenFirstNamesComparator())
          .toList();
      eventName = language.translate(
          "life_event_type_with_actors",
          new FormatArg("event", eventName),
          new FormatArg("actors", language.makeList(actors, false))
      );
      this.nameLabel = new Label(eventName);
      final DateLabel dateLabel = new DateLabel(event.date(), "?", config);
      this.getChildren().addAll(imageView, new VBox(5, this.nameLabel, dateLabel));
    }

    public String getLabelText() {
      return this.nameLabel.getText();
    }

    public LifeEvent lifeEvent() {
      return this.event;
    }
  }
}
