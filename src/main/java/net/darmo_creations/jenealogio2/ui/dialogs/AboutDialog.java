package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.*;
import net.darmo_creations.jenealogio2.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.config.theme.*;
import net.darmo_creations.jenealogio2.ui.components.*;
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
    super(
        config,
        "about",
        true,
        ButtonTypes.CLOSE
    );
    final Label titleLabel = new Label();
    titleLabel.setText(App.NAME);
    titleLabel.setStyle("-fx-font-size: 1.5em; -fx-font-weight: bold");

    final Label systemPropsLabel = new Label(config.language().translate("dialog.about.system_properties"));

    final Button button = new Button(null, config.theme().getIcon(Icon.COPY_TO_CLIPBOARD, Icon.Size.SMALL));
    button.setTooltip(new Tooltip(config.language().translate("dialog.about.copy_specs_button.tooltip")));
    button.setOnAction(event -> {
      final var clipboardContent = new ClipboardContent();
      clipboardContent.putString(App.getSystemProperties());
      Clipboard.getSystemClipboard().setContent(clipboardContent);
    });

    final HBox buttonBox = new HBox(
        5,
        systemPropsLabel,
        new Spacer(Orientation.HORIZONTAL),
        button
    );
    buttonBox.setAlignment(Pos.CENTER_LEFT);

    final TextArea systemPropsTextArea = new TextArea(App.getSystemProperties());
    systemPropsTextArea.setEditable(false);
    VBox.setVgrow(systemPropsTextArea, Priority.ALWAYS);

    final VBox vBox = new VBox(5, titleLabel, getTextArea(), buttonBox, systemPropsTextArea);
    HBox.setHgrow(vBox, Priority.ALWAYS);

    final ImageView logo = new ImageView(config.theme().getAppIcon());
    logo.setFitHeight(100);
    logo.setFitWidth(100);

    final HBox content = new HBox(10, logo, vBox);
    content.setPrefWidth(600);
    content.setPrefHeight(600);
    this.getDialogPane().setContent(content);

    final Stage stage = this.stage();
    stage.setMinHeight(400);
    stage.setMinWidth(600);
  }

  private static Node getTextArea() {
    final TextFlow textFlow = new TextFlow();
    final String text = """
        Version: *%s*
        
        Developped by Damia Vergnet ([@DamiaV](https://github.com/DamiaV) on GitHub).
        This application is available under [GPL-3.0 license](https://github.com/DamiaV/Jenealogio2/blob/master/LICENSE).
        Check for updates at <https://github.com/DamiaV/Jenealogio2>.
        
        Icons from [FatCow](https://github.com/gammasoft/fatcow).
        
        Map view powered by [Gluon Maps](https://github.com/gluonhq/maps).
        
        Map tile data and geocoding service by [OpenStreetMap and its contributors](https://www.openstreetmap.org/copyright).
        """.formatted(App.VERSION);
    textFlow.getChildren().addAll(StringUtils.parseText(text, App::openURL));
    return textFlow;
  }

  @Override
  protected List<FormatArg> getTitleFormatArgs() {
    return List.of(new FormatArg("app_name", App.NAME));
  }
}
