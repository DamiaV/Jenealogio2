package net.darmo_creations.jenealogio2.config;

import net.darmo_creations.jenealogio2.*;
import net.darmo_creations.jenealogio2.config.theme.*;
import net.darmo_creations.jenealogio2.ui.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.ini4j.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.util.*;

/**
 * This class holds configuration options for the whole application.
 * <p>
 * All options except debug are mutable at runtime. But some will require the configuration to be saved to disk
 * and the application to be restarted to apply.
 */
public final class Config implements Cloneable {
  /**
   * Array of all available language codes.
   */
  private static final String[] LANGUAGE_CODES = {
      "en",
      "fr",
      "eo",
  };
  private static final String DEFAULT_LANGUAGE_CODE = LANGUAGE_CODES[0];
  private static final Map<String, Language> LANGUAGES = new HashMap<>();

  private static final File SETTINGS_FILE = new File("settings.ini");

  private static final String APP_SECTION = "App";
  private static final String LANGUAGE_OPTION = "language";
  private static final String THEME_OPTION = "theme";
  private static final String SYNC_TREE_OPTION = "sync_tree";
  private static final String MAX_TREE_HEIGHT_OPTION = "max_tree_height";
  private static final String DATE_FORMAT_OPTION = "date_format";
  private static final String TIME_FORMAT_OPTION = "time_format";
  private static final String SHOW_DECEASED_BIRTHDAYS_OPTION = "show_deceased_birthdays";
  private static final String SHOW_LEGENDS = "show_legends";

  /**
   * Load the configuration from the settings file.
   * <p>
   * It loads all available resource bundles and themes.
   *
   * @param debug Value of the debug option.
   * @return A configuration object.
   * @throws IOException     If an IO error occurs.
   * @throws ConfigException If the file could not be parsed correctly.
   */
  public static Config loadConfig(boolean debug) throws IOException, ConfigException {
    loadLanguages();
    Theme.loadThemes();

    final Wini ini = getOrCreateIniFile();

    final String langCode = StringUtils.stripNullable(ini.get(APP_SECTION, LANGUAGE_OPTION)).orElse(DEFAULT_LANGUAGE_CODE);
    if (!LANGUAGES.containsKey(langCode))
      throw new ConfigException("unsupported language code: " + langCode);

    final String themeID = StringUtils.stripNullable(ini.get(APP_SECTION, THEME_OPTION)).orElse(Theme.DEFAULT_THEME_ID);
    final Theme theme = Theme.getTheme(themeID).orElseThrow(() -> new ConfigException("undefined theme: " + themeID));

    final boolean syncTree = ini.get(APP_SECTION, SYNC_TREE_OPTION, boolean.class);
    Integer maxTreeHeight = ini.get(APP_SECTION, MAX_TREE_HEIGHT_OPTION, Integer.class);
    if (maxTreeHeight == null)
      maxTreeHeight = GeneticFamilyTreePane.DEFAULT_MAX_HEIGHT;

    final int dateFormatOrdinal = ini.get(APP_SECTION, DATE_FORMAT_OPTION, int.class);
    final int timeFormatOrdinal = ini.get(APP_SECTION, TIME_FORMAT_OPTION, int.class);
    final DateFormat dateFormat;
    final TimeFormat timeFormat;
    try {
      dateFormat = DateFormat.values()[dateFormatOrdinal];
      timeFormat = TimeFormat.values()[timeFormatOrdinal];
    } catch (final IndexOutOfBoundsException e) {
      throw new ConfigException(e);
    }

    final boolean showDeceasedPersonsBirthdays = ini.get(APP_SECTION, SHOW_DECEASED_BIRTHDAYS_OPTION, boolean.class);
    // Default to true if option is not present
    final boolean showLegends = ini.get(APP_SECTION, SHOW_LEGENDS) == null ||
        ini.get(APP_SECTION, SHOW_LEGENDS, boolean.class);

    try {
      return new Config(
          LANGUAGES.get(langCode),
          theme,
          syncTree,
          maxTreeHeight,
          dateFormat,
          timeFormat,
          showDeceasedPersonsBirthdays,
          showLegends,
          debug
      );
    } catch (final IllegalArgumentException e) {
      throw new ConfigException(e);
    }
  }

  /**
   * Return the Ini file designated by {@link #SETTINGS_FILE}. If the file does not exist, it is created.
   *
   * @return The {@link Wini} wrapper object.
   * @throws IOException If the file does not exist and could not be created.
   */
  private static Wini getOrCreateIniFile() throws IOException {
    if (!SETTINGS_FILE.exists() && !SETTINGS_FILE.createNewFile())
      throw new IOException("Could not create %s file!".formatted(SETTINGS_FILE));
    return new Wini(SETTINGS_FILE);
  }

  /**
   * Load resource bundles for all available languages and populate {@link #LANGUAGES} field.
   *
   * @throws IOException If any IO error occurs.
   */
  private static void loadLanguages() throws IOException {
    LANGUAGES.clear();
    for (final String langCode : LANGUAGE_CODES) {
      final ResourceBundle bundle = getResourceBundle(new Locale(langCode));
      if (bundle != null) {
        final String langName = bundle.getString("language_name");
        LANGUAGES.put(langCode, new Language(langCode, langName, new Locale(langCode), bundle));
      }
    }
    if (LANGUAGES.isEmpty())
      throw new IOException("No languages found");
  }

  /**
   * Return the resource bundle for the given locale.
   *
   * @param locale A locale.
   * @return The locale’s resources.
   */
  private static ResourceBundle getResourceBundle(@NotNull Locale locale) {
    return ResourceBundle.getBundle(
        App.RESOURCES_ROOT.substring(1).replace('/', '.') + "translations.ui",
        locale
    );
  }

  /**
   * List of all available languages.
   *
   * @return A new copy of the internal list.
   */
  public static List<Language> languages() {
    return LANGUAGES.values().stream().sorted(Comparator.comparing(Language::name)).toList();
  }

  private final Language language;
  private final Theme theme;
  private final boolean debug;
  private boolean syncTreeWithMainPane;
  private int maxTreeHeight;
  private DateFormat dateFormat;
  private TimeFormat timeFormat;
  private boolean showDeceasedPersonsBirthdays;
  private boolean showLegends;

  /**
   * Create a configuration object.
   *
   * @param language             Language to use.
   * @param theme                Theme to use.
   * @param syncTreeWithMainPane Whether the tree pane and view should be synchronized.
   * @param maxTreeHeight        Maximum number of levels to display above the center widget in the tree panel.
   * @param dateFormat           Date format.
   * @param timeFormat           Time format.
   * @param showLegends          Whether to show the legends in tree views.
   * @param debug                Whether to run the app in debug mode.
   */
  public Config(
      @NotNull Language language,
      @NotNull Theme theme,
      boolean syncTreeWithMainPane,
      int maxTreeHeight,
      @NotNull DateFormat dateFormat,
      @NotNull TimeFormat timeFormat,
      boolean showDeceasedPersonsBirthdays,
      boolean showLegends,
      boolean debug
  ) {
    this.language = Objects.requireNonNull(language);
    this.theme = Objects.requireNonNull(theme);
    this.setShouldSyncTreeWithMainPane(syncTreeWithMainPane);
    this.setMaxTreeHeight(maxTreeHeight);
    this.setDateFormat(dateFormat);
    this.setTimeFormat(timeFormat);
    this.setShouldShowDeceasedPersonsBirthdays(showDeceasedPersonsBirthdays);
    this.setShouldShowLegends(showLegends);
    this.debug = debug;
  }

  /**
   * The language to use.
   */
  public Language language() {
    return this.language;
  }

  /**
   * The theme to use.
   */
  public Theme theme() {
    return this.theme;
  }

  /**
   * Whether the tree pane and view should be synchronized.
   */
  public boolean shouldSyncTreeWithMainPane() {
    return this.syncTreeWithMainPane;
  }

  /**
   * Set whether the tree pane and view should be synchronized.
   *
   * @param syncTreeWithMainPane True to enable syncing, false to disable it.
   */
  public void setShouldSyncTreeWithMainPane(boolean syncTreeWithMainPane) {
    this.syncTreeWithMainPane = syncTreeWithMainPane;
  }

  /**
   * The maximum number of levels to display above the center widget in the tree panel.
   */
  public int maxTreeHeight() {
    return this.maxTreeHeight;
  }

  /**
   * Set the maximum number of levels to display above the center widget in the tree panel.
   *
   * @param height The new maximum height.
   */
  public void setMaxTreeHeight(int height) {
    if (height < GeneticFamilyTreePane.MIN_ALLOWED_HEIGHT || height > GeneticFamilyTreePane.MAX_ALLOWED_HEIGHT)
      throw new IllegalArgumentException("invalid max tree height");
    this.maxTreeHeight = height;
  }

  public DateFormat dateFormat() {
    return this.dateFormat;
  }

  public void setDateFormat(@NotNull DateFormat dateFormat) {
    this.dateFormat = Objects.requireNonNull(dateFormat);
  }

  public TimeFormat timeFormat() {
    return this.timeFormat;
  }

  public void setTimeFormat(@NotNull TimeFormat timeFormat) {
    this.timeFormat = Objects.requireNonNull(timeFormat);
  }

  public boolean shouldShowDeceasedPersonsBirthdays() {
    return this.showDeceasedPersonsBirthdays;
  }

  public void setShouldShowDeceasedPersonsBirthdays(boolean showDeceasedPersonsBirthdays) {
    this.showDeceasedPersonsBirthdays = showDeceasedPersonsBirthdays;
  }

  public boolean shouldShowLegends() {
    return this.showLegends;
  }

  public void setShouldShowLegends(boolean showLegends) {
    this.showLegends = showLegends;
  }

  /**
   * Whether the app is in debug mode.
   */
  public boolean isDebug() {
    return this.debug;
  }

  /**
   * Return a copy of this object and replace its language by the given one.
   *
   * @param language The language to use.
   * @return A new configuration object.
   */
  public Config withLanguage(@NotNull Language language) {
    return new Config(
        language,
        this.theme,
        this.syncTreeWithMainPane,
        this.maxTreeHeight,
        this.dateFormat,
        this.timeFormat,
        this.showDeceasedPersonsBirthdays,
        this.showLegends,
        this.debug
    );
  }

  /**
   * Return a copy of this object and replace its theme by the given one.
   *
   * @param theme The theme to use.
   * @return A new configuration object.
   */
  public Config withTheme(@NotNull Theme theme) {
    return new Config(
        this.language,
        theme,
        this.syncTreeWithMainPane,
        this.maxTreeHeight,
        this.dateFormat,
        this.timeFormat,
        this.showDeceasedPersonsBirthdays,
        this.showLegends,
        this.debug
    );
  }

  /**
   * Clone this object.
   *
   * @return A new deep copy of this object.
   */
  @Override
  public Config clone() {
    try {
      return (Config) super.clone();
    } catch (final CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Save this configuration object to the disk.
   */
  public void save() throws IOException {
    App.LOGGER.info("Saving config…");
    final Wini ini = getOrCreateIniFile();
    ini.put(APP_SECTION, LANGUAGE_OPTION, this.language.code());
    ini.put(APP_SECTION, THEME_OPTION, this.theme.id());
    ini.put(APP_SECTION, SYNC_TREE_OPTION, this.syncTreeWithMainPane);
    ini.put(APP_SECTION, MAX_TREE_HEIGHT_OPTION, this.maxTreeHeight);
    ini.put(APP_SECTION, DATE_FORMAT_OPTION, this.dateFormat.ordinal());
    ini.put(APP_SECTION, TIME_FORMAT_OPTION, this.timeFormat.ordinal());
    ini.put(APP_SECTION, SHOW_DECEASED_BIRTHDAYS_OPTION, this.showDeceasedPersonsBirthdays);
    ini.put(APP_SECTION, SHOW_LEGENDS, this.showLegends);
    ini.store();
    App.LOGGER.info("Done.");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || this.getClass() != o.getClass())
      return false;
    final Config that = (Config) o;
    return this.debug == that.debug
        && this.syncTreeWithMainPane == that.syncTreeWithMainPane
        && this.maxTreeHeight == that.maxTreeHeight
        && Objects.equals(this.language, that.language)
        && Objects.equals(this.theme, that.theme)
        && this.dateFormat == that.dateFormat
        && this.timeFormat == that.timeFormat
        && this.showLegends == that.showLegends
        && this.showDeceasedPersonsBirthdays == that.showDeceasedPersonsBirthdays;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        this.language,
        this.theme,
        this.debug,
        this.syncTreeWithMainPane,
        this.maxTreeHeight,
        this.dateFormat,
        this.timeFormat,
        this.showLegends,
        this.showDeceasedPersonsBirthdays
    );
  }
}
