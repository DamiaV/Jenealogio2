package net.darmo_creations.jenealogio2.utils.text_parser;

import javafx.scene.text.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * This node represents a sequence of {@link Node} objects that may have a specific style.
 * <p>
 * When calling {@link #asText(Consumer)}, the {@link Text} objects from the sequence’s children
 * will inherit the sequence’s style.
 */
class SequenceNode implements Node {
  private final TextStyle style;
  private final List<Node> children = new LinkedList<>();

  /**
   * Create a new empty sequence node.
   *
   * @param style The sequence’s style.
   */
  public SequenceNode(@NotNull TextStyle style) {
    this.style = Objects.requireNonNull(style);
  }

  /**
   * This sequence’s style.
   */
  public TextStyle style() {
    return this.style;
  }

  /**
   * An unmodifiable view of this sequence’s child {@link Node}s.
   */
  @UnmodifiableView
  public List<Node> children() {
    return Collections.unmodifiableList(this.children);
  }

  /**
   * Add a child {@link Node} to this sequence.
   * <p>
   * If the passed node is a {@link SequenceNode}, it may be unpacked to simplify the tree structure.
   * Consecutive {@link PlainTextNode}s may also be merged to simplify the tree structure.
   *
   * @param child The child to add.
   * @throws NullPointerException If the argument is null.
   */
  public void addChild(@NotNull Node child) {
    Objects.requireNonNull(child);
    if (child instanceof SequenceNode seq && seq.style == this.style)
      seq.children.forEach(this::addChild); // Flatten unstyled sequences
    else if (!this.children.isEmpty()
             && this.children.get(this.children.size() - 1) instanceof PlainTextNode t1
             && child instanceof PlainTextNode t2) { // Merge consecutive plain text nodes
      this.children.remove(t1);
      this.children.add(new PlainTextNode(t1.text() + t2.text()));
    } else if (!(child instanceof PlainTextNode t) || !t.text().isEmpty()) // Ignore empty plain text nodes
      this.children.add(child);
  }

  public final List<Text> asText(@NotNull Consumer<String> urlClickCallback) {
    return this.children.stream()
        .map(node -> node.asText(urlClickCallback))
        .flatMap(List::stream)
        .peek(this::handleChildText)
        .toList();
  }

  private void handleChildText(@NotNull Text childText) {
    final Optional<String> css = switch (this.style) {
      case NONE -> Optional.empty();
      case BOLD -> Optional.of("-fx-font-weight: bold");
      case ITALIC -> Optional.of("-fx-font-style: italic");
      case UNDERLINE -> Optional.of("-fx-underline: true");
      case STRIKETHROUGH -> Optional.of("-fx-strikethrough: true");
    };
    if (css.isPresent() && !childText.getStyle().contains(css.get())) {
      if (childText.getStyle().isEmpty())
        childText.setStyle(css.get());
      childText.setStyle(childText.getStyle() + "; " + css.get());
    }
  }

  @Override
  public String toString() {
    final String s = switch (this.style) {
      case NONE -> "|";
      case BOLD -> "*";
      case ITALIC -> "/";
      case UNDERLINE -> "_";
      case STRIKETHROUGH -> "~";
    };
    return s + this.children.stream().map(Node::toString).collect(Collectors.joining(" + ")) + s;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || this.getClass() != o.getClass())
      return false;
    final SequenceNode that = (SequenceNode) o;
    return this.style == that.style && Objects.equals(this.children, that.children);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.style, this.children);
  }
}
