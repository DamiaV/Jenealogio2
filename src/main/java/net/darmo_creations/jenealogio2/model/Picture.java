package net.darmo_creations.jenealogio2.model;

import javafx.scene.image.*;
import net.darmo_creations.jenealogio2.model.datetime.*;
import org.jetbrains.annotations.*;

import java.nio.file.*;
import java.util.*;

/**
 * This class represents a picture (file) that can be attached to a {@link Person} or {@link LifeEventType}.
 */
public class Picture extends AttachedDocument {
  /**
   * The list of allowed picture extensions.
   */
  @Unmodifiable
  public static final List<String> FILE_EXTENSIONS = List.of(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp");

  private final Image image;

  /**
   * Create a new picture.
   *
   * @param image       The picture’s {@link Image} object.
   * @param path        The picture’s path on the file system.
   * @param description The picture’s description.
   * @param date        The date of the picture.
   */
  public Picture(final Image image, @NotNull Path path, String description, final DateTime date) {
    super(path, description, date);
    this.image = image;
  }

  /**
   * This picture’s {@link Image} object.
   */
  public Optional<Image> image() {
    return Optional.ofNullable(this.image);
  }
}
