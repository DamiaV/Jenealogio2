package net.darmo_creations.jenealogio2.io;

import javafx.embed.swing.*;
import javafx.scene.image.*;
import net.darmo_creations.jenealogio2.*;
import net.darmo_creations.jenealogio2.model.*;
import org.jetbrains.annotations.*;

import javax.imageio.*;
import java.awt.image.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import java.util.zip.*;

public final class FileUtils {
  /**
   * Unzip the given .zip file into the given directory.
   * The ZIP file’s content will be unzipped in a sub-directory named after the ZIP file.
   *
   * @param zipFilePath Path to the file to unzip.
   * @param destDir     Directory into which to unzip the file.
   * @return The name of the resulting unzipped directory.
   * @throws IOException If any I/O error occurs.
   */
  public static String unzip(final @NotNull Path zipFilePath, @NotNull Path destDir) throws IOException {
    final String dirName = splitExtension(zipFilePath.getFileName().toString()).fileName();
    destDir = destDir.resolve(dirName);
    if (!Files.exists(destDir))
      Files.createDirectories(destDir);

    final byte[] buffer = new byte[1024];
    try (final FileInputStream fis = new FileInputStream(zipFilePath.toFile());
         final ZipInputStream zis = new ZipInputStream(fis)) {
      ZipEntry ze;
      do {
        ze = zis.getNextEntry();
        if (ze != null) {
          final Path newFile = destDir.resolve(ze.getName());
          App.LOGGER.info("Unzipping to " + newFile.toAbsolutePath());
          if (ze.isDirectory())
            Files.createDirectories(newFile);
          else {
            Files.createDirectories(newFile.getParent());
            try (final FileOutputStream fos = new FileOutputStream(newFile.toFile())) {
              int len;
              while ((len = zis.read(buffer)) > 0)
                fos.write(buffer, 0, len);
            }
          }
          zis.closeEntry();
        }
      } while (ze != null);
    }

    return dirName;
  }

  /**
   * Zip the given directory’s content into a .zip file.
   * The ZIP file will be name after the target directory.
   *
   * @param targetDirectory Path to the directory to zip.
   * @param destFile        Path of the resulting zip file.
   * @throws IOException If any I/O error occurs.
   */
  public static void zip(@NotNull Path targetDirectory, @NotNull Path destFile) throws IOException {
    try (final var fos = new FileOutputStream(destFile.toFile());
         final var zipOut = new ZipOutputStream(fos)) {
      zipFile(targetDirectory, null, zipOut); // Pass null to skip the target directory itself
    }
  }

  private static void zipFile(
      @NotNull Path file,
      String fileName,
      @NotNull ZipOutputStream zipOut
  ) throws IOException {
    if (Files.isDirectory(file)) {
      if (fileName != null) {
        if (fileName.endsWith("/"))
          zipOut.putNextEntry(new ZipEntry(fileName));
        else
          zipOut.putNextEntry(new ZipEntry(fileName + "/"));
        zipOut.closeEntry();
      }
      try (final Stream<Path> files = Files.walk(file, 1)) {
        final Iterator<Path> iterator = files.iterator();
        while (iterator.hasNext()) {
          final Path path = iterator.next();
          if (!Files.isSameFile(file, path)) // Files.walk() returns the passed path, avoid recursive loop
            zipFile(path, (fileName != null ? fileName + "/" : "") + path.getFileName(), zipOut);
        }
      }
    } else {
      try (final var fis = new FileInputStream(file.toFile())) {
        final ZipEntry zipEntry = new ZipEntry(Objects.requireNonNull(fileName));
        zipOut.putNextEntry(zipEntry);
        final byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0)
          zipOut.write(bytes, 0, length);
        zipOut.closeEntry();
      }
    }
  }

  /**
   * Delete a file or directory. If the file is a directory,
   * delete its contents recursively before deleting it.
   *
   * @param file The file or directory to delete.
   * @throws IOException In any I/O error occurs.
   */
  public static void deleteRecursively(@NotNull Path file) throws IOException {
    if (Files.isDirectory(file)) {
      try (final Stream<Path> files = Files.walk(file)) {
        final Iterator<Path> iterator = files.iterator();
        while (iterator.hasNext()) {
          final Path path = iterator.next();
          if (!Files.isSameFile(path, file)) // Files.walk() returns the passed path, avoid recursive loop
            deleteRecursively(path);
        }
      }
    }
    Files.deleteIfExists(file);
  }

  /**
   * Open the given file path in the host system’s default file explorer.
   *
   * @param path The path to open.
   */
  public static void openInFileExplorer(@NotNull Path path) {
    // Cannot use Desktop.getDesktop().open(File) as it does not work properly outside of Windows
    // Possible values: https://runmodule.com/2020/10/12/possible-values-of-os-dependent-java-system-properties/
    final String osName = System.getProperty("os.name").toLowerCase();
    final String[] command;
    if (osName.contains("linux"))
      command = new String[] { "dbus-send", "--dest=org.freedesktop.FileManager1", "--type=method_call",
          "/org/freedesktop/FileManager1", "org.freedesktop.FileManager1.ShowItems",
          "array:string:file:%s".formatted(path), "string:\"\"" };
    else if (osName.contains("win"))
      command = new String[] { "explorer /select,\"{path}\"" };
    else if (osName.contains("mac"))
      command = new String[] { "open", "-R", path.toString() };
    else {
      App.LOGGER.error("Unable to open file system explorer: unsupported operating system %s".formatted(osName));
      return;
    }

    try {
      Runtime.getRuntime().exec(command);
    } catch (final IOException e) {
      App.LOGGER.exception(e);
    }
  }

  /**
   * Split the name and extension of the given file name.
   *
   * @param fileName The file name to split.
   * @return A pair containing the file’s name and its extension.
   * If the file name has no extension, the returned value will be empty.
   * The extension includes the dot.
   */
  public static FileName splitExtension(@NotNull String fileName) {
    if (fileName.contains(".")) {
      final int lastDotIndex = fileName.lastIndexOf(".");
      return new FileName(
          fileName.substring(0, lastDotIndex),
          Optional.of(fileName.substring(lastDotIndex))
      );
    }
    return new FileName(fileName, Optional.empty());
  }

  /**
   * Load the given image. If the file’s format is not supported by JavaFX,
   * but is in the {@link Picture#FILE_EXTENSIONS} list, the image will be converted in-memory
   * into a format that JavaFX supports.
   *
   * @param path The path to the image file.
   * @return The loaded image.
   * @throws IOException              If any I/O error occurs.
   * @throws IllegalArgumentException If the file’s extension in not in {@link Picture#FILE_EXTENSIONS}.
   */
  public static Image loadImage(@NotNull Path path) throws IOException {
    final FileName fileName = splitExtension(path.getFileName().toString());
    final Optional<String> ext = fileName.extension();

    if (ext.isEmpty())
      throw new IllegalArgumentException("Unsupported image format");
    else if (!Picture.FILE_EXTENSIONS.contains(ext.get().toLowerCase()))
      throw new IllegalArgumentException("Unsupported image format: " + ext.get());

    if (JAVAFX_FILE_EXTENSIONS.contains(ext.get().toLowerCase()))
      try (final var inputStream = new FileInputStream(path.toFile())) {
        return new Image(inputStream);
      }

    // Load with ImageIO then convert to Image
    final BufferedImage image = ImageIO.read(path.toFile());
    return SwingFXUtils.toFXImage(image, null);
  }

  /**
   * The list of image formats supported by JavaFX.
   * <p>
   * BMP format is excluded as BMP images with an alpha channel are not rendered by {@link ImageView},
   * see <a href="https://bugs.openjdk.org/browse/JDK-8149621">this JDK bug report</a>.
   */
  @Unmodifiable
  private static final List<String> JAVAFX_FILE_EXTENSIONS = List.of(".jpg", ".jpeg", ".png", ".gif");

  private FileUtils() {
  }
}
