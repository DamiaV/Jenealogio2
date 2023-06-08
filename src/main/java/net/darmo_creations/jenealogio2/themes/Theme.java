package net.darmo_creations.jenealogio2.themes;

import com.google.gson.Gson;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import net.darmo_creations.jenealogio2.App;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.*;

/**
 * A theme defines the appearence of the application.
 * A theme is declared by a JSON file, whose name is the theme’s ID, containing its displayed name.
 * Themes may define a custom CSS file to apply to the app’s GUI. The CSS file’s name should also be the theme’s ID.
 */
public final class Theme {
  /**
   * Jar path to the directory containing theme files.
   */
  private static final String THEMES_PATH = App.RESOURCES_ROOT + "themes/";
  /**
   * Array of available themes IDs.
   */
  private static final String[] THEME_IDS = {
      "dark",
      "light",
  };
  public static final String DEFAULT_THEME_ID = THEME_IDS[0];

  private static final String ICONS_PATH = App.IMAGES_PATH + "icons/";

  private static final Map<String, Theme> THEMES = new HashMap<>();

  public static void loadThemes() throws IOException {
    THEMES.clear();
    for (String themeID : THEME_IDS) {
      try (InputStream stream = Theme.class.getResourceAsStream(THEMES_PATH + themeID + ".json")) {
        if (stream != null) {
          loadTheme(themeID, new InputStreamReader(stream));
        }
      } catch (Exception e) {
        App.LOGGER.exception(e);
      }
    }
    if (THEMES.isEmpty()) {
      throw new IOException("no themes found");
    }
  }

  @SuppressWarnings("unchecked")
  private static void loadTheme(@NotNull String id, @NotNull Reader reader) {
    Gson parser = new Gson();
    Map<String, Object> data = (Map<String, Object>) parser.fromJson(reader, HashMap.class);
    String name = (String) data.get("name");
    THEMES.put(id, new Theme(id, name));
  }

  public static Optional<Theme> getTheme(@NotNull String id) {
    return Optional.ofNullable(THEMES.get(id));
  }

  public static List<Theme> themes() {
    return THEMES.values().stream().sorted(Comparator.comparing(Theme::name)).toList();
  }

  private final String id;
  private final String name;

  private Theme(@NotNull String id, @NotNull String name) {
    this.id = Objects.requireNonNull(id);
    this.name = Objects.requireNonNull(name);
  }

  public String id() {
    return this.id;
  }

  public String name() {
    return this.name;
  }

  public Node getIcon(@NotNull Icon icon, @NotNull Icon.Size size) {
    InputStream stream = this.getClass().getResourceAsStream(
        "%s%s_%d.png".formatted(ICONS_PATH, icon.baseName(), size.pixels()));
    if (stream == null) {
      return null;
    }
    return new ImageView(new Image(stream));
  }

  public List<URL> getStyleSheets() {
    List<URL> urls = new LinkedList<>();
    this.getStyleSheet("common").ifPresent(urls::add);
    this.getStyleSheet(this.id).ifPresent(urls::add);
    return urls;
  }

  private Optional<URL> getStyleSheet(@NotNull String name) {
    String path = "%s%s.css".formatted(THEMES_PATH, name);
    return Optional.ofNullable(this.getClass().getResource(path));
  }

  @Override
  public String toString() {
    return this.name;
  }
}
