package net.darmo_creations.jenealogio2.config;

import net.darmo_creations.jenealogio2.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * This class represents a language.
 *
 * @param code      Language’s code.
 * @param name      Language’s name in the language itself.
 * @param locale    Language’s locale.
 * @param resources Language’s resources, i.e. translations.
 */
public record Language(@NotNull String code, @NotNull String name, @NotNull Locale locale,
                       @NotNull ResourceBundle resources) {
  public Language {
    Objects.requireNonNull(code);
    Objects.requireNonNull(name);
    Objects.requireNonNull(locale);
    Objects.requireNonNull(resources);
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

  @Override
  public String toString() {
    return this.name;
  }
}
