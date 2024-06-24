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
   * Open a dialog to choose a .zip file.
   *
   * @param config The app’s config.
   * @param stage  The parent stage object.
   * @return The selected file.
   */
  public static Optional<Path> showZipFileChooser(
      final @NotNull Config config, final @NotNull Window stage, @NotNull String titleKey, @NotNull String descKey) {
    return getFile(
        config,
        stage,
        titleKey,
        false,
        descKey,
        null,
        FileUtils.EXTENSIONS.toArray(new String[0])
    );
  }

  /**
   * Open a dialog to save a .zip file.
   *
   * @param config The app’s config.
   * @param stage  The parent stage object.
   * @return The selected file.
   */
  public static Optional<Path> showZipFileSaver(
      final @NotNull Config config, final @NotNull Window stage, @NotNull String titleKey, @NotNull String descKey) {
    return getFile(
        config,
        stage,
        titleKey,
        true,
        descKey,
        null,
        FileUtils.EXTENSIONS.toArray(new String[0])
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
      final @NotNull Config config, final @NotNull Window stage, String defaultName) {
    return getRegistriesFile(
        config,
        stage,
        "registries_file_saver",
        true,
        defaultName
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
      final @NotNull Config config, final @NotNull Window stage, String defaultName) {
    return getRegistriesFile(
        config,
        stage,
        "registries_file_chooser",
        false,
        defaultName
    );
  }

  /**
   * Open a .jtreereg file chooser/saver.
   *
   * @param config      The app’s config.
   * @param stage       The parent stage object.
   * @param titleKey    Title translation key.
   * @param saver       True to open a file saver, false for file chooser.
   * @param defaultName Default file name.
   * @return The selected file.
   */
  private static Optional<Path> getRegistriesFile(
      final @NotNull Config config, @NotNull Window stage, @NotNull String titleKey, boolean saver, String defaultName) {
    return getFile(
        config,
        stage,
        titleKey,
        saver,
        "registries_file_chooser",
        defaultName,
        TreeXMLManager.REG_FILE_EXTENSION
    );
  }

  /**
   * Open a dialog to choose a file.
   *
   * @param config The app’s config.
   * @param stage  The parent stage object.
   * @return The selected file.
   */
  public static Optional<Path> showFileChooser(
      final @NotNull Config config, final @NotNull Window stage) {
    return getFile(
        config,
        stage,
        "file_chooser",
        false,
        null,
        null
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
  private static Optional<Path> getFile(
      final @NotNull Config config,
      @NotNull Window stage,
      @NotNull String titleKey,
      boolean saver,
      String descKey,
      String defaultName,
      @NotNull String... extensions
  ) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle(config.language().translate("dialog.%s.title".formatted(titleKey)));
    if (extensions.length != 0) {
      List<String> exts = Arrays.stream(extensions).map(e -> "*" + e).toList();
      String desc = config.language().translate(
          "dialog.%s.filter_description".formatted(Objects.requireNonNull(descKey)),
          new FormatArg("exts", String.join(", ", exts))
      );
      fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(desc, exts));
    }
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
    Path path = null;
    if (file != null) {
      String fileName = file.getName();
      if (extensions.length != 0 && Arrays.stream(extensions).noneMatch(fileName::endsWith)) {
        if (saver) {
          file = new File(file.getPath() + extensions[0]);
        } else {
          return Optional.empty();
        }
      }
      path = file.toPath();
    }
    return Optional.ofNullable(path);
  }

  private FileChoosers() {
  }
}
