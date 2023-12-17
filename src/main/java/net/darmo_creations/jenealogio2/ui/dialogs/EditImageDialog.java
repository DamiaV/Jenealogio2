package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import net.darmo_creations.jenealogio2.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

/**
 * This dialog allows editing the description of a {@link Picture}.
 */
public class EditImageDialog extends DialogBase<String> {
  private final ImageView imageView = new ImageView();
  private final Label imageNameLabel = new Label();
  private final TextArea imageDescTextInput = new TextArea();

  public EditImageDialog() {
    super("edit_image", true, ButtonTypes.OK, ButtonTypes.CANCEL);

    Config config = App.config();
    Language language = config.language();

    HBox hb = new HBox(this.imageView);
    hb.setAlignment(Pos.CENTER);
    this.imageView.setPreserveRatio(true);
    HBox hBox = new HBox(this.imageNameLabel);
    hBox.setAlignment(Pos.TOP_CENTER);
    VBox.setVgrow(this.imageDescTextInput, Priority.ALWAYS);

    VBox content = new VBox(
        5,
        hb,
        hBox,
        new Label(language.translate("dialog.edit_image.description")),
        this.imageDescTextInput
    );
    content.setPrefWidth(400);
    content.setPrefHeight(300);
    this.getDialogPane().setContent(content);

    this.imageView.fitWidthProperty().bind(this.stage().widthProperty().subtract(20));
    this.imageView.fitHeightProperty().bind(this.stage().heightProperty().subtract(200));

    Stage stage = this.stage();
    stage.setMinWidth(300);
    stage.setMinHeight(300);
    this.setIcon(config.theme().getAppIcon());

    this.setResultConverter(buttonType -> {
      if (!buttonType.getButtonData().isCancelButton()) {
        return StringUtils.stripNullable(this.imageDescTextInput.getText()).orElse(null);
      }
      return null;
    });
  }

  /**
   * Set the picture to edit.
   *
   * @param picture A picture.
   */
  public void setPicture(@NotNull Picture picture) {
    this.imageView.setImage(picture.image());
    this.imageNameLabel.setText(picture.name());
    this.imageDescTextInput.setText(picture.description().orElse(""));
  }
}
