package net.darmo_creations.jenealogio2.model;

import javafx.scene.image.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class Picture {
  public static final String[] FILE_EXTENSIONS = {".jpg", ".jpeg", ".png"};

  private final Image image;
  private final String name; // TODO allow renaming
  private String description;

  public Picture(final @NotNull Image image, @NotNull String name, String description) {
    this.image = Objects.requireNonNull(image);
    this.name = Objects.requireNonNull(name);
    this.description = description;
  }

  public Image image() {
    return this.image;
  }

  public String name() {
    return this.name;
  }

  public Optional<String> description() {
    return Optional.ofNullable(this.description);
  }

  public void setDescription(String description) {
    this.description = StringUtils.stripNullable(description).orElse(null);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }
    Picture picture = (Picture) o;
    return Objects.equals(this.name, picture.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.name);
  }

  @Override
  public String toString() {
    return "Picture[image=@%d, name=%s, desc=%s]".formatted(this.image.hashCode(), this.name, this.description);
  }
}
