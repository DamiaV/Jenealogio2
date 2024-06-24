package net.darmo_creations.jenealogio2.io;

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
   * @param inputStream    The stream to read from.
   * @param pictureBuilder Function that provides a picture for the given name and data.
   * @return The corresponding family tree object.
   * @throws IOException If any error occurs.
   */
  public FamilyTree readFromStream(
      @NotNull InputStream inputStream,
      @NotNull PictureBuilder pictureBuilder
  ) throws IOException {
    Document document = XmlUtils.readFile(inputStream);

    NodeList childNodes = document.getChildNodes();
    if (childNodes.getLength() != 1) {
      throw new IOException("Parse error");
    }
    Element familyTreeElement = (Element) childNodes.item(0);
    if (!familyTreeElement.getTagName().equals(FAMILY_TREE_TAG)) {
      throw new IOException("Missing root element");
    }
    int version = XmlUtils.getAttr(familyTreeElement, FAMILY_TREE_VERSION_ATTR, Integer::parseInt, null, false);
    if (version != 1) {
      throw new IOException("Unsupported XML file version: " + version);
    }
    String name = XmlUtils.getAttr(familyTreeElement, FAMILY_TREE_NAME_ATTR, s -> s, null, true);
    int rootID = XmlUtils.getAttr(familyTreeElement, FAMILY_TREE_ROOT_ATTR, Integer::parseInt, null, false);

    //noinspection OptionalGetWithoutIsPresent
    Element peopleElement = XmlUtils.getChildElement(familyTreeElement, PEOPLE_TAG, false).get();
    FamilyTree familyTree = new FamilyTree(name);

    Optional<Element> registriesElement = XmlUtils.getChildElement(familyTreeElement, REGISTRIES_TAG, true);
    if (registriesElement.isPresent()) {
      this.loadUserRegistries(registriesElement.get(), familyTree);
    }

    Optional<Element> picturesElement = XmlUtils.getChildElement(familyTreeElement, PICTURES_TAG, true);
    if (picturesElement.isPresent()) {
      this.loadPictures(picturesElement.get(), familyTree, pictureBuilder);
    }

    List<Person> persons = this.readPersons(peopleElement, familyTree);
    try {
      familyTree.setRoot(persons.get(rootID));
    } catch (IndexOutOfBoundsException e) {
      throw new IOException(e);
    }
    Optional<Element> eventsElement = XmlUtils.getChildElement(familyTreeElement, LIFE_EVENTS_TAG, true);
    if (eventsElement.isPresent()) {
      this.readLifeEvents(eventsElement.get(), persons, familyTree);
    }

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
    FamilyTree dummyTree = new FamilyTree("dummy");
    Document document = XmlUtils.readFile(new FileInputStream(file.toFile()));
    NodeList childNodes = document.getChildNodes();
    if (childNodes.getLength() != 1) {
      throw new IOException("Parse error");
    }
    Element registriesElement = (Element) childNodes.item(0);
    if (!registriesElement.getTagName().equals(REGISTRIES_TAG)) {
      throw new IOException("Missing root element");
    }
    int version = XmlUtils.getAttr(registriesElement, REGISTRIES_VERSION_ATTR, Integer::parseInt, null, false);
    if (version != 1) {
      throw new IOException("Unsupported XML file version: " + version);
    }
    this.loadUserRegistries(registriesElement, dummyTree);
    return new RegistriesWrapper(
        dummyTree.lifeEventTypeRegistry().serializableEntries(),
        dummyTree.genderRegistry().serializableEntries()
    );
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
    Optional<Element> gendersElement = XmlUtils.getChildElement(registriesElement, GENDERS_TAG, true);
    if (gendersElement.isPresent()) {
      for (Element entryElement : XmlUtils.getChildElements(gendersElement.get(), REGISTRY_ENTRY_TAG)) {
        RegistryEntryKey key = new RegistryEntryKey(XmlUtils.getAttr(entryElement, REGISTRY_ENTRY_KEY_ATTR, s -> s, null, false));
        String color = XmlUtils.getAttr(entryElement, GENDER_COLOR_ATTR, s -> s, null, false);
        if (!color.matches("^#[\\da-fA-F]{6}$")) {
          throw new IOException("Invalid color code: " + color);
        }
        if (key.isBuiltin()) {
          if (!familyTree.genderRegistry().containsKey(key)) {
            throw new IOException("Undefined GENDERS registry key: " + key.fullName());
          }
          familyTree.genderRegistry().getEntry(key).setColor(color);
        } else {
          String label = XmlUtils.getAttr(entryElement, REGISTRY_ENTRY_LABEL_ATTR, s -> s, null, true);
          familyTree.genderRegistry().registerEntry(key, label, new GenderRegistry.RegistryArgs(color));
        }
      }
    }

    Optional<Element> eventTypeElement = XmlUtils.getChildElement(registriesElement, LIFE_EVENT_TYPES_TAG, true);
    if (eventTypeElement.isPresent()) {
      for (Element entryElement : XmlUtils.getChildElements(eventTypeElement.get(), REGISTRY_ENTRY_TAG)) {
        RegistryEntryKey key = new RegistryEntryKey(XmlUtils.getAttr(entryElement, REGISTRY_ENTRY_KEY_ATTR, s -> s, null, true));
        String label = XmlUtils.getAttr(entryElement, REGISTRY_ENTRY_LABEL_ATTR, s -> s, null, true);
        int groupOrdinal = XmlUtils.getAttr(entryElement, LIFE_EVENT_TYPE_GROUP_ATTR, Integer::parseInt, null, false);
        LifeEventType.Group group;
        try {
          group = LifeEventType.Group.values()[groupOrdinal];
        } catch (IndexOutOfBoundsException e) {
          throw new IOException(e);
        }
        boolean indicatesDeath = XmlUtils.getAttr(entryElement, LIFE_EVENT_TYPE_INDICATES_DEATH_ATTR, Boolean::parseBoolean, null, false);
        boolean indicatesUnion = XmlUtils.getAttr(entryElement, LIFE_EVENT_TYPE_INDICATES_UNION_ATTR, Boolean::parseBoolean, null, false);
        int actorsNb = XmlUtils.getAttr(entryElement, LIFE_EVENT_TYPE_ACTORS_NB_ATTR, Integer::parseInt, null, false);
        if (actorsNb > 2) {
          throw new IOException("invalid actors number: " + actorsNb);
        }
        boolean unique = XmlUtils.getAttr(entryElement, LIFE_EVENT_TYPE_UNIQUE_ATTR, Boolean::parseBoolean, null, false);
        var args = new LifeEventTypeRegistry.RegistryArgs(group, indicatesDeath, indicatesUnion, actorsNb, actorsNb, unique);
        try {
          familyTree.lifeEventTypeRegistry().registerEntry(key, label, args);
        } catch (IllegalArgumentException e) {
          throw new IOException(e);
        }
      }
    }
  }

  // endregion
  // region Pictures

  /**
   * Load the pictures from the {@code <Pictures>} tag.
   *
   * @param picturesElement XML element containing the pictures’ definitions.
   * @param familyTree      The family tree to load pictures into.
   * @param pictureBuilder  Function that provides a picture for the given name.
   * @throws IOException In any error occurs.
   */
  private void loadPictures(
      @NotNull Element picturesElement,
      @NotNull FamilyTree familyTree,
      @NotNull PictureBuilder pictureBuilder
  ) throws IOException {
    List<Element> pictureElements = XmlUtils.getChildElements(picturesElement, PICTURE_TAG);
    for (Element pictureElement : pictureElements) {
      String name = XmlUtils.getAttr(pictureElement, PICTURE_NAME_ATTR, s -> s, () -> null, false);
      Optional<Element> descElement = XmlUtils.getChildElement(pictureElement, PICTURE_DESC_TAG, true);
      String desc = descElement.map(Element::getTextContent).orElse(null);
      DateTime date = this.readDateTag(pictureElement, true);
      familyTree.addDocument(pictureBuilder.build(name, desc, date));
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
    List<Person> persons = new LinkedList<>();
    Map<Person, Pair<Integer, Integer>> parentIDS = new HashMap<>();
    Map<Person, Map<Person.RelativeType, List<Integer>>> relativesIDs = new HashMap<>();

    for (Element personElement : XmlUtils.getChildElements(peopleElement, PERSON_TAG)) {
      Person person = new Person();

      this.readPicturesTag(personElement, person, familyTree);
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
   * Read the {@code <Pictures>} tag for the given object.
   *
   * @param element    XML element to extract the pictures from.
   * @param o          The object corresponding to the tag.
   * @param familyTree The tree the object belongs to.
   */
  private void readPicturesTag(
      final @NotNull Element element,
      @NotNull GenealogyObject<?> o,
      @NotNull FamilyTree familyTree
  ) throws IOException {
    Optional<Element> picturesElement = XmlUtils.getChildElement(element, PICTURES_TAG, true);
    if (picturesElement.isPresent()) {
      List<Element> pictureElements = XmlUtils.getChildElements(picturesElement.get(), PICTURE_TAG);
      for (Element pictureElement : pictureElements) {
        String name = XmlUtils.getAttr(pictureElement, PICTURE_NAME_ATTR, s -> s, () -> null, false);
        familyTree.addDocumentToObject(name, o);
        boolean isMain = XmlUtils.getAttr(pictureElement, PICTURE_MAIN_ATTR, Boolean::parseBoolean, () -> false, false);
        if (isMain) {
          familyTree.setMainPictureOfObject(name, o);
        }
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
    Optional<Element> disambIDElement = XmlUtils.getChildElement(personElement, DISAMBIGUATION_ID_TAG, true);
    if (disambIDElement.isPresent()) {
      try {
        person.setDisambiguationID(XmlUtils.getAttr(disambIDElement.get(), DISAMBIG_ID_VALUE_ATTR, Integer::parseInt, () -> null, false));
      } catch (IllegalArgumentException e) {
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
    Element lifeStatusElement = XmlUtils.getChildElement(personElement, LIFE_STATUS_TAG, false).get();
    try {
      int ordinal = XmlUtils.getAttr(lifeStatusElement, LIFE_STATUS_ORDINAL_ATTR, Integer::parseInt, null, false);
      person.setLifeStatus(LifeStatus.values()[ordinal]);
    } catch (IndexOutOfBoundsException e) {
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
    Optional<Element> genderElement = XmlUtils.getChildElement(personElement, GENDER_TAG, true);
    if (genderElement.isPresent()) {
      try {
        RegistryEntryKey key = new RegistryEntryKey(XmlUtils.getAttr(genderElement.get(), GENDER_KEY_ATTR, s -> s, null, false));
        Gender gender = familyTree.genderRegistry().getEntry(key);
        if (gender == null) {
          throw new IOException("Undefined gender registry key: " + key.fullName());
        }
        person.setGender(gender);
      } catch (IllegalArgumentException e) {
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
    Optional<Element> occupationElement = XmlUtils.getChildElement(personElement, MAIN_OCCUPATION_TAG, true);
    if (occupationElement.isPresent()) {
      String occupation = XmlUtils.getAttr(occupationElement.get(), MAIN_OCCUPATION_VALUE_ATTR, s -> s, null, true);
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
    Optional<Element> parentsElement = XmlUtils.getChildElement(personElement, PARENTS_TAG, true);
    if (parentsElement.isPresent()) {
      Integer id1 = XmlUtils.getAttr(parentsElement.get(), PARENT_ID_1_ATTR, Integer::parseInt, () -> null, false);
      Integer id2 = XmlUtils.getAttr(parentsElement.get(), PARENT_ID_2_ATTR, Integer::parseInt, () -> null, false);
      if (id1 != null && id2 != null && id1.intValue() == id2.intValue()) {
        throw new IOException("Parents cannot be identical");
      }
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
    Optional<Element> relativesElement = XmlUtils.getChildElement(personElement, RELATIVES_TAG, true);
    if (relativesElement.isPresent()) {
      HashMap<Person.RelativeType, List<Integer>> groupsMap = new HashMap<>();
      relativesIDs.put(person, groupsMap);
      for (Element groupElement : XmlUtils.getChildElements(relativesElement.get(), GROUP_TAG)) {
        int ordinal = XmlUtils.getAttr(groupElement, GROUP_ORDINAL_ATTR, Integer::parseInt, null, false);
        Person.RelativeType relativeType;
        try {
          relativeType = Person.RelativeType.values()[ordinal];
        } catch (IndexOutOfBoundsException e) {
          throw new IOException(e);
        }
        LinkedList<Integer> relativesList = new LinkedList<>();
        groupsMap.put(relativeType, relativesList);
        for (Element relativeElement : XmlUtils.getChildElements(groupElement, RELATIVE_TAG)) {
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
    Optional<Element> notesElement = XmlUtils.getChildElement(element, NOTES_TAG, true);
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
    Optional<Element> sourcesElement = XmlUtils.getChildElement(element, SOURCES_TAG, true);
    sourcesElement.ifPresent(e -> o.setSources(e.getTextContent().strip()));
  }

  private void setParents(
      final @NotNull List<Person> persons,
      final @NotNull Map<Person, Pair<Integer, Integer>> parentIDS
  ) throws IOException {
    for (var entry : parentIDS.entrySet()) {
      Person person = entry.getKey();
      Integer id1 = entry.getValue().left();
      Integer id2 = entry.getValue().right();
      if (id1 != null) {
        try {
          person.setParent(0, persons.get(id1));
        } catch (IndexOutOfBoundsException e) {
          throw new IOException(e);
        }
      }
      if (id2 != null) {
        try {
          person.setParent(1, persons.get(id2));
        } catch (IndexOutOfBoundsException e) {
          throw new IOException(e);
        }
      }
    }
  }

  private void setRelatives(
      final @NotNull List<Person> persons,
      final @NotNull Map<Person, Map<Person.RelativeType, List<Integer>>> relativesIDs
  ) throws IOException {
    for (var entry : relativesIDs.entrySet()) {
      Person person = entry.getKey();
      for (var group : entry.getValue().entrySet()) {
        Person.RelativeType type = group.getKey();
        for (int personID : group.getValue()) {
          try {
            person.addRelative(persons.get(personID), type);
          } catch (IndexOutOfBoundsException e) {
            throw new IOException(e);
          }
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
    Optional<Element> nameElement = XmlUtils.getChildElement(personElement, elementName, true);
    if (nameElement.isPresent()) {
      try {
        consumer.accept(XmlUtils.getAttr(nameElement.get(), NAME_VALUE_ATTR, s -> s, null, true));
      } catch (IllegalArgumentException e) {
        throw new IOException(e);
      }
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
    Optional<Element> namesElement = XmlUtils.getChildElement(personElement, elementName, true);
    if (namesElement.isPresent()) {
      List<String> names = new LinkedList<>();
      for (Element nameElement : XmlUtils.getChildElements(namesElement.get(), NAME_TAG)) {
        try {
          names.add(XmlUtils.getAttr(nameElement, NAME_VALUE_ATTR, s -> s, null, true));
        } catch (IllegalArgumentException e) {
          throw new IOException(e);
        }
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
    for (Element eventElement : XmlUtils.getChildElements(eventsElement, LIFE_EVENT_TAG)) {
      LifeEvent lifeEvent;
      DateTime date = this.readDateTag(eventElement, false);
      LifeEventType type = this.readLifeEventTypeTag(eventElement, familyTree);
      List<Person> actors = this.readActorsTag(eventElement, type, persons);

      try {
        //noinspection DataFlowIssue
        lifeEvent = new LifeEvent(date, type);
        familyTree.setLifeEventActors(lifeEvent, new HashSet<>(actors));
      } catch (IllegalArgumentException e) {
        throw new IOException(e);
      }

      this.readPicturesTag(eventElement, lifeEvent, familyTree);
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
    Optional<Element> childElement = XmlUtils.getChildElement(eventElement, DATE_TAG, allowMissing);
    if (childElement.isEmpty()) {
      if (allowMissing) {
        return null;
      }
      throw new IOException("Missing tag: " + DATE_TAG);
    }
    Element dateElement = childElement.get();
    String dateType = XmlUtils.getAttr(dateElement, DATE_TYPE_ATTR, s -> s, null, false);
    return switch (dateType) {
      case DATE_WITH_PRECISION -> {
        int ordinal = XmlUtils.getAttr(dateElement, DATE_PRECISION_ATTR, Integer::parseInt, null, false);
        DateTimePrecision precision;
        try {
          precision = DateTimePrecision.values()[ordinal];
        } catch (IndexOutOfBoundsException e) {
          throw new IOException(e);
        }
        CalendarSpecificDateTime d = XmlUtils.getAttr(
            dateElement, DATE_DATE_ATTR, this::deserializeDate, null, false);
        yield new DateTimeWithPrecision(d, precision);
      }
      case DATE_RANGE -> {
        CalendarSpecificDateTime startDate = XmlUtils.getAttr(
            dateElement, DATE_START_ATTR, this::deserializeDate, null, false);
        CalendarSpecificDateTime endDate = XmlUtils.getAttr(
            dateElement, DATE_END_ATTR, this::deserializeDate, null, false);
        yield new DateTimeRange(startDate, endDate);
      }
      case DATE_ALTERNATIVE -> {
        CalendarSpecificDateTime earliestDate = XmlUtils.getAttr(
            dateElement, DATE_EARLIEST_ATTR, this::deserializeDate, null, false);
        CalendarSpecificDateTime latestDate = XmlUtils.getAttr(
            dateElement, DATE_LATEST_ATTR, this::deserializeDate, null, false);
        yield new DateTimeAlternative(earliestDate, latestDate);
      }
      default -> throw new IOException("Undefined date type " + dateType);
    };
  }

  private CalendarSpecificDateTime deserializeDate(@NotNull String s) {
    String[] split = s.split(";", 2);
    if (split.length != 2) {
      throw new DateTimeParseException("Invalid date format: " + s, s, 0);
    }
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
    LifeEventType type;
    //noinspection OptionalGetWithoutIsPresent
    Element typeElement = XmlUtils.getChildElement(eventElement, TYPE_TAG, false).get();
    try {
      RegistryEntryKey key = new RegistryEntryKey(XmlUtils.getAttr(typeElement, TYPE_KEY_ATTR, s -> s, null, false));
      type = familyTree.lifeEventTypeRegistry().getEntry(key);
      if (type == null) {
        throw new IOException("Undefined life event type registry key: " + key.fullName());
      }
    } catch (IllegalArgumentException e) {
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
    List<Person> actors = new LinkedList<>();
    this.extractPersons(eventElement, ACTORS_TAG, persons, actors::add, false);
    int actorsNb = actors.size();
    if (actorsNb < type.minActors() || actorsNb > type.maxActors()) {
      throw new IOException("Wrong number of actors for event type '%s': %d"
          .formatted(type.key().fullName(), actorsNb));
    }
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
    Optional<Element> placeElement = XmlUtils.getChildElement(eventElement, PLACE_TAG, true);
    if (placeElement.isPresent()) {
      Element element = placeElement.get();
      String address = XmlUtils.getAttr(element, PLACE_ADDRESS_ATTR, s -> s, null, true);
      LatLon latLon = XmlUtils.getAttr(element, PLACE_LATLON_ATTR, LatLon::fromString, () -> null, false);
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
    Optional<Element> personsElement = XmlUtils.getChildElement(eventElement, elementName, allowMissing);
    if (personsElement.isEmpty()) {
      return;
    }
    for (Element actorElement : XmlUtils.getChildElements(personsElement.get(), PERSON_TAG)) {
      int id = XmlUtils.getAttr(actorElement, PERSON_ID_ATTR, Integer::parseInt, null, false);
      try {
        consumer.accept(persons.get(id));
      } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
        throw new IOException(e);
      }
    }
  }

  // endregion
}
