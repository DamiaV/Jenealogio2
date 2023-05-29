package net.darmo_creations.jenealogio2.model;

public enum LifeStatus {
  LIVING,
  DECEASED,
  MAYBE_LIVING,
  PROBABLY_DECEASED,
  ;

  public boolean isConsideredAlive() {
    return this == LIVING || this == MAYBE_LIVING;
  }

  public boolean isConsideredDeceased() {
    return this == DECEASED || this == PROBABLY_DECEASED;
  }
}
