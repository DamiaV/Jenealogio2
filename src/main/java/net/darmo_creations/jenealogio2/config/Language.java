package net.darmo_creations.jenealogio2.config;

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

  @Override
  public String toString() {
    return this.name;
  }
}
