package net.darmo_creations.jenealogio2.ui.components;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.converter.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.datetime.calendar.Calendar;
import net.darmo_creations.jenealogio2.model.datetime.calendar.*;
import net.time4j.engine.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * JavaFX component acting as a date-time text input.
 * <p>
 * It is composed of 5 text fields, one for each date component:
 * year, month, day, hours, and minutes.
 */
public class DateTimeField extends VBox {
  private static final Calendar<?>[] CALENDARS = {
      Calendars.GREGORIAN,
      Calendars.JULIAN,
      Calendars.FRENCH_REPUBLICAN_CALENDAR,
      Calendars.FRENCH_REPUBLICAN_DECIMAL_CALENDAR,
      Calendars.HEBREW,
      Calendars.SOLAR_HIJRI,
      Calendars.INDIAN,
      Calendars.MINGUO,
      Calendars.THAI_SOLAR,
      Calendars.COPTIC,
      Calendars.ETHIOPIAN,
  };

  private final Config config;
  private final TextField yearField = new TextField();
  private final ComboBox<String> monthField = new ComboBox<>();
  private final TextField dayField = new TextField();
  private final TextField hourField = new TextField();
  private final TextField minuteField = new TextField();
  private final ComboBox<NotNullComboBoxItem<CalendarEra>> calendarEraField = new ComboBox<>();
  private final ComboBox<NotNullComboBoxItem<Calendar<?>>> calendarField = new ComboBox<>();

  private final List<UpdateListener> updateListeners = new LinkedList<>();

  /**
   * Create an empty date-time field.
   *
   * @param config The app’s config.
   */
  public DateTimeField(final @NotNull Config config) {
    super(5);
    this.config = config;

    this.setupField(this.yearField, 50, "year", true);
    this.monthField.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> this.notifyListeners());
    this.setupField(this.dayField, 40, "day", false);
    this.setupField(this.hourField, 40, "hour", false);
    this.setupField(this.minuteField, 40, "minute", false);
    this.calendarEraField.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) ->
            this.notifyListeners());

    final Language language = this.config.language();
    for (final Calendar<?> calendar : CALENDARS)
      this.calendarField.getItems().add(new NotNullComboBoxItem<>(
          calendar,
          language.translate("calendar.%s.name".formatted(calendar.name()))
      ));
    this.calendarField.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) ->
            this.onCalendarChange(newValue.data()));
    this.calendarField.getSelectionModel().select(0);

    final Label colonLabel = new Label(":");
    HBox.setMargin(colonLabel, new Insets(5, 0, 0, 0));
    final Pane spacer = new Pane();
    spacer.setPrefWidth(5);

    final HBox fieldsBox = new HBox(5,
        this.dayField,
        this.monthField,
        this.yearField,
        spacer,
        this.hourField,
        colonLabel,
        this.minuteField,
        this.calendarEraField
    );

    final HBox calendarBox = new HBox(
        5,
        new Label(language.translate("date_time_field.calendar")),
        this.calendarField
    );
    calendarBox.setAlignment(Pos.CENTER_LEFT);
    this.getChildren().addAll(fieldsBox, calendarBox);
  }

  /**
   * Setup the given text field as accepting only positive integers.
   *
   * @param textField Text field to setup.
   * @param width     Field’s width.
   * @param name      Field’s name.
   */
  private void setupField(@NotNull TextField textField, int width, @NotNull String name, boolean allowNegative) {
    final String regex = allowNegative ? "^-?\\d*$" : "^\\d*$";
    textField.setTextFormatter(new TextFormatter<>(
        new IntegerStringConverter(),
        null,
        change -> change.getControlNewText().matches(regex) ? change : null
    ));
    textField.setPrefWidth(width);
    textField.setPromptText(this.config.language().translate("date_time_field.%s.prompt".formatted(name)));
    textField.textProperty()
        .addListener((observable, oldValue, newValue) -> this.notifyListeners());
  }

  /**
   * Return the {@link CalendarSpecificDateTime} value corresponding to the field’s values.
   */
  public @Nullable CalendarSpecificDateTime getDate() {
    try {
      final int y = Integer.parseInt(this.yearField.getText());
      final int m = this.monthField.getSelectionModel().getSelectedIndex() + 1;
      final int d = Integer.parseInt(this.dayField.getText());
      final Integer h = this.getFieldValue(this.hourField);
      final Integer mi = this.getFieldValue(this.minuteField);
      final Calendar<?> calendar = this.getSelectedCalendar();
      CalendarEra era = null;
      if (this.calendarEraField.isVisible())
        era = this.calendarEraField.getSelectionModel().getSelectedItem().data();
      return calendar.getDate(era, y, m, d, h, mi);
    } catch (final RuntimeException e) {
      return null;
    }
  }

  private @Nullable Integer getFieldValue(final @NotNull TextField field) {
    final String text = field.getText();
    return text == null || text.isEmpty() ? null : Integer.parseInt(text);
  }

  /**
   * Set the date-time value of this field.
   *
   * @param dateTime The date-time value.
   */
  public void setDate(CalendarSpecificDateTime dateTime) {
    if (dateTime != null) {
      final Calendar<?> calendar = dateTime.calendar();
      this.calendarField.getSelectionModel().select(new NotNullComboBoxItem<>(calendar));
      this.yearField.setText(String.valueOf(dateTime.year()));
      this.monthField.getSelectionModel().select(dateTime.month() - 1);
      this.dayField.setText(String.valueOf(dateTime.dayOfMonth()));
      this.hourField.setText(dateTime.hour().map(String::valueOf).orElse(null));
      this.minuteField.setText(dateTime.minute().map(String::valueOf).orElse(null));
      dateTime.era().ifPresent(era ->
          this.calendarEraField.getSelectionModel().select(new NotNullComboBoxItem<>(era)));
    } else {
      this.yearField.setText(null);
      this.monthField.getSelectionModel().select(0);
      this.dayField.setText(null);
      this.hourField.setText(null);
      this.minuteField.setText(null);
      if (!this.getSelectedCalendar().eras().isEmpty())
        this.calendarEraField.getSelectionModel().select(0);
    }
  }

  private Calendar<?> getSelectedCalendar() {
    return this.calendarField.getSelectionModel().getSelectedItem().data();
  }

  /**
   * Update the items of {@link #monthField} and {@link #calendarEraField} using the given calendar.
   *
   * @param calendar Calendar to get months from.
   */
  private void onCalendarChange(@NotNull Calendar<?> calendar) {
    this.monthField.getItems().clear();
    final Language language = this.config.language();
    final String name = calendar.name();
    for (int i = 1; i <= calendar.lengthOfYearInMonths(); i++)
      this.monthField.getItems().add(language.translate("calendar.%s.month.%d".formatted(name, i)));
    this.monthField.getSelectionModel().select(0);

    this.calendarEraField.getItems().clear();
    if (!calendar.eras().isEmpty()) {
      this.calendarEraField.setVisible(true);
      calendar.eras().forEach(era -> {
        final String text = language.translate("calendar.%s.era.%s"
            .formatted(calendar.name(), era.name().toLowerCase()));
        this.calendarEraField.getItems().add(new NotNullComboBoxItem<>(era, text));
      });
      this.calendarEraField.getSelectionModel().select(0);
    } else
      this.calendarEraField.setVisible(false);
    this.notifyListeners();
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
