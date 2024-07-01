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
  private static final Pattern CALENDAR_SUFFIX_PATTERN =
      Pattern.compile("^calendar\\.suffix\\.(\\*\\d*|\\d+)$");
  private static final Pattern PLURAL_SUFFIX_PATTERN =
      Pattern.compile("^(\\w+(?:\\.\\w+)*)\\.plural(?:_([2-9]|[1-9]\\d+))?$");

  private final String code;
  private final String name;
  private final Locale locale;
  private final ResourceBundle resources;
  private final List<Pair<Pattern, String>> daySuffixes = new LinkedList<>();
  /**
   * Mapping of plurals structured as
   * {base key: {count: plural text template}}
   */
  private final Map<String, Map<Integer, String>> plurals = new HashMap<>();
  private static final Integer DEFAULT_PLURAL_KEY = -1;

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
    this.extractSuffixes();
  }

  /**
   * Extract the plurals and day suffixes.
   * <p>
   * Day suffixes must be specified in the form {@code calendar.suffix.<pattern>} where pattern may be one of:
   * <li>{@code <digits>}: for a specific day number</li>
   * <li>{@code *<digits>}: for all days ending with specific digits</li>
   * <li>{@code *}: for all days</li>
   * <p>
   * Plurals must be specified in the form {@code <base_key>.plural[_<number>]}
   * where base key is an existing translation key. If {@code <number>} is specified,
   * the value is applied only when this specific count is passed
   * to {@link #translate(String, Integer, FormatArg...)}.
   */
  private void extractSuffixes() {
    Iterator<String> iterator = this.resources.getKeys().asIterator();
    List<Pair<String, String>> calendarSuffixes = new LinkedList<>();
    Map<String, Map<Integer, String>> plurals = new HashMap<>();
    while (iterator.hasNext()) {
      String key = iterator.next();
      Matcher matcher = CALENDAR_SUFFIX_PATTERN.matcher(key);
      if (matcher.matches())
        calendarSuffixes.add(new Pair<>(matcher.group(1), this.translate(key)));
      else {
        matcher = PLURAL_SUFFIX_PATTERN.matcher(key);
        if (matcher.matches()) {
          String baseKey = matcher.group(1);
          if (!plurals.containsKey(baseKey))
            plurals.put(baseKey, new HashMap<>());
          String number = matcher.group(2);
          String value = this.resources.getString(key);
          if (number == null || number.isEmpty())
            plurals.get(baseKey).put(DEFAULT_PLURAL_KEY, value);
          else
            plurals.get(baseKey).put(Integer.parseInt(number), value);
        }
      }
    }
    calendarSuffixes.stream()
        .sorted((p1, p2) -> -p1.left().compareTo(p2.left()))
        .forEach(p -> {
          Pattern pattern = Pattern.compile("^" + p.left().replace("*", ".*") + "$");
          this.daySuffixes.add(new Pair<>(pattern, p.right()));
        });
    this.plurals.putAll(plurals);
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
    return this.translate(key, null, formatArgs);
  }

  /**
   * Translate a key and format the resulting text, using the given count as the grammatical number.
   *
   * @param key        Resource bundle key to translate.
   * @param count      The grammatical number. May be null.
   * @param formatArgs Format arguments to use to format the translated text.
   * @return The translated and formatted text.
   */
  public String translate(@NotNull String key, Integer count, final @NotNull FormatArg @NotNull ... formatArgs) {
    String text = null;
    if (count != null && count > 1) {
      Map<Integer, String> p = this.plurals.get(key);
      if (p != null) {
        text = p.get(count);
        if (text == null)
          text = p.get(DEFAULT_PLURAL_KEY);
      }
    }
    if (text == null) {
      try {
        text = this.resources.getString(key);
      } catch (MissingResourceException e) {
        App.LOGGER.warn("Can’t find key " + key);
        return key;
      }
    }
    if (formatArgs.length != 0)
      return StringUtils.format(text, formatArgs);
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
    if (day < 1)
      throw new IllegalArgumentException("Invalid day number: " + day);
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
    if (obj == this)
      return true;
    if (obj == null || obj.getClass() != this.getClass())
      return false;
    Language that = (Language) obj;
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
