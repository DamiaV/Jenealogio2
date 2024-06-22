package net.darmo_creations.jenealogio2.model;

import org.jetbrains.annotations.*;

import java.util.*;

/**
 * A family tree has a name and contains a set of persons that belong to it.
 * Trees also have a root which is the person diplayed by default.
 */
public class FamilyTree {
  private final GenderRegistry genderRegistry = new GenderRegistry();
  private final LifeEventTypeRegistry lifeEventTypeRegistry = new LifeEventTypeRegistry();

  private final Set<Person> persons = new HashSet<>();
  private final Set<LifeEvent> lifeEvents = new HashSet<>();
  private final Map<String, Picture> pictures = new HashMap<>();
  private String name;
  private Person root;

  /**
   * Create a new family tree.
   *
   * @param name Tree’s name.
   */
  public FamilyTree(@NotNull String name) {
    this.setName(name);
  }

  /**
   * Tree’s name.
   */
  public String name() {
    return this.name;
  }

  /**
   * Set tree’s name.
   *
   * @param name The new name.
   */
  public void setName(@NotNull String name) {
    this.name = Objects.requireNonNull(name);
  }

  /**
   * A copy of this tree’s members set.
   */
  public Set<Person> persons() {
    return new HashSet<>(this.persons);
  }

  /**
   * A copy of this tree’s life events set.
   */
  public Set<LifeEvent> lifeEvents() {
    return new HashSet<>(this.lifeEvents);
  }

  /**
   * Get a view of this tree’s pictures.
   */
  public Collection<Picture> pictures() {
    return this.pictures.values();
  }

  /**
   * Get a picture with the given name.
   *
   * @param name The picture’s name.
   * @return The picture.
   */
  public Optional<Picture> getPicture(@NotNull String name) {
    return Optional.ofNullable(this.pictures.get(name));
  }

  /**
   * Add a person to this tree.
   * If this tree has no root yet, the passed person will become it.
   *
   * @param person The person to add.
   */
  public void addPerson(@NotNull Person person) {
    if (this.persons.isEmpty()) {
      this.root = person;
    }
    person.setFamilyTree(this);
    this.persons.add(person);
  }

  /**
   * Remove the given person from this tree.
   *
   * @param person The person to remove.
   * @throws IllegalArgumentException If the passed person is this tree’s root.
   */
  public void removePerson(Person person) {
    if (person == this.root) {
      throw new IllegalArgumentException("cannot delete root");
    }
    person.setParent(0, null);
    person.setParent(1, null);
    for (LifeEvent lifeEvent : person.lifeEvents()) {
      this.removeActorFromLifeEvent(lifeEvent, person);
      lifeEvent.removeWitness(person);
      this.lifeEvents.remove(lifeEvent);
    }
    for (Person child : person.children()) {
      child.removeParent(person);
    }
    for (var type : Person.RelativeType.values()) {
      for (Person nonBiologicalChild : person.nonBiologicalChildren(type)) {
        nonBiologicalChild.removeRelative(person, type);
      }
      for (Person relative : person.getRelatives(type)) {
        person.removeRelative(relative, type);
      }
    }
    this.persons.remove(person);
  }

  /**
   * Set the actors of the given life event. The event is added to this tree’s events set.
   *
   * @param lifeEvent The event to set actors of.
   * @param actors    Persons to set as actors.
   */
  public void setLifeEventActors(@NotNull LifeEvent lifeEvent, final @NotNull Set<Person> actors) {
    this.lifeEvents.add(lifeEvent);
    lifeEvent.setActors(actors);
  }

  /**
   * Remove an actor from a life event. If the event is at its minimum allowed number of actors,
   * all actors and witnesses are detached from the event and the event itself is removed from this tree’s events set.
   *
   * @param lifeEvent Life event to remove the actor from.
   * @param actor     Actor to remove.
   */
  public void removeActorFromLifeEvent(@NotNull LifeEvent lifeEvent, @NotNull Person actor) {
    if (!lifeEvent.hasActor(actor)) {
      return;
    }
    if (lifeEvent.actors().size() <= lifeEvent.type().minActors()) {
      lifeEvent.actors().forEach(a -> a.removeLifeEvent(lifeEvent));
      lifeEvent.witnesses().forEach(w -> w.removeLifeEvent(lifeEvent));
      actor.removeLifeEvent(lifeEvent);
      this.lifeEvents.remove(lifeEvent);
    } else {
      lifeEvent.removeActor(actor);
    }
  }

  /**
   * Add a witness to a life event. The event is added to this tree’s events set.
   *
   * @param lifeEvent Life event to add the witness to.
   * @param witness   The witness to add.
   */
  public void addWitnessToLifeEvent(@NotNull LifeEvent lifeEvent, @NotNull Person witness) {
    this.lifeEvents.add(lifeEvent);
    lifeEvent.addWitness(witness);
  }

  /**
   * Remove a witness from a life event.
   *
   * @param lifeEvent Life event to remove the witness from.
   * @param witness   The witness to remove.
   */
  public void removeWitnessFromLifeEvent(@NotNull LifeEvent lifeEvent, @NotNull Person witness) {
    lifeEvent.removeWitness(witness);
  }

  /**
   * Add a picture to this tree.
   *
   * @param picture The picture to add.
   * @return True if the picture was added, false otherwise.
   */
  public boolean addPicture(@NotNull Picture picture) {
    if (this.pictures.containsKey(picture.name())) {
      return false;
    }
    this.pictures.put(picture.name(), picture);
    return true;
  }

  /**
   * Remove from this tree the picture with the given name.
   * The picture is also removed from every {@link GenealogyObject}.
   *
   * @param name Name of the picture to remove.
   * @return The removed picture.
   */
  public Picture removePicture(@NotNull String name) {
    Objects.requireNonNull(name);
    this.persons.forEach(p -> this.removePictureFromObject(name, p));
    this.lifeEvents.forEach(l -> this.removePictureFromObject(name, l));
    return this.pictures.remove(name);
  }

  /**
   * Rename the given picture.
   *
   * @param oldName The name of the picture to rename.
   * @param newName The new name.
   * @throws IllegalArgumentException If both names are equal, the old name is not registered,
   *                                  or the new name is already registered.
   */
  public void renamePicture(@NotNull String oldName, @NotNull String newName) {
    if (oldName.equals(newName))
      throw new IllegalArgumentException("old and new name should not be the same");
    if (!this.pictures.containsKey(oldName))
      throw new IllegalArgumentException("no picture with name \"%s\"".formatted(oldName));
    if (this.pictures.containsKey(newName))
      throw new IllegalArgumentException("a picture with the name \"%s\" already exists".formatted(newName));
    Picture picture = this.pictures.remove(oldName);
    picture.setName(newName);
    this.pictures.put(newName, picture);
  }

  /**
   * Add a picture from this tree to the given {@link GenealogyObject}.
   *
   * @param name Name of the picture to add.
   * @param o    The object to update.
   * @throws NoSuchElementException If no picture of this tree matches the given name.
   */
  public void addPictureToObject(@NotNull String name, @NotNull GenealogyObject<?> o) {
    if (!this.pictures.containsKey(name))
      throw new NoSuchElementException("No picture with name " + name);
    o.addPicture(this.pictures.get(Objects.requireNonNull(name)));
  }

  /**
   * Remove from the given {@link GenealogyObject} the picture with the given ID.
   *
   * @param name Name of the picture to remove.
   * @param o    The object to update.
   */
  public void removePictureFromObject(@NotNull String name, @NotNull GenealogyObject<?> o) {
    o.removePicture(name);
  }

  /**
   * Set the main picture of a {@link GenealogyObject}.
   *
   * @param name Name of the picture to set as main. May be null.
   * @param o    The object to update.
   * @throws IllegalArgumentException If no picture with the given name is associated with the object.
   */
  public void setMainPictureOfObject(String name, @NotNull GenealogyObject<?> o) {
    o.setMainPicture(name);
  }

  /**
   * Indicate whether the given person is this tree’s root.
   *
   * @param person Person to check.
   * @return True if the given person is this tree’s root, false otherwise.
   */
  public boolean isRoot(final Person person) {
    return this.root == person;
  }

  /**
   * This tree’s root. Will only be empty if this tree has no member.
   */
  public Optional<Person> root() {
    return Optional.ofNullable(this.root);
  }

  public GenderRegistry genderRegistry() {
    return this.genderRegistry;
  }

  public LifeEventTypeRegistry lifeEventTypeRegistry() {
    return this.lifeEventTypeRegistry;
  }

  /**
   * Set this tree’s root.
   *
   * @param root Person to be set as root.
   * @throws NoSuchElementException If the passed person is not a member of this tree.
   */
  public void setRoot(@NotNull Person root) {
    if (!this.persons.contains(root)) {
      throw new NoSuchElementException("Person %s is not in this family tree".formatted(root));
    }
    this.root = Objects.requireNonNull(root);
  }
}
