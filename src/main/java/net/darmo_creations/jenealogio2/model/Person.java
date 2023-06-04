package net.darmo_creations.jenealogio2.model;

import javafx.scene.image.Image;
import net.darmo_creations.jenealogio2.model.calendar.CalendarDate;
import net.darmo_creations.jenealogio2.utils.Pair;
import net.darmo_creations.jenealogio2.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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
  /**
   * We assume exactly two parents per person.
   */
  private final Person[] parents = new Person[2];
  private final List<Child> children = new ArrayList<>();
  /**
   * Ordered list of all life events this person was an actor in.
   */
  private final List<LifeEvent> lifeEvents = new ArrayList<>();

  public Optional<Image> getImage() {
    return Optional.empty(); // TODO image
  }

  public Optional<Integer> disambiguationID() {
    return Optional.ofNullable(this.disambiguationID);
  }

  public void setDisambiguationID(Integer disambiguationID) {
    this.disambiguationID = disambiguationID;
  }

  public LifeStatus lifeStatus() {
    return this.lifeStatus;
  }

  public void setLifeStatus(@NotNull LifeStatus lifeStatus) {
    boolean isDead = this.getLifeEventsAsActor().stream().anyMatch(e -> e.type().indicatesDeath());
    if (isDead && lifeStatus != LifeStatus.DECEASED) {
      throw new IllegalArgumentException("cannot change status of a person with at least one event indicating death");
    }
    this.lifeStatus = Objects.requireNonNull(lifeStatus);
  }

  public List<String> legalFirstNames() {
    return new ArrayList<>(this.legalFirstNames);
  }

  public Optional<String> getJoinedLegalFirstNames() {
    return StringUtils.stripNullable(String.join(NAME_SEPARATOR, this.legalFirstNames));
  }

  public void setLegalFirstNames(final @NotNull List<String> legalFirstNames) {
    this.ensureNoDuplicates(legalFirstNames);
    this.legalFirstNames.clear();
    this.legalFirstNames.addAll(this.filterOutEmptyStrings(legalFirstNames));
  }

  public Optional<String> legalLastName() {
    return Optional.ofNullable(this.legalLastName);
  }

  public void setLegalLastName(String legalLastName) {
    this.legalLastName = StringUtils.stripNullable(legalLastName).orElse(null);
  }

  public List<String> publicFirstNames() {
    return new ArrayList<>(this.publicFirstNames);
  }

  public Optional<String> getJoinedPublicFirstNames() {
    return StringUtils.stripNullable(String.join(NAME_SEPARATOR, this.publicFirstNames));
  }

  public void setPublicFirstNames(final @NotNull List<String> publicFirstNames) {
    this.ensureNoDuplicates(publicFirstNames);
    this.publicFirstNames.clear();
    this.publicFirstNames.addAll(this.filterOutEmptyStrings(publicFirstNames));
  }

  public Optional<String> publicLastName() {
    return Optional.ofNullable(this.publicLastName);
  }

  public void setPublicLastName(String publicLastName) {
    this.publicLastName = StringUtils.stripNullable(publicLastName).orElse(null);
  }

  public List<String> nicknames() {
    return new ArrayList<>(this.nicknames);
  }

  public Optional<String> getJoinedNicknames() {
    return StringUtils.stripNullable(String.join(NAME_SEPARATOR, this.nicknames));
  }

  public Optional<String> getLastName() {
    return this.publicLastName().or(this::legalLastName);
  }

  public Optional<String> getFirstNames() {
    return this.getJoinedPublicFirstNames().or(this::getJoinedLegalFirstNames);
  }

  public void setNicknames(final @NotNull List<String> nicknames) {
    this.ensureNoDuplicates(nicknames);
    this.nicknames.clear();
    this.nicknames.addAll(this.filterOutEmptyStrings(nicknames));
  }

  public Optional<Gender> gender() {
    return Optional.ofNullable(this.gender);
  }

  public void setGender(Gender gender) {
    this.gender = gender;
  }

  public Pair<Optional<Person>, Optional<Person>> parents() {
    return new Pair<>(Optional.ofNullable(this.parents[0]), Optional.ofNullable(this.parents[1]));
  }

  public boolean hasBothParents() {
    return this.parents[0] != null && this.parents[1] != null;
  }

  public void setBiologicalParent(int index, @NotNull Person parent) {
    this.setParent(index, Objects.requireNonNull(parent), false);
  }

  public void setAdoptiveParent(int index, @NotNull Person parent) {
    this.setParent(index, Objects.requireNonNull(parent), true);
  }

  private void setParent(int index, Person parent, boolean adoptive) {
    Person previousParent = this.parents[index];
    if (previousParent == parent) {
      return;
    }
    this.parents[index] = parent;
    if (parent != null) {
      parent.children.add(new Child(this, adoptive));
    }
    if (previousParent != null) {
      previousParent.children.removeIf(child -> child.person == this);
    }
  }

  public Optional<Person> removeParent(int index) {
    Person parent = this.parents[index];
    this.setParent(index, null, false);
    return Optional.ofNullable(parent);
  }

  public List<Child> children() {
    return new ArrayList<>(this.children);
  }

  public Optional<CalendarDate> getBirthDate() {
    LifeEventType birthEventType = Registries.LIFE_EVENT_TYPES.getEntry(new RegistryEntryKey(Registry.BUILTIN_NS, "birth"));
    return this.lifeEvents.stream()
        .filter(lifeEvent -> lifeEvent.type() == birthEventType)
        .findFirst()
        .map(LifeEvent::date);
  }

  public Optional<CalendarDate> getDeathDate() {
    LifeEventType deathEventType = Registries.LIFE_EVENT_TYPES.getEntry(new RegistryEntryKey(Registry.BUILTIN_NS, "death"));
    return this.lifeEvents.stream()
        .filter(lifeEvent -> lifeEvent.type() == deathEventType)
        .findFirst()
        .map(LifeEvent::date);
  }

  public List<LifeEvent> lifeEvents() {
    return new ArrayList<>(this.lifeEvents);
  }

  public List<LifeEvent> getLifeEventsAsActor() {
    return this.lifeEvents.stream().filter(e -> e.hasActor(this)).toList();
  }

  public List<LifeEvent> getLifeEventsAsWitness() {
    return this.lifeEvents.stream().filter(e -> e.hasWitness(this)).toList();
  }

  public void addLifeEventAsActor(@NotNull LifeEvent event) {
    if (event.type().isUnique() && this.getLifeEventsAsActor().stream().anyMatch(e -> e.type() == event.type())) {
      throw new IllegalArgumentException("%s already acts in an event of type %s".formatted(this, event.type()));
    }
    // Add link from event to this actor if not yet done
    if (!event.hasActor(this)) {
      event.addActor(this);
    }
    // Add event to this actor; may have been done by the event itself in previous branch
    if (!this.lifeEvents.contains(event)) {
      this.lifeEvents.add(event);
      this.lifeEvents.sort(null);
    }
  }

  public void removeLifeEventAsActor(@NotNull LifeEvent event) {
    // Remove link from event to this actor if not yet done
    if (event.hasActor(this)) {
      event.removeActor(this);
    }
    // Remove event from this actor; may have been done by the event itself in previous branch
    if (!event.hasWitness(this)) {
      this.lifeEvents.remove(event);
    }
  }

  public void addLifeEventAsWitness(@NotNull LifeEvent event) {
    // Add link from event to this witness if not yet done
    if (!event.hasWitness(this)) {
      event.addWitness(this);
    }
    // Add event to this witness; may have been done by the event itself in previous branch
    if (!this.lifeEvents.contains(event)) {
      this.lifeEvents.add(event);
      this.lifeEvents.sort(null);
    }
  }

  public void removeLifeEventAsWitness(@NotNull LifeEvent event) {
    // Remove link from event to this witness if not yet done
    if (event.hasWitness(this)) {
      event.removeWitness(this);
    }
    // Remove event from this witness; may have been done by the event itself in previous branch
    if (!event.hasActor(this)) {
      this.lifeEvents.remove(event);
    }
  }

  private <T> void ensureNoDuplicates(final @NotNull List<T> values) {
    Set<T> seen = new HashSet<>();
    for (T value : values) {
      if (seen.contains(value)) {
        throw new IllegalArgumentException("duplicate value %s".formatted(value));
      }
      seen.add(value);
    }
  }

  private List<String> filterOutEmptyStrings(final @NotNull List<String> values) {
    return values.stream().map(String::strip).filter(s -> !s.isEmpty()).toList();
  }

  @Override
  public String toString() {
    String firstNames = this.getJoinedPublicFirstNames()
        .orElse(this.getJoinedLegalFirstNames().orElse("?"));
    String lastName = this.publicLastName()
        .orElse(this.legalLastName().orElse("?"));
    String s = firstNames + " " + lastName;
    if (this.disambiguationID != null) {
      s += " (#%d)".formatted(this.disambiguationID);
    }
    return s;
  }

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

  public record Child(@NotNull Person person, boolean adopted) {
    public Child {
      Objects.requireNonNull(person);
    }
  }
}
