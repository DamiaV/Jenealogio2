package net.darmo_creations.jenealogio2.ui.components;

import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.io.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.themes.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * This component shows an {@link AttachedDocument} along with its description and date.
 */
public class DocumentView extends HBox implements Comparable<DocumentView> {
  private static final int IMAGE_SIZE = 100;

  private final Label nameLabel = new Label();
  private final DateLabel dateLabel;
  private final Label descLabel = new Label();
  private final Button openFileButton = new Button();

  private final AttachedDocument document;

  /**
   * Create a new document view.
   *
   * @param document The document to show.
   * @param showName Whether to show the document’s name.
   * @param config   The app’s config.
   */
  public DocumentView(
      final @NotNull AttachedDocument document,
      boolean showName,
      @NotNull Config config
  ) {
    super(5);
    this.document = Objects.requireNonNull(document);
    final Node imageNode;
    if (document instanceof Picture p) {
      final Optional<Image> image = p.image();
      if (image.isPresent()) {
        final ImageView imageView = new ImageView(image.orElse(null));
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(IMAGE_SIZE);
        imageView.setFitWidth(IMAGE_SIZE);
        imageNode = imageView;
      } else {
        final Label label = new Label(config.language().translate("document_view.no_image"));
        label.setAlignment(Pos.CENTER);
        label.setPrefHeight(IMAGE_SIZE);
        label.setPrefWidth(IMAGE_SIZE);
        imageNode = label;
      }
    } else {
      ImageView imageView = config.theme().getIcon(Icon.forFile(document.fileName()), Icon.Size.BIG);
      if (imageView == null)
        imageView = new ImageView();
      imageView.setPreserveRatio(true);
      final int size = Icon.Size.BIG.pixels();
      imageView.setFitHeight(size);
      imageView.setFitWidth(size);
      imageNode = imageView;
    }
    final VBox vBox = new VBox(5);
    if (showName)
      vBox.getChildren().add(this.nameLabel);
    this.dateLabel = new DateLabel("-", config);
    vBox.getChildren().addAll(this.dateLabel, this.descLabel);
    HBox.setHgrow(vBox, Priority.ALWAYS);

    this.openFileButton.setGraphic(config.theme().getIcon(Icon.SHOW_FILE_IN_EXPLORER, Icon.Size.SMALL));
    this.openFileButton.setTooltip(new Tooltip(config.language().translate("document_view.open_file.tooltip")));
    this.openFileButton.setOnAction(event -> FileUtils.openInFileExplorer(document.path()));

    this.getChildren().addAll(imageNode, vBox, this.openFileButton);
    this.refresh();
  }

  public AttachedDocument document() {
    return this.document;
  }

  @Override
  public int compareTo(final @NotNull DocumentView o) {
    return this.document().compareTo(o.document());
  }

  /**
   * Refresh the displayed picture information.
   */
  public void refresh() {
    this.nameLabel.setText(this.document.fileName());
    this.dateLabel.setDateTime(this.document.date().orElse(null));
    this.descLabel.setText(this.document.description().orElse(""));
    this.openFileButton.setDisable(this.document instanceof Picture p && p.image().isEmpty());
  }
}
