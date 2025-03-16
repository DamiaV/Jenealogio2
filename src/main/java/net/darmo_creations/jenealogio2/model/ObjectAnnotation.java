package net.darmo_creations.jenealogio2.model;

import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class ObjectAnnotation implements Comparable<ObjectAnnotation> {
  private final GenealogyObject<?> object;
  private String note;

  public ObjectAnnotation(final @NotNull GenealogyObject<?> object, String note) {
    this.object = Objects.requireNonNull(object);
    this.note = note;
  }

  public GenealogyObject<?> object() {
    return this.object;
  }

  public Optional<String> note() {
    return Optional.ofNullable(this.note);
  }

  public void setNote(String note) {
    this.note = StringUtils.stripNullable(note).orElse(null);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || this.getClass() != o.getClass()) return false;
    final ObjectAnnotation that = (ObjectAnnotation) o;
    return Objects.equals(this.object, that.object);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(this.object);
  }

  @Override
  public int compareTo(@NotNull ObjectAnnotation o) {
    final var o1 = this.object();
    final var o2 = o.object();
    if (o1 instanceof Person && o2 instanceof LifeEvent) return -1;
    if (o1 instanceof LifeEvent && o2 instanceof Person) return 1;
    if (o1 instanceof Person p1 && o2 instanceof Person p2)
      return Person.lastThenFirstNamesComparator().compare(p1, p2);
    if (o1 instanceof LifeEvent e1 && o2 instanceof LifeEvent e2)
      return e1.compareTo(e2);
    return 0;
  }
}
