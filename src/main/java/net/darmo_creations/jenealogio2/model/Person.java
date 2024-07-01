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
    if (c1 != 0) {
      return c1;
    }
    int c2 = p1.getFirstNames().orElse("")
        .compareTo(p2.getFirstNames().orElse(""));
    if (c2 != 0) {
      return c2;
    }
    return p1.disambiguationID().orElse(-1)
        .compareTo(p2.disambiguationID().orElse(-1));
  };
  private static final Function<Boolean, Comparator<Person>> BIRTH_DATE_THEN_NAME_COMPARATOR_FACTORY = reversed -> (p1, p2) -> {
    Optional<DateTime> birthDate1 = p1.getBirthDate();
    Optional<DateTime> birthDate2 = p2.getBirthDate();
    if (birthDate1.isPresent() && birthDate2.isPresent()) {
      int c = birthDate1.get().compareTo(birthDate2.get());
      if (reversed) {
        c = -c;
      }
      if (c != 0) {
        return c;
      }
    }
    int c = lastThenFirstNamesComparator().compare(p1, p2);
    if (reversed) {
      c = -c;
    }
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
  private Gender gender;
  private String mainOccupation;
  /**
   * We assume exactly two parents per person.
   */
  private final Person[] parents = new Person[2];
  private final Set<Person> children = new HashSet<>();
  private final Map<RelativeType, Set<Person>> relatives = new HashMap<>();
  private final Map<RelativeType, Set<Person>> nonBiologicalChildren = new HashMap<>();
  /**
   * Ordered list of all life events this person was an actor in.
   */
  private final List<LifeEvent> lifeEvents = new ArrayList<>();

  /**
   * Create a new alive person.
   */
  public Person() {
    for (final var relativeType : RelativeType.values()) {
      this.relatives.put(relativeType, new HashSet<>());
      this.nonBiologicalChildren.put(relativeType, new HashSet<>());
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
  public Person setLegalLastName(String legalLastName) {
    this.legalLastName = StringUtils.stripNullable(legalLastName).orElse(null);
    return this;
  }

  /**
   * This person’s public firt names.
   *
   * @see #setPublicFirstNames(List)
   */
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
  public Person setPublicLastName(String publicLastName) {
    this.publicLastName = StringUtils.stripNullable(publicLastName).orElse(null);
    return this;
  }

  /**
   * This person’s nicknames.
   */
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
  public void setNicknames(final @NotNull List<String> nicknames) {
    this.nicknames.clear();
    this.nicknames.addAll(this.filterOutEmptyStrings(nicknames));
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
   * This person’s gender.
   */
  public Optional<Gender> gender() {
    return Optional.ofNullable(this.gender);
  }

  /**
   * Set this person’s gender.
   *
   * @param gender The gender.
   * @return This object.
   */
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
  public void setMainOccupation(String mainOccupation) {
    this.mainOccupation = mainOccupation;
  }

  /**
   * This person’s biological parents. We consider only two.
   *
   * @return A {@link Parents} object containg the parents.
   */
  public Parents parents() {
    return new Parents(Optional.ofNullable(this.parents[0]), Optional.ofNullable(this.parents[1]));
  }

  public Optional<Integer> getParentIndex(final Person person) {
    for (int i = 0; i < this.parents.length; i++)
      if (this.parents[i] == person)
        return Optional.of(i);
    return Optional.empty();
  }

  /**
   * Check if two persons have the same parents, regardless of order.
   *
   * @param person Person to check parents againts this.
   * @return True if this and the other person have the same parents, false otherwise.
   */
  public boolean hasSameParents(final Person person) {
    return this.hasAnyParents() && person.hasAnyParents() &&
           (this.parents[0] == person.parents[0] && this.parents[1] == person.parents[1]
            || this.parents[1] == person.parents[0] && this.parents[0] == person.parents[1]);
  }

  /**
   * Indicate whether this person has any non-null parent.
   */
  public boolean hasAnyParents() {
    return this.parents[0] != null || this.parents[1] != null;
  }

  /**
   * Indicate whether both of this person’s parents are non-null.
   */
  public boolean hasBothParents() {
    return this.parents[0] != null && this.parents[1] != null;
  }

  /**
   * Set a parent of this person.
   * This person is added to the passed person’s children list if not null
   * and is removed from its previous parent’s list if applicable.
   *
   * @param index  Parent’s index (either 0 or 1).
   * @param parent The parent.
   */
  public void setParent(int index, Person parent) {
    final Person previousParent = this.parents[index];
    if (previousParent == parent)
      return;
    this.parents[index] = parent;
    if (parent != null)
      parent.children.add(this);
    if (previousParent != null)
      previousParent.children.removeIf(person -> person == this);
  }

  /**
   * Remove the given parent from this person’s parents.
   *
   * @param parent Parent to remove.
   */
  public void removeParent(@NotNull Person parent) {
    int index = -1;
    for (int i = 0; i < this.parents.length; i++)
      if (this.parents[i] == parent) {
        index = i;
        break;
      }
    if (index >= 0)
      this.setParent(index, null);
  }

  /**
   * A copy of this person’s children set.
   */
  public Set<Person> children() {
    return new HashSet<>(this.children);
  }

  /**
   * Return a map associating persons and the children they had with this person, if any.
   * <p>
   * If a list is empty, the person has a union with this person but no children.
   */
  public Map<Optional<Person>, Set<Person>> getPartnersAndChildren() {
    final Map<Person, Set<Person>> partnersChildren = new HashMap<>();
    for (final Person child : this.children) {
      final var childParents = child.parents();
      final var parent1 = childParents.parent1();
      final var parent2 = childParents.parent2();
      Person parent = null;
      if (parent1.isPresent() && parent1.get() != this)
        parent = parent1.get();
      else if (parent2.isPresent() && parent2.get() != this)
        parent = parent2.get();
      if (!partnersChildren.containsKey(parent))
        partnersChildren.put(parent, new HashSet<>());
      partnersChildren.get(parent).add(child);
    }
    // Add all partners that did not have any children with this person
    //noinspection OptionalGetWithoutIsPresent
    this.getActedInEventsStream()
        .filter(e -> e.type().indicatesUnion())
        // Partner always present in unions
        .map(e -> e.actors().stream().filter(p -> p != this).findFirst().get())
        .filter(person -> !partnersChildren.containsKey(person))
        .forEach(person -> partnersChildren.put(person, new HashSet<>()));
    return partnersChildren.entrySet().stream()
        .collect(Collectors.toMap(e -> Optional.ofNullable(e.getKey()), Map.Entry::getValue));
  }

  /**
   * Return the siblings of this person, i.e. the set of all persons with the exact same parents.
   *
   * @return The set of siblings.
   */
  public Set<Person> getSameParentsSiblings() {
    final Set<Person> siblings = new HashSet<>();
    final Person parent1 = this.parents[0];
    final Person parent2 = this.parents[1];
    if (parent1 != null)
      this.addChildren(siblings, parent1);
    else if (parent2 != null)
      this.addChildren(siblings, parent2);
    return siblings;
  }

  private void addChildren(@NotNull Set<Person> siblings, final @NotNull Person parent) {
    siblings.addAll(parent.children().stream()
        .filter(c -> c != this && c.hasSameParents(this))
        .collect(Collectors.toSet()));
  }

  /**
   * Return all sibling of this person, i.e. all persons that share at least one parent with this one.
   *
   * @return A map associating a parents to their children.
   * It is guaranted that at least one of the parents in each {@link Parents} object is a parent of this person.
   */
  public Map<Parents, Set<Person>> getAllSiblings() {
    final Map<Parents, Set<Person>> siblings = new HashMap<>();
    final Person parent1 = this.parents[0];
    final Person parent2 = this.parents[1];
    if (parent1 != null)
      this.addChildren(siblings, parent1);
    if (parent2 != null)
      this.addChildren(siblings, parent2);
    return siblings;
  }

  private void addChildren(@NotNull Map<Parents, Set<Person>> siblings, final @NotNull Person parent) {
    for (final Person child : parent.children()) {
      if (child == this)
        continue;
      final Optional<Person> p1 = Optional.ofNullable(child.parents[0]);
      final Optional<Person> p2 = Optional.ofNullable(child.parents[1]);
      final var key1 = new Parents(p1, p2);
      final var key2 = new Parents(p2, p1);
      // Ensure that there isn’t already any key with the same persons but in a different order
      final var key = siblings.containsKey(key2) ? key2 : key1;
      if (!siblings.containsKey(key))
        siblings.put(key, new HashSet<>());
      siblings.get(key).add(child);
    }
  }

  /**
   * A copy of this person’s relatives of the given type.
   *
   * @param type Type of the relatives to get.
   * @return A copy of the relatives set.
   */
  public Set<Person> getRelatives(@NotNull RelativeType type) {
    return new HashSet<>(this.relatives.get(type));
  }

  /**
   * Add a relative of the given type.
   * <p>
   * This person will be add to the passed person’s non-biological children set.
   *
   * @param person Relative to add.
   * @param type   Relative’s type.
   */
  public void addRelative(@NotNull Person person, @NotNull RelativeType type) {
    this.relatives.get(type).add(Objects.requireNonNull(person));
    person.nonBiologicalChildren.get(type).add(this);
  }

  /**
   * Remove a relative of the given type.
   * <p>
   * This person will be removed from the passed person’s non-biological children set.
   *
   * @param person Relative to remove.
   * @param type   Relative’s type.
   */
  public void removeRelative(@NotNull Person person, @NotNull RelativeType type) {
    this.relatives.get(type).remove(person);
    person.nonBiologicalChildren.get(type).remove(this);
  }

  /**
   * A copy of this person’s non-biological children of the given type.
   *
   * @param type Children type.
   * @return A copy of the children set.
   */
  public Set<Person> nonBiologicalChildren(@NotNull Person.RelativeType type) {
    return new HashSet<>(this.nonBiologicalChildren.get(type));
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
  public List<LifeEvent> lifeEvents() {
    return new ArrayList<>(this.lifeEvents);
  }

  /**
   * A copy of the list of life events this person acted in.
   * <p>
   * Events are sorted according to their natural ordering.
   */
  public List<LifeEvent> getLifeEventsAsActor() {
    return this.getActedInEventsStream().toList();
  }

  /**
   * A copy of the list of life events this person witnessed.
   * <p>
   * Events are sorted according to their natural ordering.
   */
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
  private List<String> filterOutEmptyStrings(final @NotNull List<String> values) {
    return values.stream().map(String::strip).filter(s -> !s.isEmpty()).toList();
  }

  /**
   * Return a stream of life events this person acted in.
   */
  private Stream<LifeEvent> getActedInEventsStream() {
    return this.lifeEvents.stream().filter(e -> e.hasActor(this));
  }

  /**
   * Return a stream of life events this person witnessed.
   */
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
   * Enumeration of non-biological relatives types.
   */
  public enum RelativeType {
    /**
     * Adoptive parents/children.
     */
    ADOPTIVE,
    /**
     * Godparents/godchildren.
     */
    GOD,
    /**
     * Foster parents/children.
     */
    FOSTER,
  }
}
