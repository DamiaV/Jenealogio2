package net.darmo_creations.jenealogio2.ui.components;

import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.model.datetime.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * This component shows a {@link Picture} along with its description.
 */
public class PictureView extends HBox implements Comparable<PictureView> {
  private static final int IMAGE_SIZE = 100;

  private final Label nameLabel = new Label();
  private final Label dateLabel = new Label();
  private final Label imageDescLabel = new Label();

  private final Picture picture;
  private final Config config;

  /**
   * Create a new picture view.
   *
   * @param picture  The picture to show.
   * @param showName Whether to show the picture’s name.
   * @param config   The app’s config.
   */
  public PictureView(
      final @NotNull Picture picture,
      boolean showName,
      @NotNull Config config
  ) {
    super(5);
    this.picture = Objects.requireNonNull(picture);
    this.config = Objects.requireNonNull(config);
    Optional<Image> image = picture.image();
    Node imageNode;
    if (image.isPresent()) {
      ImageView imageView = new ImageView(image.orElse(null));
      imageView.setPreserveRatio(true);
      imageView.setFitHeight(IMAGE_SIZE);
      imageView.setFitWidth(IMAGE_SIZE);
      imageNode = imageView;
    } else {
      Label label = new Label(config.language().translate("picture_view.no_image"));
      label.setAlignment(Pos.CENTER);
      label.setPrefHeight(IMAGE_SIZE);
      label.setPrefWidth(IMAGE_SIZE);
      imageNode = label;
    }
    VBox vBox = new VBox(5);
    if (showName) {
      vBox.getChildren().add(this.nameLabel);
    }
    vBox.getChildren().addAll(this.dateLabel, this.imageDescLabel);
    this.getChildren().addAll(imageNode, vBox);
    this.refresh();
  }

  public Picture picture() {
    return this.picture;
  }

  @Override
  public int compareTo(final @NotNull PictureView o) {
    return this.picture().compareTo(o.picture());
  }

  /**
   * Refresh the displayed picture information.
   */
  public void refresh() {
    this.nameLabel.setText(this.picture.fileName());
    this.dateLabel.setText(this.picture.date().map(this::formatDate).orElse("-"));
    this.imageDescLabel.setText(this.picture.description().orElse(""));
  }

  private String formatDate(@NotNull DateTime date) {
    String calDate = DateTimeUtils.formatDateTime(date, false, this.config);
    String isoDate = DateTimeUtils.formatDateTime(date, true, this.config);
    return "%s (%s)".formatted(calDate, isoDate);
  }
}
