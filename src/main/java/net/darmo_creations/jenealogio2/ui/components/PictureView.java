package net.darmo_creations.jenealogio2.ui.components;

import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.utils.*;

import java.util.*;

/**
 * This component shows a {@link Picture} along with its description.
 */
public class PictureView extends HBox {
  private static final int IMAGE_SIZE = 100;

  private final Label imageDescLabel = new Label();

  private final Picture picture;

  public PictureView(Picture picture) {
    this.picture = picture;
    ImageView imageView = new ImageView(picture.image());
    imageView.setPreserveRatio(true);
    imageView.setFitHeight(IMAGE_SIZE);
    imageView.setFitWidth(IMAGE_SIZE);
    picture.description().ifPresent(this.imageDescLabel::setText);
    this.getChildren().addAll(imageView, this.imageDescLabel);
  }

  public Picture picture() {
    return this.picture;
  }

  public Optional<String> imageDescription() {
    return StringUtils.stripNullable(this.imageDescLabel.getText());
  }

  public void setImageDescription(String text) {
    this.imageDescLabel.setText(text);
  }
}
