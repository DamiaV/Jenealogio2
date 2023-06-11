package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import net.darmo_creations.jenealogio2.App;
import net.darmo_creations.jenealogio2.config.Config;
import net.darmo_creations.jenealogio2.config.DateFormat;
import net.darmo_creations.jenealogio2.config.Language;
import net.darmo_creations.jenealogio2.config.TimeFormat;
import net.darmo_creations.jenealogio2.themes.Theme;
import net.darmo_creations.jenealogio2.ui.components.NotNullComboBoxItem;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Dialog to update the app’s settings. It is not resizable.
 */
public class SettingsDialog extends DialogBase<ButtonType> {
  @FXML
  @SuppressWarnings("unused")
  private ComboBox<Language> languageCombo;
  @FXML
  @SuppressWarnings("unused")
  private ComboBox<Theme> themeCombo;
  @FXML
  @SuppressWarnings("unused")
  private ComboBox<NotNullComboBoxItem<DateFormat>> dateFormatCombo;
  @FXML
  @SuppressWarnings("unused")
  private ComboBox<NotNullComboBoxItem<TimeFormat>> timeFormatCombo;
  @FXML
  @SuppressWarnings("unused")
  private Spinner<Integer> maxTreeHeightField;

  private Config initialConfig;
  private Config localConfig;

  /**
   * Create a settings dialog.
   */
  public SettingsDialog() {
    super("settings", false, ButtonTypes.OK, ButtonTypes.CANCEL);
    //noinspection DataFlowIssue
    this.languageCombo.getItems().addAll(Config.languages());
    this.languageCombo.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> this.onLanguageSelect(newValue));
    this.themeCombo.getItems().addAll(Theme.themes());
    this.themeCombo.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> this.onThemeSelect(newValue));

    LocalDateTime sampleDate = LocalDateTime.of(1970, 1, 31, 1, 0);
    for (DateFormat dateFormat : DateFormat.values()) {
      this.dateFormatCombo.getItems().add(new NotNullComboBoxItem<>(dateFormat,
          DateTimeFormatter.ofPattern(dateFormat.getFormat()).format(sampleDate)));
    }
    this.dateFormatCombo.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> this.onDateFormatSelect(newValue));

    for (TimeFormat timeFormat : TimeFormat.values()) {
      this.timeFormatCombo.getItems().add(new NotNullComboBoxItem<>(timeFormat,
          DateTimeFormatter.ofPattern(timeFormat.getFormat()).format(sampleDate)));
    }
    this.timeFormatCombo.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> this.onTimeFormatSelect(newValue));

    this.maxTreeHeightField.valueProperty()
        .addListener((observable, oldValue, newValue) -> this.onMaxTreeHeightUpdate(newValue));

    this.setResultConverter(buttonType -> {
      if (!buttonType.getButtonData().isCancelButton()) {
        ChangeType changeType = this.configChanged();
        if (changeType.changed()) {
          try {
            App.updateConfig(this.localConfig);
            this.localConfig.save();
            if (changeType.needsRestart()) {
              Alerts.info("dialog.settings.alert.needs_restart.header", null, null);
            }
          } catch (IOException e) {
            App.LOGGER.exception(e);
            Alerts.error("dialog.settings.alert.save_error.header", null, null);
          }
        }
      }
      return buttonType;
    });
  }

  /**
   * Reset the local {@link Config} object of this dialog.
   */
  public void resetLocalConfig() {
    this.localConfig = App.config().clone();
    this.initialConfig = this.localConfig.clone();

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
    if (!this.localConfig.language().equals(this.initialConfig.language())
        || !this.localConfig.theme().equals(this.initialConfig.theme())) {
      return ChangeType.NEEDS_RESTART;
    }
    return !this.localConfig.equals(this.initialConfig) ? ChangeType.NO_RESTART_NEEDED : ChangeType.NONE;
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
