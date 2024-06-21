package net.darmo_creations.jenealogio2.io;

import javafx.embed.swing.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import org.jetbrains.annotations.*;

import javax.imageio.*;
import java.io.*;
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
   * @param config     The app’s config.
   * @throws IOException If any error occurs.
   */
  public void saveToFile(final @NotNull FamilyTree familyTree, @NotNull File file, final @NotNull Config config) throws IOException {
    try (var out = new ZipOutputStream(new FileOutputStream(file))) {
      this.writeTreeXML(familyTree, out, config);
      for (Picture image : familyTree.pictures()) {
        this.writeImageFile(image, out);
      }
    }
  }

  private void writeTreeXML(@NotNull FamilyTree familyTree, ZipOutputStream out, final @NotNull Config config) throws IOException {
    out.putNextEntry(new ZipEntry(TREE_FILE));
    this.treeXMLWriter.writeToStream(familyTree, out, config);
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
