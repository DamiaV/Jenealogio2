package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.stage.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.io.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * This class provides methods to open file saver/chooser dialogs.
 */
public final class FileChoosers {
  /**
   * Open a dialog to choose a ZIPped tree file.
   *
   * @param config The app’s config.
   * @param stage  The parent stage object.
   * @return The selected file.
   */
  public static Optional<Path> showZippedTreeFileChooser(
      final @NotNull Config config,
      final @NotNull Window stage
  ) {
    return showFileChooser(
        config,
        stage,
        "zipped_tree_file_chooser",
        false,
        "zipped_tree_file",
        null,
        ".zip"
    );
  }

  /**
   * Open a dialog to save a ZIPped tree file.
   *
   * @param config      The app’s config.
   * @param stage       The parent stage object.
   * @param defaultName The file’s default name.
   * @return The selected file.
   */
  public static Optional<Path> showZippedTreeFileSaver(
      final @NotNull Config config,
      final @NotNull Window stage,
      String defaultName
  ) {
    return showFileChooser(
        config,
        stage,
        "zipped_tree_file_saver",
        true,
        "zipped_tree_file",
        defaultName,
        ".zip"
    );
  }

  /**
   * Open a dialog to save a .jtreereg file.
   *
   * @param config      The app’s config.
   * @param stage       The parent stage object.
   * @param defaultName Default file name.
   * @return The selected file.
   */
  public static Optional<Path> showRegistriesFileSaver(
      final @NotNull Config config,
      final @NotNull Window stage,
      String defaultName
  ) {
    return showFileChooser(
        config,
        stage,
        "registries_file_saver",
        true,
        "registries_file_chooser",
        defaultName,
        TreeXMLManager.REG_FILE_EXTENSION
    );
  }

  /**
   * Open a dialog to choose a .jtreereg file.
   *
   * @param config      The app’s config.
   * @param stage       The parent stage object.
   * @param defaultName Default file name.
   * @return The selected file.
   */
  public static Optional<Path> showRegistriesFileChooser(
      final @NotNull Config config,
      final @NotNull Window stage,
      String defaultName
  ) {
    return showFileChooser(
        config,
        stage,
        "registries_file_chooser",
        false,
        "registries_file_chooser",
        defaultName,
        TreeXMLManager.REG_FILE_EXTENSION
    );
  }

  /**
   * Open a dialog to save a file.
   *
   * @param config      The app’s config.
   * @param stage       The parent stage object.
   * @param titleKey    Title translation key.
   * @param descKey     Description’s i18n key. Ignored if no extensions are specified.
   * @param defaultName Default file name.
   * @param extensions  Allowed file extensions. Leave empty to allow any file type.
   * @return The selected file.
   */
  public static Optional<Path> showFileSaver(
      final @NotNull Config config,
      final @NotNull Window stage,
      String titleKey,
      @NotNull String descKey,
      String defaultName,
      @NotNull String... extensions
  ) {
    return showFileChooser(
        config,
        stage,
        titleKey == null ? "file_saver" : titleKey,
        true,
        descKey,
        defaultName,
        extensions
    );
  }

  /**
   * Open a dialog to choose a file.
   *
   * @param config     The app’s config.
   * @param stage      The parent stage object.
   * @param descKey    Description’s i18n key. Ignored if no extensions are specified.
   * @param extensions Allowed file extensions. Leave empty to allow any file type.
   * @return The selected file.
   */
  public static Optional<Path> showFileChooser(
      final @NotNull Config config,
      final @NotNull Window stage,
      @NotNull String descKey,
      @NotNull String... extensions
  ) {
    return showFileChooser(
        config,
        stage,
        "file_chooser",
        false,
        descKey,
        null,
        extensions
    );
  }

  /**
   * Open a file chooser/saver.
   *
   * @param config      The app’s config.
   * @param stage       The parent stage object.
   * @param titleKey    Title translation key.
   * @param saver       True to open a file saver, false for file chooser.
   * @param descKey     Description’s i18n key. Ignored if no extensions are specified.
   * @param defaultName Default file name.
   * @param extensions  Allowed file extensions. Leave empty to allow any file type.
   * @return The selected file.
   */
  private static Optional<Path> showFileChooser(
      final @NotNull Config config,
      @NotNull Window stage,
      @NotNull String titleKey,
      boolean saver,
      @NotNull String descKey,
      String defaultName,
      @NotNull String... extensions
  ) {
    final FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle(config.language().translate("dialog.%s.title".formatted(titleKey)));
    if (extensions.length != 0) {
      final List<String> exts = Arrays.stream(extensions)
          .map(e -> "*" + e)
          .toList();
      final String desc = config.language().translate(
          "dialog.%s.filter_description".formatted(Objects.requireNonNull(descKey)),
          new FormatArg("exts", String.join(", ", exts))
      );
      fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(desc, exts));
    }
    if (defaultName != null) {
      if (!defaultName.endsWith(extensions[0]))
        defaultName += extensions[0];
      fileChooser.setInitialFileName(defaultName);
    }

    File file = saver ? fileChooser.showSaveDialog(stage) : fileChooser.showOpenDialog(stage);
    Path path = null;
    if (file != null) {
      final String fileName = file.getName();
      if (extensions.length != 0 && Arrays.stream(extensions).noneMatch(fileName::endsWith)) {
        if (saver) file = new File(file.getPath() + extensions[0]);
        else return Optional.empty();
      }
      path = file.toPath();
    }

    return Optional.ofNullable(path);
  }

  private FileChoosers() {
  }
}
