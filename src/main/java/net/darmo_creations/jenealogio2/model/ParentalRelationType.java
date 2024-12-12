package net.darmo_creations.jenealogio2.model;

import java.util.*;

/**
 * This enum defines the possible relations between parents and children.
 */
public enum ParentalRelationType {
  /**
   * A parent that had a child with their own egg/sperm with their partner.
   */
  BIOLOGICAL_PARENT(2, true),
  /**
   * A parent that has no biological relation with the child
   * (e.g. in a same-sex marriage where one of the parents has had an egg/sperm donor).
   */
  NON_BIOLOGICAL_PARENT,
  /**
   * A person that donated their eggs to another family’s parent.
   */
  EGG_DONOR(1, true),
  /**
   * A person that donated their sperm to another family’s parent.
   */
  SPERM_DONOR(1, true),
  /**
   * A person that bore the child for parents that could not themselves.
   */
  SURROGATE_PARENT(1, false),
  ADOPTIVE_PARENT,
  FOSTER_PARENT,
  GODPARENT,
  ;

  public static final ParentalRelationType[] GENETIC_RELATIONS;

  static {
    final List<ParentalRelationType> geneticRelations = new LinkedList<>();
    for (final var type : values())
      if (type.geneticRelation())
        geneticRelations.add(type);
    GENETIC_RELATIONS = geneticRelations.toArray(new ParentalRelationType[0]);
  }

  private final Integer maxParentsCount;
  private final boolean geneticRelation;

  ParentalRelationType() {
    this.maxParentsCount = null;
    this.geneticRelation = false;
  }

  ParentalRelationType(int maxParentsCount, boolean geneticRelation) {
    this.maxParentsCount = maxParentsCount;
    this.geneticRelation = geneticRelation;
  }

  /**
   * The maximum number of parents of this type that are allowed for a child.
   */
  public Optional<Integer> maxParentsCount() {
    return Optional.ofNullable(this.maxParentsCount);
  }

  public boolean geneticRelation() {
    return this.geneticRelation;
  }
}
