package net.darmo_creations.jenealogio2.ui.components;

import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
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

  private final Label dateLabel = new Label();
  private final Label imageDescLabel = new Label();

  private final Picture picture;
  private DateTime date;

  public PictureView(final @NotNull Picture picture, boolean showName) {
    super(5);
    this.picture = Objects.requireNonNull(picture);
    ImageView imageView = new ImageView(picture.image());
    imageView.setPreserveRatio(true);
    imageView.setFitHeight(IMAGE_SIZE);
    imageView.setFitWidth(IMAGE_SIZE);
    this.setDate(picture.date().orElse(null));
    picture.description().ifPresent(this.imageDescLabel::setText);
    VBox vBox = new VBox(5);
    if (showName) {
      vBox.getChildren().add(new Label(picture.name()));
    }
    vBox.getChildren().addAll(this.dateLabel, this.imageDescLabel);
    this.getChildren().addAll(imageView, vBox);
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

  public Optional<DateTime> date() {
    return Optional.ofNullable(this.date);
  }

  public void setDate(final DateTime date) {
    this.date = date;
    this.dateLabel.setText(date != null ? DateTimeUtils.formatDateTime(date) : "-");
  }

  @Override
  public int compareTo(final @NotNull PictureView o) {
    return this.picture().compareTo(o.picture());
  }
}
