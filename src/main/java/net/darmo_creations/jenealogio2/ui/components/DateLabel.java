package net.darmo_creations.jenealogio2.ui.components;

import javafx.scene.control.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.datetime.*;
import net.darmo_creations.jenealogio2.themes.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * A label that shows a formatted {@link DateTime} object.
 */
public class DateLabel extends Label {
  private DateTime dateTime;
  private final String emptyValue;
  private final Config config;

  /**
   * Create a new date label.
   *
   * @param emptyValue The value to show if the date is null.
   * @param config     The app’s config.
   */
  public DateLabel(String emptyValue, final @NotNull Config config) {
    this(null, emptyValue, config);
  }

  /**
   * Create a new date label.
   *
   * @param dateTime   The date to show.
   * @param emptyValue The value to show if the date is null.
   * @param config     The app’s config.
   */
  public DateLabel(DateTime dateTime, String emptyValue, final @NotNull Config config) {
    this.emptyValue = emptyValue;
    this.config = Objects.requireNonNull(config);
    this.setDateTime(dateTime);
  }

  public void setDateTime(DateTime dateTime) {
    this.dateTime = dateTime;
    this.updateText();
    this.setGraphic(dateTime != null ? this.config.theme().getIcon(Icon.HELP, Icon.Size.SMALL) : null);
  }

  private void updateText() {
    this.setText(this.dateTime != null ? DateTimeUtils.formatDateTime(this.dateTime, false, this.config) : this.emptyValue);
    this.setTooltip(this.dateTime != null ? new Tooltip(DateTimeUtils.formatDateTime(this.dateTime, true, this.config)) : null);
  }
}
