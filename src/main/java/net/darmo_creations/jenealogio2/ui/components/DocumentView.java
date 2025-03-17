package net.darmo_creations.jenealogio2.ui.components;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.config.theme.*;
import net.darmo_creations.jenealogio2.io.*;
import net.darmo_creations.jenealogio2.model.*;
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
  private final HBox tagsBox = new HBox(5);
  private final Label authorTag = new Label();
  private final Map<AnnotationType, Label> annotationLabels = new HashMap<>();

  private final AttachedDocument document;
  private final GenealogyObject<?> referenceObject;

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
    this(document, showName, null, config);
  }

  /**
   * Create a new document view.
   *
   * @param document        The document to show.
   * @param showName        Whether to show the document’s name.
   * @param referenceObject A {@link GenealogyObject} from which tags data will be taken (author, mentioned, etc.).
   *                        May be null.
   * @param config          The app’s config.
   */
  public DocumentView(
      final @NotNull AttachedDocument document,
      boolean showName,
      GenealogyObject<?> referenceObject,
      @NotNull Config config
  ) {
    super(5);
    this.document = Objects.requireNonNull(document);
    this.referenceObject = referenceObject;

    final Language language = config.language();
    final Theme theme = config.theme();

    final Node imageNode;
    if (document instanceof Picture p) {
      final Image image = p.image().orElse(theme.getIconImage(Icon.NO_IMAGE, Icon.Size.BIG));
      final ImageView imageView = new ImageView(image);
      imageView.setPreserveRatio(true);
      //noinspection DataFlowIssue
      imageView.setFitHeight(Math.min(IMAGE_SIZE, image.getHeight()));
      imageView.setFitWidth(Math.min(IMAGE_SIZE, image.getWidth()));
      imageNode = imageView;
    } else {
      ImageView imageView = theme.getIcon(Icon.forFile(document.fileName()), Icon.Size.BIG);
      if (imageView == null)
        imageView = new ImageView();
      imageView.setPreserveRatio(true);
      final int size = Icon.Size.BIG.pixels();
      imageView.setFitHeight(size);
      imageView.setFitWidth(size);
      imageNode = imageView;
    }
    final VBox vBox = new VBox(5);
    if (showName) vBox.getChildren().add(this.nameLabel);
    this.dateLabel = new DateLabel("-", config);
    vBox.getChildren().addAll(this.dateLabel, this.descLabel);
    if (this.referenceObject != null) {
      this.authorTag.setText(language.translate("document_view.tag.author"));
      this.authorTag.getStyleClass().add("document-tag");
      for (final var annotationType : AnnotationType.values()) {
        final Label tag = new Label(language.translate("document_view.tag." + annotationType.name().toLowerCase()));
        tag.getStyleClass().add("document-tag");
        this.annotationLabels.put(annotationType, tag);
      }
      vBox.getChildren().add(this.tagsBox);
    }
    HBox.setHgrow(vBox, Priority.ALWAYS);

    this.openFileButton.setGraphic(theme.getIcon(Icon.SHOW_FILE_IN_EXPLORER, Icon.Size.SMALL));
    this.openFileButton.setTooltip(new Tooltip(language.translate("document_view.open_file.tooltip")));
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
    if (this.referenceObject != null) {
      this.tagsBox.getChildren().clear();
      if (this.referenceObject instanceof Person p && p.authoredDocuments().contains(this.document))
        this.tagsBox.getChildren().add(this.authorTag);
      for (final var annotationType : AnnotationType.values())
        if (this.referenceObject.getAnnotatedInDocuments(annotationType).contains(this.document))
          this.tagsBox.getChildren().add(this.annotationLabels.get(annotationType));
    }
  }
}
