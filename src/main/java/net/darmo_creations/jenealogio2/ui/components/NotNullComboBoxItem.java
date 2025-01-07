package net.darmo_creations.jenealogio2.ui.components;

import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Subclass of {@link ComboBoxItem} that prevents held data from being null.
 *
 * @param <T> Type of wrapped data.
 */
public class NotNullComboBoxItem<T> extends ComboBoxItem<T> {
  /**
   * Create a new item with some data and the text to display.
   *
   * @param data Data to wrap.
   * @param text Text to show.
   */
  public NotNullComboBoxItem(final @NotNull T data, @NotNull String text) {
    super(Objects.requireNonNull(data), text);
  }

  /**
   * Create a new item with some data and no text to display.
   * <p>
   * This constructor is meant to be used as a selector.
   *
   * @param data Data to wrap.
   */
  public NotNullComboBoxItem(final @NotNull T data) {
    super(Objects.requireNonNull(data));
  }

  public @NotNull T data() { // Redefined to change annotation
    //noinspection DataFlowIssue
    return super.data();
  }
}
