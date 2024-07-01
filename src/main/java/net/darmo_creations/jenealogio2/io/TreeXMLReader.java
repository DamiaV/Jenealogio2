package net.darmo_creations.jenealogio2.io;

import javafx.scene.image.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.model.datetime.*;
import net.darmo_creations.jenealogio2.model.datetime.calendar.Calendar;
import net.darmo_creations.jenealogio2.model.datetime.calendar.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;
import org.w3c.dom.*;

import java.io.*;
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
    final String name = XmlUtils.getAttr(familyTreeElement, FAMILY_TREE_NAME_ATTR, s -> s, null, true);
    final int rootID = XmlUtils.getAttr(familyTreeElement, FAMILY_TREE_ROOT_ATTR, Integer::parseInt, null, false);

    //noinspection OptionalGetWithoutIsPresent
    final Element peopleElement = XmlUtils.getChildElement(familyTreeElement, PEOPLE_TAG, false).get();
    final FamilyTree familyTree = new FamilyTree(name);

    final Optional<Element> registriesElement = XmlUtils.getChildElement(familyTreeElement, REGISTRIES_TAG, true);
    if (registriesElement.isPresent())
      this.loadUserRegistries(registriesElement.get(), familyTree);

    final Optional<Element> documentsElement = XmlUtils.getChildElement(familyTreeElement, DOCUMENTS_TAG, true);
    if (documentsElement.isPresent())
      this.loadDocuments(documentsElement.get(), familyTree, documentBuilder);

    final List<Person> persons = this.readPersons(peopleElement, familyTree);
    try {
      familyTree.setRoot(persons.get(rootID));
    } catch (final IndexOutOfBoundsException e) {
      throw new IOException(e);
    }
    final Optional<Element> eventsElement = XmlUtils.getChildElement(familyTreeElement, LIFE_EVENTS_TAG, true);
    if (eventsElement.isPresent())
      this.readLifeEvents(eventsElement.get(), persons, familyTree);

    return familyTree;
  }

  /**
   * Read registries from a {@code .reg} file.
   *
   * @param file File to load.
   * @return The corresponding registries.
   * @throws IOException If any error occurs.
   */
  public RegistriesWrapper loadRegistriesFile(final @NotNull Path file) throws IOException {
    final FamilyTree dummyTree = new FamilyTree("dummy");
    final Document document = XmlUtils.readFile(new FileInputStream(file.toFile()));
    final NodeList childNodes = document.getChildNodes();
    final Element registriesElement = this.getRootElement(childNodes, REGISTRIES_TAG, REGISTRIES_VERSION_ATTR);
    this.loadUserRegistries(registriesElement, dummyTree);
    return new RegistriesWrapper(
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
    final int version = XmlUtils.getAttr(familyTreeElement, documentVersionAttr, Integer::parseInt, null, false);
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
    final Optional<Element> gendersElement = XmlUtils.getChildElement(registriesElement, GENDERS_TAG, true);
    if (gendersElement.isPresent())
      for (final Element entryElement : XmlUtils.getChildElements(gendersElement.get(), REGISTRY_ENTRY_TAG)) {
        final RegistryEntryKey key = new RegistryEntryKey(XmlUtils.getAttr(entryElement, REGISTRY_ENTRY_KEY_ATTR, s -> s, null, false));
        final String base64 = XmlUtils.getAttr(entryElement, GENDER_ICON_ATTR, s -> s, null, false);
        final String label = XmlUtils.getAttr(entryElement, REGISTRY_ENTRY_LABEL_ATTR, s -> s, null, true);
        familyTree.genderRegistry().registerEntry(key, label, new GenderRegistry.RegistryArgs(base64ToImage(base64)));
      }

    final Optional<Element> eventTypeElement = XmlUtils.getChildElement(registriesElement, LIFE_EVENT_TYPES_TAG, true);
    if (eventTypeElement.isPresent()) {
      for (final Element entryElement : XmlUtils.getChildElements(eventTypeElement.get(), REGISTRY_ENTRY_TAG)) {
        final RegistryEntryKey key = new RegistryEntryKey(XmlUtils.getAttr(entryElement, REGISTRY_ENTRY_KEY_ATTR, s -> s, null, true));
        final String label = XmlUtils.getAttr(entryElement, REGISTRY_ENTRY_LABEL_ATTR, s -> s, null, true);
        final int groupOrdinal = XmlUtils.getAttr(entryElement, LIFE_EVENT_TYPE_GROUP_ATTR, Integer::parseInt, null, false);
        final LifeEventType.Group group;
        try {
          group = LifeEventType.Group.values()[groupOrdinal];
        } catch (final IndexOutOfBoundsException e) {
          throw new IOException(e);
        }
        final boolean indicatesDeath = XmlUtils.getAttr(entryElement, LIFE_EVENT_TYPE_INDICATES_DEATH_ATTR, Boolean::parseBoolean, null, false);
        final boolean indicatesUnion = XmlUtils.getAttr(entryElement, LIFE_EVENT_TYPE_INDICATES_UNION_ATTR, Boolean::parseBoolean, null, false);
        final int actorsNb = XmlUtils.getAttr(entryElement, LIFE_EVENT_TYPE_ACTORS_NB_ATTR, Integer::parseInt, null, false);
        if (actorsNb > 2)
          throw new IOException("invalid actors number: " + actorsNb);
        final boolean unique = XmlUtils.getAttr(entryElement, LIFE_EVENT_TYPE_UNIQUE_ATTR, Boolean::parseBoolean, null, false);
        final var args = new LifeEventTypeRegistry.RegistryArgs(group, indicatesDeath, indicatesUnion, actorsNb, actorsNb, unique);
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
    final byte[] bytes = Base64.getDecoder().decode(base64);
    final int w = bytes[0];
    final int h = bytes[1];
    final int[] pixels = new int[w * h];
    for (int i = 0; i < pixels.length; i++)
      pixels[i] = bytes[2 + 4 * i] << 24
                  | bytes[2 + 4 * i + 1] << 16
                  | bytes[2 + 4 * i + 2] << 8
                  | bytes[2 + 4 * i + 3];
    final WritableImage image = new WritableImage(w, h);
    image.getPixelWriter().setPixels(0, 0, w, h, PixelFormat.getIntArgbInstance(), pixels, 0, w);
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
      @NotNull AttachedDocumentBuilder documentBuilder
  ) throws IOException {
    final List<Element> documentElements = XmlUtils.getChildElements(documentsElement, DOCUMENT_TAG);
    for (final Element documentElement : documentElements) {
      final String name = XmlUtils.getAttr(documentElement, DOCUMENT_NAME_ATTR, s -> s, () -> null, false);
      final Optional<Element> descElement = XmlUtils.getChildElement(documentElement, DOCUMENT_DESC_TAG, true);
      final String desc = descElement.map(Element::getTextContent).orElse(null);
      final DateTime date = this.readDateTag(documentElement, true);
      familyTree.addDocument(documentBuilder.build(name, desc, date));
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
    final Map<Person, Pair<Integer, Integer>> parentIDS = new HashMap<>();
    final Map<Person, Map<Person.RelativeType, List<Integer>>> relativesIDs = new HashMap<>();

    for (final Element personElement : XmlUtils.getChildElements(peopleElement, PERSON_TAG)) {
      final Person person = new Person();

      this.readDocumentsTag(personElement, person, familyTree);
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
      this.readGenderTag(personElement, person, familyTree);
      this.readMainOccupationTag(personElement, person);
      this.readParentsTag(personElement, person, parentIDS);
      this.readRelativesTag(personElement, person, relativesIDs);
      this.readNotesTag(personElement, person);
      this.readSourcesTag(personElement, person);

      familyTree.addPerson(person);
      persons.add(person);
    }

    this.setParents(persons, parentIDS);
    this.setRelatives(persons, relativesIDs);

    return persons;
  }

  /**
   * Read the {@code <Documents>} tag for the given object.
   *
   * @param element    XML element to extract the documents from.
   * @param o          The object corresponding to the tag.
   * @param familyTree The tree the object belongs to.
   */
  private void readDocumentsTag(
      final @NotNull Element element,
      @NotNull GenealogyObject<?> o,
      @NotNull FamilyTree familyTree
  ) throws IOException {
    final Optional<Element> documentsElement = XmlUtils.getChildElement(element, DOCUMENTS_TAG, true);
    if (documentsElement.isPresent()) {
      final List<Element> documentElements = XmlUtils.getChildElements(documentsElement.get(), DOCUMENT_TAG);
      for (final Element documentElement : documentElements) {
        final String name = XmlUtils.getAttr(documentElement, DOCUMENT_NAME_ATTR, s -> s, () -> null, false);
        familyTree.addDocumentToObject(name, o);
        final boolean isMain = XmlUtils.getAttr(documentElement, DOCUMENT_MAIN_PICTURE_ATTR, Boolean::parseBoolean, () -> false, false);
        if (isMain && familyTree.getDocument(name).map(d -> d instanceof Picture).orElse(false))
          familyTree.setMainPictureOfObject(name, o);
      }
    }
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
    final Optional<Element> disambIDElement = XmlUtils.getChildElement(personElement, DISAMBIGUATION_ID_TAG, true);
    if (disambIDElement.isPresent()) {
      try {
        person.setDisambiguationID(XmlUtils.getAttr(disambIDElement.get(), DISAMBIG_ID_VALUE_ATTR, Integer::parseInt, () -> null, false));
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
    //noinspection OptionalGetWithoutIsPresent
    final Element lifeStatusElement = XmlUtils.getChildElement(personElement, LIFE_STATUS_TAG, false).get();
    try {
      final int ordinal = XmlUtils.getAttr(lifeStatusElement, LIFE_STATUS_ORDINAL_ATTR, Integer::parseInt, null, false);
      person.setLifeStatus(LifeStatus.values()[ordinal]);
    } catch (final IndexOutOfBoundsException e) {
      throw new IOException(e);
    }
  }

  /**
   * Read the {@code <Gender>} tag for the given person.
   *
   * @param personElement {@code <Person>} element to extract the gender from.
   * @param person        The person corresponding to the {@code <Person>} tag.
   * @param familyTree    The tree to get the {@link Gender} object from.
   */
  private void readGenderTag(
      final @NotNull Element personElement,
      @NotNull Person person,
      final @NotNull FamilyTree familyTree
  ) throws IOException {
    final Optional<Element> genderElement = XmlUtils.getChildElement(personElement, GENDER_TAG, true);
    if (genderElement.isPresent()) {
      try {
        final RegistryEntryKey key = new RegistryEntryKey(XmlUtils.getAttr(genderElement.get(), GENDER_KEY_ATTR, s -> s, null, false));
        final Gender gender = familyTree.genderRegistry().getEntry(key);
        if (gender == null)
          throw new IOException("Undefined gender registry key: " + key.fullName());
        person.setGender(gender);
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
    final Optional<Element> occupationElement = XmlUtils.getChildElement(personElement, MAIN_OCCUPATION_TAG, true);
    if (occupationElement.isPresent()) {
      final String occupation = XmlUtils.getAttr(occupationElement.get(), MAIN_OCCUPATION_VALUE_ATTR, s -> s, null, true);
      person.setMainOccupation(occupation);
    }
  }

  /**
   * Read the {@code <Parents>} tag for the given person and update the given map.
   *
   * @param personElement {@code <Person>} element to extract parents from.
   * @param person        The person corresponding to the {@code <Person>} tag.
   * @param parentIDS     Map into which to put all the parents of the given person.
   */
  private void readParentsTag(
      final @NotNull Element personElement,
      final @NotNull Person person,
      @NotNull Map<Person, Pair<Integer, Integer>> parentIDS
  ) throws IOException {
    final Optional<Element> parentsElement = XmlUtils.getChildElement(personElement, PARENTS_TAG, true);
    if (parentsElement.isPresent()) {
      final Integer id1 = XmlUtils.getAttr(parentsElement.get(), PARENT_ID_1_ATTR, Integer::parseInt, () -> null, false);
      final Integer id2 = XmlUtils.getAttr(parentsElement.get(), PARENT_ID_2_ATTR, Integer::parseInt, () -> null, false);
      if (id1 != null && id2 != null && id1.intValue() == id2.intValue())
        throw new IOException("Parents cannot be identical");
      // Defer setting parents to when all person objects have been deserialized
      parentIDS.put(person, new Pair<>(id1, id2));
    }
  }

  /**
   * Read the {@code <Relatives>} tag for the given person and update the given map.
   *
   * @param personElement {@code <Person>} element to extract relatives from.
   * @param person        The person corresponding to the {@code <Person>} tag.
   * @param relativesIDs  Map into which to put all the relatives of the given person.
   */
  private void readRelativesTag(
      final @NotNull Element personElement,
      final @NotNull Person person,
      @NotNull Map<Person, Map<Person.RelativeType, List<Integer>>> relativesIDs
  ) throws IOException {
    final Optional<Element> relativesElement = XmlUtils.getChildElement(personElement, RELATIVES_TAG, true);
    if (relativesElement.isPresent()) {
      final HashMap<Person.RelativeType, List<Integer>> groupsMap = new HashMap<>();
      relativesIDs.put(person, groupsMap);
      for (final Element groupElement : XmlUtils.getChildElements(relativesElement.get(), GROUP_TAG)) {
        final int ordinal = XmlUtils.getAttr(groupElement, GROUP_ORDINAL_ATTR, Integer::parseInt, null, false);
        final Person.RelativeType relativeType;
        try {
          relativeType = Person.RelativeType.values()[ordinal];
        } catch (final IndexOutOfBoundsException e) {
          throw new IOException(e);
        }
        final LinkedList<Integer> relativesList = new LinkedList<>();
        groupsMap.put(relativeType, relativesList);
        for (final Element relativeElement : XmlUtils.getChildElements(groupElement, RELATIVE_TAG)) {
          // Defer setting relatives to when all person objects have been deserialized
          relativesList.add(XmlUtils.getAttr(relativeElement, RELATIVE_ID_ATTR, Integer::parseInt, null, false));
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
    final Optional<Element> notesElement = XmlUtils.getChildElement(element, NOTES_TAG, true);
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
    final Optional<Element> sourcesElement = XmlUtils.getChildElement(element, SOURCES_TAG, true);
    sourcesElement.ifPresent(e -> o.setSources(e.getTextContent().strip()));
  }

  private void setParents(
      final @NotNull List<Person> persons,
      final @NotNull Map<Person, Pair<Integer, Integer>> parentIDS
  ) throws IOException {
    for (final var entry : parentIDS.entrySet()) {
      final Person person = entry.getKey();
      final Integer id1 = entry.getValue().left();
      final Integer id2 = entry.getValue().right();
      if (id1 != null)
        try {
          person.setParent(0, persons.get(id1));
        } catch (final IndexOutOfBoundsException e) {
          throw new IOException(e);
        }
      if (id2 != null)
        try {
          person.setParent(1, persons.get(id2));
        } catch (final IndexOutOfBoundsException e) {
          throw new IOException(e);
        }
    }
  }

  private void setRelatives(
      final @NotNull List<Person> persons,
      final @NotNull Map<Person, Map<Person.RelativeType, List<Integer>>> relativesIDs
  ) throws IOException {
    for (final var entry : relativesIDs.entrySet()) {
      final Person person = entry.getKey();
      for (final var group : entry.getValue().entrySet()) {
        final Person.RelativeType type = group.getKey();
        for (final int personID : group.getValue())
          try {
            person.addRelative(persons.get(personID), type);
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
    final Optional<Element> nameElement = XmlUtils.getChildElement(personElement, elementName, true);
    if (nameElement.isPresent())
      try {
        consumer.accept(XmlUtils.getAttr(nameElement.get(), NAME_VALUE_ATTR, s -> s, null, true));
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
    final Optional<Element> namesElement = XmlUtils.getChildElement(personElement, elementName, true);
    if (namesElement.isPresent()) {
      final List<String> names = new LinkedList<>();
      for (final Element nameElement : XmlUtils.getChildElements(namesElement.get(), NAME_TAG))
        try {
          names.add(XmlUtils.getAttr(nameElement, NAME_VALUE_ATTR, s -> s, null, true));
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
   * @throws IOException If any error occurs.
   */
  private void readLifeEvents(
      final @NotNull Element eventsElement,
      final @NotNull List<Person> persons,
      @NotNull FamilyTree familyTree
  ) throws IOException {
    for (final Element eventElement : XmlUtils.getChildElements(eventsElement, LIFE_EVENT_TAG)) {
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

      this.readDocumentsTag(eventElement, lifeEvent, familyTree);
      // Witnesses
      this.extractPersons(eventElement, WITNESSES_TAG, persons,
          p -> familyTree.addWitnessToLifeEvent(lifeEvent, p), true);
      this.readPlaceTag(eventElement, lifeEvent);
      this.readNotesTag(eventElement, lifeEvent);
      this.readSourcesTag(eventElement, lifeEvent);
    }
  }

  /**
   * Read the {@code <Date>} tag for the given event.
   *
   * @param eventElement {@code <LifeEvent>} element to extract the date from.
   * @return The date of the event.
   * @throws IOException If the subtree is malformed or the date type is undefined.
   */
  private @Nullable DateTime readDateTag(final @NotNull Element eventElement, boolean allowMissing) throws IOException {
    final Optional<Element> childElement = XmlUtils.getChildElement(eventElement, DATE_TAG, allowMissing);
    if (childElement.isEmpty()) {
      if (allowMissing)
        return null;
      throw new IOException("Missing tag: " + DATE_TAG);
    }
    final Element dateElement = childElement.get();
    final String dateType = XmlUtils.getAttr(dateElement, DATE_TYPE_ATTR, s -> s, null, false);
    return switch (dateType) {
      case DATE_WITH_PRECISION -> {
        final int ordinal = XmlUtils.getAttr(dateElement, DATE_PRECISION_ATTR, Integer::parseInt, null, false);
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
          final var date = XmlUtils.getAttr(dateElement, "date" + (i + 1), this::deserializeDate, () -> null, false);
          if (date != null)
            dates.add(date);
        }
        try {
          yield new DateTimeAlternative(dates);
        } catch (final IllegalArgumentException | NullPointerException e) {
          throw new IOException(e);
        }
      }
      default -> throw new IOException("Undefined date type " + dateType);
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
   * @param eventElement {@code <LifeEvent>} element to extract the type from.
   * @param familyTree   The family tree to get {@link LifeEventType} objects from.
   * @return The type of the event.
   * @throws IOException If the event type is undefined or malformed.
   */
  private LifeEventType readLifeEventTypeTag(
      final @NotNull Element eventElement,
      final @NotNull FamilyTree familyTree
  ) throws IOException {
    final LifeEventType type;
    //noinspection OptionalGetWithoutIsPresent
    final Element typeElement = XmlUtils.getChildElement(eventElement, TYPE_TAG, false).get();
    try {
      final RegistryEntryKey key = new RegistryEntryKey(XmlUtils.getAttr(typeElement, TYPE_KEY_ATTR, s -> s, null, false));
      type = familyTree.lifeEventTypeRegistry().getEntry(key);
      if (type == null)
        throw new IOException("Undefined life event type registry key: " + key.fullName());
    } catch (final IllegalArgumentException e) {
      throw new IOException(e);
    }
    return type;
  }

  /**
   * Read the {@code <Actors>} tag for the given event.
   *
   * @param eventElement {@code <LifeEvent>} element to extract actors from.
   * @param type         The type of the event.
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
      throw new IOException("Wrong number of actors for event type '%s': %d"
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
    final Optional<Element> placeElement = XmlUtils.getChildElement(eventElement, PLACE_TAG, true);
    if (placeElement.isPresent()) {
      final Element element = placeElement.get();
      final String address = XmlUtils.getAttr(element, PLACE_ADDRESS_ATTR, s -> s, null, true);
      final LatLon latLon = XmlUtils.getAttr(element, PLACE_LATLON_ATTR, LatLon::fromString, () -> null, false);
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
    final Optional<Element> personsElement = XmlUtils.getChildElement(eventElement, elementName, allowMissing);
    if (personsElement.isEmpty())
      return;
    for (final Element actorElement : XmlUtils.getChildElements(personsElement.get(), PERSON_TAG)) {
      final int id = XmlUtils.getAttr(actorElement, PERSON_ID_ATTR, Integer::parseInt, null, false);
      try {
        consumer.accept(persons.get(id));
      } catch (final IndexOutOfBoundsException | IllegalArgumentException e) {
        throw new IOException(e);
      }
    }
  }

  // endregion
}
