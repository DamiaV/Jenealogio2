package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import net.darmo_creations.jenealogio2.App;
import net.darmo_creations.jenealogio2.config.Config;
import net.darmo_creations.jenealogio2.config.Language;
import net.darmo_creations.jenealogio2.themes.Theme;

import java.io.IOException;

public class SettingsDialog extends DialogBase<ButtonType> {
  @FXML
  @SuppressWarnings("unused")
  private ComboBox<Language> languageCombo;
  @FXML
  @SuppressWarnings("unused")
  private ComboBox<Theme> themeCombo;

  private Config initialConfig;
  private Config localConfig;

  public SettingsDialog() {
    super("settings", false, ButtonTypes.OK, ButtonTypes.CANCEL);
    //noinspection DataFlowIssue
    this.languageCombo.getItems().addAll(Config.languages());
    this.themeCombo.getItems().addAll(Theme.themes());
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

  public void resetLocalConfig() {
    this.localConfig = App.config().clone();
    this.initialConfig = this.localConfig.clone();

    this.languageCombo.getSelectionModel().select(this.localConfig.language());
    this.themeCombo.getSelectionModel().select(this.localConfig.theme());

    this.updateState();
  }

  private void updateState() {
    boolean configChanged = this.configChanged().changed();
    this.getDialogPane().lookupButton(ButtonTypes.OK).setDisable(!configChanged);
  }

  private ChangeType configChanged() {
    if (!this.localConfig.language().equals(this.initialConfig.language())
        || !this.localConfig.theme().equals(this.initialConfig.theme())) {
      return ChangeType.NEEDS_RESTART;
    }
    return !this.localConfig.equals(this.initialConfig) ? ChangeType.NO_RESTART_NEEDED : ChangeType.NONE;
  }

  @FXML
  @SuppressWarnings("unused")
  public void onLanguageSelect() {
    this.localConfig = this.localConfig.withLanguage(this.languageCombo.getSelectionModel().getSelectedItem());
    this.updateState();
  }

  @FXML
  @SuppressWarnings("unused")
  public void onThemeSelect() {
    this.localConfig = this.localConfig.withTheme(this.themeCombo.getSelectionModel().getSelectedItem());
    this.updateState();
  }

  private enum ChangeType {
    NONE(false, false),
    NO_RESTART_NEEDED(true, false),
    NEEDS_RESTART(true, true),
    ;

    private final boolean changed;
    private final boolean needsRestart;

    ChangeType(boolean changed, boolean needsRestart) {
      this.changed = changed;
      this.needsRestart = needsRestart;
    }

    public boolean changed() {
      return this.changed;
    }

    public boolean needsRestart() {
      return this.needsRestart;
    }
  }
}
