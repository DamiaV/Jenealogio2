package net.darmo_creations.jenealogio2.model;

import org.jetbrains.annotations.*;

import java.util.*;

/**
 * This class indicates what data should be used when merging two persons’ data.
 */
public record PersonMergeInfo(
    @NotNull Which lifeStatus,
    @NotNull Which legalLastName,
    @NotNull Which legalFirstNames,
    @NotNull Which publicLastName,
    @NotNull Which publicFirstNames,
    @NotNull Which nicknames,
    @NotNull Which agab,
    @NotNull Which gender,
    @NotNull Which disambiguationId,
    @NotNull Which mainOccupation,
    @NotNull Map<ParentalRelationType, Which> parents,
    @NotNull Map<ParentalRelationType, Which> children
) {
  public PersonMergeInfo {
    Objects.requireNonNull(lifeStatus);
    if (lifeStatus == Which.BOTH)
      throw new IllegalArgumentException("lifeStatus == BOTH");
    Objects.requireNonNull(legalLastName);
    if (legalLastName == Which.BOTH)
      throw new IllegalArgumentException("legalLastName == BOTH");
    Objects.requireNonNull(legalFirstNames);
    Objects.requireNonNull(publicLastName);
    if (publicLastName == Which.BOTH)
      throw new IllegalArgumentException("publicLastName == BOTH");
    Objects.requireNonNull(publicFirstNames);
    Objects.requireNonNull(nicknames);
    Objects.requireNonNull(agab);
    if (agab == Which.BOTH)
      throw new IllegalArgumentException("agab == BOTH");
    Objects.requireNonNull(gender);
    if (gender == Which.BOTH)
      throw new IllegalArgumentException("gender == BOTH");
    Objects.requireNonNull(disambiguationId);
    if (disambiguationId == Which.BOTH)
      throw new IllegalArgumentException("disambiguationId == BOTH");
    Objects.requireNonNull(mainOccupation);
    Objects.requireNonNull(parents);
    Objects.requireNonNull(children);
  }

  public enum Which {
    /**
     * Keep only the left person’s data.
     */
    LEFT,
    /**
     * Keep only the right person’s data.
     */
    RIGHT,
    /**
     * Merge both persons’ data. The left person’s data preceeds the right person’s.
     */
    BOTH,
    /**
     * The data of both persons is ignored.
     */
    NONE,
  }
}
