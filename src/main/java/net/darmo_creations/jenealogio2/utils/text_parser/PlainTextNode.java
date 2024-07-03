package net.darmo_creations.jenealogio2.utils.text_parser;

import org.jetbrains.annotations.*;

/**
 * This node represents plain text.
 */
class PlainTextNode extends TextNode {
  /**
   * Create a new plain text node.
   *
   * @param text The node’s text.
   */
  public PlainTextNode(@NotNull String text) {
    super(text);
  }
}
