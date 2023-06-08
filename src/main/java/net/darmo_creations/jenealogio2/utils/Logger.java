package net.darmo_creations.jenealogio2.utils;

import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Simple custom logger implementation.
 */
@SuppressWarnings("unused")
public final class Logger {
  private static final Level DEFAULT_LEVEL = Level.INFO;
  private static final String DEFAULT_FORMAT = "[${date}] [${level}] (${name}) ${message}";
  private static final PrintStream DEFAULT_OUTPUT = System.out;

  private final String name;
  private Level level = DEFAULT_LEVEL;
  private String format = DEFAULT_FORMAT;
  private PrintStream output = DEFAULT_OUTPUT;

  /**
   * Create a named logger.
   *
   * @param name Logger’s name.
   */
  public Logger(@NotNull String name) {
    this.name = Objects.requireNonNull(name);
  }

  /**
   * Logger’s name.
   */
  public String name() {
    return this.name;
  }

  /**
   * Logger’s level.
   */
  public Level level() {
    return this.level;
  }

  /**
   * Set this logger’s level.
   *
   * @param level New logger’s level.
   */
  public void setLevel(@NotNull Level level) {
    this.level = Objects.requireNonNull(level);
  }

  /**
   * Set this logger’s message format.
   *
   * @param format New message format.
   */
  public void setFormat(@NotNull String format) {
    this.format = Objects.requireNonNull(format);
  }

  /**
   * Set this logger’s output stream.
   *
   * @param output New output stream.
   */
  public void setOutput(@NotNull PrintStream output) {
    this.output = Objects.requireNonNull(output);
  }

  /**
   * Log a message with the {@link Level#DEBUG} level.
   *
   * @param message Message to log.
   */
  public void debug(String message) {
    this.log(message, Level.DEBUG);
  }

  /**
   * Log an object with the {@link Level#DEBUG} level.
   *
   * @param message Object to log.
   */
  public void debug(Object message) {
    this.log(Objects.toString(message), Level.DEBUG);
  }

  /**
   * Log a message with the {@link Level#INFO} level.
   *
   * @param message Message to log.
   */
  public void info(String message) {
    this.log(message, Level.INFO);
  }

  /**
   * Log an object with the {@link Level#INFO} level.
   *
   * @param message Object to log.
   */
  public void info(Object message) {
    this.log(Objects.toString(message), Level.INFO);
  }

  /**
   * Log a message with the {@link Level#WARN} level.
   *
   * @param message Message to log.
   */
  public void warn(String message) {
    this.log(message, Level.WARN);
  }

  /**
   * Log an object with the {@link Level#WARN} level.
   *
   * @param message Object to log.
   */
  public void warn(Object message) {
    this.log(Objects.toString(message), Level.WARN);
  }

  /**
   * Log a message with the {@link Level#ERROR} level.
   *
   * @param message Message to log.
   */
  public void error(String message) {
    this.log(message, Level.ERROR);
  }

  /**
   * Log an object with the {@link Level#ERROR} level.
   *
   * @param message Object to log.
   */
  public void error(Object message) {
    this.log(Objects.toString(message), Level.ERROR);
  }

  /**
   * Log a message with the {@link Level#FATAL} level.
   *
   * @param message Message to log.
   */
  public void fatal(String message) {
    this.log(message, Level.FATAL);
  }

  /**
   * Log an object with the {@link Level#FATAL} level.
   *
   * @param message Object to log.
   */
  public void fatal(Object message) {
    this.log(Objects.toString(message), Level.FATAL);
  }

  /**
   * Log an throwable object with the {@link Level#ERROR} level.
   *
   * @param exception Throwable object to log.
   */
  public void exception(@NotNull Throwable exception) {
    this.exception(exception, Level.ERROR);
  }

  /**
   * Log an throwable object with the given level.
   *
   * @param exception Throwable object to log.
   * @param level     Logging level.
   */
  public void exception(@NotNull Throwable exception, @NotNull Level level) {
    StringWriter out = new StringWriter();
    exception.printStackTrace(new PrintWriter(out));
    this.log(out.toString(), level);
  }

  /**
   * Log a message with the given level.
   *
   * @param message Message to log.
   * @param level   Logging level.
   */
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

  /**
   * This enum represents all available logging levels.
   */
  public enum Level {
    /**
     * Lowest level, reserved for debugging purposes.
     */
    DEBUG,
    /**
     * Indicates a simple information message.
     */
    INFO,
    /**
     * Indicates a warning message.
     */
    WARN,
    /**
     * Indicates that a recoverable error occured.
     */
    ERROR,
    /**
     * Indicates that an unrecoverable error occured and the app will close.
     */
    FATAL,
    ;

    /**
     * Indicate whether this level is the same or above the given one.
     *
     * @param level Other level.
     * @return True if this level is equal or above the given one, false otherwise.
     */
    public boolean isAboveOrSame(@NotNull Level level) {
      return this.ordinal() >= level.ordinal();
    }
  }
}
