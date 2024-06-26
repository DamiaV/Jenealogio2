package net.darmo_creations.jenealogio2.ui.components;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.themes.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class ErasableTextField extends HBox {
  private final TextField textField = new TextField();
  private final Button eraseButton = new Button();

  private final List<EraseListener> eraseListeners = new LinkedList<>();

  public ErasableTextField(@NotNull Config config) {
    super(5);
    HBox.setHgrow(this.textField, Priority.ALWAYS);
    this.textField.textProperty().addListener(
        (observable, oldValue, newValue) -> this.eraseButton.setDisable(newValue == null || newValue.isEmpty()));
    this.eraseButton.setGraphic(config.theme().getIcon(Icon.CLEAR_TEXT, Icon.Size.SMALL));
    this.eraseButton.setTooltip(new Tooltip(config.language().translate("erasable_text_field.erase_button.tooltip")));
    this.eraseButton.setOnAction(event -> {
      this.textField.clear();
      this.textField.requestFocus();
      this.eraseListeners.forEach(EraseListener::onErase);
    });
    this.eraseButton.setDisable(true);
    this.getChildren().addAll(this.textField, this.eraseButton);
  }

  public TextField textField() {
    return this.textField;
  }

  public Button eraseButton() {
    return this.eraseButton;
  }

  public void addEraseListener(@NotNull EraseListener listener) {
    this.eraseListeners.add(Objects.requireNonNull(listener));
  }

  public interface EraseListener {
    void onErase();
  }
}
