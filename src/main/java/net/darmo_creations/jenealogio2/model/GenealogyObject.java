package net.darmo_creations.jenealogio2.model;

import net.darmo_creations.jenealogio2.config.*;
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
  private final Map<String, AttachedDocument> documents = new HashMap<>();
  private Picture mainPicture;

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
   * A unmodifiable view of this object’s documents.
   */
  public @UnmodifiableView Collection<AttachedDocument> documents() {
    return Collections.unmodifiableCollection(this.documents.values());
  }

  /**
   * This object’s main picture.
   */
  public Optional<Picture> mainPicture() {
    return Optional.ofNullable(this.mainPicture);
  }

  /**
   * Add a document to this object.
   *
   * @param document The document to add.
   */
  void addDocument(@NotNull AttachedDocument document) {
    this.documents.put(document.fileName(), document);
  }

  /**
   * Remove from this object the document with the given ID.
   *
   * @param fileName Name of the document to remove.
   */
  void removeDocument(@NotNull String fileName) {
    Objects.requireNonNull(fileName);
    if (this.mainPicture != null && this.mainPicture.fileName().equals(fileName))
      this.setMainPicture(null);
    this.documents.remove(fileName);
  }

  /**
   * Set the main picture of this object.
   *
   * @param fileName Name of the picture to set as main. May be null.
   * @throws IllegalArgumentException If no picture with the given name is associated with this object.
   * @throws ClassCastException       If the file is not a picture.
   */
  void setMainPicture(String fileName) {
    if (fileName != null) {
      if (!this.documents.containsKey(fileName))
        throw new IllegalArgumentException("No such picture: " + fileName);
      if (!(this.documents.get(fileName) instanceof Picture p))
        throw new ClassCastException("File \"%s\" is not an image".formatted(fileName));
      this.mainPicture = p;
    } else {
      this.mainPicture = null;
    }
  }
}
