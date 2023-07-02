package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.darmo_creations.jenealogio2.App;
import net.darmo_creations.jenealogio2.config.Config;
import net.darmo_creations.jenealogio2.utils.FormatArg;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Base class for this app’s dialogs.
 *
 * @param <T> Type of returned values.
 */
public abstract class DialogBase<T> extends Dialog<T> {
  private final String name;

  /**
   * Create a modal dialog.
   *
   * @param name        Dialog’s name. Used for the title’s translation key.
   * @param resizable   Whether the dialog should be resizable.
   * @param buttonTypes The dialog’s button types.
   */
  public DialogBase(@NotNull String name, boolean resizable, ButtonType @NotNull ... buttonTypes) {
    this(name, resizable, true, buttonTypes);
  }

  /**
   * Create a dialog.
   *
   * @param name        Dialog’s name. Used for the title’s translation key.
   * @param resizable   Whether the dialog should be resizable.
   * @param modal       Whether this dialog should be modal.
   * @param buttonTypes The dialog’s button types.
   */
  public DialogBase(@NotNull String name, boolean resizable, boolean modal, ButtonType @NotNull ... buttonTypes) {
    this.name = name;
    Config config = App.config();
    config.theme().getStyleSheets()
        .forEach(url -> this.stage().getScene().getStylesheets().add(url.toExternalForm()));
    this.initModality(modal ? Modality.APPLICATION_MODAL : Modality.NONE);
    this.setResizable(resizable);
    this.setTitle(config.language().translate("dialog.%s.title".formatted(name),
        this.getTitleFormatArgs().toArray(FormatArg[]::new)));
    this.getDialogPane().getButtonTypes().addAll(buttonTypes);
  }

  /**
   * Return a list of {@link FormatArg}s to use when formatting this dialog’s title.
   */
  protected List<FormatArg> getTitleFormatArgs() {
    return List.of();
  }

  /**
   * This dialog’s name.
   */
  public String name() {
    return this.name;
  }

  /**
   * This dialog’s stage.
   */
  protected Stage stage() {
    return (Stage) this.getDialogPane().getScene().getWindow();
  }
}
