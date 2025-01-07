package net.darmo_creations.jenealogio2.model;

import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.datetime.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * This class represents a person, deceased or alive.
 */
public class Person extends GenealogyObject<Person> {
  public static final String NAME_SEPARATOR = " ";

  private static final Comparator<Person> LAST_THEN_FIRST_NAMES_COMPARATOR = (p1, p2) -> {
    int c1 = p1.getLastName().orElse("")
        .compareTo(p2.getLastName().orElse(""));
    if (c1 != 0) return c1;
    int c2 = p1.getFirstNames().orElse("")
        .compareTo(p2.getFirstNames().orElse(""));
    if (c2 != 0) return c2;
    return p1.disambiguationID().orElse(-1)
        .compareTo(p2.disambiguationID().orElse(-1));
  };
  private static final Function<Boolean, Comparator<Person>> BIRTH_DATE_THEN_NAME_COMPARATOR_FACTORY = reversed -> (p1, p2) -> {
    Optional<DateTime> birthDate1 = p1.getBirthDate();
    Optional<DateTime> birthDate2 = p2.getBirthDate();
    if (birthDate1.isPresent() && birthDate2.isPresent()) {
      int c = birthDate1.get().compareTo(birthDate2.get());
      if (reversed) c = -c;
      if (c != 0) return c;
    }
    int c = lastThenFirstNamesComparator().compare(p1, p2);
    if (reversed) c = -c;
    return c;
  };
  private static final Comparator<Person> BIRTH_DATE_THEN_NAME_COMPARATOR =
      BIRTH_DATE_THEN_NAME_COMPARATOR_FACTORY.apply(false);
  private static final Comparator<Person> BIRTH_DATE_THEN_NAME_COMPARATOR_REVERSED =
      BIRTH_DATE_THEN_NAME_COMPARATOR_FACTORY.apply(true);

  private FamilyTree familyTree;
  private Integer disambiguationID;
  private LifeStatus lifeStatus = LifeStatus.LIVING;
  private final List<String> legalFirstNames = new ArrayList<>();
  private String legalLastName;
  private final List<String> publicFirstNames = new ArrayList<>();
  private String publicLastName;
  private final List<String> nicknames = new ArrayList<>();
  private Gender assignedGenderAtBirth;
  private Gender gender;
  private String mainOccupation;
  /**
   * We assume exactly two parents per person.
   */
  private final Map<ParentalRelationType, Set<Person>> parents = new HashMap<>();
  private final Map<ParentalRelationType, Set<Person>> children = new HashMap<>();
  /**
   * Ordered list of all life events this person was an actor in.
   */
  private final List<LifeEvent> lifeEvents = new ArrayList<>();

  /**
   * Create a new alive person.
   */
  public Person() {
    for (final var relationType : ParentalRelationType.values()) {
      this.parents.put(relationType, new HashSet<>());
      this.children.put(relationType, new HashSet<>());
    }
  }

  /**
   * The family tree this person belongs to.
   */
  public FamilyTree familyTree() {
    return this.familyTree;
  }

  /**
   * Set the {@link FamilyTree} this person belongs to.
   *
   * @param familyTree A family tree.
   */
  void setFamilyTree(@NotNull FamilyTree familyTree) {
    this.familyTree = Objects.requireNonNull(familyTree);
  }

  @Override
  public String name(@NotNull Language language) {
    return this.toString();
  }

  /**
   * This person’s disambiguation ID.
   *
   * @see #setDisambiguationID(Integer)
   */
  public Optional<Integer> disambiguationID() {
    return Optional.ofNullable(this.disambiguationID);
  }

  /**
   * Set this person’s disambiguation ID.
   * <p>
   * This ID is intended to more easily differentiate two persons with the exact same names.
   *
   * @param disambiguationID The ID. May be null.
   * @return This object.
   */
  @Contract("_ -> this")
  public Person setDisambiguationID(Integer disambiguationID) {
    if (disambiguationID != null && disambiguationID < 1)
      throw new IllegalArgumentException("Disambiguation ID must be > 0");
    this.disambiguationID = disambiguationID;
    return this;
  }

  /**
   * This person’s life status.
   */
  public LifeStatus lifeStatus() {
    return this.lifeStatus;
  }

  /**
   * Set this person’s life status.
   *
   * @param lifeStatus The new life status.
   * @return This object.
   * @throws IllegalArgumentException If the person is an actor of any event that indicates their death
   *                                  but the argument is not {@link LifeStatus#DECEASED}.
   */
  @Contract("_ -> this")
  public Person setLifeStatus(@NotNull LifeStatus lifeStatus) {
    final boolean isDead = this.getLifeEventsAsActor().stream().anyMatch(e -> e.type().indicatesDeath());
    if (isDead && lifeStatus != LifeStatus.DECEASED)
      throw new IllegalArgumentException("cannot change status of a person with at least one event indicating death");
    this.lifeStatus = Objects.requireNonNull(lifeStatus);
    return this;
  }

  /**
   * A list of this person’s legal firt names.
   *
   * @see #setLegalFirstNames(List)
   */
  @Contract("-> new")
  public List<String> legalFirstNames() {
    return new ArrayList<>(this.legalFirstNames);
  }

  /**
   * This person’s legal first names joined by spaces.
   *
   * @see #setLegalFirstNames(List)
   */
  public Optional<String> getJoinedLegalFirstNames() {
    return StringUtils.stripNullable(String.join(NAME_SEPARATOR, this.legalFirstNames));
  }

  /**
   * Set this person’s legal first names.
   * <p>
   * Legal first names are the first names recognized by legal institutions.
   *
   * @param legalFirstNames List of names.
   * @return This object.
   */
  @Contract("_ -> this")
  public Person setLegalFirstNames(final @NotNull List<String> legalFirstNames) {
    this.legalFirstNames.clear();
    this.legalFirstNames.addAll(this.filterOutEmptyStrings(legalFirstNames));
    return this;
  }

  /**
   * This person’s legal last name.
   *
   * @see #setLegalLastName(String)
   */
  public Optional<String> legalLastName() {
    return Optional.ofNullable(this.legalLastName);
  }

  /**
   * Set this person’s legal last name.
   * <p>
   * Legal last name is the last name recognized by legal institutions.
   *
   * @param legalLastName The name.
   * @return This object.
   */
  @Contract("_ -> this")
  public Person setLegalLastName(String legalLastName) {
    this.legalLastName = StringUtils.stripNullable(legalLastName).orElse(null);
    return this;
  }

  /**
   * This person’s public firt names.
   *
   * @see #setPublicFirstNames(List)
   */
  @Contract("-> new")
  public List<String> publicFirstNames() {
    return new ArrayList<>(this.publicFirstNames);
  }

  /**
   * This person’s public first names joined by spaces.
   *
   * @see #setPublicFirstNames(List)
   */
  public Optional<String> getJoinedPublicFirstNames() {
    return StringUtils.stripNullable(String.join(NAME_SEPARATOR, this.publicFirstNames));
  }

  /**
   * Set this person’s public first names.
   * <p>
   * Public first names are the first names used daily by the person but not recognized by legal institutions.
   *
   * @param publicFirstNames List of names.
   * @return This object.
   */
  @Contract("_ -> this")
  public Person setPublicFirstNames(final @NotNull List<String> publicFirstNames) {
    this.publicFirstNames.clear();
    this.publicFirstNames.addAll(this.filterOutEmptyStrings(publicFirstNames));
    return this;
  }

  /**
   * This person’s public last name.
   *
   * @see #setPublicLastName(String)
   */
  public Optional<String> publicLastName() {
    return Optional.ofNullable(this.publicLastName);
  }

  /**
   * Set this person’s public last name.
   * <p>
   * Public last name is the last name used in daily life by the person, such as spousal name.
   *
   * @param publicLastName The name.
   * @return This object.
   */
  @Contract("_ -> this")
  public Person setPublicLastName(String publicLastName) {
    this.publicLastName = StringUtils.stripNullable(publicLastName).orElse(null);
    return this;
  }

  /**
   * This person’s nicknames.
   */
  @Contract("-> new")
  public List<String> nicknames() {
    return new ArrayList<>(this.nicknames);
  }

  /**
   * This person’s nicknames joined by spaces.
   */
  public Optional<String> getJoinedNicknames() {
    return StringUtils.stripNullable(String.join(NAME_SEPARATOR, this.nicknames));
  }

  /**
   * Set this person’s nicknames.
   *
   * @param nicknames The names.
   */
  @Contract("_ -> this")
  public Person setNicknames(final @NotNull List<String> nicknames) {
    this.nicknames.clear();
    this.nicknames.addAll(this.filterOutEmptyStrings(nicknames));
    return this;
  }

  /**
   * Return this person’s last name.
   * It is either the legal one if defined or the public one if not.
   */
  public Optional<String> getLastName() {
    return this.legalLastName().or(this::publicLastName);
  }

  /**
   * Return this person’s first names.
   * It is either the legal ones if defined or the public ones if not.
   */
  public Optional<String> getFirstNames() {
    return this.getJoinedLegalFirstNames().or(this::getJoinedPublicFirstNames);
  }

  /**
   * Check whether any names of this person contain the given string, regardless of case.
   *
   * @param s        The string to check.
   * @param language The language to use for lower-case conversions.
   * @return True if any match was found, false otherwise.
   */
  public boolean matchesName(@NotNull String s, @NotNull Language language) {
    final Locale locale = language.locale();
    final String lcString = s.toLowerCase(locale);
    final Predicate<String> p = n -> n != null && n.toLowerCase(locale).contains(lcString);
    return p.test(this.legalLastName)
        || p.test(this.publicLastName)
        || this.legalFirstNames.stream().anyMatch(p)
        || this.publicFirstNames.stream().anyMatch(p)
        || this.nicknames.stream().anyMatch(p);
  }

  /**
   * This person’s assigned gender at birth.
   */
  public Optional<Gender> assignedGenderAtBirth() {
    return Optional.ofNullable(this.assignedGenderAtBirth);
  }

  /**
   * Set this person’s assigned gender at birth (AGAB).
   *
   * @param assignedGenderAtBirth The AGAB.
   * @return This object.
   */
  @Contract("_ -> this")
  public Person setAssignedGenderAtBirth(Gender assignedGenderAtBirth) {
    this.assignedGenderAtBirth = assignedGenderAtBirth;
    return this;
  }

  /**
   * This person’s gender.
   */
  public Optional<Gender> gender() {
    return Optional.ofNullable(this.gender).or(this::assignedGenderAtBirth);
  }

  /**
   * Set this person’s gender.
   *
   * @param gender The gender.
   * @return This object.
   */
  @Contract("_ -> this")
  public Person setGender(Gender gender) {
    this.gender = gender;
    return this;
  }

  /**
   * This person’s main occupation during its life.
   */
  public Optional<String> mainOccupation() {
    return Optional.ofNullable(this.mainOccupation);
  }

  /**
   * Set this person’s main occupation during its life.
   *
   * @param mainOccupation The occupation.
   */
  @Contract("_ -> this")
  public Person setMainOccupation(String mainOccupation) {
    this.mainOccupation = mainOccupation;
    return this;
  }

  /**
   * The collection of parents for this person.
   *
   * @return A map of this person’s parents.
   */
  @Contract("-> new")
  public Map<ParentalRelationType, Set<Person>> parents() {
    return this.parents.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> new HashSet<>(e.getValue())));
  }

  /**
   * The set of parents of the given type for this person.
   *
   * @param type The type of parents to return.
   * @return An unmodifiable view of this person’s parents of the given type.
   */
  @Contract("_ -> new")
  @UnmodifiableView
  public Set<Person> parents(@NotNull ParentalRelationType type) {
    return Collections.unmodifiableSet(this.parents.get(type));
  }

  /**
   * Add a parent of the given type to this person.
   * This person will be added to the parent’s children.
   *
   * @param parent The parent.
   * @param type   The parent’s type.
   * @throws IllegalArgumentException If this person already has the maximum number of parents of the given type,
   *                                  or the parent has already been added to this person,
   *                                  or the relation is genetic and this person already has 2 genetic parents.
   */
  public void addParent(@NotNull Person parent, @NotNull ParentalRelationType type) {
    Objects.requireNonNull(parent);

    final var maxCount = type.maxParentsCount();
    if (maxCount.isPresent() && this.parents.get(type).size() == maxCount.get())
      throw new IllegalArgumentException("cannot add more than %d parents of type %s".formatted(maxCount.get(), type));

    for (final var entry : this.parents.entrySet())
      if (entry.getValue().contains(parent))
        throw new IllegalArgumentException("Person %s is already a parent of %s".formatted(parent, this));

    if (type.geneticRelation()) {
      int dnaRelationsCount = 0;
      for (final var entry : this.parents.entrySet()) {
        if (!entry.getKey().geneticRelation()) continue;
        dnaRelationsCount += entry.getValue().size();
      }
      if (dnaRelationsCount == 2)
        throw new IllegalArgumentException("Person %s already has 2 DNA parents".formatted(this));
    }

    this.parents.get(type).add(parent);
    parent.children.get(type).add(this);
  }

  /**
   * Remove the given parent from this person.
   * This person will be removed from the parent’s children.
   *
   * @param parent The parent to remove.
   */
  public void removeParent(@NotNull Person parent) {
    Objects.requireNonNull(parent);
    for (final var entry : this.parents.entrySet()) {
      final var relationType = entry.getKey();
      final var parents = entry.getValue();
      if (parents.contains(parent)) {
        parents.remove(parent);
        parent.children.get(relationType).remove(this);
        break;
      }
    }
  }

  /**
   * Return the type of the given parent for this person.
   *
   * @param parent The parent to check.
   * @return An {@link Optional} containing the type of the given parent
   * or an empty {@link Optional} if it is not a parent of this person.
   */
  public Optional<ParentalRelationType> getParentType(final @NotNull Person parent) {
    return this.parents.entrySet().stream()
        .filter(e -> e.getValue().contains(parent))
        .findFirst()
        .map(Map.Entry::getKey);
  }

  /**
   * Indicate whether this person has any non-null parent (includes surrogate, donor and godparents).
   */
  public boolean hasAnyParents() {
    return this.parents.entrySet().stream().anyMatch(e -> !e.getValue().isEmpty());
  }

  /**
   * The collection of children for this person.
   *
   * @return A map of this person’s children.
   */
  @Contract("-> new")
  public Map<ParentalRelationType, Set<Person>> children() {
    return this.children.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> new HashSet<>(e.getValue())));
  }

  /**
   * The set of children of the given type for this person.
   *
   * @param type The type of children to return.
   * @return An unmodifiable view of this person’s children of the given type.
   */
  @Contract("_ -> new")
  @UnmodifiableView
  public Set<Person> children(@NotNull ParentalRelationType type) {
    return Collections.unmodifiableSet(this.children.get(type));
  }

  /**
   * Return the genetic parents of this person.
   * The returned set will always contain at most 2 parents.
   *
   * @return A {@link Set} containing the genetic parents.
   */
  @Contract("-> new")
  public Set<Person> getGeneticParents() {
    final Set<Person> geneticParents = new HashSet<>();
    for (final var relationType : ParentalRelationType.GENETIC_RELATIONS)
      geneticParents.addAll(this.parents.get(relationType));
    return geneticParents;
  }

  /**
   * Return a map associating persons and the children they had with this person, if any.
   * <p>
   * If a children set is empty, the persons in the key set have a union with this person but no children.
   * If a partners set is empty, this person had children with unspecified persons.
   * <p>
   * Children this person was a surrogate parent or donor for are not returned.
   * <p>
   * Partners and children are grouped based on the relation type between this person and each of their children:
   * <li>If this person is a (non-)biological parent of a child, only return partners who are (non-)biological parents of that child.
   * <li>If this person is an adoptive parent of a child, only return partners who are adoptive parents of that child.
   * <li>If this person is a foster parent of a child, only return partners who are foster parents of that child.
   * <li>If this person is a godparent of a child, only return partners who are godparents of that child.
   *
   * @return An list of {@link FamilyUnit}s in no particular order,
   * each representing a set of partners and the children they got with this person.
   */
  @Contract("-> new")
  public List<FamilyUnit> getPartnersAndChildren() {
    final List<FamilyUnit> partnersChildren = new LinkedList<>();

    final Function<Set<Person>, Optional<Set<Person>>> findChildren = parents ->
        partnersChildren.stream()
            .filter(e -> e.parents().equals(parents))
            .findFirst()
            .map(FamilyUnit::children);

    this.findPartnersAndChildrenOfType(Set.of(ParentalRelationType.BIOLOGICAL_PARENT, ParentalRelationType.NON_BIOLOGICAL_PARENT), findChildren, partnersChildren);
    this.findPartnersAndChildrenOfType(Set.of(ParentalRelationType.ADOPTIVE_PARENT), findChildren, partnersChildren);
    this.findPartnersAndChildrenOfType(Set.of(ParentalRelationType.FOSTER_PARENT), findChildren, partnersChildren);
    this.findPartnersAndChildrenOfType(Set.of(ParentalRelationType.GODPARENT), findChildren, partnersChildren);

    // Add all partners that did not have any children with this person
    this.getActedInEventsStream()
        .filter(event -> event.type().indicatesUnion())
        .map(event -> this.filterOutThis(event.actors()))
        .filter(partners -> findChildren.apply(partners).isEmpty())
        .forEach(partners -> partnersChildren.add(new FamilyUnit(partners, new HashSet<>())));

    return partnersChildren;
  }

  private void findPartnersAndChildrenOfType(
      final @NotNull Set<ParentalRelationType> types,
      @NotNull Function<Set<Person>, Optional<Set<Person>>> findChildren,
      @NotNull List<FamilyUnit> partnersChildren
  ) {
    final Set<Person> children = new HashSet<>();
    for (final ParentalRelationType type : types)
      children.addAll(this.children.get(type));

    for (final Person child : children) {
      final Set<Person> parentsOfChild = new HashSet<>();
      for (final ParentalRelationType type : types)
        parentsOfChild.addAll(child.parents.get(type));
      final Set<Person> parentsWithoutThis = Sets.difference(parentsOfChild, Set.of(this));
      final var siblingsForParents = findChildren.apply(parentsWithoutThis);
      if (siblingsForParents.isPresent()) {
        siblingsForParents.get().add(child);
      } else {
        final Set<Person> childrenSet = new HashSet<>();
        childrenSet.add(child);
        partnersChildren.add(new FamilyUnit(parentsWithoutThis, childrenSet));
      }
    }
  }

  /**
   * Return a map associating persons and the genetic children they had with this person, if any.
   */
  @Contract("-> new")
  public Map<Optional<Person>, Set<Person>> getPartnersAndGeneticChildren() {
    final Map<Optional<Person>, Set<Person>> partnersChildren = new HashMap<>();

    for (final var childType : ParentalRelationType.GENETIC_RELATIONS) {
      for (final Person child : this.children.get(childType)) {
        final Set<Person> geneticParents = this.filterOutThis(child.getGeneticParents());
        final Optional<Person> geneticParent = geneticParents.stream().findFirst();
        if (partnersChildren.containsKey(geneticParent)) {
          partnersChildren.get(geneticParent).add(child);
        } else {
          final Set<Person> children = new HashSet<>();
          children.add(child);
          partnersChildren.put(geneticParent, children);
        }
      }
    }

    return partnersChildren;
  }

  /**
   * Remove this object from the given set (the set is not modified).
   *
   * @param set The set to filter.
   * @return A new set with this object not present in it.
   */
  @Contract("_ -> new")
  private Set<Person> filterOutThis(final @NotNull Set<Person> set) {
    return set.stream()
        .filter(p -> p != this)
        .collect(Collectors.toSet());
  }

  /**
   * Return the genetic siblings of this person,
   * i.e. the set of all persons that have the same genetic parents as this person.
   *
   * @return The set of genetic siblings.
   */
  @Contract("-> new")
  public Set<Person> getSameGeneticParentsSiblings() {
    final Set<Person> geneticParents = new HashSet<>();

    for (final var parentType : ParentalRelationType.GENETIC_RELATIONS)
      geneticParents.addAll(this.parents.get(parentType));

    final var iterator = geneticParents.iterator();
    if (iterator.hasNext()) {
      final var parent1 = iterator.next();
      // Get genetic children of the first genetic parent, excluding this person
      final Set<Person> siblings = new HashSet<>();
      for (final var childType : ParentalRelationType.GENETIC_RELATIONS)
        siblings.addAll(parent1.children.get(childType));
      if (iterator.hasNext()) {
        final var parent2 = iterator.next();
        final Set<Person> parent2Children = new HashSet<>();
        // Keep only siblings that are also children of the second genetic parent, excluding this person
        for (final var childType : ParentalRelationType.GENETIC_RELATIONS)
          parent2Children.addAll(parent2.children.get(childType));
        siblings.retainAll(parent2Children);
      }
      return this.filterOutThis(siblings);
    }

    return new HashSet<>();
  }

  /**
   * Return all siblings of this person, i.e. all persons that share at least one parent with this one.
   * Persons that only share a surrogate parent, donor, or godparents with this person are excluded.
   *
   * @return A list of {@link FamilyUnit}s in no particular order,
   * each representing a set of parents and the siblings this person has.
   */
  @Contract("-> new")
  public List<FamilyUnit> getSiblings() {
    final ParentalRelationType[] relationTypes = Arrays.stream(ParentalRelationType.values()).filter(
        v -> v != ParentalRelationType.SPERM_DONOR &&
            v != ParentalRelationType.EGG_DONOR &&
            v != ParentalRelationType.SURROGATE_PARENT &&
            v != ParentalRelationType.GODPARENT
    ).toArray(ParentalRelationType[]::new);
    final List<FamilyUnit> parentsAndSiblings = new LinkedList<>();
    final Function<Set<Person>, Optional<Set<Person>>> findSiblings = parents ->
        parentsAndSiblings.stream()
            .filter(e -> e.parents().equals(parents))
            .findFirst()
            .map(FamilyUnit::children);

    for (final var parentType : relationTypes) {
      this.parents.get(parentType).forEach(parent -> {
        for (final var childType : relationTypes) {
          this.filterOutThis(parent.children.get(childType)).forEach(child -> {
            final Set<Person> childParents = new HashSet<>();
            for (final var childParentType : relationTypes)
              childParents.addAll(child.parents.get(childParentType));
            final var siblings = findSiblings.apply(childParents);
            if (siblings.isPresent()) {
              siblings.get().add(child);
            } else {
              final Set<Person> children = new HashSet<>();
              children.add(child);
              parentsAndSiblings.add(new FamilyUnit(childParents, children));
            }
          });
        }
      });
    }

    return parentsAndSiblings;
  }

  /**
   * Calculate then return this person’s birth date.
   * <p>
   * The date is calculated by looking for a life event of type {@code builtin:birth} and returning its date.
   *
   * @return The computed birth date.
   */
  public Optional<DateTime> getBirthDate() {
    final LifeEventType birthEventType = this.familyTree.lifeEventTypeRegistry()
        .getEntry(new RegistryEntryKey(Registry.BUILTIN_NS, "birth"));
    return this.getActedInEventsStream()
        .filter(lifeEvent -> lifeEvent.type().equals(birthEventType))
        .findFirst()
        .map(LifeEvent::date);
  }

  /**
   * Calculate then return this person’s death date.
   * <p>
   * The date is calculated by looking for a life event of type {@code builtin:death} and returning its date.
   *
   * @return The computed death date.
   */
  public Optional<DateTime> getDeathDate() {
    final LifeEventType deathEventType = this.familyTree.lifeEventTypeRegistry()
        .getEntry(new RegistryEntryKey(Registry.BUILTIN_NS, "death"));
    return this.getActedInEventsStream()
        .filter(lifeEvent -> lifeEvent.type().equals(deathEventType))
        .findFirst()
        .map(LifeEvent::date);
  }

  /**
   * A copy of the list of this person’s life events, both as a witness or actor.
   * <p>
   * Events are sorted according to their natural ordering.
   */
  @Contract("-> new")
  public List<LifeEvent> lifeEvents() {
    return new ArrayList<>(this.lifeEvents);
  }

  /**
   * A copy of the list of life events this person acted in.
   * <p>
   * Events are sorted according to their natural ordering.
   */
  @Contract("-> new")
  @Unmodifiable
  public List<LifeEvent> getLifeEventsAsActor() {
    return this.getActedInEventsStream().toList();
  }

  /**
   * A copy of the list of life events this person witnessed.
   * <p>
   * Events are sorted according to their natural ordering.
   */
  @Contract("-> new")
  @Unmodifiable
  public List<LifeEvent> getLifeEventsAsWitness() {
    return this.getWitnessedEventsStream().toList();
  }

  /**
   * Add a life event to this person. Does <b>not</b> update the {@link LifeEvent} object.
   * <p>
   * If the event has a type with {@link LifeEventType#indicatesDeath()} to true,
   * this person’s life status is set to {@link LifeStatus#DECEASED}.
   *
   * @param event Life event to add.
   * @throws IllegalArgumentException If the event’s type has a unicity constraint
   *                                  and this actor already acts in another event of the same type.
   */
  void addLifeEvent(final @NotNull LifeEvent event) {
    if (event.type().isUnique() && event.hasActor(this)
        && this.getActedInEventsStream().anyMatch(e -> e.type().equals(event.type())))
      throw new IllegalArgumentException("%s already acts in an event of type '%s'"
          .formatted(this, event.type().key().fullName()));
    if (event.hasActor(this) && event.type().indicatesDeath())
      this.lifeStatus = LifeStatus.DECEASED;
    if (!this.lifeEvents.contains(event)) {
      this.lifeEvents.add(event);
      this.lifeEvents.sort(null);
    }
  }

  /**
   * Remove a life event from this person. Does <b>not</b> update the {@link LifeEvent} object.
   *
   * @param event Life event to remove.
   */
  void removeLifeEvent(final LifeEvent event) {
    this.lifeEvents.remove(event);
  }

  /**
   * Strip leading and trailing whitespace then filter out empty string from the given list.
   *
   * @param values List of strings to clean.
   * @return New list containing the transformed and filtered strings.
   */
  @Contract("_ -> new")
  @Unmodifiable
  private List<String> filterOutEmptyStrings(final @NotNull List<String> values) {
    return values.stream().map(String::strip).filter(s -> !s.isEmpty()).toList();
  }

  /**
   * Return a stream of life events this person acted in.
   */
  @Contract("-> new")
  private Stream<LifeEvent> getActedInEventsStream() {
    return this.lifeEvents.stream().filter(e -> e.hasActor(this));
  }

  /**
   * Return a stream of life events this person witnessed.
   */
  @Contract("-> new")
  private Stream<LifeEvent> getWitnessedEventsStream() {
    return this.lifeEvents.stream().filter(e -> e.hasWitness(this));
  }

  @Override
  public String toString() {
    final String firstNames = this.getFirstNames().orElse("?");
    final String lastName = this.getLastName().orElse("?");
    String s = firstNames + " " + lastName;
    if (this.disambiguationID != null)
      s += " (#%d)".formatted(this.disambiguationID);
    return s;
  }

  /**
   * Return a comparator of {@link Person} objects.
   * <p>
   * Objects are sorted by birth date if available then according to {@link Person#lastThenFirstNamesComparator()}.
   *
   * @param reversed Whether to sort in reverse order.
   * @return A comparator object.
   */
  public static Comparator<Person> birthDateThenNameComparator(boolean reversed) {
    return reversed ? BIRTH_DATE_THEN_NAME_COMPARATOR_REVERSED : BIRTH_DATE_THEN_NAME_COMPARATOR;
  }

  /**
   * Return a comparator of {@link Optional}-wrapped {@link Person} objects.
   * <p>
   * {@link Optional}s are sorted according to whether they are empty or not.
   * Empty {@link Optional}s are always sorted after non-empty {@link Optional}s.
   * If both optionals are present, comparison is delegate to {@link #birthDateThenNameComparator(boolean)}.
   *
   * @return A comparator object.
   */
  public static Comparator<Optional<Person>> optionalBirthDateThenNameComparator() {
    return (p1, p2) -> {
      final boolean p2Present = p2.isPresent();
      if (p1.isEmpty())
        return p2Present ? 1 : 0;
      if (!p2Present)
        return -1;
      return birthDateThenNameComparator(false).compare(p1.get(), p2.get());
    };
  }

  /**
   * Return a comparator of {@link Person} objects.
   * <p>
   * Objects are sorted according to the following property order:
   * <li>{@link #getLastName()}
   * <li>{@link #getFirstNames()}
   * <li>{@link #disambiguationID()}.
   *
   * @return A comparator object.
   */
  public static Comparator<Person> lastThenFirstNamesComparator() {
    return LAST_THEN_FIRST_NAMES_COMPARATOR;
  }

  /**
   * Wrapper class that associates a set of parents to a set of their children.
   * Either sets can be empty but not both at the same time.
   *
   * @param parents  The set of parents.
   * @param children The set of children for these parents.
   */
  public record FamilyUnit(@NotNull Set<Person> parents, @NotNull Set<Person> children) {
    /**
     * @throws IllegalArgumentException If both sets are empty.
     */
    public FamilyUnit {
      Objects.requireNonNull(parents);
      Objects.requireNonNull(children);
      if (parents.isEmpty() && children.isEmpty())
        throw new IllegalArgumentException("Both sets cannot be empty at the same time");
    }
  }
}
