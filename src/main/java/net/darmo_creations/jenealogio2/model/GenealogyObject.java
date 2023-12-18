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
  private final Map<String, Picture> pictures = new HashMap<>();
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
   * A view of this object’s pictures.
   */
  public Collection<Picture> pictures() {
    return this.pictures.values();
  }

  /**
   * This object’s main picture.
   */
  public Optional<Picture> mainPicture() {
    return Optional.ofNullable(this.mainPicture);
  }

  /**
   * Add a picture to this object.
   *
   * @param picture The picture to add.
   */
  void addPicture(@NotNull Picture picture) {
    this.pictures.put(picture.name(), picture);
  }

  /**
   * Remove from this object the picture with the given ID.
   *
   * @param name Name of the picture to remove.
   */
  void removePicture(@NotNull String name) {
    Objects.requireNonNull(name);
    if (this.mainPicture != null && this.mainPicture.name().equals(name)) {
      this.setMainPicture(null);
    }
    this.pictures.remove(name);
  }

  /**
   * Set the main picture of this object.
   *
   * @param name Name of the picture to set as main. May be null.
   * @throws IllegalArgumentException If no picture with the given name is associated with this object.
   */
  void setMainPicture(String name) {
    if (name != null) {
      if (!this.pictures.containsKey(name)) {
        throw new IllegalArgumentException("No such picture: " + name);
      }
      this.mainPicture = this.pictures.get(name);
    } else {
      this.mainPicture = null;
    }
  }
}
