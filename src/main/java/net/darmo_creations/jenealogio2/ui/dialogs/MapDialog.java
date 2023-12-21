package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import net.darmo_creations.jenealogio2.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.ui.components.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * This dialog shows markers relating to a family tree’s persons.
 */
// TODO credit GluonHQ and OSM
public class MapDialog extends DialogBase<ButtonType> {
  private final MapView mapView = new MapView();
  private final ComboBox<ComboBoxItem<LifeEventType>> eventTypeCombo = new ComboBox<>();
  private boolean internalUpdate;
  // TODO panel with stats about the number of occurrences of each place
  // TODO address search bar

  private FamilyTree familyTree;

  public MapDialog() {
    super("map", true, false, ButtonTypes.CLOSE);

    Language language = App.config().language();

    this.eventTypeCombo.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> {
          if (!this.internalUpdate) {
            this.updateMap(false);
          }
        });

    VBox content = new VBox(
        5,
        new HBox(5, new Label(language.translate("dialog.map.event_type")), this.eventTypeCombo),
        this.mapView
    );
    content.setPrefWidth(500);
    content.setPrefHeight(500);
    this.getDialogPane().setContent(content);

    Stage stage = this.stage();
    stage.setMinWidth(800);
    stage.setMinHeight(600);
    this.setIcon(App.config().theme().getAppIcon());
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
    LifeEventType type = this.eventTypeCombo.getSelectionModel().getSelectedItem().data();
    this.familyTree.lifeEvents().stream()
        .filter(e -> type == null || e.type() == type)
        .forEach(e -> e.place()
            .flatMap(Place::latLon)
            .ifPresent(this.mapView::addMarker));
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
}
