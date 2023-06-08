package net.darmo_creations.jenealogio2.model.calendar;

/**
 * Enumeration of available date precisions.
 */
public enum DatePrecision {
  /**
   * Date is exact.
   */
  EXACT,
  /**
   * Actual date is around the specified one.
   */
  ABOUT,
  /**
   * Actual date may be completely different than the specified one.
   */
  POSSIBLY,
  /**
   * Actual date is before the specified one.
   */
  BEFORE,
  /**
   * Actual date is after the specified one.
   */
  AFTER,
}
