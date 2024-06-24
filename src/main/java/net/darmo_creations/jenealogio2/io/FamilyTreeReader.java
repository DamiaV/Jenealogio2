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

  private final Map<String, Picture> imageCache = new HashMap<>();

  /**
   * Load a family tree from a directory.
   *
   * @param directory The directory to read.
   * @return A new {@link FamilyTree} object.
   * @throws IOException If any error occurs.
   */
  public FamilyTree loadFromDirectory(@NotNull Path directory) throws IOException {
    Path filesDir = directory.resolve(FILES_DIR);
    FamilyTree familyTree;
    try (var in = new FileInputStream(directory.resolve(TREE_FILE_NAME).toFile())) {
      familyTree = this.treeXMLReader.readFromStream(in, (name, desc, date) -> this.readImageFile(filesDir, name, desc, date));
    } catch (RuntimeException e) {
      throw new IOException(e);
    }
    return familyTree;
  }

  private Picture readImageFile(
      @NotNull Path root,
      @NotNull String name,
      String description,
      DateTime date
  ) {
    Objects.requireNonNull(name);
    if (this.imageCache.containsKey(name))
      return this.imageCache.get(name);
    Path filePath = root.resolve(name);
    Image image = null;
    try (var inputStream = new FileInputStream(filePath.toFile())) {
      image = new Image(inputStream);
    } catch (IOException e) {
      App.LOGGER.exception(e);
    }
    Picture picture = new Picture(image, filePath, description, date);
    this.imageCache.put(name, picture);
    return picture;
  }
}
