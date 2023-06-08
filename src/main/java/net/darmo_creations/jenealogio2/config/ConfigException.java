package net.darmo_creations.jenealogio2.config;

/**
 * Exception indicating that a problem occured while loading a configuration file.
 */
public class ConfigException extends Exception {
  public ConfigException(String message) {
    super(message);
  }
}
