package net.darmo_creations.jenealogio2.model;

import javafx.scene.image.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.util.*;

public final class Picture {
  private final Image image;
  private final String name;
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

  public String description() {
    return this.description;
  }

  public void setDescription(@NotNull String description) {
    this.description = StringUtils.stripNullable(description).orElse(null);
  }

  @Override
  public String toString() {
    return "Picture[image=@%d, name=%s, desc=%s]".formatted(this.image.hashCode(), this.name, this.description);
  }
}
