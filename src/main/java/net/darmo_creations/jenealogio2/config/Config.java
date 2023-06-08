package net.darmo_creations.jenealogio2.config;

import net.darmo_creations.jenealogio2.App;
import net.darmo_creations.jenealogio2.themes.Theme;
import net.darmo_creations.jenealogio2.utils.StringUtils;
import org.ini4j.Wini;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
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
  };
  private static final String DEFAULT_LANGUAGE_CODE = LANGUAGE_CODES[0];
  private static final Map<String, Language> LANGUAGES = new HashMap<>();

  private static final File SETTINGS_FILE = new File("settings.ini");

  private static final String APP_SECTION = "App";
  private static final String LANGUAGE_OPTION = "language";
  private static final String THEME_OPTION = "theme";
  private static final String SYNC_TREE_OPTION = "sync_tree";

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
    //noinspection MismatchedQueryAndUpdateOfCollection
    Wini ini = new Wini(SETTINGS_FILE);
    String langCode = StringUtils.stripNullable(ini.get(APP_SECTION, LANGUAGE_OPTION)).orElse(DEFAULT_LANGUAGE_CODE);
    if (!LANGUAGES.containsKey(langCode)) {
      throw new ConfigException("unsupported language code: " + langCode);
    }
    String themeID = StringUtils.stripNullable(ini.get(APP_SECTION, THEME_OPTION)).orElse(Theme.DEFAULT_THEME_ID);
    Theme theme = Theme.getTheme(themeID).orElseThrow(() -> new ConfigException("undefined theme: " + themeID));
    Boolean syncTree = ini.get(APP_SECTION, SYNC_TREE_OPTION, boolean.class);
    return new Config(
        LANGUAGES.get(langCode),
        theme,
        syncTree != null && syncTree,
        debug
    );
  }

  /**
   * Load resource bundles for all available languages and populate {@link #LANGUAGES} field.
   *
   * @throws IOException If any IO error occurs.
   */
  private static void loadLanguages() throws IOException {
    LANGUAGES.clear();
    for (String langCode : LANGUAGE_CODES) {
      ResourceBundle bundle = getResourceBundle(new Locale(langCode));
      if (bundle != null) {
        String langName = bundle.getString("language_name");
        LANGUAGES.put(langCode, new Language(langCode, langName, new Locale(langCode), bundle));
      }
    }
    if (LANGUAGES.isEmpty()) {
      throw new IOException("no languages found");
    }
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

  /**
   * Create a configuration object.
   *
   * @param language             Language to use.
   * @param theme                Theme to use.
   * @param syncTreeWithMainPane Whether the tree pane and view should be synchronized.
   * @param debug                Whether to run the app in debug mode.
   */
  public Config(@NotNull Language language, @NotNull Theme theme, boolean syncTreeWithMainPane, boolean debug) {
    this.language = Objects.requireNonNull(language);
    this.theme = Objects.requireNonNull(theme);
    this.syncTreeWithMainPane = syncTreeWithMainPane;
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
        this.syncTreeWithMainPane, this.debug
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
        this.syncTreeWithMainPane, this.debug
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
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Save this configuration object to the disk.
   */
  public void save() throws IOException {
    App.LOGGER.info("Saving config…");
    Wini ini = new Wini(SETTINGS_FILE);
    ini.put(APP_SECTION, LANGUAGE_OPTION, this.language.code());
    ini.put(APP_SECTION, THEME_OPTION, this.theme.id());
    ini.put(APP_SECTION, SYNC_TREE_OPTION, this.syncTreeWithMainPane);
    ini.store();
    App.LOGGER.info("Done.");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }
    Config config = (Config) o;
    return this.debug == config.debug && Objects.equals(this.language, config.language) && Objects.equals(this.theme, config.theme);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.language, this.theme, this.debug);
  }
}
