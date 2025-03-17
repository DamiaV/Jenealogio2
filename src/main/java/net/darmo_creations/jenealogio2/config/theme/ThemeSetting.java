package net.darmo_creations.jenealogio2.config.theme;

import org.jetbrains.annotations.*;

import java.util.*;

/**
 * This enum lists all available theme settings.
 */
public enum ThemeSetting {
  /**
   * Select theme depending on OS’s preference.
   */
  SYSTEM("system"),
  /**
   * Dark theme.
   */
  DARK("dark"),
  /**
   * Light theme.
   */
  LIGHT("light"),
  ;

  /**
   * An array containing all the settings that correspond to a specific theme.
   */
  public static final ThemeSetting[] THEMES = {DARK, LIGHT};

  private final String id;

  ThemeSetting(@NotNull String id) {
    this.id = Objects.requireNonNull(id);
  }

  public String id() {
    return this.id;
  }

  public static Optional<ThemeSetting> fromId(@NotNull String id) {
    for (final ThemeSetting theme : THEMES)
      if (theme.id().equals(id))
        return Optional.of(theme);
    return Optional.empty();
  }
}
