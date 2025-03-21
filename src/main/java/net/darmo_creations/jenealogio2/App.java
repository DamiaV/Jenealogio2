package net.darmo_creations.jenealogio2;

import javafx.application.*;
import javafx.stage.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.io.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.apache.commons.cli.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;

/**
 * Application’s main class.
 */
public class App extends Application {
  public static final String NAME = "Jenealogio";
  public static final String VERSION = "2.0-SNAPSHOT";

  public static final Logger LOGGER = new Logger(NAME);

  /**
   * Jar path to the resources’ root directory.
   */
  public static final String RESOURCES_ROOT = "/net/darmo_creations/jenealogio2/";
  /**
   * Jar path to the images directory.
   */
  public static final String IMAGES_PATH = RESOURCES_ROOT + "images/";

  /**
   * Path of the current working directory.
   */
  public static final Path CURRENT_DIR = Paths.get("").toAbsolutePath();
  /**
   * Path of the "temp" directory.
   */
  public static final Path TEMP_DIR = CURRENT_DIR.resolve("temp");
  /**
   * Path of the "user_data" directory containing all family trees this application manages.
   */
  public static final Path USER_DATA_DIR = CURRENT_DIR.resolve("user_data");

  /**
   * Application’s controller.
   */
  private static AppController controller;

  /**
   * App’s global configuration object.
   */
  private static Config config;

  /**
   * Application’s resource bundlo for the currently selected language.
   */
  private static ResourceBundle resourceBundle;

  /**
   * Return the resource bundle of the currently selected language.
   */
  public static ResourceBundle getResourceBundle() {
    if (resourceBundle == null)
      resourceBundle = config.language().resources();
    return resourceBundle;
  }

  private static TreesMetadataManager treesMetadataManager;

  public static TreesMetadataManager treesMetadataManager() {
    return treesMetadataManager;
  }

  private static String treeName;

  /**
   * Update the current configuration object with the given one.
   * <p>
   * Only the options that do <b>not</b> need a restart are copied.
   *
   * @param localConfig Configuration object to copy from.
   */
  public static void updateConfig(@NotNull Config localConfig) {
    config.setShouldSyncTreeWithMainPane(localConfig.shouldSyncTreeWithMainPane());
    config.setMaxTreeHeight(localConfig.maxTreeHeight());
    config.setDateFormat(localConfig.dateFormat());
    config.setTimeFormat(localConfig.timeFormat());
    config.setShouldShowDeceasedPersonsBirthdays(localConfig.shouldShowDeceasedPersonsBirthdays());
    controller.onConfigUpdate();
  }

  private static HostServices hostServices;

  /**
   * Open a URL in the user’s default web browser.
   *
   * @param url URL to open.
   */
  public static void openURL(@NotNull String url) {
    hostServices.showDocument(url);
  }

  @Override
  public void start(Stage stage) {
    LOGGER.info("Running %s (v%s)".formatted(NAME, VERSION));
    if (config.isDebug()) {
      LOGGER.setLevel(Logger.Level.DEBUG);
      LOGGER.info("Debug mode is ON");
    }
    hostServices = this.getHostServices();
    treesMetadataManager = new TreesMetadataManager();
    controller = new AppController(stage, config);
    controller.show(treeName);
  }

  public static void main(String[] args) {
    // For Gluon Maps
    System.setProperty("javafx.platform", "desktop");
    final Args parsedArgs;
    try {
      parsedArgs = parseArgs(args);
      config = Config.loadConfig(parsedArgs.debug());
    } catch (final IOException | ParseException | ConfigException e) {
      generateCrashReport(e);
      System.exit(1);
      return; // To shut up compiler errors
    }
    treeName = parsedArgs.treeName();
    try {
      launch();
    } catch (final Exception e) {
      generateCrashReport(e.getCause()); // JavaFX wraps exceptions into a RuntimeException
      System.exit(2);
    }
  }

  /**
   * Parse the CLI arguments.
   *
   * @param args Raw CLI arguments.
   * @return An object containing parsed arguments.
   * @throws ParseException If arguments could not be parsed.
   */
  private static Args parseArgs(@NotNull String[] args) throws ParseException {
    final CommandLineParser parser = new DefaultParser();
    final Options options = new Options();
    options.addOption(Option.builder("d")
        .desc("Run the application in debug mode")
        .longOpt("debug")
        .build());
    final CommandLine commandLine = parser.parse(options, args);
    final List<String> argList = commandLine.getArgList();
    final String treeName = argList.isEmpty() ? null : argList.get(0);
    return new Args(commandLine.hasOption('d'), treeName);
  }

  /**
   * Generate a crash report from the given throwable object.
   *
   * @param throwable The throwable object that caused the unrecoverable crash.
   */
  public static void generateCrashReport(@NotNull Throwable throwable) {
    final LocalDateTime date = LocalDateTime.now();
    final StringWriter out = new StringWriter();
    try (final var s = new PrintWriter(out)) {
      throwable.printStackTrace(s);
    }
    final String template = """
        --- %s (v%s) Crash Report ---
        
        Time: %s
        Description: %s
        
        -- Detailled Stack Trace --
        %s
        
        -- Technical Information --
        System properties:
        %s
        """;
    final String message = template.formatted(
        NAME,
        VERSION,
        DateTimeUtils.format(date),
        throwable.getMessage(),
        out,
        getSystemProperties()
    );
    LOGGER.fatal(message);
    final Path logsDir = App.CURRENT_DIR.resolve("logs");
    if (!Files.exists(logsDir))
      try {
        Files.createDirectory(logsDir);
      } catch (final IOException e) {
        throw new RuntimeException(e);
      }
    final String fileName = "crash_report_%s.log".formatted(DateTimeUtils.formatFileName(date));
    try (final var fw = new FileWriter(logsDir.resolve(fileName).toFile())) {
      fw.write(message);
    } catch (final IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Return a list of some system properties.
   */
  public static String getSystemProperties() {
    final StringJoiner systemProperties = new StringJoiner("\n");
    final String userHome = System.getProperty("user.home");
    final String userName = System.getProperty("user.name");
    System.getProperties().entrySet().stream()
        .filter(entry -> {
          final String key = entry.getKey().toString();
          return !key.equals("user.home") && !key.equals("user.name");
        })
        .map(entry -> {
          final Object key = entry.getKey();
          String value = entry.getValue().toString();
          if (value.contains(userHome))
            value = value.replace(userHome, "~");
          if (value.contains(userName))
            value = value.replace(userName, "*USERNAME*");
          if (value.contains("\n"))
            value = value.replace("\n", "\\n");
          if (value.contains("\r"))
            value = value.replace("\n", "\\r");
          return new Pair<>(key, value);
        })
        .sorted(Comparator.comparing(entry -> entry.left().toString()))
        .forEach(property -> systemProperties.add("%s: %s".formatted(property.left(), property.right())));
    return systemProperties.toString();
  }

  /**
   * Class holding parsed CLI arguments.
   *
   * @param debug    Whether to run the app in debug mode.
   * @param treeName Optional name of a tree to open.
   */
  private record Args(boolean debug, @Nullable String treeName) {
  }
}
