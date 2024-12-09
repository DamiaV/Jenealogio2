package net.darmo_creations.jenealogio2.ui.components;

import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Wrapper class that holds data to be put in a {@link javafx.scene.control.ComboBox}.
 * <p>
 * The equals() and hashcode() methods are overriden and only use the {@code data} value.
 *
 * @param <T> Type of the wrapped data.
 */
public class ComboBoxItem<T> {
  private final T data;
  private final String text;

  /**
   * Create a new item with some data and the text to display.
   *
   * @param data Data to wrap. May be null.
   * @param text Text to show.
   */
  public ComboBoxItem(T data, @NotNull String text) {
    this.data = data;
    this.text = text;
  }

  /**
   * Create a new item with some data and no text to display.
   * <p>
   * This constructor is meant to be used as a selector.
   *
   * @param data Data to wrap. May be null.
   */
  public ComboBoxItem(T data) {
    this(data, "");
  }

  /**
   * The data help by this item.
   */
  public @Nullable T data() {
    return this.data;
  }

  /**
   * The text to display.
   */
  public String text() {
    return this.text;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof ComboBoxItem<?> i && Objects.equals(this.data(), i.data());
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.data());
  }

  @Override
  public String toString() {
    return this.text;
  }
}
