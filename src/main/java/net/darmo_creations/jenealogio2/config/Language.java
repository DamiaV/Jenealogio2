package net.darmo_creations.jenealogio2.config;

import net.darmo_creations.jenealogio2.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.regex.*;

/**
 * This class represents a language.
 */
public final class Language {
  private static final Pattern SUFFIX_PATTERN = Pattern.compile("^calendar\\.suffix\\.(\\*\\d*|\\d+)$");

  private final String code;
  private final String name;
  private final Locale locale;
  private final ResourceBundle resources;
  private final List<Pair<Pattern, String>> daySuffixes = new LinkedList<>();

  /**
   * Create a language for the given code and resource.
   *
   * @param code      Language’s code.
   * @param name      Language’s name in the language itself.
   * @param locale    Language’s locale.
   * @param resources Language’s resources, i.e. translations.
   */
  public Language(
      @NotNull String code,
      @NotNull String name,
      @NotNull Locale locale,
      @NotNull ResourceBundle resources
  ) {
    this.code = Objects.requireNonNull(code);
    this.name = Objects.requireNonNull(name);
    this.locale = Objects.requireNonNull(locale);
    this.resources = Objects.requireNonNull(resources);
    this.extractDaySuffixes();
  }

  /**
   * Extract the day suffixes specified in the form {@code calendar.suffix.<pattern>} where pattern may be one of:
   * <li>{@code <digits>}: for a specific day number</li>
   * <li>{@code *<digits>}: for all days ending with specific digits</li>
   * <li>{@code *}: for all days</li>
   */
  private void extractDaySuffixes() {
    Iterator<String> iterator = this.resources.getKeys().asIterator();
    List<Pair<String, String>> suffixes = new ArrayList<>();
    while (iterator.hasNext()) {
      String key = iterator.next();
      Matcher matcher = SUFFIX_PATTERN.matcher(key);
      if (matcher.matches()) {
        suffixes.add(new Pair<>(matcher.group(1), this.translate(key)));
      }
    }
    suffixes.stream()
        .sorted((p1, p2) -> -p1.left().compareTo(p2.left()))
        .forEach(p -> {
          Pattern pattern = Pattern.compile("^" + p.left().replace("*", ".*") + "$");
          this.daySuffixes.add(new Pair<>(pattern, p.right()));
        });
  }

  public String code() {
    return this.code;
  }

  public String name() {
    return this.name;
  }

  public Locale locale() {
    return this.locale;
  }

  public ResourceBundle resources() {
    return this.resources;
  }

  /**
   * Translate a key and format the resulting text.
   *
   * @param key        Resource bundle key to translate.
   * @param formatArgs Format arguments to use to format the translated text.
   * @return The translated and formatted text.
   */
  public String translate(@NotNull String key, final FormatArg @NotNull ... formatArgs) {
    String text;
    try {
      text = this.resources.getString(key);
    } catch (MissingResourceException e) {
      App.LOGGER.warn(e.getMessage());
      return key;
    }
    if (formatArgs.length != 0) {
      return StringUtils.format(text, formatArgs);
    }
    return text;
  }

  /**
   * Check whether a specific key is defined for this language.
   *
   * @param key The key to check.
   * @return True if the key exists, false otherwise.
   */
  public boolean hasKey(@NotNull String key) {
    return this.resources.containsKey(key);
  }

  /**
   * Return the suffix for the given day of month.
   *
   * @param day Day of month (first day is 1)
   * @return The corresponding suffix.
   */
  public Optional<String> getDaySuffix(int day) {
    if (day < 1) {
      throw new IllegalArgumentException("Invalid day number: " + day);
    }
    return this.daySuffixes.stream()
        .filter(p -> p.left().matcher(String.valueOf(day)).matches())
        .findFirst()
        .map(Pair::right);
  }

  @Override
  public String toString() {
    return this.name;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj == null || obj.getClass() != this.getClass()) {
      return false;
    }
    var that = (Language) obj;
    return Objects.equals(this.code, that.code) &&
        Objects.equals(this.name, that.name) &&
        Objects.equals(this.locale, that.locale) &&
        Objects.equals(this.resources, that.resources);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.code, this.name, this.locale, this.resources);
  }
}
