package net.darmo_creations.jenealogio2.config;

import net.darmo_creations.jenealogio2.App;
import net.darmo_creations.jenealogio2.themes.Theme;
import net.darmo_creations.jenealogio2.utils.StringUtils;
import org.ini4j.Wini;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class Config implements Cloneable {
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
    return new Config(
        LANGUAGES.get(langCode),
        theme,
        debug
    );
  }

  private static void loadLanguages() throws IOException {
    LANGUAGES.clear();
    for (String langCode : LANGUAGE_CODES) {
      ResourceBundle bundle = getResourceBundle(langCode);
      if (bundle != null) {
        String langName = bundle.getString("language_name");
        LANGUAGES.put(langCode, new Language(langCode, langName, new Locale(langCode)));
      }
    }
    if (LANGUAGES.isEmpty()) {
      throw new IOException("no languages found");
    }
  }

  private static ResourceBundle getResourceBundle(@NotNull String langCode) {
    return ResourceBundle.getBundle(
        App.RESOURCES_ROOT.substring(1).replace('/', '.') + "translations.ui",
        new Locale(langCode)
    );
  }

  public static List<Language> languages() {
    return LANGUAGES.values().stream().sorted(Comparator.comparing(Language::name)).toList();
  }

  private final Language language;
  private final Theme theme;
  private final boolean debug;

  public Config(@NotNull Language language, @NotNull Theme theme, boolean debug) {
    this.language = Objects.requireNonNull(language);
    this.theme = Objects.requireNonNull(theme);
    this.debug = debug;
  }

  public Language language() {
    return this.language;
  }

  public Theme theme() {
    return this.theme;
  }

  public boolean isDebug() {
    return this.debug;
  }

  public Config withLanguage(@NotNull Language language) {
    return new Config(
        language,
        this.theme,
        this.debug
    );
  }

  public Config withTheme(@NotNull Theme theme) {
    return new Config(
        this.language,
        theme,
        this.debug
    );
  }

  @Override
  public Config clone() {
    try {
      return (Config) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  public void save() throws IOException {
    Wini ini = new Wini(SETTINGS_FILE);
    ini.put(APP_SECTION, LANGUAGE_OPTION, this.language.code());
    ini.put(APP_SECTION, THEME_OPTION, this.theme.id());
    ini.store();
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
