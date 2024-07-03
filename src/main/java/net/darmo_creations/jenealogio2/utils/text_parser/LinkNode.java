package net.darmo_creations.jenealogio2.utils.text_parser;

import javafx.scene.text.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.*;

/**
 * This node represents a hyperlink to an HTTP(S) URL.
 */
class LinkNode extends TextNode {
  /**
   * This node’s text, i.e. the URL.
   *
   * @param text The text/URL.
   */
  public LinkNode(@NotNull String text) {
    super(text);
  }

  @Override
  public List<Text> asText(@NotNull Consumer<String> urlClickCallback) {
    final Text text = new Text(this.text());
    text.getStyleClass().add("hyperlink"); // Add built-in JavaFX CSS class to format link
    text.setOnMouseClicked(event -> urlClickCallback.accept(this.text()));
    return List.of(text);
  }

  @Override
  public String toString() {
    return "<%s>".formatted(this.text());
  }
}
