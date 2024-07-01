package net.darmo_creations.jenealogio2.model;

import javafx.scene.image.*;
import net.darmo_creations.jenealogio2.model.datetime.*;
import org.jetbrains.annotations.*;

import java.nio.file.*;
import java.util.*;

public class Picture extends AttachedDocument {
  // TODO check other image formats (BMP, WEBP, APNG, etc.)
  public static final String[] FILE_EXTENSIONS = { ".jpg", ".jpeg", ".png", ".gif" };

  private final Image image;

  public Picture(final Image image, @NotNull Path path, String description, final DateTime date) {
    super(path, description, date);
    this.image = image;
  }

  public Optional<Image> image() {
    return Optional.ofNullable(this.image);
  }
}
