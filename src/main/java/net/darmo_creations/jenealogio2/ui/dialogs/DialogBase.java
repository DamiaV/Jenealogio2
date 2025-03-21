package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.stage.*;
import net.darmo_creations.jenealogio2.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Base class for this app’s dialogs.
 *
 * @param <R> Type of returned values.
 */
public abstract class DialogBase<R> extends Dialog<R> {
  protected Config config;
  private boolean internalTitleUpdate;

  /**
   * Create a modal dialog.
   *
   * @param config      The app’s config.
   * @param name        Dialog’s name. Used for the title’s translation key.
   * @param resizable   Whether the dialog should be resizable.
   * @param buttonTypes The dialog’s button types.
   */
  protected DialogBase(
      @NotNull Config config,
      @NotNull String name,
      boolean resizable,
      @NotNull ButtonType... buttonTypes
  ) {
    this(config, name, resizable, true, buttonTypes);
  }

  /**
   * Create a dialog.
   *
   * @param config      The app’s config.
   * @param name        Dialog’s name. Used for the title’s translation key.
   * @param resizable   Whether the dialog should be resizable.
   * @param modal       Whether this dialog should be modal.
   * @param buttonTypes The dialog’s button types.
   */
  protected DialogBase(
      @NotNull Config config,
      @NotNull String name,
      boolean resizable,
      boolean modal,
      @NotNull ButtonType... buttonTypes
  ) {
    this.config = config;
    config.theme().getStyleSheets()
        .forEach(url -> this.stage().getScene().getStylesheets().add(url.toExternalForm()));
    this.initModality(modal ? Modality.APPLICATION_MODAL : Modality.NONE);
    this.setResizable(resizable);
    this.titleProperty().addListener((observable, oldValue, newValue) -> {
      if (this.internalTitleUpdate) return;
      this.internalTitleUpdate = true;
      this.titleProperty().setValue(newValue + " – " + App.NAME);
      this.internalTitleUpdate = false;
    });
    this.setTitle(config.language().translate("dialog.%s.title".formatted(name),
        this.getTitleFormatArgs().toArray(FormatArg[]::new)));
    this.setIcon(config.theme().getAppIcon());
    this.getDialogPane().getButtonTypes().addAll(buttonTypes);
    this.stage().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
      if (event.getCode() == KeyCode.ESCAPE) { // Avoid event being consumed by focused widget
        event.consume();
        this.hide();
      }
    });
  }

  /**
   * Return a list of {@link FormatArg}s to use when formatting this dialog’s title.
   */
  protected List<FormatArg> getTitleFormatArgs() {
    return List.of();
  }

  /**
   * This dialog’s stage.
   */
  protected Stage stage() {
    return (Stage) this.getDialogPane().getScene().getWindow();
  }

  /**
   * Set this dialog’s icon image.
   *
   * @param image The image.
   */
  protected void setIcon(final Image image) {
    this.stage().getIcons().add(image);
  }
}
