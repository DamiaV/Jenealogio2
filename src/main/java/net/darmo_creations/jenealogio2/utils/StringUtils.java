package net.darmo_creations.jenealogio2.utils;

import javafx.scene.control.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import net.darmo_creations.jenealogio2.io.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.*;
import java.util.regex.*;

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
    Set<String> argNames = new HashSet<>();
    for (FormatArg arg : args) {
      String name = arg.name();
      if (argNames.contains(name)) {
        throw new IllegalArgumentException("Duplicate format argument: " + name);
      }
      argNames.add(name);
      pattern = pattern.replace("{" + name + "}", Objects.toString(arg.value()));
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

    int[] codepoints = s.strip().replaceAll("\r\n|\n|\r", "\n").chars().toArray();
    for (int i = 0; i < codepoints.length; i++) {
      int codepoint = codepoints[i];
      if (codepoint == 'h' || !urlBuffer.isEmpty()) { // Handle HTTP(S) urls
        boolean lastChar = i == codepoints.length - 1;
        if (Character.isWhitespace(codepoint) || lastChar) {
          if (lastChar) {
            urlBuffer.append(Character.toString(codepoint));
          } else {
            i--; // Step back to parse the whitespace char at next iteration
          }
          String urlCandidate = urlBuffer.toString();
          if (URL_REGEX.matcher(urlCandidate).matches()) { // Text matched, treat as hyperlink and make clickable
            if (!builder.isEmpty()) {
              texts.add(new Text(builder.toString()));
              builder.setLength(0); // Clear
            }
            Text url = new Text(urlCandidate);
            url.getStyleClass().add("hyperlink"); // JavaFX adds some default styling with this class
            url.setOnMouseClicked(event -> urlClickListener.accept(urlCandidate));
            texts.add(url);
          } else { // Text did not match, add it to the main builder as plain text
            builder.append(urlCandidate);
          }
          urlBuffer.setLength(0); // Clear
        } else {
          urlBuffer.append(Character.toString(codepoint));
        }
      } else if (codepoint == '\n') { // Start new line
        texts.add(new Text(builder + Character.toString(codepoint)));
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

  /**
   * Convert a {@link Color} into its CSS hexadecimal representation.
   *
   * @param color Color to convert.
   * @return A string in the format {@code #rrggbb}.
   */
  public static String colorToCSSHex(@NotNull Color color) {
    int r = (int) Math.round(color.getRed() * 255);
    int g = (int) Math.round(color.getGreen() * 255);
    int b = (int) Math.round(color.getBlue() * 255);
    if (color.isOpaque()) {
      return String.format("#%02x%02x%02x", r, g, b);
    }
    int a = (int) Math.round(color.getOpacity() * 255);
    return String.format("#%02x%02x%02x%02x", r, g, b, a);
  }

  /**
   * Return a {@link TextFormatter} that forbids characters contained in {@link TreeFileManager#INVALID_PATH_CHARS},
   * i.e. characters that are forbidden in Windows file paths.
   */
  public static TextFormatter<?> filePathTextFormatter() {
    return new TextFormatter<>(change -> {
      if (TreeFileManager.INVALID_PATH_CHARS.stream().anyMatch(s -> change.getControlNewText().contains(s.toString())))
        return null;
      return change;
    });
  }

  private StringUtils() {
  }
}
