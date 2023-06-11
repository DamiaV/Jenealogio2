package net.darmo_creations.jenealogio2.model;

import javafx.scene.image.Image;
import net.darmo_creations.jenealogio2.model.calendar.CalendarDate;
import net.darmo_creations.jenealogio2.utils.Pair;
import net.darmo_creations.jenealogio2.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

/**
 * This class represents a person, deceased or alive.
 */
public class Person extends GenealogyObject<Person> {
  public static final String NAME_SEPARATOR = " ";

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
    for (RelativeType type : RelativeType.values()) {
      this.relatives.put(type, new HashSet<>());
      this.nonBiologicalChildren.put(type, new HashSet<>());
    }
  }

  /**
   * This person’s profile image.
   */
  public Optional<Image> getImage() {
    return Optional.empty(); // TODO image
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
    if (disambiguationID != null && disambiguationID < 1) {
      throw new IllegalArgumentException("Disambiguation ID must be > 0");
    }
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
    boolean isDead = this.getLifeEventsAsActor().stream().anyMatch(e -> e.type().indicatesDeath());
    if (isDead && lifeStatus != LifeStatus.DECEASED) {
      throw new IllegalArgumentException("cannot change status of a person with at least one event indicating death");
    }
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
   * @return A pair containing the first parent as the left value and the second parent as the right value.
   */
  public Pair<Optional<Person>, Optional<Person>> parents() {
    return new Pair<>(Optional.ofNullable(this.parents[0]), Optional.ofNullable(this.parents[1]));
  }

  /**
   * Check whether this person has the given person as a biological parent.
   *
   * @param person Person to check as parent.
   * @return True if the passed person is a biological parent of this person, false otherwise.
   */
  public boolean hasParent(final Person person) {
    return this.parents[0] == person || this.parents[1] == person;
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
    Person previousParent = this.parents[index];
    if (previousParent == parent) {
      return;
    }
    this.parents[index] = parent;
    if (parent != null) {
      parent.children.add(this);
    }
    if (previousParent != null) {
      previousParent.children.removeIf(person -> person == this);
    }
  }

  /**
   * Remove the given parent from this person’s parents.
   *
   * @param parent Parent to remove.
   */
  public void removeParent(@NotNull Person parent) {
    int index = -1;
    for (int i = 0; i < this.parents.length; i++) {
      if (this.parents[i] == parent) {
        index = i;
        break;
      }
    }
    if (index >= 0) {
      this.setParent(index, null);
    }
  }

  /**
   * A copy of this person’s children set.
   */
  public Set<Person> children() {
    return new HashSet<>(this.children);
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
  public Optional<CalendarDate> getBirthDate() {
    LifeEventType birthEventType = Registries.LIFE_EVENT_TYPES.getEntry(new RegistryEntryKey(Registry.BUILTIN_NS, "birth"));
    return this.getActedInEventsStream()
        .filter(lifeEvent -> lifeEvent.type() == birthEventType)
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
  public Optional<CalendarDate> getDeathDate() {
    LifeEventType deathEventType = Registries.LIFE_EVENT_TYPES.getEntry(new RegistryEntryKey(Registry.BUILTIN_NS, "death"));
    return this.getActedInEventsStream()
        .filter(lifeEvent -> lifeEvent.type() == deathEventType)
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
   *
   * @param event Life event to add.
   * @throws IllegalArgumentException If the event’s type has a unicity constraint
   *                                  and this actor already acts in another event of the same type.
   */
  void addLifeEvent(final @NotNull LifeEvent event) {
    if (event.type().isUnique() && event.hasActor(this)
        && this.getActedInEventsStream().anyMatch(e -> e.type() == event.type())) {
      throw new IllegalArgumentException("%s already acts in an event of type %s".formatted(this, event.type()));
    }
    if (event.hasActor(this) && event.type().indicatesDeath()) {
      this.lifeStatus = LifeStatus.DECEASED;
    }
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
    String firstNames = this.getFirstNames().orElse("?");
    String lastName = this.getLastName().orElse("?");
    String s = firstNames + " " + lastName;
    if (this.disambiguationID != null) {
      s += " (#%d)".formatted(this.disambiguationID);
    }
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
    return (p1, p2) -> {
      Optional<CalendarDate> birthDate1 = p1.getBirthDate();
      Optional<CalendarDate> birthDate2 = p2.getBirthDate();
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
    return (p1, p2) -> {
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
