package net.darmo_creations.jenealogio2.ui.components.map_view;

import com.gluonhq.maps.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.image.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import org.controlsfx.control.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Shows a custom marker on the map.
 */
class MarkerLayer extends MapLayer {
  private final Config config;
  private final LatLon latLon;
  private final ImageView markerImageView;
  private final double markerWidth, markerHeight;
  private final PopOver popOver = new PopOver();
  private boolean popOverStyleInitialized = false;

  /**
   * Create a new marker.
   *
   * @param latLon  The point where to show the marker.
   * @param color   The marker’s icon.
   * @param tooltip Optional tooltip.
   * @param config  The app’s config.
   */
  MarkerLayer(
      final @NotNull LatLon latLon,
      @NotNull MapMarkerColor color,
      Node tooltip,
      final @NotNull Config config
  ) {
    this.config = Objects.requireNonNull(config);
    this.latLon = latLon;
    Image markerIcon = color.image();
    this.markerWidth = markerIcon != null ? markerIcon.getWidth() : 32;
    this.markerHeight = markerIcon != null ? markerIcon.getHeight() : 32;
    this.markerImageView = new ImageView(markerIcon);
    this.getChildren().add(this.markerImageView);
    this.getStyleClass().add("map-pin");
    if (tooltip != null) {
      this.getStyleClass().add("has-tooltip");
      this.setOnMouseClicked(e -> this.onMouseClicked());
      this.popOver.setContentNode(tooltip);
    }
  }

  private void onMouseClicked() {
    if (this.popOver.isShowing()) {
      this.popOver.hide();
    } else {
      this.popOver.show(this.markerImageView);
      if (!this.popOverStyleInitialized) {
        // From https://stackoverflow.com/a/36404968/3779986
        ObservableList<String> stylesheets = ((Parent) this.popOver.getSkin().getNode()).getStylesheets();
        this.config.theme().getStyleSheets()
            .forEach(path -> stylesheets.add(path.toExternalForm()));
        this.popOverStyleInitialized = true;
      }
    }
  }

  @Override
  protected void layoutLayer() {
    Point2D point2d = this.getMapPoint(this.latLon.lat(), this.latLon.lon());
    this.markerImageView.setTranslateX(point2d.getX() - this.markerWidth / 2);
    this.markerImageView.setTranslateY(point2d.getY() - this.markerHeight);
  }
}
