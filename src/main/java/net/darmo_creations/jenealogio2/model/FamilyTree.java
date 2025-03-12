package net.darmo_creations.jenealogio2.model;

import net.darmo_creations.jenealogio2.io.*;
import net.darmo_creations.jenealogio2.io.file_ops.*;
import net.darmo_creations.jenealogio2.utils.*;
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
  }

  public boolean canMergePersons(final @NotNull Person person1, final @NotNull Person person2) {
    return true; // TODO check parents, children, and life events constraints
  }

  /**
   * Merge the data of the given persons.
   * A new {@link Person} object is created and given the selected data of the passed person objects.
   * The passed persons are then removed from the tree.
   *
   * @param person1       A person
   * @param person2       Another person.
   * @param dataSelection An object indicating which person is the data source for which property.
   * @throws IllegalArgumentException If both arguments are the same object.
   */
  public void mergePersons(
      @NotNull Person person1,
      @NotNull Person person2,
      @NotNull PersonMergeInfo dataSelection
  ) {
    Objects.requireNonNull(person1);
    Objects.requireNonNull(person2);
    Objects.requireNonNull(dataSelection);
    if (person1 == person2)
      throw new IllegalArgumentException("cannot merge a person with itself");
    if (!this.canMergePersons(person1, person2))
      throw new IllegalArgumentException("cannot merge %s and %s because it would break some constraint(s)");

    final Person merged = new Person();
    this.addPerson(merged);
    if (this.isRoot(person1) || this.isRoot(person2))
      this.setRoot(merged);

    this.mergeProperties(person1, person2, dataSelection, merged);
    this.mergeParents(person1, person2, dataSelection, merged);
    this.mergeChildren(person1, person2, dataSelection, merged);
    this.mergeLifeEvents(person1, person2, merged);
    this.mergeFiles(person1, person2, merged);

    this.removePerson(person1);
    this.removePerson(person2);
  }

  private void mergeProperties(
      final @NotNull Person person1,
      final @NotNull Person person2,
      @NotNull PersonMergeInfo dataSelection,
      @NotNull Person merged
  ) {
    switch (dataSelection.lifeStatus()) {
      case LEFT -> merged.setLifeStatus(person1.lifeStatus());
      case RIGHT -> merged.setLifeStatus(person2.lifeStatus());
      case BOTH -> throw new IllegalArgumentException("cannot merge two life statuses");
    }

    switch (dataSelection.legalLastName()) {
      case LEFT -> merged.setLegalLastName(person1.legalLastName().orElseThrow());
      case RIGHT -> merged.setLegalLastName(person2.legalLastName().orElseThrow());
      case BOTH -> throw new IllegalArgumentException("cannot merge two legal last names");
    }

    switch (dataSelection.legalFirstNames()) {
      case LEFT -> merged.setLegalFirstNames(person1.legalFirstNames());
      case RIGHT -> merged.setLegalFirstNames(person2.legalFirstNames());
      case BOTH -> merged.setLegalFirstNames(this.mergeLists(person1.legalFirstNames(), person2.legalFirstNames()));
    }

    switch (dataSelection.publicLastName()) {
      case LEFT -> merged.setPublicLastName(person1.publicLastName().orElseThrow());
      case RIGHT -> merged.setPublicLastName(person2.publicLastName().orElseThrow());
      case BOTH -> throw new IllegalArgumentException("cannot merge two public last names");
    }

    switch (dataSelection.publicFirstNames()) {
      case LEFT -> merged.setPublicFirstNames(person1.publicFirstNames());
      case RIGHT -> merged.setPublicFirstNames(person2.publicFirstNames());
      case BOTH -> merged.setPublicFirstNames(this.mergeLists(person1.publicFirstNames(), person2.publicFirstNames()));
    }

    switch (dataSelection.nicknames()) {
      case LEFT -> merged.setNicknames(person1.nicknames());
      case RIGHT -> merged.setNicknames(person2.nicknames());
      case BOTH -> merged.setNicknames(this.mergeLists(person1.nicknames(), person2.nicknames()));
    }

    switch (dataSelection.agab()) {
      case LEFT -> merged.setAssignedGenderAtBirth(person1.assignedGenderAtBirth().orElseThrow());
      case RIGHT -> merged.setAssignedGenderAtBirth(person2.assignedGenderAtBirth().orElseThrow());
      case BOTH -> throw new IllegalArgumentException("cannot merge two AGABs");
    }

    switch (dataSelection.gender()) {
      case LEFT -> merged.setGender(person1.gender().orElseThrow());
      case RIGHT -> merged.setGender(person2.gender().orElseThrow());
      case BOTH -> throw new IllegalArgumentException("cannot merge two genders");
    }

    switch (dataSelection.disambiguationId()) {
      case LEFT -> merged.setDisambiguationID(person1.disambiguationID().orElseThrow());
      case RIGHT -> merged.setDisambiguationID(person2.disambiguationID().orElseThrow());
      case BOTH -> throw new IllegalArgumentException("cannot merge two disambiguation IDs");
    }

    switch (dataSelection.mainOccupation()) {
      case LEFT -> merged.setMainOccupation(person1.mainOccupation().orElseThrow());
      case RIGHT -> merged.setMainOccupation(person2.mainOccupation().orElseThrow());
      case BOTH -> {
        if (person1.mainOccupation().isPresent() && person2.mainOccupation().isPresent())
          merged.setMainOccupation(person1.mainOccupation().get() + "/" + person2.mainOccupation().get());
        else if (person1.mainOccupation().isPresent())
          merged.setMainOccupation(person1.mainOccupation().get());
        else if (person2.mainOccupation().isPresent())
          merged.setMainOccupation(person2.mainOccupation().get());
        else throw new IllegalArgumentException("missing both main occupations");
      }
    }
  }

  private void mergeParents(
      final @NotNull Person person1,
      final @NotNull Person person2,
      @NotNull PersonMergeInfo dataSelection,
      @NotNull Person merged
  ) {
    for (final var entry : dataSelection.parents().entrySet()) {
      final ParentalRelationType parentType = entry.getKey();
      switch (entry.getValue()) {
        case LEFT -> {
          for (final Person parent : person1.parents(parentType))
            merged.addParent(parent, parentType);
        }
        case RIGHT -> {
          for (final Person parent : person2.parents(parentType))
            merged.addParent(parent, parentType);
        }
        case BOTH -> {
          for (final Person parent : Sets.union(person1.parents(parentType), person2.parents(parentType)))
            merged.addParent(parent, parentType);
        }
      }
    }
  }

  private void mergeChildren(
      final @NotNull Person person1,
      final @NotNull Person person2,
      @NotNull PersonMergeInfo dataSelection,
      @NotNull Person merged
  ) {
    for (final var entry : dataSelection.children().entrySet()) {
      final ParentalRelationType childType = entry.getKey();
      switch (entry.getValue()) {
        case LEFT -> {
          for (final Person child : person1.children(childType)) {
            child.removeParent(person1);
            child.addParent(merged, childType);
          }
        }
        case RIGHT -> {
          for (final Person child : person2.children(childType)) {
            child.removeParent(person2);
            child.addParent(merged, childType);
          }
        }
        case BOTH -> {
          for (final Person child : Sets.union(person1.children(childType), person2.children(childType))) {
            final var parentType1 = child.getParentType(person1);
            if (parentType1.isPresent() && parentType1.get() == childType)
              child.removeParent(person1);
            final var parentType2 = child.getParentType(person2);
            if (parentType2.isPresent() && parentType2.get() == childType)
              child.removeParent(person2);
            child.addParent(merged, childType);
          }
        }
      }
    }
  }

  private void mergeLifeEvents(
      @NotNull Person person1,
      @NotNull Person person2,
      @NotNull Person merged
  ) {
    this.mergeActors(person1, merged);
    this.mergeActors(person2, merged);
    this.mergeWitnesses(person1, merged);
    this.mergeWitnesses(person2, merged);
  }

  private void mergeActors(@NotNull Person source, @NotNull Person target) {
    source.getLifeEventsAsActor().forEach(event -> {
      final Set<Person> actors = event.actors();
      actors.remove(source);
      actors.add(target);
      this.setLifeEventActors(event, actors);
    });
  }

  private void mergeWitnesses(@NotNull Person source, @NotNull Person target) {
    source.getLifeEventsAsWitness().forEach(event -> {
      this.removeWitnessFromLifeEvent(event, source);
      this.addWitnessToLifeEvent(event, target);
    });
  }

  private void mergeFiles(final @NotNull Person person1, final @NotNull Person person2, @NotNull Person merged) {
    person1.documents().forEach(merged::addDocument);
    person2.documents().forEach(merged::addDocument);
  }

  private List<String> mergeLists(final @NotNull List<String> list1, final @NotNull List<String> list2) {
    final List<String> result = new ArrayList<>(list1);
    result.addAll(list2);
    return result;
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
    final AttachedDocument document = this.documents.remove(fileName);
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
