package net.darmo_creations.jenealogio2.model;

import net.darmo_creations.jenealogio2.utils.StringUtils;

import java.io.File;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Base class for all genealogical objects.
 *
 * @param <T> Type of this class.
 */
public abstract class GenealogyObject<T extends GenealogyObject<T>> {
  private String notes;
  private String sources;
  private final Set<File> files = new HashSet<>();

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
}
