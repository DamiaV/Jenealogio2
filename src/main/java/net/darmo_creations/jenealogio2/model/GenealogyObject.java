package net.darmo_creations.jenealogio2.model;

import net.darmo_creations.jenealogio2.utils.StringUtils;

import java.io.File;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public abstract class GenealogyObject<T extends GenealogyObject<T>> {
  private String notes;
  private String sources;
  private final Set<File> files = new HashSet<>();

  public Optional<String> notes() {
    return Optional.ofNullable(this.notes);
  }

  @SuppressWarnings("unchecked")
  public T setNotes(String notes) {
    this.notes = StringUtils.stripNullable(notes).orElse(null);
    return (T) this;
  }

  public Optional<String> sources() {
    return Optional.ofNullable(this.sources);
  }

  @SuppressWarnings("unchecked")
  public T setSources(String sources) {
    this.sources = StringUtils.stripNullable(sources).orElse(null);
    return (T) this;
  }
}
