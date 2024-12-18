package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.datetime.*;
import net.darmo_creations.jenealogio2.themes.*;
import net.darmo_creations.jenealogio2.ui.*;
import net.darmo_creations.jenealogio2.ui.components.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * A dialog that allows selecting a {@link DateTime} value.
 */
public class DateSelectionDialog extends DialogBase<DateTime> {
  private final ComboBox<NotNullComboBoxItem<DateType>> datePrecisionCombo = new ComboBox<>();

  private DateType dateType;

  private final VBox fieldsBox;
  // For date types that require 1 or 2 dates
  private final DateTimeField dateTimeField1;
  private final Label dateTimeLabel1 = new Label();
  // For date types that require 2 dates
  private final DateTimeField dateTimeField2;
  private final Label dateTimeLabel2 = new Label();
  // For date types that accept more than 2 dates
  private final VBox listBox;
  private final Button addDateButton;
  private final Button removeDateButton;
  private final ListView<DateTimeField> dateTimeFieldList = new ListView<>();
  private final Label errorLabel = new Label();

  /**
   * Create a new date selection dialog.
   *
   * @param config The app’s config.
   */
  public DateSelectionDialog(final @NotNull Config config) {
    super(config, "select_date", true, ButtonTypes.OK, ButtonTypes.CANCEL);

    final Language language = config.language();
    final Theme theme = config.theme();

    this.datePrecisionCombo.getSelectionModel().selectedItemProperty().addListener(
        (observable, oldValue, newValue) -> this.onDateTypeUpdate(newValue.data()));
    this.populateDatePrecisionCombo();

    this.dateTimeField1 = new DateTimeField(config);
    this.dateTimeField1.getUpdateListeners().add(this::checkValidity);

    this.dateTimeField2 = new DateTimeField(config);
    this.dateTimeField2.getUpdateListeners().add(this::checkValidity);
    this.dateTimeField2.managedProperty().bind(this.dateTimeField2.visibleProperty());

    this.dateTimeLabel1.setText(language.translate("dialog.select_date.start_date"));
    this.dateTimeLabel2.setText(language.translate("dialog.select_date.end_date"));

    this.fieldsBox = new VBox(
        5,
        new HBox(5, this.dateTimeField1, this.dateTimeLabel1),
        new HBox(5, this.dateTimeField2, this.dateTimeLabel2)
    );
    VBox.setVgrow(this.fieldsBox, Priority.ALWAYS);
    this.fieldsBox.managedProperty().bind(this.fieldsBox.visibleProperty());

    this.addDateButton = new Button(null, theme.getIcon(Icon.ADD_DATE, Icon.Size.SMALL));
    this.addDateButton.setTooltip(new Tooltip(language.translate("dialog.select_date.add_date.tooltip")));
    this.addDateButton.setOnAction(event -> {
      this.dateTimeFieldList.getItems().add(this.newItem());
      this.checkValidity();
    });
    this.removeDateButton = new Button(null, theme.getIcon(Icon.REMOVE_DATE, Icon.Size.SMALL));
    this.removeDateButton.setTooltip(new Tooltip(language.translate("dialog.select_date.remove_date.tooltip")));
    this.removeDateButton.setOnAction(event -> {
      final DateTimeField selectedItem = this.dateTimeFieldList.getSelectionModel().getSelectedItem();
      if (selectedItem != null) {
        this.dateTimeFieldList.getItems().remove(selectedItem);
        this.checkValidity();
      }
    });
    this.dateTimeFieldList.getSelectionModel().selectedItemProperty().addListener(
        (observable, oldValue, newValue) -> this.updateButtons());
    this.dateTimeFieldList.getItems().addListener(
        (ListChangeListener<? super DateTimeField>) c -> this.updateButtons());
    VBox.setVgrow(this.dateTimeFieldList, Priority.ALWAYS);
    this.dateTimeFieldList.setPrefWidth(400);
    this.dateTimeFieldList.setPrefHeight(300);

    final HBox buttonsBox = new HBox(
        5,
        this.addDateButton,
        this.removeDateButton,
        new Label(language.translate("dialog.select_date.list_description", new FormatArg("nb", DateTimeAlternative.MAX_DATES)))
    );
    buttonsBox.setAlignment(Pos.CENTER_LEFT);
    this.listBox = new VBox(5, buttonsBox, this.dateTimeFieldList);
    this.listBox.managedProperty().bind(this.listBox.visibleProperty());

    this.errorLabel.setGraphic(theme.getIcon(Icon.WARNING, Icon.Size.SMALL));

    final HBox dateTypeBox = new HBox(
        5,
        new Label(language.translate("dialog.select_date.date_type")),
        this.datePrecisionCombo
    );
    dateTypeBox.setAlignment(Pos.CENTER_LEFT);
    this.getDialogPane().setContent(new VBox(
        5,
        dateTypeBox,
        this.fieldsBox,
        this.listBox,
        this.errorLabel
    ));

    final Stage stage = this.stage();
    stage.setMinWidth(500);
    stage.setMinHeight(300);

    this.setResultConverter(b -> {
      if (b.getButtonData().isCancelButton())
        return null;
      return this.getDate();
    });
  }

  /**
   * Set the {@link DateTime} value to show.
   *
   * @param dateTime A {@link DateTime} value. May be null.
   */
  public void setDateTime(DateTime dateTime) {
    if (dateTime != null) {
      this.datePrecisionCombo.getSelectionModel().select(new NotNullComboBoxItem<>(DateType.fromDate(dateTime)));
      this.resetFields(); // Must be called after select() so that this.dateType is correctly updated.
      if (dateTime instanceof DateTimeWithPrecision d)
        this.dateTimeField1.setDate(d.date());
      else if (dateTime instanceof DateTimeRange d) {
        this.dateTimeField1.setDate(d.startDate());
        this.dateTimeField2.setDate(d.endDate());
      } else if (dateTime instanceof DateTimeAlternative d) {
        this.dateTimeFieldList.getItems().clear();
        d.dates().forEach(date -> {
          final DateTimeField f = this.newItem();
          this.dateTimeFieldList.getItems().add(f);
          f.setDate(date);
        });
      }
    } else {
      this.datePrecisionCombo.getSelectionModel().select(0);
      this.resetFields(); // Must be called after select() so that this.dateType is correctly updated.
    }
  }

  private void resetFields() {
    this.dateTimeField1.setDate(null);
    this.dateTimeField2.setDate(null);
    this.dateTimeFieldList.getItems().clear();
    this.dateTimeFieldList.getItems().addAll(this.newItem(), this.newItem());
  }

  private DateTimeField newItem() {
    final DateTimeField dateTimeField = new DateTimeField(this.config);
    dateTimeField.getUpdateListeners().add(this::checkValidity);
    return dateTimeField;
  }

  /**
   * Return a {@link DateTime} object from the date fields.
   * Does not check for null values.
   */
  @SuppressWarnings("DataFlowIssue")
  private @Nullable DateTime getDate() {
    return switch (this.dateType) {
      case EXACT, ABOUT, POSSIBLY, BEFORE, AFTER ->
          new DateTimeWithPrecision(this.dateTimeField1.getDate(), this.dateType.precision());
      case RANGE -> new DateTimeRange(this.dateTimeField1.getDate(), this.dateTimeField2.getDate());
      case ALTERNATIVE ->
          new DateTimeAlternative(this.dateTimeFieldList.getItems().stream().map(DateTimeField::getDate).toList());
    };
  }

  /**
   * Check whether there is any invalid data and update the dialog’s state.
   */
  private void checkValidity() {
    this.dateTimeField1.pseudoClassStateChanged(PseudoClasses.INVALID, false);
    this.dateTimeField2.pseudoClassStateChanged(PseudoClasses.INVALID, false);
    this.dateTimeFieldList.pseudoClassStateChanged(PseudoClasses.INVALID, false);
    boolean invalid = false;
    switch (this.dateType) {
      case EXACT, ABOUT, POSSIBLY, BEFORE, AFTER -> {
        if (this.dateTimeField1.getDate() == null) {
          this.dateTimeField1.pseudoClassStateChanged(PseudoClasses.INVALID, true);
          this.errorLabel.setText(this.config.language().translate("dialog.select_date.error.invalid_date"));
          invalid = true;
        }
      }
      case RANGE -> {
        final var date1 = this.dateTimeField1.getDate();
        final var date2 = this.dateTimeField2.getDate();
        if (date1 == null) {
          this.dateTimeField1.pseudoClassStateChanged(PseudoClasses.INVALID, true);
          this.errorLabel.setText(this.config.language().translate("dialog.select_date.error.invalid_date"));
          invalid = true;
        } else if (date2 == null) {
          this.dateTimeField2.pseudoClassStateChanged(PseudoClasses.INVALID, true);
          this.errorLabel.setText(this.config.language().translate("dialog.select_date.error.invalid_date_2"));
          invalid = true;
        } else if (date1.compareTo(date2) >= 0) {
          this.dateTimeField1.pseudoClassStateChanged(PseudoClasses.INVALID, true);
          this.dateTimeField2.pseudoClassStateChanged(PseudoClasses.INVALID, true);
          this.errorLabel.setText(this.config.language().translate("dialog.select_date.error.invalid_dates"));
          invalid = true;
        }
      }
      case ALTERNATIVE -> {
        final int size = this.dateTimeFieldList.getItems().size();
        invalid = size < 2 || size > DateTimeAlternative.MAX_DATES
            || this.dateTimeFieldList.getItems().stream().anyMatch(d -> d.getDate() == null);
        if (invalid) {
          this.dateTimeFieldList.pseudoClassStateChanged(PseudoClasses.INVALID, true);
          this.errorLabel.setText(this.config.language().translate("dialog.select_date.error.invalid_date_list"));
        }
      }
    }
    this.errorLabel.setVisible(invalid);
    this.getDialogPane().lookupButton(ButtonTypes.OK).setDisable(invalid);
  }

  private void updateButtons() {
    final int size = this.dateTimeFieldList.getItems().size();
    this.addDateButton.setDisable(size >= DateTimeAlternative.MAX_DATES);
    this.removeDateButton.setDisable(this.dateTimeFieldList.getItems().size() <= 2);
    this.checkValidity();
  }

  private void onDateTypeUpdate(@NotNull DateType dateType) {
    this.dateType = dateType;
    switch (dateType) {
      case EXACT, ABOUT, POSSIBLY, BEFORE, AFTER -> {
        this.dateTimeLabel1.setVisible(false);
        this.dateTimeLabel2.setVisible(false);
        this.dateTimeField2.setVisible(false);
        this.fieldsBox.setVisible(true);
        this.listBox.setVisible(false);
      }
      case RANGE -> {
        this.dateTimeLabel1.setVisible(true);
        this.dateTimeLabel2.setVisible(true);
        this.dateTimeField2.setVisible(true);
        this.fieldsBox.setVisible(true);
        this.listBox.setVisible(false);
      }
      case ALTERNATIVE -> {
        this.dateTimeLabel1.setVisible(false);
        this.dateTimeLabel2.setVisible(false);
        this.fieldsBox.setVisible(false);
        this.listBox.setVisible(true);
      }
    }
    this.checkValidity();
  }

  private void populateDatePrecisionCombo() {
    for (final DateType dateType : DateType.values()) {
      this.datePrecisionCombo.getItems()
          .add(new NotNullComboBoxItem<>(dateType, this.config.language().translate(dateType.key())));
    }
  }

  private enum DateType {
    /**
     * Setup fields for {@link DateTimeWithPrecision} class with precision {@link DateTimePrecision#EXACT}.
     */
    EXACT(DateTimePrecision.EXACT),
    /**
     * Setup fields for {@link DateTimeWithPrecision} class with precision {@link DateTimePrecision#ABOUT}.
     */
    ABOUT(DateTimePrecision.ABOUT),
    /**
     * Setup fields for {@link DateTimeWithPrecision} class with precision {@link DateTimePrecision#POSSIBLY}.
     */
    POSSIBLY(DateTimePrecision.POSSIBLY),
    /**
     * Setup fields for {@link DateTimeWithPrecision} class with precision {@link DateTimePrecision#BEFORE}.
     */
    BEFORE(DateTimePrecision.BEFORE),
    /**
     * Setup fields for {@link DateTimeWithPrecision} class with precision {@link DateTimePrecision#AFTER}.
     */
    AFTER(DateTimePrecision.AFTER),
    /**
     * Setup fields for {@link DateTimeAlternative} class.
     */
    ALTERNATIVE(null),
    /**
     * Setup fields for {@link DateTimeRange} class.
     */
    RANGE(null),
    ;

    private final String key;
    private final DateTimePrecision precision;

    DateType(DateTimePrecision precision) {
      this.precision = precision;
      this.key = "date_field.precision." + this.name().toLowerCase();
    }

    /**
     * The translation key.
     */
    public String key() {
      return this.key;
    }

    public DateTimePrecision precision() {
      return this.precision;
    }

    /**
     * Return the date type corresponding to the given concrete date class.
     *
     * @param date A date object.
     * @return The corresponding date type.
     */
    public static DateType fromDate(@NotNull DateTime date) {
      Objects.requireNonNull(date);
      if (date instanceof DateTimeWithPrecision d)
        return switch (d.precision()) {
          case EXACT -> EXACT;
          case ABOUT -> ABOUT;
          case POSSIBLY -> POSSIBLY;
          case BEFORE -> BEFORE;
          case AFTER -> AFTER;
        };
      if (date instanceof DateTimeAlternative)
        return ALTERNATIVE;
      if (date instanceof DateTimeRange)
        return RANGE;
      throw new IllegalArgumentException();
    }
  }
}
