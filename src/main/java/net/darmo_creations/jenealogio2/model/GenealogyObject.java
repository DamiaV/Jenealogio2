package net.darmo_creations.jenealogio2.model;

import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.io.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Base class for all genealogical objects.
 *
 * @param <T> Type of this class.
 */
public abstract class GenealogyObject<T extends GenealogyObject<T>> {
  private String notes;
  private String sources;
  private final Map<AnnotationType, Set<AttachedDocument>> documentAnnotations =
      new EnumMap<>(AnnotationType.class);
  private Picture mainPicture;

  protected GenealogyObject() {
    for (final var annotationType : AnnotationType.values())
      this.documentAnnotations.put(annotationType, new HashSet<>());
  }

  /**
   * The name of this object in the given language.
   */
  public abstract String name(@NotNull Language language);

  /**
   * This object’s notes.
   */
  public Optional<String> notes() {
    return Optional.ofNullable(this.notes);
  }

  /**
   * Set this object’s notes. If not null, the string is stripped from any leading and trailing whitespace.
   *
   * @param notes The notes.
   * @return This object.
   */
  @SuppressWarnings("unchecked")
  public T setNotes(String notes) {
    this.notes = StringUtils.stripNullable(notes).orElse(null);
    return (T) this;
  }

  /**
   * This object’s sources.
   */
  public Optional<String> sources() {
    return Optional.ofNullable(this.sources);
  }

  /**
   * Set this object’s sources. If not null, the string is stripped from any leading and trailing whitespace.
   *
   * @param sources The sources.
   * @return This object.
   */
  @SuppressWarnings("unchecked")
  public T setSources(String sources) {
    this.sources = StringUtils.stripNullable(sources).orElse(null);
    return (T) this;
  }

  /**
   * This object’s main picture.
   */
  public Optional<Picture> mainPicture() {
    return Optional.ofNullable(this.mainPicture);
  }

  /**
   * The set of documents this object is annotated in.
   *
   * @param annotationType The specific type of annotation to consider.
   * @return An unmodifiable view of the internal set.
   */
  public Set<AttachedDocument> getAnnotatedInDocuments(@NotNull AnnotationType annotationType) {
    return Collections.unmodifiableSet(this.documentAnnotations.get(annotationType));
  }

  /**
   * Add a document where this object is annotated.
   * Does <b>not</b> update the {@link AttachedDocument} object.
   *
   * @param document       The document where this object is annotated.
   * @param annotationType The specific annotation type to which the document has to be added.
   */
  void addAnnotatedInDocument(
      final @NotNull AttachedDocument document,
      @NotNull AnnotationType annotationType
  ) {
    this.documentAnnotations.get(annotationType)
        .add(Objects.requireNonNull(document));
  }

  /**
   * Remove a document from this object’s annotation set.
   * Does <b>not</b> update the {@link AttachedDocument} object.
   *
   * @param annotationType The specific annotation type from which the document has to be removed.
   * @param document       The document to remove.
   */
  void removeAnnotatedInDocument(
      @NotNull AnnotationType annotationType,
      final @NotNull AttachedDocument document
  ) {
    this.documentAnnotations.get(annotationType)
        .remove(Objects.requireNonNull(document));
  }

  /**
   * Set the main picture of this object.
   *
   * @param picture The {@link Picture} to set as main. May be null.
   */
  void setMainPicture(final Picture picture) {
    this.mainPicture = picture;
  }
}
