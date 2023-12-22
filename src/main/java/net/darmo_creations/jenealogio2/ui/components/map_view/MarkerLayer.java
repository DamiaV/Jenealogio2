package net.darmo_creations.jenealogio2.ui.components.map_view;

import com.gluonhq.maps.*;
import javafx.geometry.*;
import javafx.scene.image.*;
import net.darmo_creations.jenealogio2.model.*;
import org.jetbrains.annotations.*;

/**
 * Shows a custom marker on the map.
 */
class MarkerLayer extends MapLayer {
  private final LatLon latLon;
  private final ImageView markerImageView;
  private final double markerWidth, markerHeight;

  /**
   * Create a new marker.
   *
   * @param latLon The point where to show the marker.
   * @param color  The marker’s color.
   */
  MarkerLayer(final @NotNull LatLon latLon, @NotNull MapMarkerColor color) {
    this.latLon = latLon;
    Image markerIcon = color.image();
    this.markerWidth = markerIcon != null ? markerIcon.getWidth() : 32;
    this.markerHeight = markerIcon != null ? markerIcon.getHeight() : 32;
    this.markerImageView = new ImageView(markerIcon);
    this.getChildren().add(this.markerImageView);
    this.getStyleClass().add("map-pin");
    this.setOnMouseClicked(e -> this.onMouseClicked());
    this.setOnMouseEntered(e -> this.onMouseEntered());
    this.setOnMouseExited(e -> this.onMouseExited());
  }

  private void onMouseClicked() {
    // TODO show and keep popup until clicked again
  }

  private void onMouseEntered() {
    // TODO show popup
  }

  private void onMouseExited() {
    // TODO hide popup
  }

  @Override
  protected void layoutLayer() {
    Point2D point2d = this.getMapPoint(this.latLon.lat(), this.latLon.lon());
    this.markerImageView.setTranslateX(point2d.getX() - this.markerWidth / 2);
    this.markerImageView.setTranslateY(point2d.getY() - this.markerHeight);
  }
}
