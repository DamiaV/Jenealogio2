package net.darmo_creations.jenealogio2.config;

import org.jetbrains.annotations.*;

import java.util.*;

/**
 * This enum lists all available language settings.
 */
public enum LanguageSetting {
  /**
   * Select language depending on OS’s preference.
   */
  SYSTEM("system"),

  /**
   * English.
   */
  EN("en"),
  /**
   * Esperanto
   */
  EO("eo"),
  /**
   * French
   */
  FR("fr"),
  ;

  /**
   * An array containing all the settings that correspond to a specific theme.
   */
  public static final LanguageSetting[] LANGUAGES = {EN, EO, FR};

  private final String id;

  LanguageSetting(@NotNull String id) {
    this.id = Objects.requireNonNull(id);
  }

  public String id() {
    return this.id;
  }

  public static Optional<LanguageSetting> fromId(@NotNull String id) {
    for (final LanguageSetting theme : LANGUAGES)
      if (theme.id().equals(id))
        return Optional.of(theme);
    return Optional.empty();
  }
}
