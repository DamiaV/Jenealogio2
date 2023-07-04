package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.stage.FileChooser;
import javafx.stage.Window;
import net.darmo_creations.jenealogio2.App;
import net.darmo_creations.jenealogio2.io.TreeFileManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * This class provides methods to open file saver/chooser dialogs.
 */
public final class FileChoosers {
  /**
   * Open a dialog to save a .jtree file.
   *
   * @param stage       The parent stage object.
   * @param defaultName Default file name.
   * @return The selected file.
   */
  public static Optional<File> showTreeFileSaver(final @NotNull Window stage, String defaultName) {
    return getTreeFile(stage, "tree_file_saver", true, defaultName);
  }

  /**
   * Open a dialog to choose a .jtree file.
   *
   * @param stage       The parent stage object.
   * @param defaultName Default file name.
   * @return The selected file.
   */
  public static Optional<File> showTreeFileChooser(final @NotNull Window stage, String defaultName) {
    return getTreeFile(stage, "tree_file_chooser", false, defaultName);
  }

  /**
   * Open a .jtree file chooser/saver.
   *
   * @param stage       The parent stage object.
   * @param titleKey    Title translation key.
   * @param saver       True to open a file saver, false for file chooser.
   * @param defaultName Default file name.
   * @return The selected file.
   */
  private static Optional<File> getTreeFile(@NotNull Window stage, @NotNull String titleKey, boolean saver, String defaultName) {
    return getFile(stage, titleKey, saver, TreeFileManager.EXTENSION, "tree_file_chooser", defaultName);
  }

  /**
   * Open a dialog to save a .reg file.
   *
   * @param stage       The parent stage object.
   * @param defaultName Default file name.
   * @return The selected file.
   */
  public static Optional<File> showRegistriesFileSaver(final @NotNull Window stage, String defaultName) {
    return getRegistriesFile(stage, "registries_file_saver", true, defaultName);
  }

  /**
   * Open a dialog to choose a .reg file.
   *
   * @param stage       The parent stage object.
   * @param defaultName Default file name.
   * @return The selected file.
   */
  public static Optional<File> showRegistriesFileChooser(final @NotNull Window stage, String defaultName) {
    return getRegistriesFile(stage, "registries_file_chooser", false, defaultName);
  }

  /**
   * Open a .reg file chooser/saver.
   *
   * @param stage       The parent stage object.
   * @param titleKey    Title translation key.
   * @param saver       True to open a file saver, false for file chooser.
   * @param defaultName Default file name.
   * @return The selected file.
   */
  private static Optional<File> getRegistriesFile(@NotNull Window stage, @NotNull String titleKey, boolean saver, String defaultName) {
    return getFile(stage, titleKey, saver, TreeFileManager.REG_FILE_EXTENSION, "registries_file_chooser", defaultName);
  }

  /**
   * Open a file chooser/saver.
   *
   * @param stage       The parent stage object.
   * @param titleKey    Title translation key.
   * @param saver       True to open a file saver, false for file chooser.
   * @param extension   File extension.
   * @param descKey     Description’s i18n key.
   * @param defaultName Default file name.
   * @return The selected file.
   */
  private static Optional<File> getFile(@NotNull Window stage, @NotNull String titleKey, boolean saver,
                                        @NotNull String extension, @NotNull String descKey, String defaultName) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle(App.config().language().translate("dialog.%s.title".formatted(titleKey)));
    var ext = List.of("*" + extension);
    String desc = App.config().language().translate("dialog.%s.filter_description".formatted(descKey));
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(desc, ext));
    if (defaultName != null) {
      if (!defaultName.endsWith(extension)) {
        defaultName += extension;
      }
      fileChooser.setInitialFileName(defaultName);
    }
    File file;
    if (saver) {
      file = fileChooser.showSaveDialog(stage);
    } else {
      file = fileChooser.showOpenDialog(stage);
    }
    if (file != null && !file.getName().endsWith(extension)) {
      if (saver) {
        file = new File(file.getPath() + extension);
      } else {
        return Optional.empty();
      }
    }
    return Optional.ofNullable(file);
  }

  private FileChoosers() {
  }
}
