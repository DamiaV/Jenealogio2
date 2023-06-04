package net.darmo_creations.jenealogio2.ui.components;

import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import net.darmo_creations.jenealogio2.App;
import net.darmo_creations.jenealogio2.config.Language;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.themes.Icon;
import net.darmo_creations.jenealogio2.themes.Theme;
import net.darmo_creations.jenealogio2.ui.PseudoClasses;
import net.darmo_creations.jenealogio2.ui.dialogs.Alerts;
import net.darmo_creations.jenealogio2.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.text.Collator;
import java.util.*;

public class LifeEventView extends TitledPane {
  private final ComboBox<NotNullComboBoxItem<LifeEventType>> eventTypeCombo = new ComboBox<>();
  private final ComboBox<NotNullComboBoxItem<DateField.DateType>> datePrecisionCombo = new ComboBox<>();
  private final DateField dateField = new DateField();
  private final TextField placeField = new TextField();
  private final ComboBox<NotNullComboBoxItem<Person>> partnerCombo = new ComboBox<>();
  private final ListView<Person> witnessesList = new ListView<>();
  private final TextArea notesField = new TextArea();
  private final TextArea sourcesField = new TextArea();

  private final LifeEvent lifeEvent;
  private final Person person;
  private final List<Person> persons;

  private final List<DeletionListener> deletionListeners = new LinkedList<>();
  private final List<UpdateListener> updateListeners = new LinkedList<>();
  private final List<TypeListener> typeListeners = new LinkedList<>();
  private final Label titleLabel = new Label();

  public LifeEventView(
      @NotNull LifeEvent lifeEvent,
      @NotNull Person person,
      final @NotNull Collection<Person> persons,
      boolean expanded,
      final @NotNull ListView<LifeEventView> parent
  ) {
    this.lifeEvent = Objects.requireNonNull(lifeEvent);
    this.person = person;
    // Get all persons except the one we are currently editing and sort by name
    this.persons = persons.stream()
        .filter(p -> p != person)
        .sorted(Person.lastThenFirstNamesComparator())
        .toList();
    this.setAnimated(false);
    this.setExpanded(expanded);
    Language language = App.config().language();
    Theme theme = App.config().theme();

    BorderPane borderPane = new BorderPane();
    Button deleteButton = new Button("", theme.getIcon(Icon.DELETE_EVENT, Icon.Size.SMALL));
    deleteButton.setTooltip(new Tooltip(language.translate("life_event_view.delete")));
    deleteButton.setOnAction(this::onDelete);
    BorderPane.setAlignment(this.titleLabel, Pos.CENTER_LEFT);
    borderPane.setLeft(this.titleLabel);
    borderPane.setRight(deleteButton);
    borderPane.prefWidthProperty().bind(parent.widthProperty().subtract(70));
    this.setGraphic(borderPane);

    AnchorPane anchorPane = new AnchorPane();
    this.setContent(anchorPane);

    GridPane gridPane = new GridPane();
    gridPane.setHgap(4);
    gridPane.setVgap(4);
    AnchorPane.setTopAnchor(gridPane, 10.0);
    AnchorPane.setBottomAnchor(gridPane, 10.0);
    AnchorPane.setLeftAnchor(gridPane, 10.0);
    AnchorPane.setRightAnchor(gridPane, 10.0);
    anchorPane.getChildren().add(gridPane);

    this.populateEventTypeCombo(language);
    this.eventTypeCombo.getSelectionModel().selectedItemProperty().addListener(this::onEventTypeChange);
    gridPane.addRow(0, new Label(language.translate("life_event_view.type")), this.eventTypeCombo);

    this.populateDatePrecisionCombo(language);
    this.datePrecisionCombo.getSelectionModel().selectedItemProperty().addListener(
        (observable, oldValue, newValue) -> {
          this.dateField.setDateType(newValue.data());
          this.notifyUpdateListeners();
        });
    HBox dateHBox = new HBox(4);
    dateHBox.getChildren().add(this.datePrecisionCombo);
    this.dateField.getUpdateListeners().add(this::notifyUpdateListeners);
    dateHBox.getChildren().add(this.dateField);
    gridPane.addRow(1, new Label(language.translate("life_event_view.date")), dateHBox);

    gridPane.addRow(2, new Label(language.translate("life_event_view.place")), this.placeField);

    this.populatePartnerCombo();
    this.partnerCombo.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> this.notifyUpdateListeners());
    gridPane.addRow(3, new Label(language.translate("life_event_view.partner")), this.partnerCombo);

    VBox witnessesVBox = new VBox(4);
    HBox buttonsHBox = new HBox(4);
    witnessesVBox.getChildren().addAll(buttonsHBox, this.witnessesList);
    Button addWitnessButton = new Button(language.translate("life_event_view.witnesses.add"),
        theme.getIcon(Icon.ADD_WITNESS, Icon.Size.SMALL));
    addWitnessButton.setOnAction(this::onAddWitness);
    Button removeWitnessButton = new Button(language.translate("life_event_view.witnesses.remove"),
        theme.getIcon(Icon.REMOVE_WITNESS, Icon.Size.SMALL));
    removeWitnessButton.setOnAction(event -> this.onRemoveWitness());
    removeWitnessButton.setDisable(true);
    Pane spacer = new Pane();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    buttonsHBox.getChildren().addAll(spacer, addWitnessButton, removeWitnessButton);
    this.witnessesList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
        removeWitnessButton.setDisable(this.witnessesList.getSelectionModel().getSelectedItems().isEmpty()));
    this.witnessesList.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.DELETE) {
        this.onRemoveWitness();
      }
    });
    this.witnessesList.setPrefHeight(100);
    gridPane.addRow(4, new Label(language.translate("life_event_view.witnesses")), witnessesVBox);

    this.notesField.setPrefHeight(100);
    gridPane.addRow(5, new Label(language.translate("life_event_view.notes")), this.notesField);

    this.sourcesField.setPrefHeight(100);
    gridPane.addRow(6, new Label(language.translate("life_event_view.sources")), this.sourcesField);

    for (int i = 0; i < gridPane.getColumnCount(); i++) {
      gridPane.getColumnConstraints().add(new ColumnConstraints());
    }
    for (int i = 0; i < gridPane.getRowCount(); i++) {
      gridPane.getRowConstraints().add(new RowConstraints());
    }
    gridPane.getColumnConstraints().get(1).setHgrow(Priority.ALWAYS);
    ObservableList<RowConstraints> rowConstraints = gridPane.getRowConstraints();
    rowConstraints.get(4).setValignment(VPos.TOP);
    rowConstraints.get(5).setValignment(VPos.TOP);
    rowConstraints.get(6).setValignment(VPos.TOP);
    rowConstraints.get(6).setVgrow(Priority.ALWAYS);

    this.populateFields();
  }

  private void onDelete(ActionEvent event) {
    this.deletionListeners.forEach(l -> l.onDelete(this));
  }

  private void onAddWitness(ActionEvent event) {
    List<Person> potentialWitnesses = this.persons.stream()
        .filter(p -> !this.witnessesList.getItems().contains(p))
        .toList();
    Optional<Person> result = Alerts.chooser(
        "alert.choose_witness.header", "alert.choose_witness.title", potentialWitnesses);
    result.ifPresent(person -> this.witnessesList.getItems().add(person));
  }

  private void onRemoveWitness() {
    ObservableList<Person> selectedItem = this.witnessesList.getSelectionModel().getSelectedItems();
    this.witnessesList.getItems().removeAll(new LinkedList<>(selectedItem));
  }

  public LifeEvent lifeEvent() {
    return this.lifeEvent;
  }

  public LifeEventType selectedLifeEventType() {
    return this.eventTypeCombo.getSelectionModel().getSelectedItem().data();
  }

  /**
   * Check whether there is any invalid data in this form.
   *
   * @return True if there is none, false otherwise.
   */
  public boolean checkValidity() {
    boolean valid;
    if (this.dateField.checkValidity()) {
      boolean datePresent = this.dateField.getDate().isPresent();
      this.dateField.pseudoClassStateChanged(PseudoClasses.INVALID, !datePresent);
      valid = datePresent;
    } else {
      valid = false;
    }
    boolean invalidPartner = this.eventTypeCombo.getSelectionModel().getSelectedItem().data().maxActors() > 1
        && this.partnerCombo.getItems().isEmpty();
    this.partnerCombo.pseudoClassStateChanged(PseudoClasses.INVALID, invalidPartner);
    valid &= !invalidPartner;
    this.pseudoClassStateChanged(PseudoClasses.INVALID, !valid);
    return valid;
  }

  public void applyChanges() {
    this.lifeEvent.setType(this.eventTypeCombo.getSelectionModel().getSelectedItem().data());
    // Remove all actors that are not the edited person and add back the selected ones
    for (Person actor : this.lifeEvent.actors()) {
      if (actor != this.person) {
        this.lifeEvent.removeActor(actor);
        actor.removeLifeEvent(this.lifeEvent);
      }
    }
    if (!this.partnerCombo.isDisabled()) {
      Person actor = this.partnerCombo.getSelectionModel().getSelectedItem().data();
      this.lifeEvent.addActor(actor);
      actor.addLifeEvent(this.lifeEvent);
    }

    // Remove all witnesses and add back the selected ones
    for (Person witness : this.lifeEvent.witnesses()) {
      witness.removeLifeEvent(this.lifeEvent);
      this.lifeEvent.removeWitness(witness);
    }
    for (Person witness : this.witnessesList.getItems()) {
      this.lifeEvent.addWitness(witness);
      witness.addLifeEvent(this.lifeEvent);
    }

    this.lifeEvent.setDate(this.dateField.getDate().orElseThrow(() -> new IllegalArgumentException("missing date")));
    this.lifeEvent.setPlace(StringUtils.stripNullable(this.placeField.getText()).orElse(null));
    this.lifeEvent.setNotes(StringUtils.stripNullable(this.notesField.getText()).orElse(null));
    this.lifeEvent.setSources(StringUtils.stripNullable(this.sourcesField.getText()).orElse(null));
    if (!this.person.getLifeEventsAsActor().contains(this.lifeEvent)) {
      this.person.addLifeEvent(this.lifeEvent);
    }
  }

  public List<DeletionListener> getDeletionListeners() {
    return this.deletionListeners;
  }

  public List<UpdateListener> getUpdateListeners() {
    return this.updateListeners;
  }

  private void notifyUpdateListeners() {
    this.updateListeners.forEach(UpdateListener::onUpdate);
  }

  public List<TypeListener> getTypeListeners() {
    return this.typeListeners;
  }

  private void onEventTypeChange(
      ObservableValue<? extends NotNullComboBoxItem<LifeEventType>> observable,
      NotNullComboBoxItem<LifeEventType> oldValue,
      NotNullComboBoxItem<LifeEventType> newValue) {
    this.titleLabel.setText(newValue.text());
    this.partnerCombo.setDisable(newValue.data().maxActors() == 1);
    this.notifyUpdateListeners();
    this.typeListeners.forEach(listener -> listener.onTypeUpdate(newValue.data()));
  }

  private void populateEventTypeCombo(@NotNull Language language) {
    Map<LifeEventType.Group, List<LifeEventType>> groups = new HashMap<>();
    for (LifeEventType eventType : Registries.LIFE_EVENT_TYPES.entries()) {
      LifeEventType.Group group = eventType.group();
      if (!groups.containsKey(group)) {
        groups.put(group, new LinkedList<>());
      }
      groups.get(group).add(eventType);
    }

    Collator collator = Collator.getInstance(language.locale());
    for (LifeEventType.Group group : LifeEventType.Group.values()) {
      List<LifeEventType> types = groups.get(group);
      String prefix = "[%s] ".formatted(language.translate("life_event_type_group." + group.name().toLowerCase()));
      types.stream().map(type -> {
            String name = type.key().namespace().equals(Registry.BUILTIN_NS)
                ? language.translate("life_event_type." + type.key().name())
                : type.key().name();
            return new NotNullComboBoxItem<>(type, prefix + name);
          })
          .sorted((i1, i2) -> collator.compare(i1.text(), i2.text())) // Perform locale-dependent comparison
          .forEach(item -> this.eventTypeCombo.getItems().add(item));
    }
  }

  private void populateDatePrecisionCombo(@NotNull Language language) {
    for (DateField.DateType dateType : DateField.DateType.values()) {
      this.datePrecisionCombo.getItems().add(new NotNullComboBoxItem<>(dateType, language.translate(dateType.key())));
    }
  }

  private void populatePartnerCombo() {
    for (Person person : this.persons) {
      this.partnerCombo.getItems().add(new NotNullComboBoxItem<>(person, person.toString()));
    }
  }

  private void populateFields() {
    this.eventTypeCombo.getSelectionModel()
        .select(new NotNullComboBoxItem<>(this.lifeEvent.type()));
    this.datePrecisionCombo.getSelectionModel()
        .select(new NotNullComboBoxItem<>(DateField.DateType.fromDate(this.lifeEvent.date())));
    this.dateField.setDate(this.lifeEvent.date());
    this.placeField.setText(this.lifeEvent.place().orElse(""));
    Optional<Person> otherActor = this.lifeEvent.actors().stream().filter(p -> p != this.person).findFirst();
    if (otherActor.isPresent()) {
      this.partnerCombo.getSelectionModel().select(new NotNullComboBoxItem<>(otherActor.get()));
      this.partnerCombo.setDisable(false);
    } else {
      if (!this.partnerCombo.getItems().isEmpty()) {
        this.partnerCombo.getSelectionModel().select(0);
      }
      this.partnerCombo.setDisable(true);
    }
    this.lifeEvent.witnesses().forEach(p -> this.witnessesList.getItems().add(p));
    this.notesField.setText(this.lifeEvent.notes().orElse(""));
    this.sourcesField.setText(this.lifeEvent.sources().orElse(""));
  }

  @FunctionalInterface
  public interface DeletionListener {
    void onDelete(@NotNull LifeEventView lifeEventView);
  }

  @FunctionalInterface
  public interface UpdateListener {
    void onUpdate();
  }

  @FunctionalInterface
  public interface TypeListener {
    void onTypeUpdate(@NotNull LifeEventType lifeEventType);
  }
}
