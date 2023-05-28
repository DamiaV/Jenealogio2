package net.darmo_creations.jenealogio2.utils;

import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Objects;

@SuppressWarnings("unused")
public final class Logger {
  private static final Level DEFAULT_LEVEL = Level.INFO;
  private static final String DEFAULT_FORMAT = "[${date}] [${level}] (${name}) ${message}";
  private static final PrintStream DEFAULT_OUTPUT = System.out;

  private final String name;
  private Level level = DEFAULT_LEVEL;
  private String format = DEFAULT_FORMAT;
  private PrintStream output = DEFAULT_OUTPUT;

  public Logger(@NotNull String name) {
    this.name = Objects.requireNonNull(name);
  }

  public String name() {
    return this.name;
  }

  public Level level() {
    return this.level;
  }

  public void setLevel(@NotNull Level level) {
    this.level = Objects.requireNonNull(level);
  }

  public void setFormat(@NotNull String format) {
    this.format = Objects.requireNonNull(format);
  }

  public void setOutput(@NotNull PrintStream output) {
    this.output = Objects.requireNonNull(output);
  }

  public void debug(String message) {
    this.log(message, Level.DEBUG);
  }

  public void debug(Object message) {
    this.log(Objects.toString(message), Level.DEBUG);
  }

  public void info(String message) {
    this.log(message, Level.INFO);
  }

  public void info(Object message) {
    this.log(Objects.toString(message), Level.INFO);
  }

  public void warn(String message) {
    this.log(message, Level.WARN);
  }

  public void warn(Object message) {
    this.log(Objects.toString(message), Level.WARN);
  }

  public void error(String message) {
    this.log(message, Level.ERROR);
  }

  public void error(Object message) {
    this.log(Objects.toString(message), Level.ERROR);
  }

  public void fatal(String message) {
    this.log(message, Level.FATAL);
  }

  public void fatal(Object message) {
    this.log(Objects.toString(message), Level.FATAL);
  }

  public void exception(@NotNull Throwable exception) {
    this.exception(exception, Level.ERROR);
  }

  public void exception(@NotNull Throwable exception, @NotNull Level level) {
    StringWriter out = new StringWriter();
    exception.printStackTrace(new PrintWriter(out));
    this.log(out.toString(), level);
  }

  private void log(String message, @NotNull Level level) {
    if (level.isAboveOrSame(this.level)) {
      String s = this.format
          .replace("${date}", DateTimeUtils.format(LocalDateTime.now()))
          .replace("${level}", level.name())
          .replace("${name}", this.name)
          .replace("${message}", message);
      this.output.println(s);
    }
  }

  public enum Level {
    DEBUG,
    INFO,
    WARN,
    ERROR,
    FATAL,
    ;

    public boolean isAboveOrSame(@NotNull Level level) {
      return this.ordinal() >= level.ordinal();
    }
  }
}
