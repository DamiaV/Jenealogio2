package net.darmo_creations.jenealogio2.model;

/**
 * Enumeration of all available person life statuses.
 */
public enum LifeStatus {
  /**
   * The person is known to be alive.
   */
  LIVING,
  /**
   * The person is known to be deceased.
   */
  DECEASED,
  /**
   * The person may be alive.
   */
  MAYBE_LIVING,
  /**
   * The person may be deceased.
   */
  PROBABLY_DECEASED,
  ;

  /**
   * Indicate whether this status indicates the death of the associated person.
   */
  public boolean isConsideredDeceased() {
    return this == DECEASED || this == PROBABLY_DECEASED;
  }
}
