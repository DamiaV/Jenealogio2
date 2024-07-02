package net.darmo_creations.jenealogio2.model;

import net.darmo_creations.jenealogio2.io.*;
import net.darmo_creations.jenealogio2.model.datetime.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.nio.file.*;
import java.util.*;

/**
 * This class represents a document (file) that can be attached to a {@link Person} or {@link LifeEventType}.
 */
public class AttachedDocument implements Comparable<AttachedDocument> {
  private Path path;
  private String fileName;
  private String name;
  private final String normalizedFileExt;
  private String description;
  private DateTime date;

  /**
   * Create a new document.
   *
   * @param path        The document’s path on the file system.
   * @param description The document’s description.
   * @param date        The date of the document.
   */
  public AttachedDocument(@NotNull Path path, String description, final DateTime date) {
    this.path = path;
    final var split = FileUtils.splitExtension(path.getFileName().toString());
    this.fileName = path.getFileName().toString();
    this.setName(split.fileName());
    this.normalizedFileExt = split.extension().map(String::toLowerCase).orElse(null);
    this.setDescription(description);
    this.setDate(date);
  }

  /**
   * The current path of this document on the file system.
   * The file name may differ from the {@link #fileName()} property.
   */
  public Path path() {
    return this.path;
  }

  /**
   * Set the current path of this document on the file system.
   *
   * @param path This document’s new path.
   */
  public void setPath(@NotNull Path path) {
    this.path = path;
  }

  /**
   * The file name of this document.
   * May differ from the name of {@link #path()}.
   */
  public String fileName() {
    return this.fileName;
  }

  public Optional<String> normalizedFileExtension() {
    return Optional.ofNullable(this.normalizedFileExt);
  }

  /**
   * This document’s name. It corresponds to the value of {@link #fileName()} before the extension’s dot.
   */
  public final String name() {
    return this.name;
  }

  /**
   * Set this document’s name. Also updates the {@link #fileName()} property, keeping the extension.
   *
   * @param name The new name.
   */
  public final void setName(@NotNull String name) {
    this.name = Objects.requireNonNull(name);
    this.fileName = name + FileUtils.splitExtension(this.fileName).extension().orElse("");
  }

  /**
   * This document’s description.
   */
  public final Optional<String> description() {
    return Optional.ofNullable(this.description);
  }

  /**
   * Set this document’s description.
   *
   * @param description The new description. May be null.
   */
  public final void setDescription(String description) {
    this.description = StringUtils.stripNullable(description).orElse(null);
  }

  /**
   * This document’s date.
   */
  public final Optional<DateTime> date() {
    return Optional.ofNullable(this.date);
  }

  /**
   * Set this document’s date.
   *
   * @param date The new date. May be null.
   */
  public final void setDate(final DateTime date) {
    this.date = date;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || this.getClass() != o.getClass())
      return false;
    final Picture picture = (Picture) o;
    return Objects.equals(this.fileName(), picture.fileName());
  }

  @Override
  public int compareTo(final @NotNull AttachedDocument o) {
    if (this.date == null)
      return o.date == null ? this.compareNames(o) : -1;
    if (o.date == null)
      return 1;
    final int c = this.date.compareTo(o.date);
    return c == 0 ? this.compareNames(o) : c;
  }

  private int compareNames(final @NotNull AttachedDocument o) {
    return this.fileName.toLowerCase().compareTo(o.fileName.toLowerCase());
  }

  @Override
  public String toString() {
    return "Document[name=%s, ext=%s, date=%s, desc=%s]".formatted(
        this.name,
        this.normalizedFileExt,
        this.date,
        this.description
    );
  }
}
