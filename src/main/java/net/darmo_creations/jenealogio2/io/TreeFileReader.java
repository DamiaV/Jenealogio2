package net.darmo_creations.jenealogio2.io;

import javafx.scene.image.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.model.datetime.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
 * Reads {@code .jtree} files. See {@link TreeFileManager} for more details.
 */
public class TreeFileReader extends TreeFileManager {
  private final TreeXMLReader treeXMLReader = new TreeXMLReader();

  private final Map<String, Picture> imageCache = new HashMap<>();

  /**
   * Load a family tree from a {@code .jtree} file.
   *
   * @param file The file to read.
   * @return A new {@link FamilyTree} object.
   * @throws IOException If any error occurs.
   */
  public FamilyTree loadFile(@NotNull File file) throws IOException {
    FamilyTree familyTree;

    try (var zipFile = new ZipFile(file);
         var inputStream = zipFile.getInputStream(zipFile.getEntry(TREE_FILE))) {
      familyTree = this.treeXMLReader.readFromStream(
          inputStream, (name, desc, date) -> this.readImageFile(zipFile, name, desc, date));
    } catch (RuntimeException e) {
      throw new IOException(e);
    }

    return familyTree;
  }

  private Optional<Picture> readImageFile(
      @NotNull ZipFile zipFile,
      @NotNull String name,
      String description,
      DateTime date
  ) {
    Objects.requireNonNull(name);
    if (this.imageCache.containsKey(name)) {
      return Optional.ofNullable(this.imageCache.get(name));
    }
    ZipEntry entry = zipFile.getEntry("%s/%s".formatted(IMAGES_DIR, name));
    if (entry == null) {
      return Optional.empty();
    }
    try (var inputStream = zipFile.getInputStream(entry)) {
      Image image = new Image(inputStream);
      Picture picture = new Picture(image, name, description, date);
      this.imageCache.put(name, picture);
      return Optional.of(picture);
    } catch (IOException e) {
      return Optional.empty();
    }
  }
}
