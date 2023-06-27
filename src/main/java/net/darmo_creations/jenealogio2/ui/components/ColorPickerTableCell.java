package net.darmo_creations.jenealogio2.ui.components;

import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import net.darmo_creations.jenealogio2.utils.StringUtils;

/**
 * A class containing a {@link TableCell} implementation that draws a
 * {@link ColorPicker} node inside the cell.
 * <p>
 * The ColorPickerTableCell is rendered as a {@link Label} when not
 * being edited, and as a ColorPicker when in editing mode. The ColorPicker will
 * stretch to fill the entire table cell.
 *
 * @param <S> The type of the TableView generic type.
 */
public class ColorPickerTableCell<S> extends TableCell<S, Color> {
  private ColorPicker colorPicker;

  public ColorPickerTableCell() {
    this.getStyleClass().add("color-picker-table-cell");
    this.itemProperty().addListener((observable, oldValue, newValue) ->
        this.setStyle(newValue != null ? "-fx-text-fill: " + StringUtils.colorToCSSHex(newValue) : null));
  }

  @Override
  public void startEdit() {
    if (!this.isEditable() || !this.getTableView().isEditable() || !this.getTableColumn().isEditable()) {
      return;
    }
    if (this.colorPicker == null) {
      this.colorPicker = this.createColorPicker();
    }
    this.colorPicker.setValue(this.getItem());
    super.startEdit();
    this.setText(null);
    this.setGraphic(this.colorPicker);
  }

  @Override
  public void cancelEdit() {
    super.cancelEdit();
    Color color = this.getItem();
    this.setText(color != null ? StringUtils.colorToCSSHex(color) : null);
    this.setGraphic(null);
  }

  @Override
  public void updateItem(Color item, boolean empty) {
    super.updateItem(item, empty);
    if (this.isEmpty()) {
      this.setText(null);
      this.setGraphic(null);
    } else {
      Color color = this.getItem();
      if (this.isEditing()) {
        if (this.colorPicker != null) {
          this.colorPicker.setValue(color);
        }
        this.setText(null);
        this.setGraphic(this.colorPicker);
      } else {
        this.setText(color != null ? StringUtils.colorToCSSHex(color) : null);
        this.setGraphic(null);
      }
    }
  }

  private ColorPicker createColorPicker() {
    ColorPicker colorPicker = new ColorPicker();
    colorPicker.setMaxWidth(Double.MAX_VALUE);
    colorPicker.setOnAction(event -> this.commitEdit(colorPicker.getValue()));
    // Cancel edit when ESCAPE key is released
    colorPicker.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
      if (e.getCode() == KeyCode.ESCAPE) {
        this.cancelEdit();
      }
    });
    return colorPicker;
  }
}