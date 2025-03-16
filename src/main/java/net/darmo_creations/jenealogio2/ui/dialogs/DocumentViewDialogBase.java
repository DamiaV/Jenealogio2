package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import net.darmo_creations.jenealogio2.config.*;
import org.jetbrains.annotations.*;

/**
 * Base class for dialogs that show a document.
 *
 * @param <R> Return type of this dialog.
 */
public abstract class DocumentViewDialogBase<R> extends DialogBase<R> {
  private final HBox imageViewBox;
  protected final ImageView imageView = new ImageView();
  private final SplitPane content;

  protected DocumentViewDialogBase(
      @NotNull Config config,
      @NotNull String name,
      boolean resizable,
      boolean modal,
      @NotNull ButtonType... buttonTypes
  ) {
    super(config, name, resizable, modal, buttonTypes);

    this.imageViewBox = new HBox(this.imageView);
    this.imageViewBox.setAlignment(Pos.CENTER);
    this.imageViewBox.setMinHeight(200);
    this.imageViewBox.heightProperty().addListener(
        (observable, oldValue, newValue) ->
            this.updateImageViewSize());
    this.stage().widthProperty().addListener(
        (observable, oldValue, newValue) ->
            this.updateImageViewSize());
    this.imageView.setPreserveRatio(true);
    this.imageView.managedProperty().bind(this.imageView.visibleProperty());

    this.content = new SplitPane(this.imageViewBox);
    this.content.setOrientation(Orientation.VERTICAL);
    this.content.setDividerPositions(0.65);
    this.content.setPrefWidth(850);
    this.content.setPrefHeight(600);
    this.getDialogPane().setContent(this.content);

    final Stage stage = this.stage();
    stage.setMinWidth(850);
    stage.setMinHeight(650);
  }

  /**
   * Set the content of the bottom half of the split pane.
   *
   * @param node The node to use as content.
   */
  public final void setContent(@NotNull Node node) {
    if (this.content.getItems().size() == 1)
      this.content.getItems().add(node);
    else this.content.getItems().set(1, node);
  }

  private void updateImageViewSize() {
    final Image image = this.imageView.getImage();
    if (image != null) {
      final double width = Math.min(image.getWidth(), this.stage().getWidth() - 20);
      this.imageView.setFitWidth(width);
      final double height = Math.min(image.getHeight(), this.imageViewBox.getHeight() - 10);
      this.imageView.setFitHeight(height);
    }
  }
}
