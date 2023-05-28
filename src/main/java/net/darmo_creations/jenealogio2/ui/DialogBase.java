package net.darmo_creations.jenealogio2.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Modality;
import net.darmo_creations.jenealogio2.App;
import net.darmo_creations.jenealogio2.config.Config;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public abstract class DialogBase<T> extends Dialog<T> {
  private final String name;

  public DialogBase(@NotNull String name, boolean resizable, @NotNull ButtonType... buttonTypes) {
    this.name = name;
    Config config = App.config();
    FXMLLoader loader = App.getFxmlLoader(name + "-dialog");
    loader.setController(this);
    DialogPane dialogPane;
    try {
      dialogPane = loader.load();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    config.theme().getStyleSheet().ifPresent(css -> dialogPane.getStylesheets().add(css.toExternalForm()));
    this.setDialogPane(dialogPane);
    this.initModality(Modality.APPLICATION_MODAL);
    this.setResizable(resizable);
    this.setTitle(loader.getResources().getString("dialog.%s.title".formatted(name)));
    this.getDialogPane().getButtonTypes().addAll(buttonTypes);
  }

  public String name() {
    return this.name;
  }
}
