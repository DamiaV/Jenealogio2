package net.darmo_creations.jenealogio2.utils;

import javafx.scene.control.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import net.darmo_creations.jenealogio2.io.*;
import net.darmo_creations.jenealogio2.utils.text_parser.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.*;

/**
 * Class providing methods to handle strings.
 */
public final class StringUtils {
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
  public static String format(@NotNull String pattern, final @NotNull FormatArg @NotNull ... args) {
    final Set<String> argNames = new HashSet<>();
    for (final FormatArg arg : args) {
      final String name = arg.name();
      if (argNames.contains(name))
        throw new IllegalArgumentException("Duplicate format argument: " + name);
      argNames.add(name);
      pattern = pattern.replace("{" + name + "}", Objects.toString(arg.value()));
    }
    return pattern;
  }

  /**
   * Parse the given text into a list of {@link Text} nodes, using a very simple custom markup language.
   * <p>
   * See {@link TextParser}’s documentation for more details.
   *
   * @param s                The text to parse.
   * @param urlClickListener A callback to attach to the mouse click event of each URL node.
   * @return The parsed text.
   * @see TextParser
   */
  public static List<Text> parseText(@NotNull String s, @NotNull Consumer<String> urlClickListener) {
    return new TextParser().parseString(s, urlClickListener);
  }

  /**
   * Convert a {@link Color} into its CSS hexadecimal representation.
   *
   * @param color Color to convert.
   * @return A string in the format {@code #rrggbb}.
   */
  public static String colorToCSSHex(@NotNull Color color) {
    final int r = (int) Math.round(color.getRed() * 255);
    final int g = (int) Math.round(color.getGreen() * 255);
    final int b = (int) Math.round(color.getBlue() * 255);
    if (color.isOpaque()) {
      return String.format("#%02x%02x%02x", r, g, b);
    }
    final int a = (int) Math.round(color.getOpacity() * 255);
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
