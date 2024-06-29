package net.darmo_creations.jenealogio2.ui.components;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.*;

/**
 * A table cell that shows an {@link Image}.
 */
public class ImagePickerTableCell<S> extends TableCell<S, Image> {
  private final ImageView imageView = new ImageView();
  private final HBox hBox = new HBox(5);
  private final Supplier<Optional<Image>> imageSupplier;

  /**
   * Create a new table cell.
   *
   * @param imageSupplier A function that supplies an {@link Image}.
   */
  public ImagePickerTableCell(@NotNull Supplier<Optional<Image>> imageSupplier) {
    this.imageSupplier = Objects.requireNonNull(imageSupplier);
    this.getStyleClass().add("image-picker-table-cell");
    this.hBox.getChildren().add(this.imageView);
    this.hBox.setAlignment(Pos.CENTER);
  }

  @Override
  public void startEdit() {
    if (!this.isEditable() || !this.getTableView().isEditable() || !this.getTableColumn().isEditable())
      return;
    super.startEdit();
    this.imageSupplier.get().ifPresent(this::commitEdit);
  }

  @Override
  public void updateItem(Image item, boolean empty) {
    super.updateItem(item, empty);
    this.setText(null);
    if (this.isEmpty())
      this.setGraphic(null);
    else {
      this.imageView.setImage(item);
      this.setGraphic(this.hBox);
    }
  }
}
