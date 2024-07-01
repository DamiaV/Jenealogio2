package net.darmo_creations.jenealogio2.io;

import org.jetbrains.annotations.*;

import java.util.*;

/**
 * This record represents a file name.
 *
 * @param fileName  The file name without its extension.
 * @param extension An {@link Optional} containing the file’s extension if any, an empty {@link Optional} otherwise.
 */
public record FileName(@NotNull String fileName, @NotNull Optional<String> extension) {
  public FileName {
    Objects.requireNonNull(fileName);
    Objects.requireNonNull(extension);
  }
}
