package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.application.*;
import javafx.beans.property.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.config.theme.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.ui.components.*;
import net.darmo_creations.jenealogio2.ui.components.map_view.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.stream.*;

/**
 * This dialog shows markers relating to a family tree’s persons.
 */
public class MapDialog extends DialogBase<ButtonType> {
  private final MapView mapView;
  private final ComboBox<ComboBoxItem<LifeEventType>> eventTypeCombo = new ComboBox<>();
  private final ErasableTextField searchField;
  private final Button searchButton = new Button();
  private final ListView<PlaceView> placesList = new ListView<>();

  private boolean internalUpdate;
  private final IntegerProperty resultMarkerId = new SimpleIntegerProperty(0);

  private FamilyTree familyTree;

  /**
   * Create a new map dialog.
   *
   * @param config The app’s config.
   */
  public MapDialog(final @NotNull Config config) {
    super(
        config,
        "map",
        true,
        false,
        ButtonTypes.CLOSE
    );
    final Language language = config.language();
    final Theme theme = config.theme();

    this.eventTypeCombo.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> {
          if (!this.internalUpdate)
            this.updateMap();
        });

    this.searchField = new ErasableTextField(config);
    HBox.setHgrow(this.searchField, Priority.ALWAYS);
    this.searchField.textField().setPromptText(language.translate("dialog.map.search.input"));
    this.searchField.textField().textProperty().addListener(
        (observableValue, oldValue, newValue) -> this.updateButtons());
    this.searchField.textField().setOnAction(e -> this.onSearchAddress());
    this.searchField.addEraseListener(this::removeResultMarker);
    this.searchButton.setGraphic(theme.getIcon(Icon.SEARCH, Icon.Size.SMALL));
    this.searchButton.setTooltip(new Tooltip(language.translate("dialog.map.search.button")));
    this.searchButton.setOnAction(e -> this.onSearchAddress());

    this.placesList.setOnMouseClicked(e -> this.onPlaceClick());

    this.mapView = new MapView(config);
    final HBox filterBox = new HBox(
        5,
        new Label(language.translate("dialog.map.event_type")),
        this.eventTypeCombo
    );
    filterBox.setAlignment(Pos.CENTER_LEFT);
    final SplitPane content = new SplitPane(
        new VBox(
            5,
            filterBox,
            new HBox(5, this.searchField, this.searchButton),
            this.mapView
        ),
        this.placesList
    );
    content.setPadding(new Insets(5));
    content.setOrientation(Orientation.VERTICAL);
    content.setDividerPositions(0.8);
    content.setPrefWidth(500);
    content.setPrefHeight(500);
    this.getDialogPane().setContent(content);

    final Stage stage = this.stage();
    stage.setMinWidth(800);
    stage.setMinHeight(600);

    this.updateButtons();
    this.mapView.setZoom(2);
    this.mapView.setCenter(new LatLon(0, 0));
  }

  private void onPlaceClick() {
    final PlaceView selectedItem = this.placesList.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      this.mapView.setCenter(selectedItem.latLon());
      this.mapView.setZoom(15);
    }
  }

  private void onSearchAddress() {
    StringUtils.stripNullable(this.searchField.textField().getText()).ifPresent(s -> {
      this.removeResultMarker();
      this.searchButton.setDisable(true);
      this.searchField.setDisable(true);
      GeoCoder.geoCode(s).thenAcceptAsync(latLon ->
          Platform.runLater(() -> {
            this.searchButton.setDisable(false);
            this.searchField.setDisable(false);
            latLon.ifPresent(ll -> {
              this.mapView.setCenter(ll);
              this.mapView.setZoom(15);
              final int id = this.mapView.addMarker(ll, MapMarkerColor.BLUE, null);
              this.resultMarkerId.set(id);
            });
          }));
    });
  }

  private void removeResultMarker() {
    final int id = this.resultMarkerId.get();
    if (id > 0) {
      this.mapView.removeMarker(id);
      this.resultMarkerId.set(-1);
    }
  }

  private void updateButtons() {
    this.searchButton.setDisable(StringUtils.stripNullable(this.searchField.textField().getText()).isEmpty());
  }

  /**
   * Refresh displayed information from the given tree.
   *
   * @param familyTree Tree to get information from.
   */
  public void refresh(final @NotNull FamilyTree familyTree) {
    this.familyTree = Objects.requireNonNull(familyTree);
    this.populateEventTypeCombo();
    this.updateMap();
  }

  /**
   * Refresh the map markers.
   */
  private void updateMap() {
    this.mapView.removeMarkers();
    this.placesList.getItems().clear();

    final LifeEventType type = this.eventTypeCombo.getSelectionModel().getSelectedItem().data();
    final Map<LatLon, List<LifeEvent>> groupedEvents = new HashMap<>();

    for (final LifeEvent lifeEvent : this.familyTree.lifeEvents()) {
      if (type != null && !lifeEvent.type().equals(type))
        continue;
      final Optional<Place> place = lifeEvent.place();
      if (place.isPresent()) {
        final Optional<LatLon> latLon = place.get().latLon();
        if (latLon.isPresent()) {
          final LatLon ll = latLon.get();
          if (!groupedEvents.containsKey(ll))
            groupedEvents.put(ll, new LinkedList<>());
          groupedEvents.get(ll).add(lifeEvent);
        }
      }
    }

    groupedEvents.entrySet().stream()
        .sorted((e1, e2) -> { // Sort by list size then place address
          final List<LifeEvent> events1 = e1.getValue();
          final List<LifeEvent> events2 = e2.getValue();
          final int compare = -Integer.compare(events1.size(), events2.size());
          if (compare != 0)
            return compare;
          // place() will always return a non-empty value because we filtered events above
          return events1.get(0).place().orElseThrow().address()
              .compareToIgnoreCase(events2.get(0).place().orElseThrow().address());
        })
        .forEach(e -> {
          final LatLon latLon = e.getKey();
          final List<LifeEvent> events = e.getValue();
          final int nb = events.size();
          final MapMarkerColor color;
          if (nb <= 5)
            color = MapMarkerColor.GREEN;
          else if (nb <= 10)
            color = MapMarkerColor.YELLOW_GREEN;
          else if (nb <= 15)
            color = MapMarkerColor.YELLOW;
          else if (nb <= 20)
            color = MapMarkerColor.ORANGE;
          else
            color = MapMarkerColor.RED;
          final Place place = events.get(0).place().orElseThrow();
          final Map<LifeEventType, Integer> typeCounts = events.stream()
              .collect(Collectors.groupingBy(
                  LifeEvent::type,
                  Collectors.reducing(0, i -> 1, Integer::sum)
              ));
          this.mapView.addMarker(latLon, color, new EventTypesTooltip(place.address(), typeCounts, this.config));
          this.placesList.getItems().add(new PlaceView(place, nb));
        });
  }

  /**
   * Populate the event type combobox.
   */
  private void populateEventTypeCombo() {
    this.internalUpdate = true;
    final var selectedItem = this.eventTypeCombo.getSelectionModel().getSelectedItem();

    final Language language = this.config.language();

    this.eventTypeCombo.getItems().clear();
    this.eventTypeCombo.getItems().add(
        new ComboBoxItem<>(null, language.translate("dialog.map.event_type.all")));

    LifeEventView.populateEventTypeCombo(this.familyTree, this.eventTypeCombo, this.config);

    if (selectedItem != null && this.eventTypeCombo.getItems().contains(selectedItem))
      this.eventTypeCombo.getSelectionModel().select(selectedItem);
    else
      this.eventTypeCombo.getSelectionModel().select(0);
    this.internalUpdate = false;
  }

  private class PlaceView {
    private final LatLon latLon;
    private final String text;

    public PlaceView(@NotNull Place place, int nb) {
      final String address = place.address();
      this.latLon = place.latLon().orElseThrow();
      this.text = MapDialog.this.config.language().translate(
          "dialog.map.place_count",
          nb,
          new FormatArg("address", address),
          new FormatArg("count", nb)
      );
    }

    public LatLon latLon() {
      return this.latLon;
    }

    @Override
    public String toString() {
      return this.text;
    }
  }
}
