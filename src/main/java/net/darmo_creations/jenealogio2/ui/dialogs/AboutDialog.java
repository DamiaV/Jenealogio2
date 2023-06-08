package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import net.darmo_creations.jenealogio2.App;
import net.darmo_creations.jenealogio2.utils.FormatArg;
import net.darmo_creations.jenealogio2.utils.StringUtils;

import java.net.URL;

/**
 * Dialog that displays information about this app. It is not resizable.
 */
public class AboutDialog extends DialogBase<ButtonType> {
  @FXML
  @SuppressWarnings("unused")
  private ImageView logo;
  @FXML
  @SuppressWarnings("unused")
  private Label titleLabel;
  @FXML
  @SuppressWarnings("unused")
  private TextArea contentView;

  /**
   * Create an about dialog.
   */
  public AboutDialog() {
    super("about", false, ButtonTypes.CLOSE);
    this.setTitle(StringUtils.format(this.getTitle(), new FormatArg("app_name", App.NAME)));
    this.logo.setImage(this.createImage());
    this.titleLabel.setText(App.NAME);
    this.contentView.setText("""
        App version: %s
                
        Developped by Damia Vergnet (@Darmo117 on GitHub).
        This application is available under GPL-3.0 license.
                
        Icons from FatCow (https://github.com/gammasoft/fatcow).
                
        System properties:
        %s
        """.formatted(App.VERSION, App.getSystemProperties()));
  }

  /**
   * Return an {@link Image} object of the app’s icon.
   */
  private Image createImage() {
    URL url = this.getClass().getResource(App.IMAGES_PATH + "icons/app-icon.png");
    if (url == null) {
      return null;
    }
    return new Image(url.toExternalForm());
  }
}
