package net.darmo_creations.jenealogio2.ui.components;

import javafx.beans.property.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.datetime.*;
import net.darmo_creations.jenealogio2.themes.*;
import net.darmo_creations.jenealogio2.ui.dialogs.*;
import org.jetbrains.annotations.*;

/**
 * This component allows the selection of a {@link DateTime} value.
 */
public class DateTimeSelector extends HBox {
  private final ObjectProperty<DateTime> dateTimeProperty = new SimpleObjectProperty<>();

  /**
   * Create a new {@link DateTime} selector.
   *
   * @param config The app’s config.
   */
  public DateTimeSelector(@NotNull Config config) {
    super(5);
    this.setAlignment(Pos.CENTER_LEFT);

    final var dateSelectionDialog = new DateSelectionDialog(config);

    final Button editButton = new Button(null, config.theme().getIcon(Icon.SELECT_DATE, Icon.Size.SMALL));
    editButton.setTooltip(new Tooltip(config.language().translate("date_selector.edit_button.tooltip")));
    editButton.setOnAction(event -> {
      dateSelectionDialog.setDateTime(this.getDateTime());
      dateSelectionDialog.showAndWait().ifPresent(this::setDateTime);
    });

    final Button clearButton = new Button(null, config.theme().getIcon(Icon.CLEAR_DATE, Icon.Size.SMALL));
    clearButton.setTooltip(new Tooltip(config.language().translate("date_selector.clear_button.tooltip")));
    clearButton.setOnAction(event -> this.setDateTime(null));

    final DateLabel label = new DateLabel("-", config);

    this.dateTimeProperty.addListener((observable, oldValue, newValue) -> {
      label.setDateTime(this.getDateTime());
      clearButton.setDisable(newValue == null);
    });
    clearButton.setDisable(true);

    this.getChildren().addAll(editButton, clearButton, label);
  }

  public @Nullable DateTime getDateTime() {
    return this.dateTimeProperty.get();
  }

  public void setDateTime(DateTime dateTime) {
    this.dateTimeProperty.set(dateTime);
  }

  public ObjectProperty<DateTime> dateTimeProperty() {
    return this.dateTimeProperty;
  }
}
