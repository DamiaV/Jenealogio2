package net.darmo_creations.jenealogio2.ui;

import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import net.darmo_creations.jenealogio2.App;
import net.darmo_creations.jenealogio2.config.Config;
import net.darmo_creations.jenealogio2.config.Language;
import net.darmo_creations.jenealogio2.themes.Theme;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class SettingsDialog extends DialogBase<ButtonType> {
  @FXML
  @SuppressWarnings("unused")
  private ComboBox<Language> languageCombo;
  @FXML
  @SuppressWarnings("unused")
  private ComboBox<Theme> themeCombo;

  private Config localConfig;

  public SettingsDialog() {
    super("settings", false, ButtonTypes.OK, ButtonTypes.CANCEL);
    //noinspection DataFlowIssue
    this.languageCombo.getItems().addAll(Config.languages());
    this.themeCombo.getItems().addAll(Theme.themes());
    this.setResultConverter(param -> {
      if (!param.getButtonData().isCancelButton() && this.configChanged()) {
        try {
          App.updateConfig(this.localConfig);
          this.localConfig.save();
          Alerts.info("dialog.settings.alert.needs_restart.content", null, null);
        } catch (IOException e) {
          App.LOGGER.exception(e);
          Alerts.error("dialog.settings.alert.save_error.content", null, null);
        }
      }
      return null;
    });
  }

  public void setConfig(final @NotNull Config config) {
    this.localConfig = config.clone();
    this.languageCombo.getSelectionModel().select(this.localConfig.language());
    this.themeCombo.getSelectionModel().select(this.localConfig.theme());
    this.updateState();
  }

  private void updateState() {
    boolean configChanged = this.configChanged();
    this.getDialogPane().lookupButton(ButtonTypes.OK).setDisable(!configChanged);
  }

  private boolean configChanged() {
    return !this.localConfig.equals(App.config());
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
}
