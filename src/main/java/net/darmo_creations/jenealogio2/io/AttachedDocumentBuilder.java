package net.darmo_creations.jenealogio2.io;

import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.model.datetime.*;
import org.jetbrains.annotations.*;

/**
 * Base interface for {@link AttachedDocument} object builders.
 */
public interface AttachedDocumentBuilder {
  /**
   * Build an {@link AttachedDocument} object for the given data.
   *
   * @param name        Document’s name.
   * @param description Document’s name.
   * @param date        Document’s date.
   * @return The document.
   */
  AttachedDocument build(@NotNull String name, String description, DateTime date);
}
