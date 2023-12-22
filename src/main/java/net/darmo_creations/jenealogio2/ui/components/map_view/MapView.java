package net.darmo_creations.jenealogio2.ui.components.map_view;

import com.gluonhq.maps.*;
import javafx.scene.*;
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
   * @param latLon       Marker’s coordinates.
   * @param color        Marker’s color.
   * @param layerTooltip Optional tooltip to show on mouse click.
   * @return ID of the created marker, will always be > 0.
   */
  public int addMarker(@NotNull LatLon latLon, @NotNull MapMarkerColor color, Node layerTooltip) {
    MarkerLayer layer = new MarkerLayer(latLon, color, layerTooltip);
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
}
