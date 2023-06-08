package net.darmo_creations.jenealogio2.ui.components;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import net.darmo_creations.jenealogio2.App;
import net.darmo_creations.jenealogio2.config.Language;
import net.darmo_creations.jenealogio2.model.calendar.*;
import net.darmo_creations.jenealogio2.ui.PseudoClasses;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A JavaFX component containing two date fields.
 * It returns a {@link CalendarDate} from the fields’ values.
 */
public class CalendarDateField extends HBox {
  private final DateTimeField dateTimeField = new DateTimeField();
  private final DateTimeField secondDateTimeField = new DateTimeField();
  private final Label label = new Label();

  private DateType dateType;

  private final List<UpdateListener> updateListeners = new LinkedList<>();

  /**
   * Create a field with the type {@link DateType#EXACT}.
   */
  public CalendarDateField() {
    super(4);
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
    LocalDateTime date = this.dateTimeField.getDate();
    LocalDateTime secondDate = this.secondDateTimeField.getDate();
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
   * Return a {@link CalendarDate} object from the date fields.
   */
  public Optional<CalendarDate> getDate() {
    LocalDateTime date = this.dateTimeField.getDate();
    LocalDateTime secondDate = this.secondDateTimeField.getDate();
    if ((date == null ^ secondDate == null) && this.dateType.requiresTwoFields()) {
      throw new IllegalArgumentException("missing date");
    }
    if (date == null) {
      return Optional.empty();
    }
    if (secondDate != null && date.isAfter(secondDate)) {
      // Swap dates if wrong way around
      LocalDateTime d = date;
      date = secondDate;
      secondDate = d;
    }
    return Optional.of(switch (this.dateType) {
      case EXACT -> new DateWithPrecision(date, DatePrecision.EXACT);
      case ABOUT -> new DateWithPrecision(date, DatePrecision.ABOUT);
      case POSSIBLY -> new DateWithPrecision(date, DatePrecision.POSSIBLY);
      case BEFORE -> new DateWithPrecision(date, DatePrecision.BEFORE);
      case AFTER -> new DateWithPrecision(date, DatePrecision.AFTER);
      case OR -> //noinspection DataFlowIssue
          new DateAlternative(date, secondDate);
      case BETWEEN -> //noinspection DataFlowIssue
          new DateRange(date, secondDate);
    });
  }

  /**
   * Set the value of date fields from the given {@link CalendarDate}.
   *
   * @param date Date object to extract data from.
   * @throws IllegalArgumentException If the date’s concrete type is not compatible with the currently set date type.
   */
  public void setDate(CalendarDate date) {
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
    if (date instanceof DateAlternative d) {
      this.secondDateTimeField.setDate(d.latestDate());
    } else if (date instanceof DateRange d) {
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
    Language language = App.config().language();
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
     * Setup fields for {@link DateWithPrecision} class with precision {@link DatePrecision#EXACT}.
     */
    EXACT(false),
    /**
     * Setup fields for {@link DateWithPrecision} class with precision {@link DatePrecision#ABOUT}.
     */
    ABOUT(false),
    /**
     * Setup fields for {@link DateWithPrecision} class with precision {@link DatePrecision#POSSIBLY}.
     */
    POSSIBLY(false),
    /**
     * Setup fields for {@link DateWithPrecision} class with precision {@link DatePrecision#BEFORE}.
     */
    BEFORE(false),
    /**
     * Setup fields for {@link DateWithPrecision} class with precision {@link DatePrecision#AFTER}.
     */
    AFTER(false),
    /**
     * Setup fields for {@link DateAlternative} class.
     */
    OR(true),
    /**
     * Setup fields for {@link DateRange} class.
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
    public static DateType fromDate(@NotNull CalendarDate date) {
      Objects.requireNonNull(date);
      if (date instanceof DateWithPrecision d) {
        return switch (d.precision()) {
          case EXACT -> EXACT;
          case ABOUT -> ABOUT;
          case POSSIBLY -> POSSIBLY;
          case BEFORE -> BEFORE;
          case AFTER -> AFTER;
        };
      }
      if (date instanceof DateAlternative) {
        return OR;
      }
      if (date instanceof DateRange) {
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
