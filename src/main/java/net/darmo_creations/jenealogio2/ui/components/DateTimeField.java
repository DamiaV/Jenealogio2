package net.darmo_creations.jenealogio2.ui.components;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.converter.*;
import net.darmo_creations.jenealogio2.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.datetime.calendar.Calendar;
import net.darmo_creations.jenealogio2.model.datetime.calendar.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * JavaFX component acting as a date-time text input.
 * <p>
 * It is composed of 5 text fields, one for each date component:
 * year, month, day, hours, and minutes.
 */
public class DateTimeField extends HBox {
  private static final Calendar<?>[] CALENDARS = {
      Calendars.GREGORIAN,
      Calendars.JULIAN,
      Calendars.FRENCH_REPUBLICAN_DECIMAL_CALENDAR,
      Calendars.FRENCH_REPUBLICAN_CALENDAR,
      Calendars.COPTIC,
      Calendars.ETHIOPIAN,
  };

  private final TextField yearField = new TextField();
  private final ComboBox<String> monthField = new ComboBox<>();
  private final TextField dayField = new TextField();
  private final TextField hourField = new TextField();
  private final TextField minuteField = new TextField();
  private final ComboBox<NotNullComboBoxItem<Calendar<?>>> calendarField = new ComboBox<>();

  private final List<UpdateListener> updateListeners = new LinkedList<>();

  /**
   * Create an empty date-time field.
   */
  public DateTimeField() {
    super(4);

    this.setupField(this.yearField, 50, "year", true);
    this.monthField.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> this.notifyListeners());
    this.setupField(this.dayField, 40, "day", false);
    this.setupField(this.hourField, 40, "hour", false);
    this.setupField(this.minuteField, 40, "minute", false);

    Language language = App.config().language();
    for (Calendar<?> calendar : CALENDARS) {
      this.calendarField.getItems()
          .add(new NotNullComboBoxItem<>(calendar, language.translate("calendar.%s.name".formatted(calendar.name()))));
    }
    this.calendarField.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> this.updateMonths(newValue.data()));
    this.calendarField.getSelectionModel().select(0);

    Label colonLabel = new Label(":");
    HBox.setMargin(colonLabel, new Insets(4, 0, 0, 0));
    Pane spacer = new Pane();
    spacer.setPrefWidth(4);

    HBox fieldsBox = new HBox(4,
        this.dayField,
        this.monthField,
        this.yearField,
        spacer,
        this.hourField,
        colonLabel,
        this.minuteField
    );

    this.getChildren().add(new VBox(4, fieldsBox, this.calendarField));
  }

  /**
   * Setup the given text field as accepting only positive integers.
   *
   * @param textField Text field to setup.
   * @param width     Field’s width.
   * @param name      Field’s name.
   */
  private void setupField(@NotNull TextField textField, int width, @NotNull String name, boolean allowNegative) {
    String regex = allowNegative ? "^-?\\d*$" : "^\\d*$";
    textField.setTextFormatter(new TextFormatter<>(
        new IntegerStringConverter(),
        null,
        change -> change.getControlNewText().matches(regex) ? change : null
    ));
    textField.setPrefWidth(width);
    textField.setPromptText(App.config().language().translate("date_time_field.%s.prompt".formatted(name)));
    textField.textProperty().addListener((observable, oldValue, newValue) -> this.notifyListeners());
  }

  /**
   * Return the {@link CalendarSpecificDateTime} value corresponding to the field’s values.
   */
  public @Nullable CalendarSpecificDateTime getDate() {
    try {
      int y = Integer.parseInt(this.yearField.getText());
      int m = this.monthField.getSelectionModel().getSelectedIndex() + 1;
      int d = Integer.parseInt(this.dayField.getText());
      Integer h = this.getFieldValue(this.hourField);
      Integer mi = this.getFieldValue(this.minuteField);
      Calendar<?> calendar = this.calendarField.getSelectionModel().getSelectedItem().data();
      return calendar.getDate(y, m, d, h, mi);
    } catch (RuntimeException e) {
      return null;
    }
  }

  private @Nullable Integer getFieldValue(final @NotNull TextField field) {
    String text = field.getText();
    return text == null || text.isEmpty() ? null : Integer.parseInt(text);
  }

  /**
   * Set the date-time value of this field.
   *
   * @param dateTime The date-time value.
   */
  public void setDate(CalendarSpecificDateTime dateTime) {
    if (dateTime != null) {
      this.calendarField.getSelectionModel().select(new NotNullComboBoxItem<>(dateTime.calendar()));
      this.yearField.setText(String.valueOf(dateTime.year()));
      this.monthField.getSelectionModel().select(dateTime.month() - 1);
      this.dayField.setText(String.valueOf(dateTime.dayOfMonth()));
      this.hourField.setText(dateTime.hour().map(String::valueOf).orElse(null));
      this.minuteField.setText(dateTime.minute().map(String::valueOf).orElse(null));
    } else {
      this.yearField.setText(null);
      this.monthField.getSelectionModel().select(0);
      this.dayField.setText(null);
      this.hourField.setText(null);
      this.minuteField.setText(null);
    }
  }

  /**
   * Update {@link #monthField}’s items using the given calendar.
   *
   * @param calendar Calendar to get months from.
   */
  private void updateMonths(@NotNull Calendar<?> calendar) {
    this.monthField.getItems().clear();
    Language language = App.config().language();
    String name = calendar.name();
    for (int i = 1; i <= calendar.lengthOfYearInMonths(); i++) {
      this.monthField.getItems().add(language.translate("calendar.%s.month.%d".formatted(name, i)));
    }
    this.monthField.getSelectionModel().select(0);
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
