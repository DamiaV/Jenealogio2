package net.darmo_creations.jenealogio2.io;

import net.darmo_creations.jenealogio2.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.io.file_ops.*;
import net.darmo_creations.jenealogio2.model.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.nio.file.*;

/**
 * Writes {@link FamilyTree}s to the file system. See {@link TreeFileManager} for more details.
 */
public class FamilyTreeWriter extends TreeFileManager {
  private final TreeXMLWriter treeXMLWriter = new TreeXMLWriter();

  /**
   * Save a family tree to the file system.
   *
   * @param familyTree Family tree object to save.
   * @param directory  Directory to write to.
   * @param config     The app’s config.
   * @throws IOException If any error occurs.
   */
  public void saveToDirectory(final @NotNull FamilyTree familyTree, @NotNull Path directory, final @NotNull Config config) throws IOException {
    Path filesDir = directory.resolve(FILES_DIR);
    if (!Files.exists(filesDir))
      Files.createDirectories(filesDir);
    try (var out = new FileOutputStream(directory.resolve(TREE_FILE_NAME).toFile())) {
      this.treeXMLWriter.writeToStream(familyTree, out, config);
    }
    this.performFileOperations(filesDir, familyTree);
  }

  private void performFileOperations(@NotNull Path root, @NotNull FamilyTree familyTree) {
    for (FileOperation operation : familyTree.pendingFileOperations()) {
      try {
        if (operation instanceof ImportFileOperation ifo) {
          // FamilyTreeReader generates this operation when building the family tree, skip it
          if (!ifo.sourceFile().equals(root.resolve(ifo.fileName()))) {
            Path newPath = Files.copy(ifo.sourceFile(), root.resolve(ifo.fileName()));
            operation.document().setPath(newPath);
          }
        } else if (operation instanceof DeleteFileOperation dfo)
          Files.deleteIfExists(root.resolve(dfo.fileName()));
        else if (operation instanceof RenameFileOperation rfo) {
          Path newPath = Files.move(root.resolve(rfo.fileName()), root.resolve(rfo.newFileName()));
          operation.document().setPath(newPath);
        }
      } catch (IOException e) {
        App.LOGGER.exception(e);
      }
    }
    familyTree.clearPendingFileOperations();
  }
}
