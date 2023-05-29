package net.darmo_creations.jenealogio2.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import net.darmo_creations.jenealogio2.App;
import net.darmo_creations.jenealogio2.utils.StringUtils;

import java.net.URL;
import java.util.Collections;

public class AboutDialog extends DialogBase<Void> {
  @FXML
  @SuppressWarnings("unused")
  private ImageView logo;
  @FXML
  @SuppressWarnings("unused")
  private Label titleLabel;
  @FXML
  @SuppressWarnings("unused")
  private TextArea contentView;

  public AboutDialog() {
    super("about", false, ButtonTypes.CLOSE);
    this.setTitle(StringUtils.format(this.getTitle(), Collections.singletonMap("app_name", App.NAME)));
    this.logo.setImage(this.createImage(App.RESOURCES_ROOT + "icons/app-icon.png"));
    this.titleLabel.setText(App.NAME);
    this.contentView.setText("""
        App version: %s
        Java version: %s
        JVM vendor: %s (%s)
                
        Developped by Damia Vergnet (@Darmo117 on GitHub).
        This application is available under GPL-3.0 license.
                
        Icons from FatCow (https://github.com/gammasoft/fatcow).
        """.formatted(App.VERSION, System.getProperty("java.version"), System.getProperty("java.vendor"), System.getProperty("java.vendor.url")));
  }

  public Image createImage(String resourceName) {
    URL url = this.getClass().getResource(resourceName);
    if (url == null) {
      return null;
    }
    return new Image(url.toExternalForm());
  }
}
