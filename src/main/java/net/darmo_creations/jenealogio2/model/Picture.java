package net.darmo_creations.jenealogio2.model;

import javafx.scene.image.*;

public final class Picture {
  private final Image image;
  private String name;

  public Picture(Image image, String name) {
    this.image = image;
    this.name = name;
  }

  public Image image() {
    return this.image;
  }

  public String name() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "Picture[image=@%d, name=%s]".formatted(this.image.hashCode(), this.name);
  }
}
