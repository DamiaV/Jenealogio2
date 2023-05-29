package net.darmo_creations.jenealogio2.config;

import net.darmo_creations.jenealogio2.utils.FormatArg;
import net.darmo_creations.jenealogio2.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public record Language(@NotNull String code, @NotNull String name, @NotNull Locale locale,
                       @NotNull ResourceBundle resources) {
  public Language {
    Objects.requireNonNull(code);
    Objects.requireNonNull(name);
    Objects.requireNonNull(locale);
    Objects.requireNonNull(resources);
  }

  public String translate(@NotNull String key, final FormatArg @NotNull ... formatArgs) {
    String text = this.resources.getString(key);
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
