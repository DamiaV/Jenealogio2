package net.darmo_creations.jenealogio2.ui.components;

import javafx.application.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.util.converter.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.config.theme.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.ui.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.controlsfx.control.textfield.*;
import org.jetbrains.annotations.*;

import java.text.*;
import java.util.*;

/**
 * JavaFX component that presents a form to edit a {@link LifeEvent} object.
 */
public class LifeEventView extends TitledPane implements PersonRequester, CoordinatesRequester {
  private final Label titleLabel = new Label();
  private final DateLabel dateLabel;
  private final ComboBox<NotNullComboBoxItem<LifeEventType>> eventTypeCombo = new ComboBox<>();
  private final DateTimeSelector dateTimeSelector;
  private final TextField placeAddressField = new TextField();
  private final PlaceAutoCompletionBinding placeAutoCompletionBinding;
  private final TextField placeLatField = new TextField();
  private final TextField placeLonField = new TextField();
  private final Button fetchLatLonButton = new Button();
  private final Button openMapDialogButton = new Button();
  private final Label loadingLabel = new Label();
  private final Label partnerLabel = new Label();
  private final Button partnerButton = new Button();
  private final ListView<Person> witnessesList = new ListView<>();
  private final TextArea notesField = new TextArea();
  private final TextArea sourcesField = new TextArea();

  private final FamilyTree familyTree;
  private final LifeEvent lifeEvent;
  private final Person person;
  private final Config config;
  private Person partner;

  private final List<DeletionListener> deletionListeners = new LinkedList<>();
  private final List<UpdateListener> updateListeners = new LinkedList<>();
  private final List<TypeListener> typeListeners = new LinkedList<>();
  private PersonRequestListener personRequestListener;
  private CoordinatesRequestListener coordinatesRequestListener;

  /**
   * Create a new {@link LifeEvent} editing form.
   *
   * @param lifeEvent Life event object to edit.
   * @param person    Person object that acts in the life event object.
   * @param expanded  Whether to expand this form by default.
   * @param parent    Parent {@link ListView} component.
   * @param config    The app’s config.
   */
  public LifeEventView(
      @NotNull FamilyTree familyTree,
      @NotNull LifeEvent lifeEvent,
      @NotNull Person person,
      boolean expanded,
      final @NotNull ListView<LifeEventView> parent,
      final @NotNull Config config
  ) {
    this.familyTree = Objects.requireNonNull(familyTree);
    this.lifeEvent = Objects.requireNonNull(lifeEvent);
    this.person = Objects.requireNonNull(person);
    this.config = config;
    this.setAnimated(false);
    this.setExpanded(expanded);
    final Language language = config.language();
    final Theme theme = config.theme();

    final BorderPane borderPane = new BorderPane();
    final Button deleteButton = new Button("", theme.getIcon(Icon.DELETE_EVENT, Icon.Size.SMALL));
    deleteButton.setTooltip(new Tooltip(language.translate("life_event_view.delete")));
    deleteButton.setOnAction(event -> this.onDelete());
    BorderPane.setAlignment(this.titleLabel, Pos.CENTER_LEFT);
    borderPane.setLeft(this.titleLabel);
    this.dateLabel = new DateLabel(null, this.config);
    final HBox topBox = new HBox(5, this.dateLabel, deleteButton);
    topBox.setAlignment(Pos.CENTER_LEFT);
    borderPane.setRight(topBox);
    borderPane.prefWidthProperty().bind(parent.widthProperty().subtract(70));
    this.setGraphic(borderPane);

    final AnchorPane anchorPane = new AnchorPane();
    this.setContent(anchorPane);

    final GridPane gridPane = new GridPane();
    gridPane.setHgap(5);
    gridPane.setVgap(5);
    AnchorPane.setTopAnchor(gridPane, 10.0);
    AnchorPane.setBottomAnchor(gridPane, 10.0);
    AnchorPane.setLeftAnchor(gridPane, 10.0);
    AnchorPane.setRightAnchor(gridPane, 10.0);
    anchorPane.getChildren().add(gridPane);

    populateEventTypeCombo(familyTree, this.eventTypeCombo, config);
    this.eventTypeCombo.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) ->
            this.onEventTypeChange(newValue));
    gridPane.addRow(0, new Label(language.translate("life_event_view.type")), this.eventTypeCombo);

    this.dateTimeSelector = new DateTimeSelector(config);
    this.dateTimeSelector.dateTimeProperty().addListener((observable, oldValue, newValue) -> {
      this.updateDateLabel();
      this.notifyUpdateListeners();
    });
    final Label dateLabel = new Label(language.translate("life_event_view.date"));
    dateLabel.setPadding(new Insets(3, 0, 0, 0));
    gridPane.addRow(1, dateLabel, this.dateTimeSelector);

    HBox.setHgrow(this.placeAddressField, Priority.ALWAYS);
    this.placeAddressField.setPromptText(language.translate("life_event_view.place.address"));
    this.placeAddressField.textProperty().addListener((observableValue, oldValue, newValue) -> {
      final boolean noAddress = StringUtils.stripNullable(newValue).isEmpty();
      this.placeLatField.setDisable(noAddress);
      this.placeLonField.setDisable(noAddress);
      this.fetchLatLonButton.setDisable(noAddress);
      this.openMapDialogButton.setDisable(noAddress);
    });
    this.placeAutoCompletionBinding = new PlaceAutoCompletionBinding(
        this.placeAddressField,
        this::onPlaceSuggestionRequest,
        this::onPlaceCompletion
    );

    this.placeLatField.setPromptText(language.translate("life_event_view.place.latitude"));
    this.placeLatField.setTextFormatter(new TextFormatter<>(
        new DoubleStringConverter(),
        null,
        null
    ));
    this.placeLatField.setPrefWidth(100);

    this.placeLonField.setPromptText(language.translate("life_event_view.place.longitude"));
    this.placeLonField.setTextFormatter(new TextFormatter<>(
        new DoubleStringConverter(),
        null,
        null
    ));
    this.placeLonField.setPrefWidth(100);

    this.fetchLatLonButton.setText(language.translate("life_event_view.place.lookup_latlon"));
    this.fetchLatLonButton.setGraphic(theme.getIcon(Icon.GET_LATLON, Icon.Size.SMALL));
    this.fetchLatLonButton.setOnAction(e -> this.onLookupPlaceLatLon());

    this.loadingLabel.setPrefWidth(30);

    this.openMapDialogButton.setText(language.translate("life_event_view.place.select_latlon_from_map"));
    this.openMapDialogButton.setGraphic(theme.getIcon(Icon.OPEN_LATLON_SELECTOR, Icon.Size.SMALL));
    this.openMapDialogButton.setOnAction(e -> this.onSelectLatLonFromMap());

    final HBox placeBox = new HBox(
        5,
        this.placeAddressField,
        this.placeLatField,
        this.placeLonField,
        this.fetchLatLonButton,
        this.loadingLabel,
        this.openMapDialogButton
    );
    gridPane.addRow(2, new Label(language.translate("life_event_view.place")), placeBox);

    this.partnerButton.setGraphic(theme.getIcon(Icon.EDIT_PARTNER, Icon.Size.SMALL));
    this.partnerButton.setTooltip(new Tooltip(language.translate("life_event_view.partner.edit")));
    this.partnerButton.setOnAction(e -> this.onPartnerSelect());
    gridPane.addRow(
        3,
        new Label(language.translate("life_event_view.partner")),
        new HBox(5, this.partnerLabel, this.partnerButton)
    );

    final VBox witnessesVBox = new VBox(5);
    final HBox buttonsHBox = new HBox(5);
    witnessesVBox.getChildren().addAll(buttonsHBox, this.witnessesList);
    final Button addWitnessButton = new Button(language.translate("life_event_view.witnesses.add"),
        theme.getIcon(Icon.ADD_WITNESS, Icon.Size.SMALL));
    addWitnessButton.setOnAction(event -> this.onAddWitness());
    final Button removeWitnessButton = new Button(language.translate("life_event_view.witnesses.remove"),
        theme.getIcon(Icon.REMOVE_WITNESS, Icon.Size.SMALL));
    removeWitnessButton.setOnAction(event -> this.onRemoveWitness());
    removeWitnessButton.setDisable(true);
    buttonsHBox.getChildren().addAll(
        new Spacer(Orientation.HORIZONTAL),
        addWitnessButton,
        removeWitnessButton
    );
    this.witnessesList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
        removeWitnessButton.setDisable(this.witnessesList.getSelectionModel().getSelectedItems().isEmpty()));
    this.witnessesList.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.DELETE)
        this.onRemoveWitness();
    });
    this.witnessesList.setPrefHeight(100);
    final Label witnessesLabel = new Label(language.translate("life_event_view.witnesses"));
    witnessesLabel.setPadding(new Insets(3, 0, 0, 0));
    gridPane.addRow(4, witnessesLabel, witnessesVBox);

    this.notesField.setPrefHeight(100);
    final Label notesLabel = new Label(language.translate("life_event_view.notes"));
    notesLabel.setPadding(new Insets(3, 0, 0, 0));
    gridPane.addRow(5, notesLabel, this.notesField);

    this.sourcesField.setPrefHeight(100);
    final Label sourcesLabel = new Label(language.translate("life_event_view.sources"));
    sourcesLabel.setPadding(new Insets(3, 0, 0, 0));
    gridPane.addRow(6, sourcesLabel, this.sourcesField);

    for (int i = 0; i < gridPane.getColumnCount(); i++)
      gridPane.getColumnConstraints().add(new ColumnConstraints());
    for (int i = 0; i < gridPane.getRowCount(); i++)
      gridPane.getRowConstraints().add(new RowConstraints());
    gridPane.getColumnConstraints().get(1).setHgrow(Priority.ALWAYS);
    final ObservableList<RowConstraints> rowConstraints = gridPane.getRowConstraints();
    rowConstraints.get(1).setValignment(VPos.TOP);
    rowConstraints.get(4).setValignment(VPos.TOP);
    rowConstraints.get(5).setValignment(VPos.TOP);
    rowConstraints.get(6).setValignment(VPos.TOP);
    rowConstraints.get(6).setVgrow(Priority.ALWAYS);

    this.populateFields();
  }

  private void onLookupPlaceLatLon() {
    this.loadingLabel.setGraphic(this.config.theme().getIcon(Icon.LOADING, Icon.Size.SMALL));
    this.fetchLatLonButton.setDisable(true);
    StringUtils.stripNullable(this.placeAddressField.getText())
        .ifPresent(address ->
            GeoCoder.geoCode(address).thenAcceptAsync(latLon ->
                Platform.runLater(() -> {
                  this.loadingLabel.setGraphic(null);
                  this.fetchLatLonButton.setDisable(false);
                  latLon.ifPresent(this::setLatLonFields);
                })));
  }

  private void onSelectLatLonFromMap() {
    this.coordinatesRequestListener.onCoordinatesRequest()
        .ifPresent(this::setLatLonFields);
  }

  private void setLatLonFields(@NotNull LatLon latLon) {
    this.placeLatField.setText(String.valueOf(latLon.lat()));
    this.placeLonField.setText(String.valueOf(latLon.lon()));
  }

  private void onPartnerSelect() {
    if (this.personRequestListener == null) return;
    this.personRequestListener.onPersonRequest(this.getExclusionList()).ifPresent(p -> {
      this.setPartner(p);
      this.notifyUpdateListeners();
    });
  }

  private Collection<Place> onPlaceSuggestionRequest(@NotNull AutoCompletionBinding.ISuggestionRequest request) {
    final String userText = request.getUserText().toLowerCase();
    return this.familyTree.lifeEvents().stream()
        .filter(e -> e.place().isPresent() && e.place().get().address().toLowerCase().contains(userText))
        .map(e -> e.place().orElseThrow())
        .distinct()
        .toList();
  }

  private void onPlaceCompletion(@NotNull Place place) {
    place.latLon().ifPresent(this::setLatLonFields);
  }

  /**
   * Update header’s date label text.
   */
  private void updateDateLabel() {
    this.dateLabel.setDateTime(this.dateTimeSelector.getDateTime());
  }

  /**
   * Called when delete button is clicked.
   * Notifies all {@link DeletionListener}s.
   */
  private void onDelete() {
    this.deletionListeners.forEach(l -> l.onDelete(this));
  }

  /**
   * Called when the add witness button is clicked.
   */
  private void onAddWitness() {
    if (this.personRequestListener == null) return;
    this.personRequestListener.onPersonRequest(this.getExclusionList())
        .ifPresent(this.witnessesList.getItems()::add);
  }

  private List<Person> getExclusionList() {
    final List<Person> exclusionList = new LinkedList<>(this.witnessesList.getItems());
    if (this.partner != null)
      exclusionList.add(this.partner);
    return exclusionList;
  }

  /**
   * Called when the remove witness action (button or keyboard key) is fired.
   */
  private void onRemoveWitness() {
    final ObservableList<Person> selectedItem = this.witnessesList.getSelectionModel().getSelectedItems();
    this.witnessesList.getItems().removeAll(new LinkedList<>(selectedItem));
  }

  /**
   * The life event object being edited.
   */
  public LifeEvent lifeEvent() {
    return this.lifeEvent;
  }

  /**
   * The currently selected life event type.
   */
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
    final boolean datePresent = this.dateTimeSelector.getDateTime() != null;
    this.dateTimeSelector.pseudoClassStateChanged(PseudoClasses.INVALID, !datePresent);
    valid = datePresent;
    final boolean invalidPartner =
        this.eventTypeCombo.getSelectionModel().getSelectedItem().data().maxActors() > 1 &&
            this.partner == null;
    this.partnerLabel.pseudoClassStateChanged(PseudoClasses.INVALID, invalidPartner);
    valid &= !invalidPartner;
    this.pseudoClassStateChanged(PseudoClasses.INVALID, !valid);
    return valid;
  }

  /**
   * Update the wrapped life event object with this form’s data.
   */
  public void applyChanges() {
    this.lifeEvent.setType(this.eventTypeCombo.getSelectionModel().getSelectedItem().data());
    final Set<Person> actors = new HashSet<>();
    actors.add(this.person);
    if (!this.partnerButton.isDisabled())
      actors.add(this.partner);
    this.familyTree.setLifeEventActors(this.lifeEvent, actors);

    // Remove all witnesses and add back the selected ones
    for (final Person witness : this.lifeEvent.witnesses())
      this.familyTree.removeWitnessFromLifeEvent(this.lifeEvent, witness);
    for (final Person witness : this.witnessesList.getItems())
      this.familyTree.addWitnessToLifeEvent(this.lifeEvent, witness);

    this.lifeEvent.setDate(Optional.ofNullable(this.dateTimeSelector.getDateTime())
        .orElseThrow(() -> new IllegalArgumentException("Missing date")));
    final String address = StringUtils.stripNullable(this.placeAddressField.getText()).orElse(null);
    if (address != null) {
      LatLon latLon;
      try {
        final double lat = Double.parseDouble(this.placeLatField.getText());
        final double lon = Double.parseDouble(this.placeLonField.getText());
        latLon = new LatLon(lat, lon);
      } catch (final RuntimeException e) {
        latLon = null;
      }
      this.lifeEvent.setPlace(new Place(address, latLon));
    } else
      this.lifeEvent.setPlace(null);
    this.lifeEvent.setNotes(StringUtils.stripNullable(this.notesField.getText()).orElse(null));
    this.lifeEvent.setSources(StringUtils.stripNullable(this.sourcesField.getText()).orElse(null));
  }

  /**
   * Return the list of all {@link DeletionListener}s.
   * They are notified whenever the delete button is clicked.
   */
  public List<DeletionListener> getDeletionListeners() {
    return this.deletionListeners;
  }

  /**
   * Return the list of all {@link UpdateListener}s.
   * They are notified whenever the event type, date precision, date fields or partner is updated.
   */
  public List<UpdateListener> getUpdateListeners() {
    return this.updateListeners;
  }

  /**
   * Notify all {@link UpdateListener}s.
   */
  private void notifyUpdateListeners() {
    this.updateListeners.forEach(UpdateListener::onUpdate);
  }

  /**
   * Return the list of all {@link TypeListener}s.
   * They are notified whenever the event type is updated.
   */
  public List<TypeListener> getTypeListeners() {
    return this.typeListeners;
  }

  @Override
  public void setPersonRequestListener(@NotNull PersonRequestListener listener) {
    this.personRequestListener = listener;
  }

  @Override
  public void setCoordinatesRequestListener(@NotNull CoordinatesRequestListener listener) {
    this.coordinatesRequestListener = listener;
  }

  /**
   * Called when the event type is changed.
   *
   * @param item The selected type item.
   */
  private void onEventTypeChange(@NotNull NotNullComboBoxItem<LifeEventType> item) {
    this.titleLabel.setText(item.text());
    final boolean oneActor = item.data().maxActors() == 1;
    this.partnerButton.setDisable(oneActor);
    this.setPartner(this.partner); // Refresh display
    this.notifyUpdateListeners();
    this.typeListeners.forEach(listener -> listener.onTypeUpdate(item.data()));
  }

  /**
   * Populate the given event type combobox from the given family tree.
   *
   * @param familyTree     The tree to get {@link LifeEventType}s from.
   * @param eventTypeCombo The {@link ComboBox} to populate.
   * @param config         The app’s config.
   */
  public static void populateEventTypeCombo(
      final @NotNull FamilyTree familyTree,
      @NotNull ComboBox<? super NotNullComboBoxItem<LifeEventType>> eventTypeCombo,
      final @NotNull Config config
  ) {
    final Map<LifeEventType.Group, List<LifeEventType>> groups = new HashMap<>();
    for (final LifeEventType eventType : familyTree.lifeEventTypeRegistry().entries()) {
      final var group = eventType.group();
      if (!groups.containsKey(group))
        groups.put(group, new LinkedList<>());
      groups.get(group).add(eventType);
    }

    final Language language = config.language();
    final Collator collator = Collator.getInstance(language.locale());
    for (final LifeEventType.Group group : LifeEventType.Group.values()) {
      final List<LifeEventType> types = groups.get(group);
      final String prefix = "[%s] ".formatted(
          language.translate("life_event_type_group." + group.name().toLowerCase()));
      types.stream()
          .map(type -> {
            final String name = type.isBuiltin()
                ? language.translate("life_event_types." + type.key().name())
                : Objects.requireNonNull(type.userDefinedName());
            return new NotNullComboBoxItem<>(type, prefix + name);
          })
          // Perform locale-dependent comparison
          .sorted((i1, i2) -> collator.compare(i1.text(), i2.text()))
          .forEach(item -> eventTypeCombo.getItems().add(item));
    }
  }

  /**
   * Populate this form’s fields with data from the wrapped life event object.
   */
  private void populateFields() {
    this.eventTypeCombo.getSelectionModel()
        .select(new NotNullComboBoxItem<>(this.lifeEvent.type()));
    this.dateTimeSelector.setDateTime(this.lifeEvent.date());
    final Optional<Place> place = this.lifeEvent.place();
    if (place.isPresent()) {
      final Place p = place.get();
      this.placeAddressField.setText(p.address());
      p.latLon().ifPresent(this::setLatLonFields);
    } else {
      this.placeAddressField.setText(null);
      this.placeLatField.setText(null);
      this.placeLonField.setText(null);
    }
    final Optional<Person> otherActor = this.lifeEvent.actors().stream()
        .filter(p -> p != this.person)
        .findFirst();
    this.setPartner(otherActor.orElse(null));
    this.lifeEvent.witnesses().forEach(p -> this.witnessesList.getItems().add(p));
    this.notesField.setText(this.lifeEvent.notes().orElse(""));
    this.sourcesField.setText(this.lifeEvent.sources().orElse(""));
  }

  private void setPartner(final Person partner) {
    final var selectedItem = this.eventTypeCombo.getSelectionModel().getSelectedItem();
    final boolean oneActor = selectedItem.data().maxActors() == 1;
    this.partnerButton.setDisable(oneActor);
    this.partner = partner;
    final String cssClass = "unknown";
    final ObservableList<String> styleClass = this.partnerLabel.getStyleClass();
    if (oneActor) {
      this.partnerLabel.setText(null);
      styleClass.remove(cssClass);
    } else if (partner != null) {
      this.partnerLabel.setText(this.partner.toString());
      styleClass.remove(cssClass);
    } else {
      this.partnerLabel.setText(this.config.language().translate("life_event_view.partner.not_set"));
      if (!styleClass.contains(cssClass))
        styleClass.add(cssClass);
    }
  }

  /**
   * Deletion listeners are notified whenever the delete button is clicked.
   */
  @FunctionalInterface
  public interface DeletionListener {
    void onDelete(@NotNull LifeEventView lifeEventView);
  }

  /**
   * Update listeners are notifies whenever the event type, date precision, date fields or partner is updated.
   */
  @FunctionalInterface
  public interface UpdateListener {
    void onUpdate();
  }

  /**
   * Type listeners are notified whenever the event type is updated.
   */
  @FunctionalInterface
  public interface TypeListener {
    void onTypeUpdate(@NotNull LifeEventType lifeEventType);
  }

  public void dispose() {
    this.placeAutoCompletionBinding.dispose();
  }
}
