package net.darmo_creations.jenealogio2.ui.components.map_view;

import com.gluonhq.maps.*;
import javafx.geometry.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import net.darmo_creations.jenealogio2.model.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * This class is intended to abstract away the actual map view implementation
 * used by the app in case it changes in the future.
 */
public class MapView extends AnchorPane {
  private final GluonHQMapView mapView = new GluonHQMapView();
  private final Map<Integer, MapLayer> layers = new HashMap<>();
  private int lastLayerID = 0;

  public MapView() {
    AnchorPane.setTopAnchor(this.mapView, 0.0);
    AnchorPane.setBottomAnchor(this.mapView, 0.0);
    AnchorPane.setLeftAnchor(this.mapView, 0.0);
    AnchorPane.setRightAnchor(this.mapView, 0.0);
    this.getChildren().add(this.mapView);
  }

  /**
   * Set this map’s zoom level.
   *
   * @param zoom The new zoom level.
   */
  public void setZoom(double zoom) {
    this.mapView.setZoom(zoom);
  }

  /**
   * Center this map around the given coordinates.
   *
   * @param latLon Coordinates to center around.
   */
  public void setCenter(@NotNull LatLon latLon) {
    this.mapView.setCenter(toMapPoint(latLon));
  }

  /**
   * Add a marker at the given coordinates.
   *
   * @param latLon Marker’s coordinates.
   * @param color  Marker’s color.
   * @return ID of the created marker, will always be > 0.
   */
  public int addMarker(@NotNull LatLon latLon, @NotNull MapMarkerColor color) {
    MarkerLayer layer = new MarkerLayer(latLon, color);
    this.mapView.addLayer(layer);
    int id = this.lastLayerID++;
    this.layers.put(id, layer);
    return id;
  }

  /**
   * Remove the marker with the given ID.
   * If the ID is invalid, nothing happens.
   *
   * @param id ID of the marker to remove.
   */
  public void removeMarker(int id) {
    MapLayer removedLayer = this.layers.remove(id);
    if (removedLayer != null) {
      this.mapView.removeLayer(removedLayer);
    }
  }

  /**
   * Remove all markers.
   */
  public void removeMarkers() {
    this.layers.values().forEach(this.mapView::removeLayer);
    this.layers.clear();
  }

  public void showAllMarkers() {
    // TODO
  }

  /**
   * Convert a {@link LatLon} object into a {@link MapPoint} object.
   */
  private static MapPoint toMapPoint(@NotNull LatLon latLon) {
    return new MapPoint(latLon.lat(), latLon.lon());
  }

  /**
   * Shows a custom marker on the map.
   */
  private static class MarkerLayer extends MapLayer {
    private final LatLon latLon;
    private final ImageView markerImageView;
    private final double markerWidth, markerHeight;

    /**
     * Create a new marker.
     *
     * @param latLon The point where to show the marker.
     * @param color  The marker’s color.
     */
    public MarkerLayer(final @NotNull LatLon latLon, @NotNull MapMarkerColor color) {
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
}
