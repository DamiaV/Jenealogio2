package net.darmo_creations.jenealogio2.ui.components;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.converter.IntegerStringConverter;
import net.darmo_creations.jenealogio2.App;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

/**
 * JavaFX component acting as a date-time text input.
 * <p>
 * It is composed of 5 text fields, one for each date component:
 * year, month, day, hours, and minutes.
 */
public class DateTimeField extends HBox {
  private final TextField yearField = new TextField();
  private final TextField monthField = new TextField();
  private final TextField dayField = new TextField();
  private final TextField hourField = new TextField();
  private final TextField minuteField = new TextField();

  private final List<UpdateListener> updateListeners = new LinkedList<>();

  /**
   * Create an empty date-time field.
   */
  public DateTimeField() {
    super(4);

    this.setupField(this.yearField, 50, "year");
    this.setupField(this.monthField, 40, "month");
    this.setupField(this.dayField, 40, "day");
    this.setupField(this.hourField, 40, "hour");
    this.setupField(this.minuteField, 40, "minute");

    Label slash1Label = new Label("/");
    HBox.setMargin(slash1Label, new Insets(4, 0, 0, 0));
    Label slash2Label = new Label("/");
    HBox.setMargin(slash2Label, new Insets(4, 0, 0, 0));
    Label colonLabel = new Label(":");
    HBox.setMargin(colonLabel, new Insets(4, 0, 0, 0));
    Pane spacer = new Pane();
    spacer.setPrefWidth(4);

    this.getChildren().addAll(
        this.yearField,
        slash1Label,
        this.monthField,
        slash2Label,
        this.dayField,
        spacer,
        this.hourField,
        colonLabel,
        this.minuteField
    );
  }

  /**
   * Setup the given text field as accepting only positive integers.
   *
   * @param textField Text field to setup.
   * @param width     Field’s width.
   * @param name      Field’s name.
   */
  private void setupField(@NotNull TextField textField, int width, @NotNull String name) {
    textField.setTextFormatter(new TextFormatter<>(
        new IntegerStringConverter(),
        null,
        change -> change.getControlNewText().matches("^\\d*$") ? change : null
    ));
    textField.setPrefWidth(width);
    textField.setPromptText(App.config().language().translate("date_time_field.%s.prompt".formatted(name)));
    textField.textProperty().addListener((observable, oldValue, newValue) -> this.notifyListeners());
  }

  /**
   * Return the {@link LocalDateTime} value corresponding to the field’s values.
   */
  public @Nullable LocalDateTime getDate() {
    try {
      int y = Integer.parseInt(this.yearField.getText());
      int m = Integer.parseInt(this.monthField.getText());
      int d = Integer.parseInt(this.dayField.getText());
      int h = Integer.parseInt(this.hourField.getText());
      int mi = Integer.parseInt(this.minuteField.getText());
      return LocalDateTime.of(y, m, d, h, mi);
    } catch (RuntimeException e) {
      return null;
    }
  }

  /**
   * Set the date-time value of this field.
   *
   * @param dateTime The date-time value.
   */
  public void setDate(LocalDateTime dateTime) {
    if (dateTime != null) {
      this.yearField.setText(String.valueOf(dateTime.getYear()));
      this.monthField.setText(String.valueOf(dateTime.getMonthValue()));
      this.dayField.setText(String.valueOf(dateTime.getDayOfMonth()));
      this.hourField.setText(String.valueOf(dateTime.getHour()));
      this.minuteField.setText(String.valueOf(dateTime.getMinute()));
    } else {
      this.yearField.setText(null);
      this.monthField.setText(null);
      this.dayField.setText(null);
      this.hourField.setText(null);
      this.minuteField.setText(null);
    }
  }

  /**
   * Return the list of all {@link UpdateListener}s.
   */
  public List<UpdateListener> getUpdateListeners() {
    return this.updateListeners;
  }

  /**
   * Notify all {@link UpdateListener}s of an update.
   */
  private void notifyListeners() {
    this.updateListeners.forEach(UpdateListener::onUpdate);
  }

  /**
   * Interface for listeners to fields updates.
   */
  @FunctionalInterface
  public interface UpdateListener {
    void onUpdate();
  }
}
