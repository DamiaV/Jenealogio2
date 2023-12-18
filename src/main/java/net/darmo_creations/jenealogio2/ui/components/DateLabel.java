package net.darmo_creations.jenealogio2.ui.components;

import javafx.scene.control.*;
import net.darmo_creations.jenealogio2.model.datetime.*;
import net.darmo_creations.jenealogio2.utils.*;

public class DateLabel extends Label {
  private DateTime dateTime;
  private final String emptyValue;

  public DateLabel(String emptyValue) {
    this(null, emptyValue);
  }

  public DateLabel(DateTime dateTime, String emptyValue) {
    this.emptyValue = emptyValue;
    this.setDateTime(dateTime);
  }

  public DateTime getDateTime() {
    return this.dateTime;
  }

  public void setDateTime(DateTime dateTime) {
    this.dateTime = dateTime;
    this.updateText();
  }

  private void updateText() {
    this.setText(this.dateTime != null ? DateTimeUtils.formatDateTime(this.dateTime, false) : this.emptyValue);
    this.setTooltip(this.dateTime != null ? new Tooltip(DateTimeUtils.formatDateTime(this.dateTime, true)) : null);
  }
}
