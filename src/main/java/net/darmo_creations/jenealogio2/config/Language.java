package net.darmo_creations.jenealogio2.config;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Objects;

public record Language(@NotNull String code, @NotNull String name, @NotNull Locale locale) {
  public Language {
    Objects.requireNonNull(code);
    Objects.requireNonNull(name);
    Objects.requireNonNull(locale);
  }

  @Override
  public String toString() {
    return this.name;
  }
}
