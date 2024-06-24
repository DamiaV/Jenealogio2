package net.darmo_creations.jenealogio2.io.file_ops;

import net.darmo_creations.jenealogio2.model.*;
import org.jetbrains.annotations.*;

import java.util.*;

public abstract sealed class FileOperation
    permits ImportFileOperation, DeleteFileOperation, RenameFileOperation {
  private final String fileName;
  private final AttachedDocument document;

  public FileOperation(@NotNull String fileName, @NotNull AttachedDocument document) {
    this.fileName = Objects.requireNonNull(fileName);
    this.document = Objects.requireNonNull(document);
  }

  public String fileName() {
    return this.fileName;
  }

  public AttachedDocument document() {
    return this.document;
  }
}
