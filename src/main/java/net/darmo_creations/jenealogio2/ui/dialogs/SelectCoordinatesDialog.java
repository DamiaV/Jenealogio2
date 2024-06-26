package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.application.*;
import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.themes.*;
import net.darmo_creations.jenealogio2.ui.components.*;
import net.darmo_creations.jenealogio2.ui.components.map_view.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

/**
 * A dialog used select coordinates on a map.
 */
public class SelectCoordinatesDialog extends DialogBase<LatLon> {
  private final ErasableTextField searchField;
  private final Button searchButton = new Button();
  private final MapView mapView;

  private LatLon selectedPoint;
  private final IntegerProperty selectedMarkerId = new SimpleIntegerProperty(0);

  /**
   * Create a new dialog to select coordinates.
   *
   * @param config The app’s config.
   */
  public SelectCoordinatesDialog(final @NotNull Config config) {
    super(config, "select_coordinates", true, ButtonTypes.OK, ButtonTypes.CANCEL);

    Language language = config.language();
    Theme theme = config.theme();

    this.searchField = new ErasableTextField(config);
    HBox.setHgrow(this.searchField, Priority.ALWAYS);
    this.searchField.textField().setPromptText(language.translate("dialog.select_coordinates.search.input"));
    this.searchField.textField().textProperty().addListener((observableValue, oldValue, newValue) -> this.updateButtons());
    this.searchField.textField().setOnAction(e -> this.onSearchAddress());
    this.searchButton.setGraphic(theme.getIcon(Icon.SEARCH, Icon.Size.SMALL));
    this.searchButton.setTooltip(new Tooltip(language.translate("dialog.select_coordinates.search.button")));
    this.searchButton.setOnAction(e -> this.onSearchAddress());

    this.mapView = new MapView(config);
    VBox.setVgrow(this.mapView, Priority.ALWAYS);
    this.mapView.setOnPointClicked(this::onPointClicked);

    VBox content = new VBox(
        5,
        new Label(
            language.translate("dialog.select_coordinates.description"),
            theme.getIcon(Icon.INFO, Icon.Size.SMALL)
        ),
        new HBox(5, this.searchField, this.searchButton),
        this.mapView
    );
    content.setPrefWidth(400);
    content.setPrefHeight(400);
    this.getDialogPane().setContent(content);

    Stage stage = this.stage();
    stage.setMinWidth(400);
    stage.setMinHeight(400);

    this.setResultConverter(buttonType -> {
      if (!buttonType.getButtonData().isCancelButton()) {
        return this.selectedPoint;
      }
      return null;
    });

    this.mapView.setZoom(2);
    this.mapView.setCenter(new LatLon(0, 0));
    this.updateButtons();
  }

  private void onPointClicked(@NotNull LatLon latLon) {
    this.selectedPoint = latLon;
    this.removeClickedMarker();
    int id = this.mapView.addMarker(latLon, MapMarkerColor.GREEN, null);
    this.selectedMarkerId.set(id);
    this.updateButtons();
  }

  private void onSearchAddress() {
    StringUtils.stripNullable(this.searchField.textField().getText()).ifPresent(s -> {
      this.removeClickedMarker();
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
              this.selectedMarkerId.set(id);
            });
          }));
    });
  }

  private void removeClickedMarker() {
    int id = this.selectedMarkerId.get();
    if (id > 0) {
      this.mapView.removeMarker(id);
      this.selectedMarkerId.set(-1);
    }
  }

  private void updateButtons() {
    this.searchButton.setDisable(StringUtils.stripNullable(this.searchField.textField().getText()).isEmpty());
    this.getDialogPane().lookupButton(ButtonTypes.OK).setDisable(this.selectedPoint == null);
  }
}
