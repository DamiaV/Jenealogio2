package net.darmo_creations.jenealogio2.utils.text_parser;

import javafx.scene.text.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.*;

/**
 * This node represents a hyperlink to an HTTP(S) URL.
 */
class LinkNode extends TextNode {
  private final String url;

  /**
   * This node’s text, i.e. the URL.
   *
   * @param url  The link’s URL.
   * @param text The link’s text. If null, the URL will be used as text.
   */
  public LinkNode(@NotNull String url, String text) {
    super(text != null ? text : url);
    this.url = Objects.requireNonNull(url);
  }

  @Override
  public List<Text> asText(@NotNull Consumer<String> urlClickCallback) {
    final Text text = new Text(this.text());
    text.getStyleClass().add("hyperlink"); // Add built-in JavaFX CSS class to format link
    text.setOnMouseClicked(event -> urlClickCallback.accept(this.url));
    return List.of(text);
  }

  @Override
  public String toString() {
    final String text = this.text();
    if (this.url.equals(text))
      return "<%s>".formatted(text);
    return "[%s](%s)".formatted(text, this.url);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || this.getClass() != o.getClass() || !super.equals(o))
      return false;
    final LinkNode linkNode = (LinkNode) o;
    return Objects.equals(this.url, linkNode.url);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), this.url);
  }
}
