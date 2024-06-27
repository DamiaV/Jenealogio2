package net.darmo_creations.jenealogio2.io.gedcom;

import org.jetbrains.annotations.*;

import java.util.*;

public final class GedcomRecord {
  private final int level;
  @Nullable
  private final String xrefId;
  private final String tagName;
  @Nullable
  private final String lineValue;

  public GedcomRecord(@NotNull String line) {
    // TEMP values
    this.level = 0;
    this.xrefId = null;
    this.tagName = null;
    this.lineValue = line;
    // TODO parse line
  }

  public int level() {
    return this.level;
  }

  public Optional<String> xrefId() {
    return Optional.ofNullable(this.xrefId);
  }

  public String tagName() {
    return this.tagName;
  }

  public Optional<String> lineValue() {
    return Optional.ofNullable(this.lineValue);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || this.getClass() != o.getClass())
      return false;
    GedcomRecord that = (GedcomRecord) o;
    return this.level == that.level
           && Objects.equals(this.xrefId, that.xrefId)
           && Objects.equals(this.tagName, that.tagName)
           && Objects.equals(this.lineValue, that.lineValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.level, this.xrefId, this.tagName, this.lineValue);
  }
}
