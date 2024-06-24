package net.darmo_creations.jenealogio2.io.file_ops;

import net.darmo_creations.jenealogio2.model.*;
import org.jetbrains.annotations.*;

public final class RenameFileOperation extends FileOperation {
  private final String newFileName;

  public RenameFileOperation(@NotNull String fileName, @NotNull String newFileName, @NotNull AttachedDocument document) {
    super(fileName, document);
    this.newFileName = newFileName;
  }

  public String newFileName() {
    return this.newFileName;
  }
}
