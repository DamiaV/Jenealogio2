package net.darmo_creations.jenealogio2.themes;

import com.google.gson.Gson;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import net.darmo_creations.jenealogio2.App;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;
import java.util.*;

public final class Theme {
  private static final String BUILTIN_THEMES_PATH = "/net/darmo_creations/jenealogio2/themes/";
  private static final String[] BUILTIN_THEME_IDS = {
      "dark",
      "light",
  };
  public static final String DEFAULT_THEME_ID = BUILTIN_THEME_IDS[0];
  private static final File USER_THEMES_DIR = new File("themes");

  private static final Map<String, Theme> THEMES = new HashMap<>();

  public static void loadThemes() throws IOException {
    THEMES.clear();
    loadBuiltinThemes();
    loadUserThemes();
    if (THEMES.isEmpty()) {
      throw new IOException("no themes found");
    }
  }

  private static void loadBuiltinThemes() {
    for (String themeID : BUILTIN_THEME_IDS) {
      try (InputStream stream = Theme.class.getResourceAsStream(BUILTIN_THEMES_PATH + themeID + ".json")) {
        if (stream != null) {
          loadTheme(themeID, new InputStreamReader(stream));
        }
      } catch (Exception e) {
        App.LOGGER.exception(e);
      }
    }
  }

  private static void loadUserThemes() {
    File[] files = USER_THEMES_DIR.listFiles((directory, fileName) -> fileName.endsWith(".json"));
    if (files == null) {
      return;
    }
    for (File file : files) {
      try (FileReader reader = new FileReader(file)) {
        loadTheme(file.getName().split("\\.(?=[^.]+$)")[0], reader);
      } catch (Exception e) {
        App.LOGGER.exception(e);
      }
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
        "%sicons/%s_%d.png".formatted(App.RESOURCES_ROOT, icon.baseName(), size.pixels()));
    if (stream == null) {
      return null;
    }
    return new ImageView(new Image(stream));
  }

  public Optional<URL> getStyleSheet() {
    String path = "%s%s.css".formatted(BUILTIN_THEMES_PATH, this.id);
    return Optional.ofNullable(this.getClass().getResource(path));
  }

  @Override
  public String toString() {
    return this.name;
  }
}
