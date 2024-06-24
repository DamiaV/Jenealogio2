package net.darmo_creations.jenealogio2.ui.components;

import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.io.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.model.datetime.*;
import net.darmo_creations.jenealogio2.themes.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * This component shows an {@link AttachedDocument} along with its description and date.
 */
public class DocumentView<T extends AttachedDocument> extends HBox implements Comparable<DocumentView<T>> {
  private static final int IMAGE_SIZE = 100;

  private final Label nameLabel = new Label();
  private final Label dateLabel = new Label();
  private final Label descLabel = new Label();

  private final T document;
  private final Config config;

  /**
   * Create a new document view.
   *
   * @param document The document to show.
   * @param showName Whether to show the document’s name.
   * @param config   The app’s config.
   */
  public DocumentView(
      final @NotNull T document,
      boolean showName,
      @NotNull Config config
  ) {
    super(5);
    this.document = Objects.requireNonNull(document);
    this.config = Objects.requireNonNull(config);
    Node imageNode;
    if (document instanceof Picture p) {
      Optional<Image> image = p.image();
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
    } else {
      Icon icon = FileUtils.splitExtension(document.fileName()).right()
          .map(ext -> Icon.fromName("FILE_EXT_" + ext.substring(1).toUpperCase(), Icon.UNKNOWN_FILE_EXT))
          .orElse(Icon.UNKNOWN_FILE_EXT);
      ImageView imageView = config.theme().getIcon(icon, Icon.Size.BIG);
      if (imageView == null)
        imageView = new ImageView();
      imageView.setPreserveRatio(true);
      int size = Icon.Size.BIG.pixels();
      imageView.setFitHeight(size);
      imageView.setFitWidth(size);
      imageNode = imageView;
    }
    VBox vBox = new VBox(5);
    if (showName) {
      vBox.getChildren().add(this.nameLabel);
    }
    vBox.getChildren().addAll(this.dateLabel, this.descLabel);
    this.getChildren().addAll(imageNode, vBox);
    this.refresh();
  }

  public T document() {
    return this.document;
  }

  @Override
  public int compareTo(final @NotNull DocumentView<T> o) {
    return this.document().compareTo(o.document());
  }

  /**
   * Refresh the displayed picture information.
   */
  public void refresh() {
    this.nameLabel.setText(this.document.fileName());
    this.dateLabel.setText(this.document.date().map(this::formatDate).orElse("-"));
    this.descLabel.setText(this.document.description().orElse(""));
  }

  private String formatDate(@NotNull DateTime date) {
    String calDate = DateTimeUtils.formatDateTime(date, false, this.config);
    String isoDate = DateTimeUtils.formatDateTime(date, true, this.config);
    return "%s (%s)".formatted(calDate, isoDate);
  }
}