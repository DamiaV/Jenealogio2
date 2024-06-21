package net.darmo_creations.jenealogio2.ui.components;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.datetime.*;
import net.darmo_creations.jenealogio2.model.datetime.calendar.*;
import net.darmo_creations.jenealogio2.ui.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * A JavaFX component containing two date fields.
 * It returns a {@link DateTime} from the fields’ values.
 */
public class CalendarDateField extends HBox {
  private final Config config;
  private final DateTimeField dateTimeField;
  private final DateTimeField secondDateTimeField;
  private final Label label = new Label();

  private DateType dateType;

  private final List<UpdateListener> updateListeners = new LinkedList<>();

  /**
   * Create a field with the type {@link DateType#EXACT}.
   *
   * @param config The app’s config.
   */
  public CalendarDateField(final @NotNull Config config) {
    super(4);
    this.config = Objects.requireNonNull(config);
    this.dateTimeField = new DateTimeField(config);
    this.secondDateTimeField = new DateTimeField(config);
    this.getChildren().addAll(this.dateTimeField, this.label, this.secondDateTimeField);
    HBox.setMargin(this.label, new Insets(4, 0, 0, 0));
    this.setDateType(DateType.EXACT);
    this.dateTimeField.getUpdateListeners().add(this::notifyListeners);
    this.secondDateTimeField.getUpdateListeners().add(this::notifyListeners);
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
   * Check whether there is any invalid data in this component.
   *
   * @return True if there is none, false otherwise.
   */
  public boolean checkValidity() {
    this.dateTimeField.pseudoClassStateChanged(PseudoClasses.INVALID, false);
    this.secondDateTimeField.pseudoClassStateChanged(PseudoClasses.INVALID, false);
    CalendarSpecificDateTime date = this.dateTimeField.getDate();
    CalendarSpecificDateTime secondDate = this.secondDateTimeField.getDate();
    boolean invalid = false;
    if (this.dateType.requiresTwoFields()) {
      if (date == null && secondDate != null) {
        this.dateTimeField.pseudoClassStateChanged(PseudoClasses.INVALID, true);
        invalid = true;
      } else if (date != null && secondDate == null) {
        this.secondDateTimeField.pseudoClassStateChanged(PseudoClasses.INVALID, true);
        invalid = true;
      }
    }
    return !invalid;
  }

  /**
   * Return a {@link DateTime} object from the date fields.
   */
  public Optional<DateTime> getDate() {
    CalendarSpecificDateTime date = this.dateTimeField.getDate();
    CalendarSpecificDateTime secondDate = this.secondDateTimeField.getDate();
    if (date == null || secondDate == null && this.dateType.requiresTwoFields()) {
      return Optional.empty();
    }
    if (this.dateType.requiresTwoFields() && date.toISO8601Date().isAfter(secondDate.toISO8601Date())) {
      // Swap dates if wrong way around
      CalendarSpecificDateTime d = date;
      date = secondDate;
      secondDate = d;
    }
    return Optional.of(switch (this.dateType) {
      case EXACT -> new DateTimeWithPrecision(date, DateTimePrecision.EXACT);
      case ABOUT -> new DateTimeWithPrecision(date, DateTimePrecision.ABOUT);
      case POSSIBLY -> new DateTimeWithPrecision(date, DateTimePrecision.POSSIBLY);
      case BEFORE -> new DateTimeWithPrecision(date, DateTimePrecision.BEFORE);
      case AFTER -> new DateTimeWithPrecision(date, DateTimePrecision.AFTER);
      case OR -> //noinspection DataFlowIssue
          new DateTimeAlternative(date, secondDate);
      case BETWEEN -> //noinspection DataFlowIssue
          new DateTimeRange(date, secondDate);
    });
  }

  /**
   * Set the value of date fields from the given {@link DateTime}.
   *
   * @param date Date object to extract data from.
   * @throws IllegalArgumentException If the date’s concrete type is not compatible with the currently set date type.
   */
  public void setDate(DateTime date) {
    if (date == null) {
      this.dateTimeField.setDate(null);
      this.secondDateTimeField.setDate(null);
      return;
    }
    DateType type = DateType.fromDate(date);
    if (type != this.dateType) {
      throw new IllegalArgumentException("expected date of type %s, got %s".formatted(this.dateType, type));
    }
    this.dateTimeField.setDate(date.date());
    if (date instanceof DateTimeAlternative d) {
      this.secondDateTimeField.setDate(d.latestDate());
    } else if (date instanceof DateTimeRange d) {
      this.secondDateTimeField.setDate(d.endDate());
    } else {
      this.secondDateTimeField.setDate(null);
    }
  }

  /**
   * Set the date type.
   *
   * @param dateType The date type.
   */
  public void setDateType(@NotNull DateType dateType) {
    this.dateType = Objects.requireNonNull(dateType);
    Language language = this.config.language();
    if (dateType == DateType.OR) {
      this.label.setText(language.translate("date_field.or"));
      this.label.setVisible(true);
      this.secondDateTimeField.setVisible(true);
    } else if (dateType == DateType.BETWEEN) {
      this.label.setText(language.translate("date_field.and"));
      this.label.setVisible(true);
      this.secondDateTimeField.setVisible(true);
    } else {
      this.label.setVisible(false);
      this.secondDateTimeField.setVisible(false);
    }
  }

  /**
   * The type of date this field represents.
   */
  public enum DateType {
    /**
     * Setup fields for {@link DateTimeWithPrecision} class with precision {@link DateTimePrecision#EXACT}.
     */
    EXACT(false),
    /**
     * Setup fields for {@link DateTimeWithPrecision} class with precision {@link DateTimePrecision#ABOUT}.
     */
    ABOUT(false),
    /**
     * Setup fields for {@link DateTimeWithPrecision} class with precision {@link DateTimePrecision#POSSIBLY}.
     */
    POSSIBLY(false),
    /**
     * Setup fields for {@link DateTimeWithPrecision} class with precision {@link DateTimePrecision#BEFORE}.
     */
    BEFORE(false),
    /**
     * Setup fields for {@link DateTimeWithPrecision} class with precision {@link DateTimePrecision#AFTER}.
     */
    AFTER(false),
    /**
     * Setup fields for {@link DateTimeAlternative} class.
     */
    OR(true),
    /**
     * Setup fields for {@link DateTimeRange} class.
     */
    BETWEEN(true),
    ;

    private final String key;
    private final boolean requiresTwoFields;

    DateType(boolean requiresTwoFields) {
      this.requiresTwoFields = requiresTwoFields;
      this.key = "date_field.precision." + this.name().toLowerCase();
    }

    /**
     * The translation key.
     */
    public String key() {
      return this.key;
    }

    /**
     * Indicates whether this date type requires two date fields.
     */
    public boolean requiresTwoFields() {
      return this.requiresTwoFields;
    }

    /**
     * Return the date type corresponding to the given concrete date class.
     *
     * @param date A date object.
     * @return The corresponding date type.
     */
    public static DateType fromDate(@NotNull DateTime date) {
      Objects.requireNonNull(date);
      if (date instanceof DateTimeWithPrecision d) {
        return switch (d.precision()) {
          case EXACT -> EXACT;
          case ABOUT -> ABOUT;
          case POSSIBLY -> POSSIBLY;
          case BEFORE -> BEFORE;
          case AFTER -> AFTER;
        };
      }
      if (date instanceof DateTimeAlternative) {
        return OR;
      }
      if (date instanceof DateTimeRange) {
        return BETWEEN;
      }
      throw new IllegalArgumentException();
    }
  }

  /**
   * Interface for listeners to date field updates.
   */
  @FunctionalInterface
  public interface UpdateListener {
    void onUpdate();
  }
}
