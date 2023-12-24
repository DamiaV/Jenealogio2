package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.application.*;
import javafx.beans.property.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import net.darmo_creations.jenealogio2.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.themes.*;
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
  private final MapView mapView = new MapView();
  private final ComboBox<ComboBoxItem<LifeEventType>> eventTypeCombo = new ComboBox<>();
  private final TextField searchField = new TextField();
  private final Button searchButton = new Button();
  private final ListView<PlaceView> placesList = new ListView<>();

  private boolean internalUpdate;
  private final IntegerProperty resultMarkerId = new SimpleIntegerProperty(0);

  private FamilyTree familyTree;

  public MapDialog() {
    super("map", true, false, ButtonTypes.CLOSE);

    Config config = App.config();
    Language language = config.language();
    Theme theme = config.theme();

    this.eventTypeCombo.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> {
          if (!this.internalUpdate) {
            this.updateMap(false);
          }
        });

    HBox.setHgrow(this.searchField, Priority.ALWAYS);
    this.searchField.setPromptText(language.translate("dialog.map.search.input"));
    this.searchField.textProperty().addListener((observableValue, oldValue, newValue) -> this.updateButtons());
    this.searchField.setOnAction(e -> this.onSearchAddress());
    Button eraseSearchButton = new Button(null, theme.getIcon(Icon.CLEAR_TEXT, Icon.Size.SMALL));
    eraseSearchButton.setTooltip(new Tooltip(language.translate("dialog.map.search.erase")));
    eraseSearchButton.setOnAction(e -> this.onEraseSearch());
    this.searchButton.setGraphic(theme.getIcon(Icon.SEARCH, Icon.Size.SMALL));
    this.searchButton.setTooltip(new Tooltip(language.translate("dialog.map.search.button")));
    this.searchButton.setOnAction(e -> this.onSearchAddress());

    this.placesList.setOnMouseClicked(e -> this.onPlaceClick());

    SplitPane content = new SplitPane(
        new VBox(
            5,
            new HBox(5, new Label(language.translate("dialog.map.event_type")), this.eventTypeCombo),
            new HBox(5, this.searchField, eraseSearchButton, this.searchButton),
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

    Stage stage = this.stage();
    stage.setMinWidth(800);
    stage.setMinHeight(600);
    this.setIcon(theme.getAppIcon());

    this.updateButtons();
    this.mapView.setZoom(2);
    this.mapView.setCenter(new LatLon(0, 0));
  }

  private void onPlaceClick() {
    PlaceView selectedItem = this.placesList.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      this.mapView.setCenter(selectedItem.latLon());
      this.mapView.setZoom(15);
    }
  }

  private void onSearchAddress() {
    StringUtils.stripNullable(this.searchField.getText()).ifPresent(s -> {
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
              int id = this.mapView.addMarker(ll, MapMarkerColor.BLUE, null);
              this.resultMarkerId.set(id);
            });
          }));
    });
  }

  private void onEraseSearch() {
    this.searchField.setText(null);
    this.removeResultMarker();
    this.updateButtons();
  }

  private void removeResultMarker() {
    int id = this.resultMarkerId.get();
    if (id > 0) {
      this.mapView.removeMarker(id);
      this.resultMarkerId.set(-1);
    }
  }

  private void updateButtons() {
    this.searchButton.setDisable(StringUtils.stripNullable(this.searchField.getText()).isEmpty());
  }

  /**
   * Refresh displayed information from the given tree.
   *
   * @param familyTree Tree to get information from.
   */
  public void refresh(final @NotNull FamilyTree familyTree) {
    this.familyTree = Objects.requireNonNull(familyTree);
    this.populateEventTypeCombo();
    this.updateMap(true);
  }

  /**
   * Refresh the map markers.
   *
   * @param resetView Whether the view should be reset, i.e. should show all markers after refresh.
   */
  private void updateMap(boolean resetView) {
    this.mapView.removeMarkers();
    this.placesList.getItems().clear();

    LifeEventType type = this.eventTypeCombo.getSelectionModel().getSelectedItem().data();
    Map<LatLon, List<LifeEvent>> groupedEvents = new HashMap<>();

    for (LifeEvent lifeEvent : this.familyTree.lifeEvents()) {
      if (type != null && lifeEvent.type() != type) {
        continue;
      }
      Optional<Place> place = lifeEvent.place();
      if (place.isPresent()) {
        Optional<LatLon> latLon = place.get().latLon();
        if (latLon.isPresent()) {
          LatLon ll = latLon.get();
          if (!groupedEvents.containsKey(ll)) {
            groupedEvents.put(ll, new LinkedList<>());
          }
          groupedEvents.get(ll).add(lifeEvent);
        }
      }
    }

    groupedEvents.entrySet().stream()
        .sorted((e1, e2) -> { // Sort by list size then place address
          List<LifeEvent> events1 = e1.getValue();
          List<LifeEvent> events2 = e2.getValue();
          int compare = -Integer.compare(events1.size(), events2.size());
          if (compare != 0) {
            return compare;
          }
          // place() will always return a non-empty value because we filtered events above
          //noinspection OptionalGetWithoutIsPresent
          return events1.get(0).place().get().address()
              .compareToIgnoreCase(events2.get(0).place().get().address());
        })
        .forEach(e -> {
          LatLon latLon = e.getKey();
          List<LifeEvent> events = e.getValue();
          int nb = events.size();
          MapMarkerColor color;
          if (nb <= 5) {
            color = MapMarkerColor.GREEN;
          } else if (nb <= 10) {
            color = MapMarkerColor.YELLOW_GREEN;
          } else if (nb <= 15) {
            color = MapMarkerColor.YELLOW;
          } else if (nb <= 20) {
            color = MapMarkerColor.ORANGE;
          } else {
            color = MapMarkerColor.RED;
          }
          //noinspection OptionalGetWithoutIsPresent
          Place place = events.get(0).place().get();
          Map<LifeEventType, Integer> typeCounts = events.stream()
              .collect(Collectors.groupingBy(LifeEvent::type, Collectors.reducing(0, i -> 1, Integer::sum)));
          this.mapView.addMarker(latLon, color, new EventTypesTooltip(place.address(), typeCounts));
          this.placesList.getItems().add(new PlaceView(place, nb));
        });

    if (resetView) {
      this.mapView.showAllMarkers();
    }
  }

  /**
   * Populate the event type combobox.
   */
  private void populateEventTypeCombo() {
    this.internalUpdate = true;
    var selectedItem = this.eventTypeCombo.getSelectionModel().getSelectedItem();

    Language language = App.config().language();

    this.eventTypeCombo.getItems().clear();
    this.eventTypeCombo.getItems().add(new ComboBoxItem<>(null, language.translate("dialog.map.event_type.all")));

    LifeEventView.populateEventTypeCombo(this.familyTree, this.eventTypeCombo);

    if (selectedItem != null && this.eventTypeCombo.getItems().contains(selectedItem)) {
      this.eventTypeCombo.getSelectionModel().select(selectedItem);
    } else {
      this.eventTypeCombo.getSelectionModel().select(0);
    }
    this.internalUpdate = false;
  }

  private static class PlaceView {
    private final LatLon latLon;
    private final String text;

    public PlaceView(@NotNull Place place, int nb) {
      String address = place.address();
      //noinspection OptionalGetWithoutIsPresent
      this.latLon = place.latLon().get();
      this.text = App.config().language().translate(
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
