package net.darmo_creations.jenealogio2;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.darmo_creations.jenealogio2.config.Config;
import net.darmo_creations.jenealogio2.config.ConfigException;
import net.darmo_creations.jenealogio2.utils.DateTimeUtils;
import net.darmo_creations.jenealogio2.utils.Logger;
import org.apache.commons.cli.*;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class App extends Application {
  public static final String NAME = "Jenealogio";
  public static final String VERSION = "2.0-SNAPSHOT";

  public static final Logger LOGGER = new Logger(NAME);

  public static final String RESOURCES_ROOT = "/net/darmo_creations/jenealogio2/";
  private static final String VIEWS_PATH = RESOURCES_ROOT + "views/";

  private static Config config;

  public static Config config() {
    return config;
  }

  private static ResourceBundle resourceBundle;

  public static ResourceBundle getResourceBundle() {
    if (resourceBundle == null) {
      resourceBundle = config().language().resources();
    }
    return resourceBundle;
  }

  public static FXMLLoader getFxmlLoader(@NotNull String fileName) {
    FXMLLoader loader = new FXMLLoader(App.class.getResource(VIEWS_PATH + fileName + ".fxml"));
    loader.setResources(getResourceBundle());
    return loader;
  }

  public static void updateConfig(@NotNull Config localConfig) {
    // TODO
  }

  @Override
  public void start(Stage stage) throws IOException {
    LOGGER.info("Running %s (v%s)".formatted(NAME, VERSION));
    if (config.isDebug()) {
      LOGGER.setLevel(Logger.Level.DEBUG);
      LOGGER.info("Debug mode is ON");
    }
    FXMLLoader loader = getFxmlLoader("main-window");
    Scene scene = new Scene(loader.load());
    config.theme().getStyleSheet().ifPresent(css -> scene.getStylesheets().add(css.toExternalForm()));
    stage.setMinWidth(300);
    stage.setMinHeight(200);
    stage.setTitle(NAME);
    stage.setScene(scene);
    stage.show();
  }

  public static void main(String[] args) {
    try {
      config = Config.loadConfig(parseArgs(args).debug());
    } catch (IOException | ParseException | ConfigException e) {
      generateCrashReport(e);
      System.exit(1);
    }
    try {
      launch(args);
    } catch (Exception e) {
      generateCrashReport(e.getCause()); // JavaFX wraps exceptions into a RuntimeException
      System.exit(2);
    }
  }

  private static Args parseArgs(@NotNull String[] args) throws ParseException {
    CommandLineParser parser = new DefaultParser();
    Options options = new Options();
    options.addOption(Option.builder("d")
        .desc("Run the application in debug mode")
        .longOpt("debug")
        .build());
    CommandLine commandLine = parser.parse(options, args);
    return new Args(commandLine.hasOption('d'));
  }

  public static void generateCrashReport(@NotNull Throwable e) {
    LocalDateTime date = LocalDateTime.now();
    StringWriter out = new StringWriter();
    e.printStackTrace(new PrintWriter(out));
    String message = """
        --- %s (v%s) Crash Report ---
                
        Time: %s
        Description: %s
                
        %s
        """.formatted(NAME, VERSION, DateTimeUtils.format(date), e.getMessage(), out);
    LOGGER.fatal(message);
    File logsDir = new File("logs");
    if (!logsDir.exists()) {
      //noinspection ResultOfMethodCallIgnored
      logsDir.mkdir();
    }
    String fileName = "crash_report_%s.log".formatted(DateTimeUtils.formatFileName(date));
    try (FileWriter fw = new FileWriter(new File(logsDir, fileName))) {
      fw.write(message);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  private record Args(boolean debug) {
  }
}
