package net.darmo_creations.jenealogio2.utils;

import javafx.scene.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Class providing methods to handle strings.
 */
public final class StringUtils {
  // From @diegoperini on https://mathiasbynens.be/demo/url-regex and https://gist.github.com/dperini/729294
  private static final Pattern URL_REGEX =
      Pattern.compile("^https?://(\\S+(:\\S*)?@)?((?!(10|127)(\\.\\d{1,3}){3})(?!(169\\.254|192\\.168)(\\.\\d{1,3}){2})(?!172\\.(1[6-9]|2\\d|3[0-1])(\\.\\d{1,3}){2})([1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(\\.(1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(\\.([1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(([a-z0-9\\x{00a1}-\\x{ffff}][a-z0-9\\x{00a1}-\\x{ffff}_-]{0,62})?[a-z0-9\\x{00a1}-\\x{ffff}]\\.)+([a-z\\x{00a1}-\\x{ffff}]{2,}\\.?))(:\\d{2,5})?([/?#]\\S*)?$");

  /**
   * Strip a string of all leading and trailing whitespace. If the string is null, is empty,
   * or only contains whitespace, null is returned.
   *
   * @param s A string.
   * @return The same string stripped of all leading and trailing whitespace or an empty optional
   * if it was null, empty, or all whitespace.
   */
  public static Optional<String> stripNullable(String s) {
    return s == null || s.isBlank() ? Optional.empty() : Optional.of(s.strip());
  }

  /**
   * Format a string using named brace placeholders.
   * <p>
   * Each placeholder must be of the form {@code {name}}.
   *
   * @param pattern String pattern.
   * @param args    Arguments to substitute to placeholders.
   *                Placeholders will be substituted by the value of the format argument with the exact same name.
   * @return The formatted string.
   */
  public static String format(@NotNull String pattern, final @NotNull FormatArg... args) {
    for (FormatArg e : args) {
      pattern = pattern.replace("{" + e.name() + "}", Objects.toString(e.value()));
    }
    return pattern;
  }

  /**
   * Parse a text and return a list of {@link Text} nodes that each correspond to a full line or URL.
   *
   * @param s                Text to parse.
   * @param urlClickListener A callback to attach to the mouse click event of each URL node.
   * @return The parsed text.
   */
  public static List<Text> parseText(@NotNull String s, @NotNull Consumer<String> urlClickListener) {
    List<Text> texts = new LinkedList<>();
    StringBuilder builder = new StringBuilder();
    StringBuilder urlBuffer = new StringBuilder();

    int[] codepoints = s.replaceAll("\n?\r|\n\r?", "\n").chars().toArray();
    for (int i = 0; i < codepoints.length; i++) {
      int codepoint = codepoints[i];
      if (codepoint == 'h' || !urlBuffer.isEmpty()) {
        if (Character.isWhitespace(codepoint) || i == codepoints.length - 1) {
          String urlCandidate = urlBuffer.toString();
          if (URL_REGEX.matcher(urlCandidate).matches()) {
            Text url = new Text(urlCandidate);
            url.getStyleClass().add("hyperlink");
            url.setOnMouseClicked(event -> urlClickListener.accept(urlCandidate));
            texts.add(url);
          } else {
            builder.append(urlCandidate);
          }
          if (i != codepoints.length - 1) {
            i--; // Step back to parse the whitespace char at next iteration
          }
          urlBuffer.setLength(0); // Clear
        } else {
          urlBuffer.append(Character.toString(codepoint));
        }
      } else if (codepoint == '\n') {
        String s1 = builder + Character.toString(codepoint);
        texts.add(new Text(s1));
        builder.setLength(0); // Clear
      } else {
        builder.append(Character.toString(codepoint));
      }
    }

    if (!urlBuffer.isEmpty()) {
      builder.append(urlBuffer);
    }
    if (!builder.isEmpty()) {
      texts.add(new Text(builder.toString()));
    }

    return texts;
  }

  private StringUtils() {
  }
}
