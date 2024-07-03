package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import net.darmo_creations.jenealogio2.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.themes.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Dialog that displays information about this app. It is not resizable.
 */
public class AboutDialog extends DialogBase<ButtonType> {
  /**
   * Create an about dialog.
   *
   * @param config The app’s config.
   */
  public AboutDialog(final @NotNull Config config) {
    super(config, "about", false, ButtonTypes.CLOSE);
    final Label titleLabel = new Label();
    titleLabel.setText(App.NAME);
    titleLabel.setStyle("-fx-font-size: 1.5em; -fx-font-weight: bold");

    final Pane spacer = new Pane();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    final Button button = new Button(null, config.theme().getIcon(Icon.COPY_TO_CLIPBOARD, Icon.Size.SMALL));
    button.setTooltip(new Tooltip(config.language().translate("dialog.about.copy_specs_button.tooltip")));
    button.setOnAction(event -> {
      final var clipboardContent = new ClipboardContent();
      clipboardContent.putString(App.getSystemProperties());
      Clipboard.getSystemClipboard().setContent(clipboardContent);
    });

    final HBox hBox = new HBox(5, titleLabel, spacer, button);

    final var contentView = getTextArea();
    VBox.setVgrow(contentView, Priority.ALWAYS);

    final VBox vBox = new VBox(10, hBox, contentView);

    final ImageView logo = new ImageView(config.theme().getAppIcon());
    logo.setFitHeight(100);
    logo.setFitWidth(100);

    final HBox content = new HBox(10, logo, vBox);
    content.setPrefWidth(600);
    content.setPrefHeight(600);
    this.getDialogPane().setContent(content);
  }

  private static Node getTextArea() {
    final TextFlow contentView = new TextFlow();
    final String text = """
        App version: %s
                
        Developped by Damia Vergnet (@Darmo117 on GitHub).
        This application is available under GPL-3.0 license.
                
        Icons from FatCow (<https://github.com/gammasoft/fatcow>).
                
        Map view by Gluon Maps (<https://github.com/gluonhq/maps>).
                
        Map tile data and geocoding service by OpenStreetMap and its contributors (<https://www.openstreetmap.org/copyright>).
                
        System properties:
        %s
        """.formatted(App.VERSION, App.getSystemProperties());
    contentView.getChildren().addAll(StringUtils.parseText(text, App::openURL));
    return new ScrollPane(contentView);
  }

  @Override
  protected List<FormatArg> getTitleFormatArgs() {
    return List.of(new FormatArg("app_name", App.NAME));
  }
}
