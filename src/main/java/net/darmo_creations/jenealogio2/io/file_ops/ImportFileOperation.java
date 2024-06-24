package net.darmo_creations.jenealogio2.io.file_ops;

import net.darmo_creations.jenealogio2.model.*;
import org.jetbrains.annotations.*;

import java.nio.file.*;

public final class ImportFileOperation extends FileOperation {
  private final Path sourceFile;

  public ImportFileOperation(@NotNull String fileName, @NotNull Path sourceFile, @NotNull AttachedDocument document) {
    super(fileName, document);
    this.sourceFile = sourceFile;
  }

  public Path sourceFile() {
    return this.sourceFile;
  }
}
