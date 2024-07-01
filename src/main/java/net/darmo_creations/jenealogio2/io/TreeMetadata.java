package net.darmo_creations.jenealogio2.io;

import org.jetbrains.annotations.*;

import java.time.*;
import java.util.*;

/**
 * This class holds metadata about a family tree directory.
 *
 * @param name          The tree’s name.
 * @param directoryName The directory’s name.
 * @param lastOpenDate  The last time the tree was opened in the app.
 */
public record TreeMetadata(@NotNull String name, @NotNull String directoryName, LocalDateTime lastOpenDate)
    implements Comparable<TreeMetadata> {
  public TreeMetadata {
    Objects.requireNonNull(name);
    Objects.requireNonNull(directoryName);
  }

  @Override
  public int compareTo(@NotNull TreeMetadata o) {
    if (this.lastOpenDate() == null && o.lastOpenDate() == null)
      return this.compareNames(o);
    if (this.lastOpenDate() == null)
      return 1;
    if (o.lastOpenDate() == null)
      return -1;
    int i = -this.lastOpenDate().compareTo(o.lastOpenDate()); // Sort by latest date first
    return i == 0 ? this.compareNames(o) : i;
  }

  private int compareNames(TreeMetadata o) {
    return this.name().compareTo(o.name());
  }
}
