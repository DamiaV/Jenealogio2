package net.darmo_creations.jenealogio2.utils.text_parser;

import javafx.scene.text.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.*;

/**
 * Base interface for text tree nodes.
 */
interface Node {
  /**
   * Convert this node into a list of {@link Text} object.
   *
   * @param urlClickCallback A callback to call when a hyperlink is clicked.
   * @return A list of {@link Text} objects for this node.
   */
  List<Text> asText(@NotNull Consumer<String> urlClickCallback);
}
