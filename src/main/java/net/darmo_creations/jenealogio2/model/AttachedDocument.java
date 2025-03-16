package net.darmo_creations.jenealogio2.model;

import net.darmo_creations.jenealogio2.io.*;
import net.darmo_creations.jenealogio2.model.datetime.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

/**
 * This class represents a document (file) that can be attached to a {@link FamilyTree}.
 */
public class AttachedDocument implements Comparable<AttachedDocument> {
  private Path path;
  private String fileName;
  private String name;
  private final String normalizedFileExt;
  private String description;
  private DateTime date;
  private final List<Person> authors = new ArrayList<>();
  private final Map<AnnotationType, Map<GenealogyObject<?>, String>> annotations =
      new EnumMap<>(AnnotationType.class);

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
    for (final var annotationType : AnnotationType.values())
      this.annotations.put(annotationType, new HashMap<>());
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

  /**
   * The file’s extension in lower case, with the leading dot.
   */
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

  /**
   * The authors of this document.
   *
   * @return An unmodifiable view of the internal list.
   */
  @UnmodifiableView
  public final List<Person> authors() {
    return Collections.unmodifiableList(this.authors);
  }

  /**
   * Add an author to this document.
   * This document will be added to the person’s authored documents set.
   *
   * @param author The author to add.
   * @param index  The index at which to add the author in the list.
   */
  public final void addAuthor(@NotNull Person author, int index) {
    this.authors.add(index, Objects.requireNonNull(author));
    author.addAuthoredDocument(this);
  }

  /**
   * Remove an author from this document.
   * This document will be removed from the person’s authored documents set.
   *
   * @param author The author to remove.
   */
  public final void removeAuthor(@NotNull Person author) {
    this.authors.remove(author);
    author.removeAuthoredDocument(this);
  }

  /**
   * Remove all authors from this document.
   */
  public final void clearAuthors() {
    this.authors.forEach(p -> p.removeAuthoredDocument(this));
    this.authors.clear();
  }

  /**
   * The objects annotated in this document.
   *
   * @param annotationType The type of annotation to return.
   * @return An unmodifiable copy of the internal set.
   */
  @UnmodifiableView
  public final Map<GenealogyObject<?>, Optional<String>> annotatedObjects(
      @NotNull AnnotationType annotationType
  ) {
    return this.annotations.get(annotationType)
        .entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> Optional.ofNullable(e.getValue())));
  }

  /**
   * The persons annotated in this document, along with their location in the document.
   *
   * @param annotationType The type of annotation to return.
   * @return An unmodifiable copy of the internal set.
   */
  @Unmodifiable
  public final Map<Person, Optional<String>> annotatedPersons(
      @NotNull AnnotationType annotationType
  ) {
    return this.annotations.get(annotationType)
        .entrySet()
        .stream()
        .filter(e -> e.getKey() instanceof Person)
        .collect(Collectors.toMap(e -> (Person) e.getKey(), e -> Optional.ofNullable(e.getValue())));
  }

  /**
   * The events annotated in this document, along with their location in the document.
   *
   * @param annotationType The type of annotations to return.
   * @return An unmodifiable copy of the internal map.
   */
  @Unmodifiable
  public final Map<LifeEvent, Optional<String>> annotatedEvents(
      @NotNull AnnotationType annotationType
  ) {
    return this.annotations.get(annotationType)
        .entrySet()
        .stream()
        .filter(e -> e.getKey() instanceof LifeEvent)
        .collect(Collectors.toMap(e -> (LifeEvent) e.getKey(),
            e -> Optional.ofNullable(e.getValue())));
  }

  /**
   * Add an object to this document as an annotation.
   * This document will be added to the object’s annotation set.
   *
   * @param annotationType The type of annotation to add.
   * @param object         The object to add.
   * @param note           A note indicating where the object is present in this document.
   */
  public final void annotateObject(
      @NotNull AnnotationType annotationType,
      @NotNull GenealogyObject<?> object,
      String note
  ) {
    this.annotations.get(annotationType).put(object, note);
    object.addAnnotatedInDocument(this, annotationType);
  }

  /**
   * Remove an object annotation from this document.
   * This document will be removed from the object’s annotations set.
   *
   * @param annotationType The type of annotation to remove.
   * @param object         The object to remove.
   */
  public final void removeObjectAnnotation(
      @NotNull AnnotationType annotationType,
      @NotNull GenealogyObject<?> object
  ) {
    this.annotations.get(annotationType).remove(object);
    object.removeAnnotatedInDocument(annotationType, this);
  }

  /**
   * Remove all annotations from this document.
   */
  public final void clearObjectAnnotations() {
    for (final var annotationType : AnnotationType.values()) {
      final var annotations = this.annotations.get(annotationType);
      annotations.keySet().forEach(
          o -> o.removeAnnotatedInDocument(annotationType, this));
      annotations.clear();
    }
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
