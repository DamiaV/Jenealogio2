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
    return Optional.empty(); // TODO
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
    if (isDead) {
      throw new IllegalArgumentException("cannot change status of a person with at least one event indicating death");
    }
    this.lifeStatus = Objects.requireNonNull(lifeStatus);
  }

  public List<String> legalFirstNames() {
    return new ArrayList<>(this.legalFirstNames);
  }

  public Optional<String> getJoinedLegalFirstNames() {
    return StringUtils.stripNullable(String.join(", ", this.legalFirstNames));
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
    return StringUtils.stripNullable(String.join(", ", this.publicFirstNames));
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
    return Optional.empty(); // TODO
  }

  public Optional<CalendarDate> getDeathDate() {
    return Optional.empty(); // TODO
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
    if (!event.hasActor(this)) {
      event.addActor(this);
    }
    if (!this.lifeEvents.contains(event)) {
      this.lifeEvents.add(event);
      this.lifeEvents.sort(null);
    }
    // TODO update life status
  }

  public void removeLifeEventAsActor(@NotNull LifeEvent event) {
    if (event.hasActor(this)) {
      event.removeActor(this);
    }
    if (!event.hasWitness(this)) {
      this.lifeEvents.remove(event);
    }
    // TODO update life status
  }

  public void addLifeEventAsWitness(@NotNull LifeEvent event) {
    if (!event.hasWitness(this)) {
      event.addWitness(this);
    }
    if (!this.lifeEvents.contains(event)) {
      this.lifeEvents.add(event);
      this.lifeEvents.sort(null);
    }
  }

  public void removeLifeEventAsWitness(@NotNull LifeEvent event) {
    if (event.hasWitness(this)) {
      event.removeWitness(this);
    }
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

  public record Child(@NotNull Person person, boolean adopted) {
    public Child {
      Objects.requireNonNull(person);
    }
  }
}
