package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import net.darmo_creations.jenealogio2.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.themes.*;
import net.darmo_creations.jenealogio2.ui.components.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.time.*;

/**
 * Dialog to update the app’s settings. It is not resizable.
 */
public class SettingsDialog extends DialogBase<ButtonType> {
  private final ComboBox<Language> languageCombo = new ComboBox<>();
  private final ComboBox<Theme> themeCombo = new ComboBox<>();
  private final ComboBox<NotNullComboBoxItem<DateFormat>> dateFormatCombo = new ComboBox<>();
  private final ComboBox<NotNullComboBoxItem<TimeFormat>> timeFormatCombo = new ComboBox<>();
  private final Spinner<Integer> maxTreeHeightField = new Spinner<>(1, 7, 1);

  private Config localConfig;

  /**
   * Create a settings dialog.
   *
   * @param config The app’s config.
   */
  public SettingsDialog(final @NotNull Config config) {
    super(config, "settings", false, ButtonTypes.OK, ButtonTypes.CANCEL);

    VBox content = new VBox(this.createInterfaceForm(), new Separator(), this.createTreeForm());
    content.setPrefWidth(500);
    this.getDialogPane().setContent(content);

    this.setResultConverter(buttonType -> {
      if (!buttonType.getButtonData().isCancelButton()) {
        ChangeType changeType = this.configChanged();
        if (changeType.changed()) {
          try {
            App.updateConfig(this.localConfig);
            this.localConfig.save();
            if (changeType.needsRestart()) {
              Alerts.info(config, "dialog.settings.alert.needs_restart.header", null, null);
            }
          } catch (IOException e) {
            App.LOGGER.exception(e);
            Alerts.error(config, "dialog.settings.alert.save_error.header", null, null);
          }
        }
      }
      return buttonType;
    });
  }

  private BorderPane createInterfaceForm() {
    Language language = this.config.language();
    this.languageCombo.getItems().addAll(Config.languages());
    this.languageCombo.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> this.onLanguageSelect(newValue));
    this.themeCombo.getItems().addAll(Theme.themes());
    this.themeCombo.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> this.onThemeSelect(newValue));

    LocalDateTime sampleDate = LocalDateTime.of(1970, 9, 1, 1, 2);
    for (DateFormat dateFormat : DateFormat.values()) {
      this.dateFormatCombo.getItems().add(new NotNullComboBoxItem<>(dateFormat,
          new CalendarDateTimeFormatter(language, dateFormat.getFormat()).format(sampleDate)));
    }
    this.dateFormatCombo.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> this.onDateFormatSelect(newValue));

    for (TimeFormat timeFormat : TimeFormat.values()) {
      this.timeFormatCombo.getItems().add(new NotNullComboBoxItem<>(timeFormat,
          new CalendarDateTimeFormatter(language, timeFormat.getFormat()).format(sampleDate)));
    }
    this.timeFormatCombo.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> this.onTimeFormatSelect(newValue));

    //noinspection unchecked
    return this.getBorderPane(
        "dialog.settings.interface_box.title",
        new Pair<>("dialog.settings.interface_box.language.label", this.languageCombo),
        new Pair<>("dialog.settings.interface_box.theme.label", this.themeCombo),
        new Pair<>("dialog.settings.interface_box.date_format.label", this.dateFormatCombo),
        new Pair<>("dialog.settings.interface_box.time_format.label", this.timeFormatCombo)
    );
  }

  private BorderPane createTreeForm() {
    this.maxTreeHeightField.valueProperty()
        .addListener((observable, oldValue, newValue) -> this.onMaxTreeHeightUpdate(newValue));

    //noinspection unchecked,SuspiciousNameCombination
    return this.getBorderPane(
        "dialog.settings.tree_box.title",
        new Pair<>("dialog.settings.tree_box.max_height.label", this.maxTreeHeightField)
    );
  }

  @SuppressWarnings("unchecked")
  private BorderPane getBorderPane(@NotNull String title, final Pair<String, ? extends Control>... rows) {
    Label titleLabel = new Label(this.config.language().translate(title));
    BorderPane.setAlignment(titleLabel, Pos.CENTER);

    GridPane gridPane = new GridPane();
    gridPane.setHgap(10);
    gridPane.setVgap(5);
    gridPane.setPadding(new Insets(10, 0, 10, 0));
    BorderPane.setAlignment(gridPane, Pos.CENTER);

    for (int i = 0; i < rows.length; i++) {
      Label nodeLabel = new Label(this.config.language().translate(rows[i].left()));
      GridPane.setHalignment(nodeLabel, HPos.RIGHT);
      Node node = rows[i].right();
      GridPane.setHalignment(node, HPos.LEFT);
      gridPane.addRow(i, nodeLabel, node);
      RowConstraints rc = new RowConstraints();
      rc.setVgrow(Priority.SOMETIMES);
      gridPane.getRowConstraints().add(rc);
    }

    ColumnConstraints cc1 = new ColumnConstraints();
    cc1.setMaxWidth(300);
    cc1.setHgrow(Priority.SOMETIMES);
    ColumnConstraints cc2 = new ColumnConstraints();
    cc2.setHgrow(Priority.SOMETIMES);
    gridPane.getColumnConstraints().addAll(cc1, cc2);

    return new BorderPane(gridPane, titleLabel, null, null, null);
  }

  /**
   * Reset the local {@link Config} object of this dialog.
   *
   * @param config The app’s config.
   */
  public void resetLocalConfig(final @NotNull Config config) {
    this.config = config.clone();
    this.localConfig = config.clone();

    this.languageCombo.getSelectionModel().select(this.localConfig.language());
    this.themeCombo.getSelectionModel().select(this.localConfig.theme());
    this.maxTreeHeightField.getValueFactory().setValue(this.localConfig.maxTreeHeight());
    this.dateFormatCombo.getSelectionModel().select(new NotNullComboBoxItem<>(this.localConfig.dateFormat()));
    this.timeFormatCombo.getSelectionModel().select(new NotNullComboBoxItem<>(this.localConfig.timeFormat()));

    this.updateState();
  }

  /**
   * Update the state of this dialog’s buttons.
   */
  private void updateState() {
    boolean configChanged = this.configChanged().changed();
    this.getDialogPane().lookupButton(ButtonTypes.OK).setDisable(!configChanged);
  }

  /**
   * Indicate whether the local config object has changed.
   *
   * @return The type of change.
   */
  private ChangeType configChanged() {
    if (!this.localConfig.language().equals(this.config.language())
        || !this.localConfig.theme().equals(this.config.theme())) {
      return ChangeType.NEEDS_RESTART;
    }
    return !this.localConfig.equals(this.config) ? ChangeType.NO_RESTART_NEEDED : ChangeType.NONE;
  }

  private void onLanguageSelect(@NotNull Language newValue) {
    this.localConfig = this.localConfig.withLanguage(newValue);
    this.updateState();
  }

  private void onThemeSelect(@NotNull Theme newValue) {
    this.localConfig = this.localConfig.withTheme(newValue);
    this.updateState();
  }

  private void onMaxTreeHeightUpdate(int newValue) {
    this.localConfig.setMaxTreeHeight(newValue);
    this.updateState();
  }

  private void onDateFormatSelect(@NotNull NotNullComboBoxItem<DateFormat> newValue) {
    this.localConfig.setDateFormat(newValue.data());
    this.updateState();
  }

  private void onTimeFormatSelect(@NotNull NotNullComboBoxItem<TimeFormat> newValue) {
    this.localConfig.setTimeFormat(newValue.data());
    this.updateState();
  }

  /**
   * Enumeration of the differente types of config changes.
   */
  private enum ChangeType {
    /**
     * No changes.
     */
    NONE(false, false),
    /**
     * Some changes were made. No need to restart the app to apply them.
     */
    NO_RESTART_NEEDED(true, false),
    /**
     * Some changes were made. Some require to restart the app to apply them.
     */
    NEEDS_RESTART(true, true),
    ;

    private final boolean changed;
    private final boolean needsRestart;

    ChangeType(boolean changed, boolean needsRestart) {
      this.changed = changed;
      this.needsRestart = needsRestart;
    }

    /**
     * Indicate whether this change type indicates changes were made.
     */
    public boolean changed() {
      return this.changed;
    }

    /**
     * Indicate whether this change type requires the app to restart to apply fully.
     */
    public boolean needsRestart() {
      return this.needsRestart;
    }
  }
}
