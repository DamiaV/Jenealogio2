package net.darmo_creations.jenealogio2.ui.components;

import com.gluonhq.maps.*;
import javafx.geometry.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import net.darmo_creations.jenealogio2.*;
import net.darmo_creations.jenealogio2.model.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.util.*;

/**
 * This class is intended to abstract away the actual map view implementation
 * used by the app in case it changes in the future.
 */
public class MapView extends AnchorPane {
  private final com.gluonhq.maps.MapView mapView = new com.gluonhq.maps.MapView();
  private final List<MapLayer> layers = new LinkedList<>();

  public MapView() {
    AnchorPane.setTopAnchor(this.mapView, 0.0);
    AnchorPane.setBottomAnchor(this.mapView, 0.0);
    AnchorPane.setLeftAnchor(this.mapView, 0.0);
    AnchorPane.setRightAnchor(this.mapView, 0.0);
    this.getChildren().add(this.mapView);
  }

  public void setZoom(double zoom) {
    this.mapView.setZoom(zoom);
  }

  public void setCenter(@NotNull LatLon latLon) {
    this.mapView.setCenter(toMapPoint(latLon));
  }

  public void addMarker(@NotNull LatLon latLon) {
    PinLayer layer = new PinLayer(toMapPoint(latLon));
    this.mapView.addLayer(layer);
    this.layers.add(layer);
  }

  public void removeMarkers() {
    this.layers.forEach(this.mapView::removeLayer);
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
   * Shows a custom pin on the map.
   */
  private static class PinLayer extends MapLayer {
    private final MapPoint mapPoint;
    private final ImageView mapPinImageView;
    private final double pinWidth, pinHeight;

    /**
     * @param mapPoint The point where to show the pin.
     * @see com.gluonhq.maps.MapPoint
     */
    public PinLayer(final @NotNull MapPoint mapPoint) {
      this.mapPoint = mapPoint;
      Image pinIcon = this.getPinIcon();
      this.pinWidth = pinIcon != null ? pinIcon.getWidth() : 32;
      this.pinHeight = pinIcon != null ? pinIcon.getHeight() : 32;
      this.mapPinImageView = new ImageView(pinIcon);
      this.getChildren().add(this.mapPinImageView);
      this.getStyleClass().add("map-pin");
      this.setOnMouseClicked(e -> this.onMouseClicked());
      this.setOnMouseEntered(e -> this.onMouseEntered());
      this.setOnMouseExited(e -> this.onMouseExited());
    }

    /**
     * Get the pin icon as an {@link Image}.
     */
    private @Nullable Image getPinIcon() {
      InputStream stream = this.getClass().getResourceAsStream(
          "%s%s.png".formatted(App.IMAGES_PATH, "map_pin"));
      if (stream == null) {
        App.LOGGER.warn("Missing icon: map_pin");
        return null;
      }
      return new Image(stream);
    }

    private void onMouseClicked() {
      // TODO
    }

    private void onMouseEntered() {
      // TODO
    }

    private void onMouseExited() {
      // TODO
    }

    @Override
    protected void layoutLayer() {
      Point2D point2d = this.getMapPoint(this.mapPoint.getLatitude(), this.mapPoint.getLongitude());
      this.mapPinImageView.setTranslateX(point2d.getX() - this.pinWidth / 2);
      this.mapPinImageView.setTranslateY(point2d.getY() - this.pinHeight);
    }
  }
}
