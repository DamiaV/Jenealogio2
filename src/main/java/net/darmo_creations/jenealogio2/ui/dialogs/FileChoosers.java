package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.darmo_creations.jenealogio2.App;
import net.darmo_creations.jenealogio2.io.TreeFileManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Optional;

public final class FileChoosers {
  public static Optional<File> showTreeFileSaver(final @NotNull Stage stage) {
    return getFile(stage, "dialog.tree_file_saver.title", true);
  }

  public static Optional<File> showTreeFileChooser(final @NotNull Stage stage) {
    return getFile(stage, "dialog.tree_file_chooser.title", false);
  }

  private static Optional<File> getFile(@NotNull Stage stage, @NotNull String titleKey, boolean saver) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle(App.config().language().translate(titleKey));
    var extension = List.of("*" + TreeFileManager.EXTENSION);
    String desc = App.config().language().translate("dialog.tree_file_chooser.filter_description");
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(desc, extension));
    File file;
    if (saver) {
      file = fileChooser.showSaveDialog(stage);
    } else {
      file = fileChooser.showOpenDialog(stage);
    }
    if (file != null && !file.getName().endsWith(TreeFileManager.EXTENSION)) {
      if (saver) {
        file = new File(file.getPath() + TreeFileManager.EXTENSION);
      } else {
        return Optional.empty();
      }
    }
    return Optional.ofNullable(file);
  }

  private FileChoosers() {
  }
}
