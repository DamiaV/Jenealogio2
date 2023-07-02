package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import net.darmo_creations.jenealogio2.App;
import net.darmo_creations.jenealogio2.utils.FormatArg;

import java.net.URL;
import java.util.List;

/**
 * Dialog that displays information about this app. It is not resizable.
 */
public class AboutDialog extends DialogBase<ButtonType> {
  /**
   * Create an about dialog.
   */
  public AboutDialog() {
    super("about", false, ButtonTypes.CLOSE);
    Label titleLabel = new Label();
    titleLabel.setText(App.NAME);
    titleLabel.setStyle("-fx-font-size: 1.2em; -fx-font-weight: bold");
    VBox.setMargin(titleLabel, new Insets(10));

    TextArea contentView = new TextArea();
    contentView.setText("""
        App version: %s
                
        Developped by Damia Vergnet (@Darmo117 on GitHub).
        This application is available under GPL-3.0 license.
                
        Icons from FatCow (https://github.com/gammasoft/fatcow).
                
        System properties:
        %s
        """.formatted(App.VERSION, App.getSystemProperties()));
    contentView.setEditable(false);
    VBox.setVgrow(contentView, Priority.ALWAYS);

    VBox vBox = new VBox(10, titleLabel, contentView);

    ImageView logo = new ImageView(createImage());
    logo.setFitHeight(100);
    logo.setFitWidth(100);

    HBox content = new HBox(10, logo, vBox);
    content.setPrefWidth(600);
    content.setPrefHeight(300);
    this.getDialogPane().setContent(content);
  }

  @Override
  protected List<FormatArg> getTitleFormatArgs() {
    return List.of(new FormatArg("app_name", App.NAME));
  }

  /**
   * Return an {@link Image} object of the app’s icon.
   */
  private static Image createImage() {
    URL url = AboutDialog.class.getResource(App.IMAGES_PATH + "app-icon.png");
    if (url == null) {
      return null;
    }
    return new Image(url.toExternalForm());
  }
}
