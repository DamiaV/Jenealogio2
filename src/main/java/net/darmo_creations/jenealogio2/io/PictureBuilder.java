package net.darmo_creations.jenealogio2.io;

import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.model.datetime.*;
import org.jetbrains.annotations.*;

/**
 * Base interface for {@link Picture} object builders.
 */
public interface PictureBuilder {
  /**
   * Build a {@link Picture} object for the given data.
   *
   * @param name        Picture’s name.
   * @param description Picture’s name.
   * @param date        Picture’s date.
   * @return The picture.
   */
  Picture build(@NotNull String name, String description, DateTime date);
}
