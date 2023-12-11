package net.darmo_creations.jenealogio2.io;

import javafx.embed.swing.*;
import net.darmo_creations.jenealogio2.model.*;
import org.jetbrains.annotations.*;

import javax.imageio.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
 * Writes {@code .jtree} files. See {@link TreeFileManager} for more details.
 */
public class TreeFileWriter extends TreeFileManager {
  private final TreeXMLWriter treeXMLWriter = new TreeXMLWriter();

  /**
   * Save a family tree to an {@code .jtree} file.
   *
   * @param familyTree Family tree object to save.
   * @param file       File to write to.
   * @throws IOException If any error occurs.
   */
  public void saveToFile(final @NotNull FamilyTree familyTree, @NotNull File file) throws IOException {
    try (var out = new ZipOutputStream(new FileOutputStream(file))) {
      this.writeTreeXML(familyTree, out);
      for (Person person : familyTree.persons()) {
        Optional<Picture> image = person.mainPicture();
        if (image.isPresent()) {
          this.writeImageFile(image.get(), out);
        }
      }
    }
  }

  private void writeTreeXML(@NotNull FamilyTree familyTree, ZipOutputStream out) throws IOException {
    out.putNextEntry(new ZipEntry(TREE_FILE));
    this.treeXMLWriter.writeToStream(familyTree, out);
    out.closeEntry();
  }

  private void writeImageFile(final @NotNull Picture image, @NotNull ZipOutputStream outputStream) throws IOException {
    String name = image.name();
    outputStream.putNextEntry(new ZipEntry("%s/%s".formatted(IMAGES_DIR, name)));
    String ext = name.substring(name.lastIndexOf(".") + 1);
    ImageIO.write(SwingFXUtils.fromFXImage(image.image(), null), ext, outputStream);
    outputStream.closeEntry();
  }
}
