package net.darmo_creations.jenealogio2.utils.text_parser;

import javafx.scene.text.*;
import org.jetbrains.annotations.*;

import java.text.*;
import java.util.*;
import java.util.function.*;
import java.util.regex.*;

/**
 * This class parses text with a very simple custom markup language.
 * <p>
 * The parser recognizes the following tags:
 * <li><code>*text*</code>: bold text
 * <li><code>/text/</code>: italic text
 * <li><code>_text_</code>: underlined text
 * <li><code>~text~</code>: strikethrough text
 * <li><code>&lt;url></code>: hyperlink
 */
public class TextParser {
  // From @diegoperini on https://mathiasbynens.be/demo/url-regex and https://gist.github.com/dperini/729294
  private static final Pattern URL_REGEX = Pattern.compile(
      "^https?://(\\S+(:\\S*)?@)?((?!(10|127)(\\.\\d{1,3}){3})(?!(169\\.254|192\\.168)(\\.\\d{1,3}){2})(?!172\\.(1[6-9]|2\\d|3[0-1])(\\.\\d{1,3}){2})([1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(\\.(1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(\\.([1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(([a-z0-9\\x{00a1}-\\x{ffff}][a-z0-9\\x{00a1}-\\x{ffff}_-]{0,62})?[a-z0-9\\x{00a1}-\\x{ffff}]\\.)+([a-z\\x{00a1}-\\x{ffff}]{2,}\\.?))(:\\d{2,5})?([/?#]\\S*)?$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
  );
  private static final List<Character> TAGS = List.of('*', '/', '_', '~');

  private int[] codepoints;
  private int index;
  private final Stack<Character> stack = new Stack<>();

  /**
   * Parse a string into a list of {@link Text} objects.
   *
   * @param s                The string to parse.
   * @param urlClickCallback A callback to call when a hyperlink is clicked.
   * @return The list of parsed {@link Text} objects.
   */
  public List<Text> parseString(@NotNull String s, @NotNull Consumer<String> urlClickCallback) {
    return this.parseTree(s).asText(urlClickCallback);
  }

  /**
   * Parse the given string into a {@link Node} tree.
   *
   * @param s The string to parse.
   * @return The corresponding {@link Node} tree.
   * @apiNote This method is accessible to package-level only
   * and not private in order to make testing possible.
   */
  Node parseTree(@NotNull String s) {
    this.codepoints = s.strip().replaceAll("\r\n?|\n", "\n").chars().toArray();
    this.index = 0;
    this.stack.clear();
    final SequenceNode sequenceNode = this.parse().orElseGet(() -> new SequenceNode(TextStyle.NONE));
    if (sequenceNode.children().isEmpty())
      return new PlainTextNode("");
    if (sequenceNode.style() == TextStyle.NONE && sequenceNode.children().size() == 1)
      return sequenceNode.children().get(0);
    return sequenceNode;
  }

  private Optional<SequenceNode> parse() {
    final var sequence = new SequenceNode(this.stack.isEmpty() ? TextStyle.NONE : switch (this.stack.peek()) {
      case '*' -> TextStyle.BOLD;
      case '/' -> TextStyle.ITALIC;
      case '_' -> TextStyle.UNDERLINE;
      case '~' -> TextStyle.STRIKETHROUGH;
      default -> TextStyle.NONE;
    });
    final var textBuffer = new StringBuilder();
    boolean escaping = false;

    for (; this.index < this.codepoints.length; this.index++) {
      final int codepoint = this.codepoints[this.index];

      if (codepoint == '\\') {
        if (escaping) {
          textBuffer.append(Character.toString(codepoint));
          escaping = false;
        } else
          escaping = true;

      } else if (!escaping && codepoint == '<') {
        if (!textBuffer.isEmpty()) {
          sequence.addChild(new PlainTextNode(textBuffer.toString()));
          textBuffer.setLength(0);
        }
        final int i = this.index;
        this.index++;
        final Optional<Node> node = this.parseUrl();
        if (node.isPresent())
          sequence.addChild(node.get());
        else { // Could not parse URL, rollback to the initial position
          this.index = i;
          textBuffer.append(Character.toString(codepoint));
        }

      } else if (!escaping && codepoint == '[') {
        if (!textBuffer.isEmpty()) {
          sequence.addChild(new PlainTextNode(textBuffer.toString()));
          textBuffer.setLength(0);
        }
        final int i = this.index;
        this.index++;
        final Optional<Node> node = this.parseUrlWithText();
        if (node.isPresent())
          sequence.addChild(node.get());
        else { // Could not parse URL, rollback to the initial position
          this.index = i;
          textBuffer.append(Character.toString(codepoint));
        }

      } else if (!escaping && codepoint < 255 && TAGS.contains((char) codepoint)) {
        try {
          final var node = this.handleTag((char) codepoint, textBuffer, sequence);
          if (node.isPresent())
            return node;
        } catch (final ParseException e) {
          return Optional.empty();
        }

      } else if (codepoint == '\n') {
        if (escaping) {
          textBuffer.append('\\');
          escaping = false;
        }
        textBuffer.append(Character.toString(codepoint));
        if (!this.stack.isEmpty()) // Unclosed tag, abort
          break;

      } else {
        if (escaping) {
          if (codepoint > 255 || !TAGS.contains((char) codepoint))
            textBuffer.append('\\');
          escaping = false;
        }
        textBuffer.append(Character.toString(codepoint));
      }
    }

    if (!textBuffer.isEmpty())
      sequence.addChild(new PlainTextNode(textBuffer.toString()));

    if (!this.stack.isEmpty()) { // Unclosed tag, return an unstyled sequence instead
      final SequenceNode sequenceNode = new SequenceNode(TextStyle.NONE);
      sequenceNode.addChild(new PlainTextNode(this.stack.pop().toString()));
      sequence.children().forEach(sequenceNode::addChild);
      return Optional.of(sequenceNode);
    }

    return Optional.of(sequence);
  }

  private Optional<SequenceNode> handleTag(
      char tag,
      @NotNull StringBuilder textBuffer,
      @NotNull SequenceNode sequence
  ) throws ParseException {
    if (!this.stack.isEmpty() && this.stack.peek() != tag
        && this.stack.subList(0, this.stack.size() - 1).contains(tag)) {
      this.stack.pop();
      throw new ParseException("Interwoven tags", this.index);

    } else if (!this.stack.isEmpty() && this.stack.peek() == tag) { // Close current tag
      this.stack.pop();
      if (!textBuffer.isEmpty())
        sequence.addChild(new PlainTextNode(textBuffer.toString()));
      else if (sequence.children().isEmpty())
        throw new ParseException("Empty tag " + tag, this.index);
      return Optional.of(sequence);

    } else { // Save current index and recursively parse the new tag
      this.stack.push(tag);
      if (!textBuffer.isEmpty()) {
        sequence.addChild(new PlainTextNode(textBuffer.toString()));
        textBuffer.setLength(0);
      }
      final int i = this.index;
      this.index++; // Skip current one
      final Optional<SequenceNode> node = this.parse();
      if (node.isPresent()) // Ok, add sub-tag to current sequence
        sequence.addChild(node.get());
      else { // Could not parse, rollback to the initial position and add character to buffer
        this.index = i;
        textBuffer.append(tag);
      }
    }

    return Optional.empty();
  }

  private Optional<Node> parseUrl() {
    final var buffer = new StringBuilder();

    for (; this.index < this.codepoints.length; this.index++) {
      final int codepoint = this.codepoints[this.index];
      if (codepoint == '>') {
        final String urlCandidate = buffer.toString();
        if (URL_REGEX.matcher(urlCandidate).matches())
          return Optional.of(new LinkNode(urlCandidate, null));
        else
          return Optional.empty();
      } else if (Character.isWhitespace(codepoint))
        return Optional.empty();
      else
        buffer.append(Character.toString(codepoint));
    }

    return Optional.empty();
  }

  private Optional<Node> parseUrlWithText() {
    final var textBuffer = new StringBuilder();
    final var urlBuffer = new StringBuilder();
    boolean inText = true;
    boolean inUrl = false;

    for (; this.index < this.codepoints.length; this.index++) {
      final int codepoint = this.codepoints[this.index];
      if (inText) {
        if (codepoint == ']')
          inText = false;
        else
          textBuffer.append(Character.toString(codepoint));
      } else if (inUrl) {
        if (codepoint == ')') {
          final String urlCandidate = urlBuffer.toString();
          if (URL_REGEX.matcher(urlCandidate).matches())
            return Optional.of(new LinkNode(urlCandidate, textBuffer.toString()));
          else
            return Optional.empty();
        } else
          urlBuffer.append(Character.toString(codepoint));
      } else if (codepoint == '(')
        inUrl = true;
      else
        return Optional.empty();
    }

    return Optional.empty();
  }
}
