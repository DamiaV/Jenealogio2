package net.darmo_creations.jenealogio2.model;

import net.darmo_creations.jenealogio2.io.*;
import net.darmo_creations.jenealogio2.io.file_ops.*;
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
  private final Map<String, AttachedDocument> documents = new HashMap<>();
  private final List<FileOperation> fileOperations = new LinkedList<>();
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
   * An unmodifiable view of this tree’s documents.
   */
  public @UnmodifiableView Collection<AttachedDocument> documents() {
    return Collections.unmodifiableCollection(this.documents.values());
  }

  /**
   * Get a document with the given name.
   *
   * @param fileName The document’s name.
   * @return The document.
   */
  public Optional<AttachedDocument> getDocument(@NotNull String fileName) {
    return Optional.ofNullable(this.documents.get(fileName));
  }

  /**
   * An unmodifiable view of all pending file operations of this tree.
   */
  public @UnmodifiableView List<FileOperation> pendingFileOperations() {
    return Collections.unmodifiableList(this.fileOperations);
  }

  /**
   * Clear all pending file operations of this tree.
   */
  public void clearPendingFileOperations() {
    this.fileOperations.clear();
  }

  /**
   * Add a person to this tree.
   * If this tree has no root yet, the passed person will become it.
   *
   * @param person The person to add.
   */
  public void addPerson(@NotNull Person person) {
    if (this.persons.isEmpty())
      this.root = person;
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
    if (person == this.root)
      throw new IllegalArgumentException("cannot delete root");
    person.setParent(0, null);
    person.setParent(1, null);
    for (LifeEvent lifeEvent : person.lifeEvents()) {
      this.removeActorFromLifeEvent(lifeEvent, person);
      lifeEvent.removeWitness(person);
      this.lifeEvents.remove(lifeEvent);
    }
    for (Person child : person.children())
      child.removeParent(person);
    for (var relativeType : Person.RelativeType.values()) {
      for (Person nonBiologicalChild : person.nonBiologicalChildren(relativeType))
        nonBiologicalChild.removeRelative(person, relativeType);
      for (Person relative : person.getRelatives(relativeType))
        person.removeRelative(relative, relativeType);
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
    if (!lifeEvent.hasActor(actor))
      return;
    if (lifeEvent.actors().size() <= lifeEvent.type().minActors()) {
      lifeEvent.actors().forEach(a -> a.removeLifeEvent(lifeEvent));
      lifeEvent.witnesses().forEach(w -> w.removeLifeEvent(lifeEvent));
      actor.removeLifeEvent(lifeEvent);
      this.lifeEvents.remove(lifeEvent);
    } else
      lifeEvent.removeActor(actor);
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
   * Add a document to this tree.
   *
   * @param document The document to add.
   * @return True if the document was added, false otherwise.
   */
  public boolean addDocument(@NotNull AttachedDocument document) {
    if (this.documents.containsKey(document.fileName()))
      return false;
    this.documents.put(document.fileName(), document);
    this.fileOperations.add(new ImportFileOperation(document.fileName(), document.path(), document));
    return true;
  }

  /**
   * Remove from this tree the document with the given name.
   * The document is also removed from every {@link GenealogyObject}.
   *
   * @param fileName Name of the document to remove.
   * @return The removed document or null if no document was removed.
   */
  public @Nullable AttachedDocument removeDocument(@NotNull String fileName) {
    if (!this.documents.containsKey(fileName))
      return null;
    Objects.requireNonNull(fileName);
    this.persons.forEach(p -> this.removeDocumentFromObject(fileName, p));
    this.lifeEvents.forEach(l -> this.removeDocumentFromObject(fileName, l));
    AttachedDocument document = this.documents.remove(fileName);
    this.fileOperations.add(new DeleteFileOperation(fileName, document));
    return document;
  }

  /**
   * Rename the given document.
   *
   * @param oldFileName The file name of the document to rename.
   * @param newName     The new name (without the extension).
   * @throws IllegalArgumentException If both names are equal, the old name is not registered,
   *                                  or the new name is already registered.
   */
  public void renameDocument(@NotNull String oldFileName, @NotNull String newName) {
    var split = FileUtils.splitExtension(oldFileName);
    String oldName = split.left();
    String newFileName = newName + split.right().orElse("");
    if (oldName.equals(newName))
      throw new IllegalArgumentException("Old and new name should not be the same");
    if (!this.documents.containsKey(oldFileName))
      throw new IllegalArgumentException("No document with name \"%s\"".formatted(oldFileName));
    if (this.documents.containsKey(newFileName))
      throw new IllegalArgumentException("A document with the name \"%s\" already exists".formatted(newFileName));
    AttachedDocument document = this.documents.remove(oldFileName);
    document.setName(newName);
    this.documents.put(newFileName, document);
    this.fileOperations.add(new RenameFileOperation(oldFileName, newFileName, document));
  }

  /**
   * Add a document from this tree to the given {@link GenealogyObject}.
   *
   * @param fileName Name of the document to add.
   * @param o        The object to update.
   * @throws NoSuchElementException If no document of this tree matches the given name.
   */
  public void addDocumentToObject(@NotNull String fileName, @NotNull GenealogyObject<?> o) {
    if (!this.documents.containsKey(fileName))
      throw new NoSuchElementException("No document with name " + fileName);
    o.addDocument(this.documents.get(Objects.requireNonNull(fileName)));
  }

  /**
   * Remove from the given {@link GenealogyObject} the document with the given ID.
   *
   * @param fileName Name of the document to remove.
   * @param o        The object to update.
   */
  public void removeDocumentFromObject(@NotNull String fileName, @NotNull GenealogyObject<?> o) {
    o.removeDocument(fileName);
  }

  /**
   * Set the main picture of a {@link GenealogyObject}.
   *
   * @param fileName Name of the picture to set as main. May be null.
   * @param o        The object to update.
   * @throws IllegalArgumentException If no picture with the given name is associated with the object.
   * @throws ClassCastException       If the file is not a picture.
   */
  public void setMainPictureOfObject(String fileName, @NotNull GenealogyObject<?> o) {
    o.setMainPicture(fileName);
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
    if (!this.persons.contains(root))
      throw new NoSuchElementException("Person %s is not in this family tree".formatted(root));
    this.root = Objects.requireNonNull(root);
  }
}
