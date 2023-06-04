package net.darmo_creations.jenealogio2.ui.components;

import javafx.geometry.Insets;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import net.darmo_creations.jenealogio2.App;
import net.darmo_creations.jenealogio2.config.Language;
import net.darmo_creations.jenealogio2.model.calendar.*;
import net.darmo_creations.jenealogio2.ui.PseudoClasses;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DateField extends HBox {
  private final DatePicker datePicker = new DatePicker();
  private final Label label = new Label();
  private final DatePicker secondDatePicker = new DatePicker();

  private DateType dateType;

  private final List<UpdateListener> updateListeners = new LinkedList<>();

  public DateField() {
    super(4);
    this.getChildren().addAll(this.datePicker, this.label, this.secondDatePicker);
    HBox.setMargin(this.label, new Insets(4, 0, 0, 0));
    this.setDateType(DateType.EXACT);
    this.datePicker.valueProperty().addListener((observable, oldValue, newValue) -> this.notifyListeners());
    this.secondDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> this.notifyListeners());
  }

  public List<UpdateListener> getUpdateListeners() {
    return this.updateListeners;
  }

  private void notifyListeners() {
    this.updateListeners.forEach(UpdateListener::onUpdate);
  }

  /**
   * Check whether there is any invalid data in this form.
   *
   * @return True if there is none, false otherwise.
   */
  public boolean checkValidity() {
    this.datePicker.pseudoClassStateChanged(PseudoClasses.INVALID, false);
    this.secondDatePicker.pseudoClassStateChanged(PseudoClasses.INVALID, false);
    LocalDate date = this.datePicker.getValue();
    LocalDate secondDate = this.secondDatePicker.getValue();
    boolean invalid = false;
    if (this.dateType.requiresTwoFields()) {
      if (date == null && secondDate != null) {
        this.datePicker.pseudoClassStateChanged(PseudoClasses.INVALID, true);
        invalid = true;
      } else if (date != null && secondDate == null) {
        this.secondDatePicker.pseudoClassStateChanged(PseudoClasses.INVALID, true);
        invalid = true;
      }
    }
    return !invalid;
  }

  public Optional<CalendarDate> getDate() {
    LocalDate date = this.datePicker.getValue();
    LocalDate secondDate = this.secondDatePicker.getValue();
    if ((date == null ^ secondDate == null) && this.dateType.requiresTwoFields()) {
      throw new IllegalArgumentException("missing date");
    }
    if (date == null) {
      return Optional.empty();
    }
    if (secondDate != null && date.isAfter(secondDate)) {
      // Swap dates if wrong way around
      LocalDate d = date;
      date = secondDate;
      secondDate = d;
    }
    final LocalDateTime dateTime = date.atStartOfDay();
    return Optional.of(switch (this.dateType) {
      case EXACT -> new DateWithPrecision(dateTime, DatePrecision.EXACT);
      case ABOUT -> new DateWithPrecision(dateTime, DatePrecision.ABOUT);
      case POSSIBLY -> new DateWithPrecision(dateTime, DatePrecision.POSSIBLY);
      case BEFORE -> new DateWithPrecision(dateTime, DatePrecision.BEFORE);
      case AFTER -> new DateWithPrecision(dateTime, DatePrecision.AFTER);
      case OR -> //noinspection DataFlowIssue
          new DateAlternative(dateTime, secondDate.atStartOfDay());
      case BETWEEN -> //noinspection DataFlowIssue
          new DateRange(dateTime, secondDate.atStartOfDay());
    });
  }

  public void setDate(CalendarDate date) {
    if (date == null) {
      this.datePicker.setValue(null);
      this.secondDatePicker.setValue(null);
      return;
    }
    DateType type = DateType.fromDate(date);
    if (type != this.dateType) {
      throw new IllegalArgumentException("expected date of type %s, got %s".formatted(this.dateType, type));
    }
    this.datePicker.setValue(date.date().toLocalDate());
    if (date instanceof DateAlternative d) {
      this.secondDatePicker.setValue(d.latestDate().toLocalDate());
    } else if (date instanceof DateRange d) {
      this.secondDatePicker.setValue(d.endDate().toLocalDate());
    } else {
      this.secondDatePicker.setValue(null);
    }
  }

  public void setDateType(@NotNull DateType dateType) {
    this.dateType = Objects.requireNonNull(dateType);
    Language language = App.config().language();
    if (dateType == DateType.OR) {
      this.label.setText(language.translate("date_field.or"));
      this.label.setVisible(true);
      this.secondDatePicker.setVisible(true);
    } else if (dateType == DateType.BETWEEN) {
      this.label.setText(language.translate("date_field.and"));
      this.label.setVisible(true);
      this.secondDatePicker.setVisible(true);
    } else {
      this.label.setVisible(false);
      this.secondDatePicker.setVisible(false);
    }
  }

  public enum DateType {
    EXACT(false),
    ABOUT(false),
    POSSIBLY(false),
    BEFORE(false),
    AFTER(false),
    OR(true),
    BETWEEN(true),
    ;

    private final String key;
    private final boolean requiresTwoFields;

    DateType(boolean requiresTwoFields) {
      this.requiresTwoFields = requiresTwoFields;
      this.key = "date_field.precision." + this.name().toLowerCase();
    }

    public String key() {
      return this.key;
    }

    public boolean requiresTwoFields() {
      return this.requiresTwoFields;
    }

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

  @FunctionalInterface
  public interface UpdateListener {
    void onUpdate();
  }
}
