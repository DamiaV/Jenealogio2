package net.darmo_creations.jenealogio2.model;

import net.darmo_creations.jenealogio2.model.LifeEventType.RegistryArgs;

/**
 * This class declares all registries used throughout the app.
 */
public final class Registries {
  /**
   * Registry declaring all available person genders.
   */
  public static final Registry<Gender, String> GENDERS = new GenderRegistry();

  /**
   * Registry declaring all available life event types.
   */
  public static final Registry<LifeEventType, RegistryArgs> LIFE_EVENT_TYPES = new LifeEventTypeRegistry();

  private Registries() {
  }
}
