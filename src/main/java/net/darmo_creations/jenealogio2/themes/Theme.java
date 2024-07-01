package net.darmo_creations.jenealogio2.themes;

import com.google.gson.*;
import javafx.scene.image.*;
import net.darmo_creations.jenealogio2.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.net.*;
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

  /**
   * Load all available themes.
   *
   * @throws IOException If no themes were found.
   */
  public static void loadThemes() throws IOException {
    THEMES.clear();
    for (String themeID : THEME_IDS) {
      try (var stream = Theme.class.getResourceAsStream(THEMES_PATH + themeID + ".json")) {
        if (stream != null)
          try (var reader = new InputStreamReader(stream)) {
            var data = new Gson().fromJson(reader, Map.class);
            THEMES.put(themeID, new Theme(themeID, (String) data.get("name")));
          }
      } catch (RuntimeException e) {
        App.LOGGER.exception(e);
      }
    }
    if (THEMES.isEmpty())
      throw new IOException("no themes found");
  }

  /**
   * Return the theme with the given ID.
   *
   * @param id ID of the theme to fetch.
   * @return The theme.
   */
  public static Optional<Theme> getTheme(@NotNull String id) {
    return Optional.ofNullable(THEMES.get(id));
  }

  /**
   * Return a list of all available themes.
   */
  public static List<Theme> themes() {
    return THEMES.values().stream().sorted(Comparator.comparing(Theme::name)).toList();
  }

  private final String id;
  private final String name;

  /**
   * Create a theme.
   *
   * @param id   Theme’s ID.
   * @param name Theme’s name.
   */
  private Theme(@NotNull String id, @NotNull String name) {
    this.id = Objects.requireNonNull(id);
    this.name = Objects.requireNonNull(name);
  }

  /**
   * Theme’s ID.
   */
  public String id() {
    return this.id;
  }

  /**
   * Theme’s name.
   */
  public String name() {
    return this.name;
  }

  /**
   * Return an {@link ImageView} for the given icon.
   *
   * @param icon The icon to load.
   * @param size Icon’s size.
   * @return An {@link ImageView} object or null if the icon could not be loaded.
   */
  public @Nullable ImageView getIcon(@NotNull Icon icon, @NotNull Icon.Size size) {
    Image image = this.getIconImage(icon, size);
    return image != null ? new ImageView(image) : null;
  }

  /**
   * Return an {@link Image} for the given icon.
   *
   * @param icon The icon to load.
   * @param size Icon’s size.
   * @return An {@link Image} object or null if the icon could not be loaded.
   */
  public @Nullable Image getIconImage(@NotNull Icon icon, @NotNull Icon.Size size) {
    String path = "%s%s_%d.png".formatted(ICONS_PATH, icon.baseName(), size.pixels());
    try (var stream = this.getClass().getResourceAsStream(path)) {
      if (stream == null) {
        App.LOGGER.warn("Missing icon: " + icon.baseName());
        return null;
      }
      return new Image(stream);
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * Get the app’s icon as an {@link Image}.
   */
  public @Nullable Image getAppIcon() {
    String path = "%s%s.png".formatted(App.IMAGES_PATH, "app_icon");
    try (var stream = this.getClass().getResourceAsStream(path)) {
      if (stream == null) {
        App.LOGGER.warn("Missing icon: app_icon");
        return null;
      }
      return new Image(stream);
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * Return the URLs of this theme’s stylesheets.
   */
  public List<URL> getStyleSheets() {
    List<URL> urls = new LinkedList<>();
    this.getStyleSheet("common").ifPresent(urls::add);
    this.getStyleSheet(this.id).ifPresent(urls::add);
    return urls;
  }

  /**
   * Get the URL of a stylesheet.
   *
   * @param name Stylesheet’s name.
   * @return The URL.
   */
  private Optional<URL> getStyleSheet(@NotNull String name) {
    String path = "%s%s.css".formatted(THEMES_PATH, name);
    return Optional.ofNullable(this.getClass().getResource(path));
  }

  @Override
  public String toString() {
    return this.name;
  }
}
