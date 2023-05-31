package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.darmo_creations.jenealogio2.App;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public abstract class DialogBase extends Dialog<ButtonType> {
  private final String name;

  public DialogBase(@NotNull String name, boolean resizable, @NotNull ButtonType... buttonTypes) {
    this.name = name;
    FXMLLoader loader = App.getFxmlLoader(name.replace('_', '-') + "-dialog");
    loader.setController(this);
    try {
      this.getDialogPane().setContent(loader.load());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    App.config().theme().getStyleSheet()
        .ifPresent(styleSheet -> this.stage().getScene().getStylesheets().add(styleSheet.toExternalForm()));
    this.initModality(Modality.APPLICATION_MODAL);
    this.setResizable(resizable);
    this.setTitle(loader.getResources().getString("dialog.%s.title".formatted(name)));
    this.getDialogPane().getButtonTypes().addAll(buttonTypes);
  }

  public String name() {
    return this.name;
  }

  public Stage stage() {
    return (Stage) this.getDialogPane().getScene().getWindow();
  }
}
