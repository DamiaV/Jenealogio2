package net.darmo_creations.jenealogio2.utils.text_parser;

import javafx.scene.text.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.*;

/**
 * Base class for nodes that contain text.
 */
abstract class TextNode implements Node {
  private final String text;

  /**
   * Create a new text node.
   *
   * @param text Some text.
   */
  protected TextNode(@NotNull String text) {
    this.text = Objects.requireNonNull(text);
  }

  /**
   * This node’s text.
   */
  public String text() {
    return this.text;
  }

  public List<Text> asText(@NotNull Consumer<String> urlClickCallback) {
    return List.of(new Text(this.text));
  }

  @Override
  public String toString() {
    return "\"" + this.text + "\"";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || this.getClass() != o.getClass())
      return false;
    final TextNode textNode = (TextNode) o;
    return Objects.equals(this.text, textNode.text);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(this.text);
  }
}
