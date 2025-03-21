package net.darmo_creations.jenealogio2.io;

import javafx.scene.image.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.model.datetime.*;
import net.darmo_creations.jenealogio2.model.datetime.calendar.*;
import net.darmo_creations.jenealogio2.model.datetime.calendar.Calendar;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;
import org.w3c.dom.*;

import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.time.format.*;
import java.util.*;
import java.util.function.*;

/**
 * Deserializes {@link FamilyTree} objects from XML data.
 */
public class TreeXMLReader extends TreeXMLManager {
  // region Public methods

  /**
   * Read a family tree object from an input stream.
   *
   * @param inputStream     The stream to read from.
   * @param documentBuilder Function that provides a document for the given name and data.
   * @return The corresponding family tree object.
   * @throws IOException If any error occurs.
   */
  public FamilyTree readFromStream(
      @NotNull InputStream inputStream,
      @NotNull AttachedDocumentBuilder documentBuilder
  ) throws IOException {
    final Document document = XmlUtils.readFile(inputStream);

    final NodeList childNodes = document.getChildNodes();
    final Element familyTreeElement = this.getRootElement(childNodes, FAMILY_TREE_TAG, FAMILY_TREE_VERSION_ATTR);
    final String name = XmlUtils.getAttr(
        familyTreeElement,
        FAMILY_TREE_NAME_ATTR,
        s -> s,
        null,
        true
    );
    final int rootID = XmlUtils.getAttr(
        familyTreeElement,
        FAMILY_TREE_ROOT_ATTR,
        Integer::parseInt,
        () -> -1,
        false
    );

    final Element peopleElement = XmlUtils
        .getChildElement(familyTreeElement, PEOPLE_TAG, false)
        .orElseThrow();
    final FamilyTree familyTree = new FamilyTree(name);

    final Optional<Element> registriesElement = XmlUtils
        .getChildElement(familyTreeElement, REGISTRIES_TAG, true);
    if (registriesElement.isPresent())
      this.loadUserRegistries(registriesElement.get(), familyTree);

    final Optional<Element> documentsElement = XmlUtils
        .getChildElement(familyTreeElement, DOCUMENTS_TAG, true);
    final Map<AttachedDocument, DocumentAnnotations> annotations = new HashMap<>();
    if (documentsElement.isPresent())
      this.loadDocuments(documentsElement.get(), familyTree, documentBuilder, annotations);

    final List<Person> persons = this.readPersons(peopleElement, familyTree);
    if (rootID == -1) {
      if (!persons.isEmpty())
        throw new IOException("Missing root attribute");
    } else {
      try {
        familyTree.setRoot(persons.get(rootID));
      } catch (final IndexOutOfBoundsException e) {
        throw new IOException(e);
      }
    }
    final Optional<Element> eventsElement = XmlUtils
        .getChildElement(familyTreeElement, LIFE_EVENTS_TAG, true);
    final List<LifeEvent> lifeEvents;
    if (eventsElement.isPresent())
      lifeEvents = this.readLifeEvents(eventsElement.get(), persons, familyTree);
    else lifeEvents = new ArrayList<>();

    this.applyDocumentAnnotations(persons, lifeEvents, annotations);

    return familyTree;
  }

  /**
   * Read registries from a {@code .reg} file.
   *
   * @param file File to load.
   * @return The corresponding registries.
   * @throws IOException If any error occurs.
   */
  public RegistriesValues loadRegistriesFile(final @NotNull Path file) throws IOException {
    final FamilyTree dummyTree = new FamilyTree("dummy");
    final Document document = XmlUtils.readFile(new FileInputStream(file.toFile()));
    final NodeList childNodes = document.getChildNodes();
    final Element registriesElement = this.getRootElement(childNodes, REGISTRIES_TAG, REGISTRIES_VERSION_ATTR);
    this.loadUserRegistries(registriesElement, dummyTree);
    return new RegistriesValues(
        dummyTree.lifeEventTypeRegistry().serializableEntries(),
        dummyTree.genderRegistry().serializableEntries()
    );
  }

  private Element getRootElement(
      final @NotNull NodeList childNodes,
      @NotNull String rootTagName,
      @NotNull String documentVersionAttr
  ) throws IOException {
    if (childNodes.getLength() != 1)
      throw new IOException("Parse error");
    final Element familyTreeElement = (Element) childNodes.item(0);
    if (!familyTreeElement.getTagName().equals(rootTagName))
      throw new IOException("Missing root element");
    final int version = XmlUtils.getAttr(
        familyTreeElement,
        documentVersionAttr,
        Integer::parseInt,
        null,
        false
    );
    if (version != 1)
      throw new IOException("Unsupported XML file version: " + version);
    return familyTreeElement;
  }

  /**
   * Read user-defined registry entries.
   *
   * @param registriesElement XML element containing the registries.
   * @param familyTree        Tree to update.
   * @throws IOException If any error occurs.
   */
  private void loadUserRegistries(
      final @NotNull Element registriesElement,
      @NotNull FamilyTree familyTree
  ) throws IOException {
    final Optional<Element> gendersElement = XmlUtils
        .getChildElement(registriesElement, GENDERS_TAG, true);
    if (gendersElement.isPresent())
      for (final Element entryElement : XmlUtils.getChildrenElements(gendersElement.get(), REGISTRY_ENTRY_TAG)) {
        final RegistryEntryKey key = new RegistryEntryKey(XmlUtils.getAttr(
            entryElement,
            REGISTRY_ENTRY_KEY_ATTR,
            s -> s,
            null,
            false
        ));
        final String base64 = XmlUtils.getAttr(
            entryElement,
            GENDER_ICON_ATTR,
            s -> s,
            null,
            false
        );
        final String label = XmlUtils.getAttr(
            entryElement,
            REGISTRY_ENTRY_LABEL_ATTR,
            s -> s,
            null,
            true
        );
        familyTree.genderRegistry().registerEntry(key, label, new GenderRegistry.RegistryArgs(base64ToImage(base64)));
      }

    final Optional<Element> eventTypeElement = XmlUtils
        .getChildElement(registriesElement, LIFE_EVENT_TYPES_TAG, true);
    if (eventTypeElement.isPresent()) {
      for (final Element entryElement : XmlUtils.getChildrenElements(eventTypeElement.get(), REGISTRY_ENTRY_TAG)) {
        final RegistryEntryKey key = new RegistryEntryKey(XmlUtils.getAttr(
            entryElement,
            REGISTRY_ENTRY_KEY_ATTR,
            s -> s,
            null,
            true
        ));
        final String label = XmlUtils.getAttr(
            entryElement,
            REGISTRY_ENTRY_LABEL_ATTR,
            s -> s,
            null,
            true
        );
        final int groupOrdinal = XmlUtils.getAttr(
            entryElement,
            LIFE_EVENT_TYPE_GROUP_ATTR,
            Integer::parseInt,
            null,
            false
        );
        final LifeEventType.Group group;
        try {
          group = LifeEventType.Group.values()[groupOrdinal];
        } catch (final IndexOutOfBoundsException e) {
          throw new IOException(e);
        }
        final boolean indicatesDeath = XmlUtils.getAttr(
            entryElement,
            LIFE_EVENT_TYPE_INDICATES_DEATH_ATTR,
            Boolean::parseBoolean,
            null,
            false
        );
        final boolean indicatesUnion = XmlUtils.getAttr(
            entryElement,
            LIFE_EVENT_TYPE_INDICATES_UNION_ATTR,
            Boolean::parseBoolean,
            null,
            false
        );
        final int actorsNb = XmlUtils.getAttr(
            entryElement,
            LIFE_EVENT_TYPE_ACTORS_NB_ATTR,
            Integer::parseInt,
            null,
            false
        );
        if (actorsNb > 2)
          throw new IOException("invalid actors number: " + actorsNb);
        final boolean unique = XmlUtils.getAttr(
            entryElement,
            LIFE_EVENT_TYPE_UNIQUE_ATTR,
            Boolean::parseBoolean,
            null,
            false
        );
        final var args = new LifeEventTypeRegistry.RegistryArgs(
            group,
            indicatesDeath,
            indicatesUnion,
            actorsNb,
            actorsNb,
            unique
        );
        try {
          familyTree.lifeEventTypeRegistry().registerEntry(key, label, args);
        } catch (final IllegalArgumentException e) {
          throw new IOException(e);
        }
      }
    }
  }

  /**
   * Convert a Base64 string into an {@link Image}.
   *
   * @param base64 The Base64 string to convert.
   * @return The computed image.
   */
  private static Image base64ToImage(@NotNull String base64) {
    final var bb = ByteBuffer.wrap(Base64.getDecoder().decode(base64));
    final int w = bb.get(0);
    final int h = bb.get(1);
    final int[] buffer = new int[w * h];
    for (int i = 0; i < buffer.length; i++)
      buffer[i] = bb.getInt(2 + 4 * i);
    final WritableImage image = new WritableImage(w, h);
    image.getPixelWriter().setPixels(0, 0, w, h, PixelFormat.getIntArgbInstance(), buffer, 0, w);
    return image;
  }

  // endregion
  // region Documents

  /**
   * Load the documents from the {@code <Documents>} tag.
   *
   * @param documentsElement XML element containing the documents’ definitions.
   * @param familyTree       The family tree to load documents into.
   * @param documentBuilder  Function that provides an {@link AttachedDocument} for the given name.
   * @throws IOException In any error occurs.
   */
  private void loadDocuments(
      @NotNull Element documentsElement,
      @NotNull FamilyTree familyTree,
      @NotNull AttachedDocumentBuilder documentBuilder,
      @NotNull Map<AttachedDocument, DocumentAnnotations> annotations
  ) throws IOException {
    final List<Element> documentElements = XmlUtils.getChildrenElements(documentsElement, DOCUMENT_TAG);
    for (final Element documentElement : documentElements) {
      final String name = XmlUtils.getAttr(
          documentElement,
          DOCUMENT_NAME_ATTR,
          s -> s,
          () -> null,
          false
      );
      final Optional<Element> descElement = XmlUtils
          .getChildElement(documentElement, DOCUMENT_DESC_TAG, true);
      final String desc = descElement.map(Element::getTextContent).orElse(null);
      final DateTime date = this.readDateTag(documentElement, true);
      final AttachedDocument document = documentBuilder.build(name, desc, date);
      familyTree.addDocument(document);

      final Optional<Element> authorsElement = XmlUtils
          .getChildElement(documentElement, AUTHORS_TAG, true);
      final List<Integer> authorsIds;
      if (authorsElement.isPresent()) {
        authorsIds = XmlUtils.getAttr(
            authorsElement.get(),
            AUTHORS_IDS_ATTR,
            s -> Arrays.stream(s.split(",")).map(Integer::parseInt).toList(),
            null,
            false
        );
      } else authorsIds = new ArrayList<>();

      final Optional<Element> annotationsElement = XmlUtils
          .getChildElement(documentElement, DOCUMENT_ANNOTATIONS_TAG, true);
      if (annotationsElement.isEmpty()) continue;

      final Map<AnnotationType, Set<Annotation>> docAnnotations = new EnumMap<>(AnnotationType.class);
      for (final var annotationType : AnnotationType.values()) {
        docAnnotations.put(annotationType, new HashSet<>());
        final Optional<Element> docAnnotationsElement = XmlUtils
            .getChildElement(annotationsElement.get(), StringUtils.capitalize(annotationType.name()), true);
        if (docAnnotationsElement.isPresent()) {
          this.readAnnotations(docAnnotationsElement.get(), PERSON_ANNOTATION_TAG, docAnnotations.get(annotationType), Person.class);
          this.readAnnotations(docAnnotationsElement.get(), LIFE_EVENT_ANNOTATION_TAG, docAnnotations.get(annotationType), LifeEvent.class);
        }
      }
      annotations.put(document, new DocumentAnnotations(authorsIds, docAnnotations));
    }
  }

  private void readAnnotations(
      final @NotNull Element mentionsElement,
      @NotNull String tagName,
      @NotNull Set<Annotation> annotations,
      @NotNull Class<? extends GenealogyObject<?>> objectClass
  ) throws IOException {
    for (final Element element : XmlUtils.getChildrenElements(mentionsElement, tagName)) {
      final Integer id = XmlUtils.getAttr(
          element,
          ANNOTATION_OBJECT_ID_ATTR,
          Integer::parseInt,
          null,
          false
      );
      final String note = XmlUtils.getAttr(
          element,
          ANNOTATION_NOTE_ATTR,
          s -> s,
          () -> null,
          true
      );
      annotations.add(new Annotation(id, objectClass, note));
    }
  }

  private void applyDocumentAnnotations(
      final @NotNull List<Person> persons,
      final @NotNull List<LifeEvent> lifeEvents,
      final @NotNull Map<AttachedDocument, DocumentAnnotations> annotations
  ) {
    for (final var entry : annotations.entrySet()) {
      final var document = entry.getKey();
      final var docAnnotations = entry.getValue();
      final List<Integer> authorsIds = docAnnotations.authorsIds();

      for (int i = 0; i < authorsIds.size(); i++)
        document.addAuthor(persons.get(authorsIds.get(i)), i);

      for (final var annotationType : AnnotationType.values())
        for (final var annotation : docAnnotations.annotations().get(annotationType)) {
          final int objectId = annotation.objectId();
          final var objectClass = annotation.objectClass();
          final GenealogyObject<?> object;
          if (objectClass == Person.class) object = persons.get(objectId);
          else if (objectClass == LifeEvent.class) object = lifeEvents.get(objectId);
          else throw new IllegalArgumentException("invalid object objectClass: " + objectClass);
          document.annotateObject(annotationType, object, annotation.note());
        }
    }
  }

  private record DocumentAnnotations(
      @NotNull List<Integer> authorsIds,
      @NotNull Map<AnnotationType, Set<Annotation>> annotations
  ) {
    DocumentAnnotations {
      Objects.requireNonNull(authorsIds);
      Objects.requireNonNull(annotations);
    }
  }

  private record Annotation(
      int objectId,
      @NotNull Class<? extends GenealogyObject<?>> objectClass,
      String note
  ) {
    Annotation {
      Objects.requireNonNull(objectClass);
    }
  }

  // endregion
  // region Persons

  /**
   * Read all Person XML elements.
   *
   * @param peopleElement XML element containing Person elements.
   * @param familyTree    The family tree to populate.
   * @return The list of loaded persons.
   * @throws IOException In any error occurs.
   */
  private List<Person> readPersons(
      final @NotNull Element peopleElement,
      @NotNull FamilyTree familyTree
  ) throws IOException {
    final List<Person> persons = new LinkedList<>();
    final Map<Person, Map<ParentalRelationType, List<Integer>>> parentsIDs = new HashMap<>();

    for (final Element personElement : XmlUtils.getChildrenElements(peopleElement, PERSON_TAG)) {
      final Person person = new Person();

      this.readDisambiguationIdTag(personElement, person);
      this.readLifeStatusTag(personElement, person);
      // Legal last name
      this.readName(personElement, LEGAL_LAST_NAME_TAG, person::setLegalLastName);
      // Legal first names
      this.readNames(personElement, LEGAL_FIRST_NAMES_TAG, person::setLegalFirstNames);
      // Public last name
      this.readName(personElement, PUBLIC_LAST_NAME_TAG, person::setPublicLastName);
      // Public first names
      this.readNames(personElement, PUBLIC_FIRST_NAMES_TAG, person::setPublicFirstNames);
      // Nicknames
      this.readNames(personElement, NICKNAMES_TAG, person::setNicknames);
      this.readGenderTag(personElement, familyTree, AGAB_TAG, AGAB_KEY_ATTR, person::setAssignedGenderAtBirth);
      this.readGenderTag(personElement, familyTree, GENDER_TAG, GENDER_KEY_ATTR, person::setGender);
      this.readMainOccupationTag(personElement, person);
      this.readParentsTag(personElement, person, parentsIDs);
      this.readNotesTag(personElement, person);
      this.readSourcesTag(personElement, person);
      this.readMainPictureTag(personElement, person, familyTree);

      familyTree.addPerson(person);
      persons.add(person);
    }

    this.setParents(persons, parentsIDs);

    return persons;
  }

  /**
   * Read the {@code <DisambiguationID>} tag for the given person.
   *
   * @param personElement {@code <Person>} element to extract the disambiguation ID from.
   * @param person        The person corresponding to the {@code <Person>} tag.
   */
  private void readDisambiguationIdTag(
      final @NotNull Element personElement,
      @NotNull Person person
  ) throws IOException {
    final Optional<Element> disambIDElement = XmlUtils
        .getChildElement(personElement, DISAMBIGUATION_ID_TAG, true);
    if (disambIDElement.isPresent()) {
      try {
        person.setDisambiguationID(XmlUtils.getAttr(
            disambIDElement.get(),
            DISAMBIG_ID_VALUE_ATTR,
            Integer::parseInt,
            () -> null,
            false
        ));
      } catch (final IllegalArgumentException e) {
        throw new IOException(e);
      }
    }
  }

  /**
   * Read the {@code <LifeStatus>} tag for the given person.
   *
   * @param personElement {@code <Person>} element to extract the life status from.
   * @param person        The person corresponding to the {@code <Person>} tag.
   */
  private void readLifeStatusTag(
      final @NotNull Element personElement,
      @NotNull Person person
  ) throws IOException {
    final Element lifeStatusElement = XmlUtils
        .getChildElement(personElement, LIFE_STATUS_TAG, false).orElseThrow();
    try {
      final int ordinal = XmlUtils.getAttr(
          lifeStatusElement,
          LIFE_STATUS_ORDINAL_ATTR,
          Integer::parseInt,
          null,
          false
      );
      person.setLifeStatus(LifeStatus.values()[ordinal]);
    } catch (final IndexOutOfBoundsException e) {
      throw new IOException(e);
    }
  }

  /**
   * Read the given gender tag for the given person.
   *
   * @param personElement  {@code <Person>} element to extract the gender from.
   * @param familyTree     The tree to get the {@link Gender} object from.
   * @param tagName        The name of the gender tag to read.
   * @param attrName       The name of the attribute to read on the tag.
   * @param genderConsumer A function that consumes the extracted {@link Gender} object.
   */
  private void readGenderTag(
      final @NotNull Element personElement,
      final @NotNull FamilyTree familyTree,
      final @NotNull String tagName,
      final @NotNull String attrName,
      final @NotNull Consumer<Gender> genderConsumer
  ) throws IOException {
    final Optional<Element> genderElement = XmlUtils
        .getChildElement(personElement, tagName, true);
    if (genderElement.isPresent()) {
      try {
        final RegistryEntryKey key = new RegistryEntryKey(XmlUtils.getAttr(
            genderElement.get(),
            attrName,
            s -> s,
            null,
            false
        ));
        final Gender gender = familyTree.genderRegistry().getEntry(key);
        if (gender == null)
          throw new IOException("Undefined gender registry key: " + key.fullName());
        genderConsumer.accept(gender);
      } catch (final IllegalArgumentException e) {
        throw new IOException(e);
      }
    }
  }

  /**
   * Read the {@code <MainOccupation>} tag for the given person.
   *
   * @param personElement {@code <Person>} element to extract the main occupation from.
   * @param person        The person corresponding to the {@code <Person>} tag.
   */
  private void readMainOccupationTag(
      final @NotNull Element personElement,
      @NotNull Person person
  ) throws IOException {
    final Optional<Element> occupationElement = XmlUtils
        .getChildElement(personElement, MAIN_OCCUPATION_TAG, true);
    if (occupationElement.isPresent()) {
      final String occupation = XmlUtils.getAttr(
          occupationElement.get(),
          MAIN_OCCUPATION_VALUE_ATTR,
          s -> s,
          null,
          true
      );
      person.setMainOccupation(occupation);
    }
  }

  /**
   * Read the {@code <Parents>} tag for the given person and update the given map.
   *
   * @param personElement {@code <Person>} element to extract relatives from.
   * @param person        The person corresponding to the {@code <Person>} tag.
   * @param relativesIDs  Map into which to put all the parents of the given person.
   */
  private void readParentsTag(
      final @NotNull Element personElement,
      final @NotNull Person person,
      @NotNull Map<Person, Map<ParentalRelationType, List<Integer>>> relativesIDs
  ) throws IOException {
    final Optional<Element> relativesElement = XmlUtils
        .getChildElement(personElement, PARENTS_TAG, true);
    if (relativesElement.isPresent()) {
      final HashMap<ParentalRelationType, List<Integer>> groupsMap = new HashMap<>();
      relativesIDs.put(person, groupsMap);
      for (final Element groupElement : XmlUtils
          .getChildrenElements(relativesElement.get(), PARENT_GROUP_TAG)) {
        final int ordinal = XmlUtils.getAttr(
            groupElement,
            PARENT_GROUP_ORDINAL_ATTR,
            Integer::parseInt,
            null,
            false
        );
        final ParentalRelationType parentType;
        try {
          parentType = ParentalRelationType.values()[ordinal];
        } catch (final IndexOutOfBoundsException e) {
          throw new IOException(e);
        }
        final LinkedList<Integer> parentsList = new LinkedList<>();
        groupsMap.put(parentType, parentsList);
        for (final Element parentElement : XmlUtils.getChildrenElements(groupElement, PARENT_TAG)) {
          // Defer setting relatives to when all person objects have been deserialized
          parentsList.add(XmlUtils.getAttr(
              parentElement,
              PARENT_ID_ATTR,
              Integer::parseInt,
              null,
              false
          ));
        }
      }
    }
  }

  /**
   * Read the {@code <Notes>} tag for the given object.
   *
   * @param element XML element to extract notes from.
   * @param o       The object corresponding to the tag.
   */
  private void readNotesTag(
      final @NotNull Element element,
      @NotNull GenealogyObject<?> o
  ) throws IOException {
    final Optional<Element> notesElement = XmlUtils
        .getChildElement(element, NOTES_TAG, true);
    notesElement.ifPresent(e -> o.setNotes(e.getTextContent().strip()));
  }

  /**
   * Read the {@code <Sources>} tag for the given object.
   *
   * @param element XML element to extract sources from.
   * @param o       The object corresponding to the tag.
   */
  private void readSourcesTag(
      final @NotNull Element element,
      @NotNull GenealogyObject<?> o
  ) throws IOException {
    final Optional<Element> sourcesElement = XmlUtils
        .getChildElement(element, SOURCES_TAG, true);
    sourcesElement.ifPresent(e -> o.setSources(e.getTextContent().strip()));
  }

  private void readMainPictureTag(
      final @NotNull Element element,
      @NotNull GenealogyObject<?> o,
      @NotNull FamilyTree familyTree
  ) throws IOException {
    final Optional<Element> mainPictureElement = XmlUtils
        .getChildElement(element, MAIN_PICTURE_TAG, true);
    if (mainPictureElement.isEmpty()) return;

    final String fileName = XmlUtils.getAttr(
        mainPictureElement.get(),
        MAIN_PICTURE_NAME_ATTR,
        s -> s,
        null,
        false
    );
    final var document = familyTree.getDocument(fileName);
    if (document.isPresent() && document.get() instanceof Picture)
      familyTree.setMainPictureOfObject(fileName, o);
  }

  private void setParents(
      final @NotNull List<Person> persons,
      final @NotNull Map<Person, Map<ParentalRelationType, List<Integer>>> parentsIDs
  ) throws IOException {
    for (final var entry : parentsIDs.entrySet()) {
      final Person person = entry.getKey();
      for (final var group : entry.getValue().entrySet()) {
        final ParentalRelationType type = group.getKey();
        for (final int personID : group.getValue())
          try {
            person.addParent(persons.get(personID), type);
          } catch (final IndexOutOfBoundsException e) {
            throw new IOException(e);
          }
      }
    }
  }

  /**
   * Read a name of a Person XML element.
   *
   * @param personElement Person element to read from.
   * @param elementName   Name of the element to read name from.
   * @param consumer      Function that consumes the read name.
   * @throws IOException If any error occurs.
   */
  private void readName(
      final @NotNull Element personElement,
      @NotNull String elementName,
      @NotNull Consumer<String> consumer
  ) throws IOException {
    final Optional<Element> nameElement = XmlUtils
        .getChildElement(personElement, elementName, true);
    if (nameElement.isPresent())
      try {
        consumer.accept(XmlUtils.getAttr(
            nameElement.get(),
            NAME_VALUE_ATTR,
            s -> s,
            null,
            true
        ));
      } catch (final IllegalArgumentException e) {
        throw new IOException(e);
      }
  }

  /**
   * Read a list of names of a Person XML element.
   *
   * @param personElement Person element to read from.
   * @param elementName   Name of the element to read names from.
   * @param consumer      Function that consumes the read names.
   * @throws IOException If any error occurs.
   */
  private void readNames(
      final @NotNull Element personElement,
      @NotNull String elementName,
      @NotNull Consumer<List<String>> consumer
  ) throws IOException {
    final Optional<Element> namesElement = XmlUtils
        .getChildElement(personElement, elementName, true);
    if (namesElement.isPresent()) {
      final List<String> names = new LinkedList<>();
      for (final Element nameElement : XmlUtils
          .getChildrenElements(namesElement.get(), NAME_TAG))
        try {
          names.add(XmlUtils.getAttr(
              nameElement,
              NAME_VALUE_ATTR,
              s -> s,
              null,
              true
          ));
        } catch (final IllegalArgumentException e) {
          throw new IOException(e);
        }
      consumer.accept(names);
    }
  }

  // endregion
  // region Life events

  /**
   * Read all LifeEvent XML elements.
   *
   * @param eventsElement XML element to read from.
   * @param persons       List of loaded persons to fetch IDs from.
   * @return The list of loaded events.
   * @throws IOException If any error occurs.
   */
  private List<LifeEvent> readLifeEvents(
      final @NotNull Element eventsElement,
      final @NotNull List<Person> persons,
      @NotNull FamilyTree familyTree
  ) throws IOException {
    final List<LifeEvent> lifeEvents = new LinkedList<>();

    for (final Element eventElement : XmlUtils.getChildrenElements(eventsElement, LIFE_EVENT_TAG)) {
      final LifeEvent lifeEvent;
      final DateTime date = this.readDateTag(eventElement, false);
      final LifeEventType type = this.readLifeEventTypeTag(eventElement, familyTree);
      final List<Person> actors = this.readActorsTag(eventElement, type, persons);

      try {
        //noinspection DataFlowIssue
        lifeEvent = new LifeEvent(date, type);
        familyTree.setLifeEventActors(lifeEvent, new HashSet<>(actors));
      } catch (final IllegalArgumentException e) {
        throw new IOException(e);
      }
      lifeEvents.add(lifeEvent);

      // Witnesses
      this.extractPersons(
          eventElement,
          WITNESSES_TAG,
          persons,
          p -> familyTree.addWitnessToLifeEvent(lifeEvent, p),
          true
      );
      this.readPlaceTag(eventElement, lifeEvent);
      this.readNotesTag(eventElement, lifeEvent);
      this.readSourcesTag(eventElement, lifeEvent);
      this.readMainPictureTag(eventElement, lifeEvent, familyTree);
    }

    return lifeEvents;
  }

  /**
   * Read the {@code <Date>} tag for the given event.
   *
   * @param eventElement {@code <LifeEvent>} element to extract the date from.
   * @return The date of the event.
   * @throws IOException If the subtree is malformed or the date objectClass is undefined.
   */
  private @Nullable DateTime readDateTag(
      final @NotNull Element eventElement,
      boolean allowMissing
  ) throws IOException {
    final Optional<Element> childElement = XmlUtils
        .getChildElement(eventElement, DATE_TAG, allowMissing);
    if (childElement.isEmpty()) {
      if (allowMissing)
        return null;
      throw new IOException("Missing tag: " + DATE_TAG);
    }
    final Element dateElement = childElement.get();
    final String dateType = XmlUtils.getAttr(
        dateElement,
        DATE_TYPE_ATTR,
        s -> s,
        null,
        false
    );
    return switch (dateType) {
      case DATE_WITH_PRECISION -> {
        final int ordinal = XmlUtils.getAttr(
            dateElement,
            DATE_PRECISION_ATTR,
            Integer::parseInt,
            null,
            false
        );
        final DateTimePrecision precision;
        try {
          precision = DateTimePrecision.values()[ordinal];
        } catch (final IndexOutOfBoundsException e) {
          throw new IOException(e);
        }
        final CalendarSpecificDateTime d = XmlUtils.getAttr(
            dateElement, DATE_DATE_ATTR, this::deserializeDate, null, false);
        yield new DateTimeWithPrecision(d, precision);
      }
      case DATE_RANGE -> {
        final CalendarSpecificDateTime startDate = XmlUtils.getAttr(
            dateElement, DATE_START_ATTR, this::deserializeDate, null, false);
        final CalendarSpecificDateTime endDate = XmlUtils.getAttr(
            dateElement, DATE_END_ATTR, this::deserializeDate, null, false);
        yield new DateTimeRange(startDate, endDate);
      }
      case DATE_ALTERNATIVE -> {
        final List<CalendarSpecificDateTime> dates = new LinkedList<>();
        for (int i = 0; i < DateTimeAlternative.MAX_DATES; i++) {
          final var date = XmlUtils.getAttr(
              dateElement,
              "date" + (i + 1),
              this::deserializeDate,
              () -> null,
              false
          );
          if (date != null)
            dates.add(date);
        }
        try {
          yield new DateTimeAlternative(dates);
        } catch (final IllegalArgumentException | NullPointerException e) {
          throw new IOException(e);
        }
      }
      default -> throw new IOException("Undefined date objectClass " + dateType);
    };
  }

  private CalendarSpecificDateTime deserializeDate(@NotNull String s) {
    final String[] split = s.split(";", 2);
    if (split.length != 2)
      throw new DateTimeParseException("Invalid date format: " + s, s, 0);
    return Calendar.forName(split[1]).parse(split[0]);
  }

  /**
   * Read the {@code <Type>} tag for the given event.
   *
   * @param eventElement {@code <LifeEvent>} element to extract the objectClass from.
   * @param familyTree   The family tree to get {@link LifeEventType} objects from.
   * @return The objectClass of the event.
   * @throws IOException If the event objectClass is undefined or malformed.
   */
  private LifeEventType readLifeEventTypeTag(
      final @NotNull Element eventElement,
      final @NotNull FamilyTree familyTree
  ) throws IOException {
    final LifeEventType type;
    final Element typeElement = XmlUtils
        .getChildElement(eventElement, TYPE_TAG, false).orElseThrow();
    try {
      final RegistryEntryKey key = new RegistryEntryKey(XmlUtils.getAttr(
          typeElement,
          TYPE_KEY_ATTR,
          s -> s,
          null,
          false
      ));
      type = familyTree.lifeEventTypeRegistry().getEntry(key);
      if (type == null)
        throw new IOException("Undefined life event objectClass registry key: " + key.fullName());
    } catch (final IllegalArgumentException e) {
      throw new IOException(e);
    }
    return type;
  }

  /**
   * Read the {@code <Actors>} tag for the given event.
   *
   * @param eventElement {@code <LifeEvent>} element to extract actors from.
   * @param type         The objectClass of the event.
   * @param persons      List of all persons from the current family tree.
   * @return The list of all actors for the event.
   * @throws IOException If the XML subtree is malformed or the event has an invalid number of actors.
   */
  private List<Person> readActorsTag(
      final @NotNull Element eventElement,
      final @NotNull LifeEventType type,
      final @NotNull List<Person> persons
  ) throws IOException {
    final List<Person> actors = new LinkedList<>();
    this.extractPersons(eventElement, ACTORS_TAG, persons, actors::add, false);
    final int actorsNb = actors.size();
    if (actorsNb < type.minActors() || actorsNb > type.maxActors())
      throw new IOException("Wrong number of actors for event objectClass '%s': %d"
          .formatted(type.key().fullName(), actorsNb));
    return actors;
  }

  /**
   * Read the {@code <Place>} tag for the given event.
   *
   * @param eventElement {@code <LifeEvent>} element to extract the place from.
   * @param lifeEvent    The event corresponding to the {@code <LifeEvent>} tag.
   */
  private void readPlaceTag(
      final @NotNull Element eventElement,
      @NotNull LifeEvent lifeEvent
  ) throws IOException {
    final Optional<Element> placeElement = XmlUtils
        .getChildElement(eventElement, PLACE_TAG, true);
    if (placeElement.isPresent()) {
      final Element element = placeElement.get();
      final String address = XmlUtils.getAttr(
          element,
          PLACE_ADDRESS_ATTR,
          s -> s,
          null,
          true
      );
      final LatLon latLon = XmlUtils.getAttr(
          element,
          PLACE_LATLON_ATTR,
          LatLon::fromString,
          () -> null,
          false
      );
      lifeEvent.setPlace(new Place(address, latLon));
    }
  }

  /**
   * Read persons list from a LifeEvent XML element.
   *
   * @param eventElement LifeEvent element to read from.
   * @param elementName  Name of the element to read.
   * @param persons      List of loaded persons to fetch IDs from.
   * @param consumer     Function that consumes the read persons.
   * @param allowMissing True to allow the element designated by {@code elementName} to be missing;
   *                     false to throw an error if missing.
   * @throws IOException If any error occurs.
   */
  private void extractPersons(
      final @NotNull Element eventElement,
      @NotNull String elementName,
      final @NotNull List<Person> persons,
      @NotNull Consumer<Person> consumer,
      boolean allowMissing
  ) throws IOException {
    final Optional<Element> personsElement = XmlUtils
        .getChildElement(eventElement, elementName, allowMissing);
    if (personsElement.isEmpty())
      return;
    for (final Element actorElement : XmlUtils.getChildrenElements(personsElement.get(), PERSON_TAG)) {
      final int id = XmlUtils.getAttr(
          actorElement,
          PERSON_ID_ATTR,
          Integer::parseInt,
          null,
          false
      );
      try {
        consumer.accept(persons.get(id));
      } catch (final IndexOutOfBoundsException | IllegalArgumentException e) {
        throw new IOException(e);
      }
    }
  }

  // endregion
}
