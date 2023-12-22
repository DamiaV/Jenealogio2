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
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.util.*;

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
              this.mapView.setZoom(10);
              int id = this.mapView.addMarker(ll, MapView.MarkerColor.BLUE);
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
      if (type != null && lifeEvent.type() == type) {
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
          int compare = -Integer.compare(e1.getValue().size(), e2.getValue().size());
          if (compare != 0) {
            return compare;
          }
          // place() will always return a non-empty value because we filtered events above
          //noinspection OptionalGetWithoutIsPresent
          return e1.getValue().get(0).place().get().address()
              .compareToIgnoreCase(e2.getValue().get(0).place().get().address());
        })
        .forEach(e -> {
          int nb = e.getValue().size();
          MapView.MarkerColor color;
          if (nb <= 5) {
            color = MapView.MarkerColor.GREEN;
          } else if (nb <= 10) {
            color = MapView.MarkerColor.YELLOW_GREEN;
          } else if (nb <= 15) {
            color = MapView.MarkerColor.YELLOW;
          } else if (nb <= 20) {
            color = MapView.MarkerColor.ORANGE;
          } else {
            color = MapView.MarkerColor.RED;
          }
          this.mapView.addMarker(e.getKey(), color);
          //noinspection OptionalGetWithoutIsPresent
          this.placesList.getItems().add(new PlaceView(e.getValue().get(0).place().get(), nb));
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
    private final String address;
    private final LatLon latLon;
    private final int nb;

    public PlaceView(@NotNull Place place, int nb) {
      this.address = place.address();
      //noinspection OptionalGetWithoutIsPresent
      this.latLon = place.latLon().get();
      this.nb = nb;
    }

    public LatLon latLon() {
      return this.latLon;
    }

    @Override
    public String toString() {
      return "%s (%d)".formatted(this.address, this.nb);
    }
  }
}