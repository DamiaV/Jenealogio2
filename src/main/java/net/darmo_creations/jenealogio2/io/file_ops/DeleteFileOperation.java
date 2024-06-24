package net.darmo_creations.jenealogio2.io.file_ops;

import net.darmo_creations.jenealogio2.model.*;
import org.jetbrains.annotations.*;

public final class DeleteFileOperation extends FileOperation {
  public DeleteFileOperation(@NotNull String fileName, @NotNull AttachedDocument document) {
    super(fileName, document);
  }
}
