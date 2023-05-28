package net.darmo_creations.jenealogio2.ui;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import net.darmo_creations.jenealogio2.App;

import java.net.URL;

public class AboutDialog extends DialogBase<Void> {
  @FXML
  private ImageView logo;

  public AboutDialog() {
    super("about", false, ButtonTypes.CLOSE);
    this.logo.setImage(this.createImage(App.RESOURCES_ROOT + "icons/app-icon.png"));
  }

  public Image createImage(String resourceName) {
    URL url = this.getClass().getResource(resourceName);
    return new Image(url.toExternalForm());
  }
}
