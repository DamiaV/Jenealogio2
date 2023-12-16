package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.stage.*;
import net.darmo_creations.jenealogio2.*;
import net.darmo_creations.jenealogio2.io.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.util.*;

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
    return getFile(
        stage,
        titleKey,
        saver,
        "tree_file_chooser",
        defaultName,
        TreeFileManager.EXTENSION
    );
  }

  /**
   * Open a dialog to save a .reg file.
   *
   * @param stage       The parent stage object.
   * @param defaultName Default file name.
   * @return The selected file.
   */
  public static Optional<File> showRegistriesFileSaver(final @NotNull Window stage, String defaultName) {
    return getRegistriesFile(
        stage,
        "registries_file_saver",
        true,
        defaultName
    );
  }

  /**
   * Open a dialog to choose a .reg file.
   *
   * @param stage       The parent stage object.
   * @param defaultName Default file name.
   * @return The selected file.
   */
  public static Optional<File> showRegistriesFileChooser(final @NotNull Window stage, String defaultName) {
    return getRegistriesFile(
        stage,
        "registries_file_chooser",
        false,
        defaultName
    );
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
    return getFile(
        stage,
        titleKey,
        saver,
        "registries_file_chooser",
        defaultName,
        TreeXMLManager.REG_FILE_EXTENSION
    );
  }

  /**
   * Open a dialog to choose an image file.
   *
   * @param stage       The parent stage object.
   * @param defaultName Default file name.
   * @return The selected file.
   */
  public static Optional<File> showImageFileChooser(final @NotNull Window stage, String defaultName) {
    return getFile(
        stage,
        "image_file_chooser",
        false,
        "image_file_chooser",
        defaultName,
        Picture.FILE_EXTENSIONS
    );
  }

  /**
   * Open a file chooser/saver.
   *
   * @param stage       The parent stage object.
   * @param titleKey    Title translation key.
   * @param saver       True to open a file saver, false for file chooser.
   * @param descKey     Description’s i18n key.
   * @param defaultName Default file name.
   * @param extensions  Allowed file extensions.
   * @return The selected file.
   */
  private static Optional<File> getFile(
      @NotNull Window stage,
      @NotNull String titleKey,
      boolean saver,
      @NotNull String descKey,
      String defaultName,
      @NotNull String... extensions
  ) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle(App.config().language().translate("dialog.%s.title".formatted(titleKey)));
    List<String> exts = Arrays.stream(extensions).map(e -> "*" + e).toList();
    String desc = App.config().language().translate(
        "dialog.%s.filter_description".formatted(descKey),
        new FormatArg("exts", String.join(", ", exts))
    );
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(desc, exts));
    if (defaultName != null) {
      if (!defaultName.endsWith(extensions[0])) {
        defaultName += extensions[0];
      }
      fileChooser.setInitialFileName(defaultName);
    }
    File file;
    if (saver) {
      file = fileChooser.showSaveDialog(stage);
    } else {
      file = fileChooser.showOpenDialog(stage);
    }
    if (file != null) {
      String fileName = file.getName();
      if (Arrays.stream(extensions).noneMatch(fileName::endsWith)) {
        if (saver) {
          file = new File(file.getPath() + extensions[0]);
        } else {
          return Optional.empty();
        }
      }
    }
    return Optional.ofNullable(file);
  }

  private FileChoosers() {
  }
}
