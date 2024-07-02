package net.darmo_creations.jenealogio2.io;

import javafx.scene.image.*;
import net.darmo_creations.jenealogio2.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.model.datetime.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Loads {@link FamilyTree}s from the file system. See {@link TreeFileManager} for more details.
 */
public class FamilyTreeReader extends TreeFileManager {
  private final TreeXMLReader treeXMLReader = new TreeXMLReader();

  /**
   * Load a family tree from a directory.
   *
   * @param directory The directory to read.
   * @return A new {@link FamilyTree} object.
   * @throws IOException If any error occurs.
   */
  public FamilyTree loadFromDirectory(@NotNull Path directory) throws IOException {
    final Path filesDir = directory.resolve(FILES_DIR);
    final FamilyTree familyTree;
    try (final var in = new FileInputStream(directory.resolve(TREE_FILE_NAME).toFile())) {
      familyTree = this.treeXMLReader.readFromStream(
          in,
          (name, desc, date) -> {
            final Path path = filesDir.resolve(name);
            final Optional<String> ext = FileUtils.splitExtension(name).extension();
            if (ext.isPresent() && Picture.FILE_EXTENSIONS.contains(ext.get().toLowerCase()))
              return this.readImageFile(path, desc, date);
            return new AttachedDocument(path, desc, date);
          }
      );
    } catch (final RuntimeException e) {
      throw new IOException(e);
    }
    return familyTree;
  }

  private Picture readImageFile(
      @NotNull Path filePath,
      String description,
      DateTime date
  ) {
    Image image = null;
    try (final var inputStream = new FileInputStream(filePath.toFile())) {
      image = new Image(inputStream);
    } catch (final IOException e) {
      App.LOGGER.exception(e);
    }
    return new Picture(image, filePath, description, date);
  }
}
