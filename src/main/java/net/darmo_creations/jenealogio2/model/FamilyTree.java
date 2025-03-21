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
  private final List<FileOperation> pendingFileOperations = new LinkedList<>();
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
    return Collections.unmodifiableList(this.pendingFileOperations);
  }

  /**
   * Clear all pending file operations of this tree.
   */
  public void clearPendingFileOperations() {
    this.pendingFileOperations.clear();
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
    for (final var type : ParentalRelationType.values()) {
      for (final Person parent : new HashSet<>(person.parents(type)))
        person.removeParent(parent);
      for (final Person child : new HashSet<>(person.children(type)))
        child.removeParent(person);
    }
    for (final LifeEvent lifeEvent : person.lifeEvents()) {
      this.removeActorFromLifeEvent(lifeEvent, person);
      lifeEvent.removeWitness(person);
      this.lifeEvents.remove(lifeEvent);
    }
    this.persons.remove(person);
    person.authoredDocuments().forEach(d -> d.removeAuthor(person));
    for (final var annotationType : AnnotationType.values())
      person.getAnnotatedInDocuments(annotationType)
          .forEach(d -> d.removeObjectAnnotation(annotationType, person));
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
    this.pendingFileOperations.add(new ImportFileOperation(document.fileName(), document.path(), document));
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
    final AttachedDocument document = this.documents.remove(fileName);
    Objects.requireNonNull(fileName);
    this.persons.forEach(p -> {
      p.removeAuthoredDocument(document);
      for (final var annotationType : AnnotationType.values())
        p.removeAnnotatedInDocument(annotationType, document);
    });
    this.lifeEvents.forEach(event -> {
      for (final var annotationType : AnnotationType.values())
        event.removeAnnotatedInDocument(annotationType, document);
    });
    this.pendingFileOperations.add(new DeleteFileOperation(fileName, document));
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
    final var split = FileUtils.splitExtension(oldFileName);
    final String oldName = split.fileName();
    final String newFileName = newName + split.extension().orElse("");
    if (oldName.equals(newName))
      throw new IllegalArgumentException("Old and new name should not be the same");
    if (!this.documents.containsKey(oldFileName))
      throw new IllegalArgumentException("No document with name \"%s\"".formatted(oldFileName));
    if (this.documents.containsKey(newFileName))
      throw new IllegalArgumentException("A document with the name \"%s\" already exists".formatted(newFileName));
    final AttachedDocument document = this.documents.remove(oldFileName);
    document.setName(newName);
    this.documents.put(newFileName, document);
    this.pendingFileOperations.add(new RenameFileOperation(oldFileName, newFileName, document));
  }

  /**
   * Set the main picture of a {@link GenealogyObject}.
   *
   * @param fileName Name of the picture to set as main. May be null.
   * @param o        The object to update.
   * @throws IllegalArgumentException If the file is not a picture.
   */
  public void setMainPictureOfObject(String fileName, @NotNull GenealogyObject<?> o) {
    if (fileName == null) {
      o.setMainPicture(null);
      return;
    }
    final AttachedDocument document = this.documents.get(fileName);
    if (!(document instanceof Picture p))
      throw new IllegalArgumentException("File \"%s\" is not an image".formatted(document.fileName()));
    o.setMainPicture(p);
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

  public GenderRegistry genderRegistry() {
    return this.genderRegistry;
  }

  public LifeEventTypeRegistry lifeEventTypeRegistry() {
    return this.lifeEventTypeRegistry;
  }
}
