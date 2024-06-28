package net.darmo_creations.jenealogio2.ui.components.map_view;

import com.gluonhq.maps.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.text.*;
import net.darmo_creations.jenealogio2.*;

/**
 * Adds attributions to the {@link com.gluonhq.maps.MapView} and fixes the following methods:
 * <li>{@link #setZoom(double)}</li>
 * <li>{@link #setCenter(double, double)}</li>
 * <li>{@link #setCenter(MapPoint)}</li>
 */
class GluonHQMapView extends com.gluonhq.maps.MapView {
  GluonHQMapView() {
    Hyperlink osmLink = new Hyperlink("OpenStreetMap");
    osmLink.setOnAction(e -> App.openURL("https://www.openstreetmap.org/copyright/"));
    Hyperlink gluonLink = new Hyperlink("GluonHQ Maps");
    gluonLink.setOnAction(e -> App.openURL("https://github.com/gluonhq/maps"));
    TextFlow label = new TextFlow(
        new Text("©"),
        osmLink,
        new Text("contributors | Powered by"),
        gluonLink
    );
    label.getStyleClass().add("map-attributions");
    label.setPadding(new Insets(0, 0, 0, 5));
    label.setStyle("-fx-background-color: #fffb");
    // Pin text to bottom-right corner
    label.layoutXProperty().bind(this.widthProperty().subtract(label.widthProperty()));
    label.layoutYProperty().bind(this.heightProperty().subtract(label.heightProperty()));
    this.getChildren().add(label);
  }

  @Override
  public void setZoom(double zoom) {
    // Hack to trigger a the InvalidationListener of the underlying BaseMap object.
    super.setZoom(zoom + 0.00001);
    super.setZoom(zoom);
  }

  @Override
  public void setCenter(double lat, double lon) {
    // Hack to trigger a the InvalidationListener of the underlying BaseMap object.
    super.setCenter(lat + 0.00001, lon + 0.00001);
    super.setCenter(lat, lon);
  }

  @Override
  public void addLayer(MapLayer child) {
    super.addLayer(child);
    // Fix markers not being positioned correctly
    this.markDirty();
  }
}
