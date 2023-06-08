package net.darmo_creations.jenealogio2.io;

import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.model.calendar.*;
import net.darmo_creations.jenealogio2.utils.Pair;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Reads .jtree files.
 */
public class TreeFileReader extends TreeFileManager {
  /**
   * Read a family tree object from a .jtree file.
   *
   * @param file The file to load.
   * @return The corresponding family tree object.
   * @throws IOException If any error occurs.
   */
  public FamilyTree loadFile(@NotNull File file) throws IOException {
    Document document = this.readFile(file);

    NodeList childNodes = document.getChildNodes();
    if (childNodes.getLength() != 1) {
      throw new IOException("Parse error");
    }
    Element familyTreeElement = (Element) childNodes.item(0);
    if (!familyTreeElement.getTagName().equals(FAMILY_TREE_TAG)) {
      throw new IOException("Missing root element");
    }
    int version = this.getAttr(familyTreeElement, FAMILY_TREE_VERSION_ATTR, Integer::parseInt, () -> 1, false);
    if (version != 1) {
      throw new IOException("Unsupported XML file version: " + version);
    }
    String name = this.getAttr(familyTreeElement, FAMILY_TREE_NAME_ATTR, s -> s, null, true);
    int rootID = this.getAttr(familyTreeElement, FAMILY_TREE_ROOT_ATTR, Integer::parseInt, null, false);

    Optional<Element> registriesElement = this.getChildElement(familyTreeElement, REGISTRIES_TAG, true);
    if (registriesElement.isPresent()) {
      this.loadUserRegistries(registriesElement.get());
    }

    //noinspection OptionalGetWithoutIsPresent
    Element peopleElement = this.getChildElement(familyTreeElement, PEOPLE_TAG, false).get();
    FamilyTree familyTree = new FamilyTree(name);
    List<Person> persons = this.readPersons(peopleElement, familyTree);
    try {
      familyTree.setRoot(persons.get(rootID));
    } catch (IndexOutOfBoundsException e) {
      throw new IOException(e);
    }
    Optional<Element> eventsElement = this.getChildElement(familyTreeElement, LIFE_EVENTS_TAG, true);
    if (eventsElement.isPresent()) {
      this.readLifeEvents(eventsElement.get(), persons);
    }

    return familyTree;
  }

  /**
   * Read user-defined registry entries.
   *
   * @param registriesElement XML element containing the registries.
   * @throws IOException If any error occurs.
   */
  private void loadUserRegistries(@NotNull Element registriesElement) throws IOException {
    Registries.GENDERS.reset();
    Optional<Element> gendersElement = this.getChildElement(registriesElement, GENDERS_TAG, true);
    if (gendersElement.isPresent()) {
      for (Element entryElement : this.getChildElements(gendersElement.get(), REGISTRY_ENTRY_TAG)) {
        String name = this.getAttr(entryElement, REGISTRY_ENTRY_NAME_ATTR, s -> s, null, true);
        String color = this.getAttr(entryElement, GENDER_COLOR_ATTR, s -> s, null, false);
        if (!color.matches("^#[\\da-fA-F]{6}$")) {
          throw new IOException("Invalid color code: " + color);
        }
        Registries.GENDERS.registerEntry(new RegistryEntryKey(Registry.USER_NS, name), color);
      }
    }

    Registries.LIFE_EVENT_TYPES.reset();
    Optional<Element> eventTypeElement = this.getChildElement(registriesElement, LIFE_EVENT_TYPES_TAG, true);
    if (eventTypeElement.isPresent()) {
      for (Element entryElement : this.getChildElements(eventTypeElement.get(), REGISTRY_ENTRY_TAG)) {
        String name = this.getAttr(entryElement, REGISTRY_ENTRY_NAME_ATTR, s -> s, null, true);
        int groupOrdinal = this.getAttr(entryElement, LIFE_EVENT_TYPE_GROUP_ATTR, Integer::parseInt, null, false);
        LifeEventType.Group group;
        try {
          group = LifeEventType.Group.values()[groupOrdinal];
        } catch (IndexOutOfBoundsException e) {
          throw new IOException(e);
        }
        boolean indicatesDeath = this.getAttr(entryElement, LIFE_EVENT_TYPE_INDICATES_DEATH_ATTR, Boolean::parseBoolean, null, false);
        int minActors = this.getAttr(entryElement, LIFE_EVENT_TYPE_MIN_ACTORS_ATTR, Integer::parseInt, null, false);
        int maxActors = this.getAttr(entryElement, LIFE_EVENT_TYPE_MAX_ACTORS_ATTR, Integer::parseInt, null, false);
        boolean unique = this.getAttr(entryElement, LIFE_EVENT_TYPE_UNIQUE_ATTR, Boolean::parseBoolean, null, false);
        var args = new LifeEventType.RegistryArgs(group, indicatesDeath, minActors, maxActors, unique);
        try {
          Registries.LIFE_EVENT_TYPES.registerEntry(new RegistryEntryKey(Registry.USER_NS, name), args);
        } catch (IllegalArgumentException e) {
          throw new IOException(e);
        }
      }
    }
  }

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

    for (Element personElement : this.getChildElements(peopleElement, PERSON_TAG)) {
      Person person = new Person();

      // TODO load image

      // Disambiguation ID
      Optional<Element> disambIDElement = this.getChildElement(personElement, DISAMBIGUATION_ID_TAG, true);
      if (disambIDElement.isPresent()) {
        try {
          person.setDisambiguationID(this.getAttr(disambIDElement.get(), DISAMBIG_ID_VALUE_ATTR, Integer::parseInt, () -> null, false));
        } catch (IllegalArgumentException e) {
          throw new IOException(e);
        }
      }

      // Life status
      //noinspection OptionalGetWithoutIsPresent
      Element lifeStatusElement = this.getChildElement(personElement, LIFE_STATUS_TAG, false).get();
      try {
        int ordinal = this.getAttr(lifeStatusElement, LIFE_STATUS_ORDINAL_ATTR, Integer::parseInt, null, false);
        person.setLifeStatus(LifeStatus.values()[ordinal]);
      } catch (IndexOutOfBoundsException e) {
        throw new IOException(e);
      }

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

      // Gender
      Optional<Element> genderElement = this.getChildElement(personElement, GENDER_TAG, true);
      if (genderElement.isPresent()) {
        try {
          RegistryEntryKey key = new RegistryEntryKey(this.getAttr(genderElement.get(), GENDER_KEY_ATTR, s -> s, null, false));
          Gender gender = Registries.GENDERS.getEntry(key);
          if (gender == null) {
            throw new IOException("Undefined gender registry key: " + key.fullName());
          }
          person.setGender(gender);
        } catch (IllegalArgumentException e) {
          throw new IOException(e);
        }
      }

      // Parents
      Optional<Element> parentsElement = this.getChildElement(personElement, PARENTS_TAG, true);
      if (parentsElement.isPresent()) {
        Integer id1 = this.getAttr(parentsElement.get(), PARENT_ID_1_ATTR, Integer::parseInt, () -> null, false);
        Integer id2 = this.getAttr(parentsElement.get(), PARENT_ID_2_ATTR, Integer::parseInt, () -> null, false);
        if (id1 != null && id2 != null && id1.intValue() == id2.intValue()) {
          throw new IOException("Parents cannot be identical");
        }
        // Defer setting parents to when all person objects have been deserialized
        parentIDS.put(person, new Pair<>(id1, id2));
      }

      // Relatives
      Optional<Element> relativesElement = this.getChildElement(personElement, RELATIVES_TAG, true);
      if (relativesElement.isPresent()) {
        HashMap<Person.RelativeType, List<Integer>> groupsMap = new HashMap<>();
        relativesIDs.put(person, groupsMap);
        for (Element groupElement : this.getChildElements(relativesElement.get(), GROUP_TAG)) {
          int ordinal = this.getAttr(groupElement, GROUP_ORDINAL_ATTR, Integer::parseInt, null, false);
          Person.RelativeType relativeType;
          try {
            relativeType = Person.RelativeType.values()[ordinal];
          } catch (IndexOutOfBoundsException e) {
            throw new IOException(e);
          }
          LinkedList<Integer> relativesList = new LinkedList<>();
          groupsMap.put(relativeType, relativesList);
          for (Element relativeElement : this.getChildElements(groupElement, RELATIVE_TAG)) {
            // Defer setting relatives to when all person objects have been deserialized
            relativesList.add(this.getAttr(relativeElement, RELATIVE_ID_ATTR, Integer::parseInt, null, false));
          }
        }
      }

      // Notes
      Optional<Element> notesElement = this.getChildElement(personElement, NOTES_TAG, true);
      notesElement.ifPresent(element -> person.setNotes(element.getTextContent().strip()));

      // Sources
      Optional<Element> sourcesElement = this.getChildElement(personElement, SOURCES_TAG, true);
      sourcesElement.ifPresent(element -> person.setSources(element.getTextContent().strip()));

      familyTree.addPerson(person);
      persons.add(person);
    }

    // Set parents once all person objects have been deserialized
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

    // Set relatives once all person objects have been deserialized
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

    return persons;
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
    Optional<Element> nameElement = this.getChildElement(personElement, elementName, true);
    if (nameElement.isPresent()) {
      try {
        consumer.accept(this.getAttr(nameElement.get(), NAME_VALUE_ATTR, s -> s, null, true));
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
    Optional<Element> namesElement = this.getChildElement(personElement, elementName, true);
    if (namesElement.isPresent()) {
      List<String> names = new LinkedList<>();
      for (Element nameElement : this.getChildElements(namesElement.get(), NAME_TAG)) {
        try {
          names.add(this.getAttr(nameElement, NAME_VALUE_ATTR, s -> s, null, true));
        } catch (IllegalArgumentException e) {
          throw new IOException(e);
        }
      }
      consumer.accept(names);
    }
  }

  /**
   * Read all LifeEvent XML elements.
   *
   * @param eventsElement XML element to read from.
   * @param persons       List of loaded persons to fetch IDs from.
   * @throws IOException If any error occurs.
   */
  private void readLifeEvents(
      final @NotNull Element eventsElement,
      final @NotNull List<Person> persons
  ) throws IOException {
    for (Element eventElement : this.getChildElements(eventsElement, LIFE_EVENT_TAG)) {
      // Date
      //noinspection OptionalGetWithoutIsPresent
      Element dateElement = this.getChildElement(eventElement, DATE_TAG, false).get();
      String dateType = this.getAttr(dateElement, DATE_TYPE_ATTR, s -> s, null, false);
      CalendarDate date = switch (dateType) {
        case DATE_WITH_PRECISION -> {
          int ordinal = this.getAttr(dateElement, DATE_PRECISION_ATTR, Integer::parseInt, null, false);
          DatePrecision precision;
          try {
            precision = DatePrecision.values()[ordinal];
          } catch (IndexOutOfBoundsException e) {
            throw new IOException(e);
          }
          LocalDateTime d = this.getAttr(dateElement, DATE_DATE_ATTR, LocalDateTime::parse, null, false);
          yield new DateWithPrecision(d, precision);
        }
        case DATE_RANGE -> {
          LocalDateTime startDate = this.getAttr(dateElement, DATE_START_ATTR, LocalDateTime::parse, null, false);
          LocalDateTime endDate = this.getAttr(dateElement, DATE_END_ATTR, LocalDateTime::parse, null, false);
          yield new DateRange(startDate, endDate);
        }
        case DATE_ALTERNATIVE -> {
          LocalDateTime earliestDate = this.getAttr(dateElement, DATE_EARLIEST_ATTR, LocalDateTime::parse, null, false);
          LocalDateTime latestDate = this.getAttr(dateElement, DATE_LATEST_ATTR, LocalDateTime::parse, null, false);
          yield new DateAlternative(earliestDate, latestDate);
        }
        default -> throw new IOException("Undefined date type " + dateType);
      };

      // Type
      LifeEventType type;
      //noinspection OptionalGetWithoutIsPresent
      Element typeElement = this.getChildElement(eventElement, TYPE_TAG, false).get();
      try {
        RegistryEntryKey key = new RegistryEntryKey(this.getAttr(typeElement, TYPE_KEY_ATTR, s -> s, null, false));
        type = Registries.LIFE_EVENT_TYPES.getEntry(key);
        if (type == null) {
          throw new IOException("Undefined life event type registry key: " + key.fullName());
        }
      } catch (IllegalArgumentException e) {
        throw new IOException(e);
      }

      List<Person> actors = new LinkedList<>();

      this.readPersons(eventElement, ACTORS_TAG, persons, actors::add, false);
      int actorsNb = actors.size();
      if (actorsNb < type.minActors() || actorsNb > type.maxActors()) {
        throw new IOException("Wrong number of minActors for event type %s: %d".formatted(type.key().fullName(), actorsNb));
      }

      LifeEvent lifeEvent = new LifeEvent(actors.get(0), date, type);

      for (Person actor : actors) {
        try {
          if (!lifeEvent.hasActor(actor)) { // First actor has already been added above
            lifeEvent.addActor(actor);
          }
          actor.addLifeEvent(lifeEvent);
          if (lifeEvent.type().indicatesDeath()) {
            actor.setLifeStatus(LifeStatus.DECEASED);
          }
        } catch (IllegalArgumentException e) {
          throw new IOException(e);
        }
      }

      // Witnesses
      this.readPersons(eventElement, WITNESSES_TAG, persons, lifeEvent::addWitness, true);

      // Place
      Optional<Element> placeElement = this.getChildElement(eventElement, PLACE_TAG, true);
      placeElement.ifPresent(element -> lifeEvent.setPlace(element.getTextContent().strip()));
      if (placeElement.isPresent()) {
        lifeEvent.setPlace(this.getAttr(placeElement.get(), PLACE_VALUE_ATTR, s -> s, null, true));
      }

      // Notes
      Optional<Element> notesElement = this.getChildElement(eventElement, NOTES_TAG, true);
      notesElement.ifPresent(element -> lifeEvent.setNotes(element.getTextContent().strip()));

      // Sources
      Optional<Element> sourcesElement = this.getChildElement(eventElement, SOURCES_TAG, true);
      sourcesElement.ifPresent(element -> lifeEvent.setSources(element.getTextContent().strip()));
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
  private void readPersons(
      final @NotNull Element eventElement,
      @NotNull String elementName,
      final @NotNull List<Person> persons,
      @NotNull Consumer<Person> consumer,
      boolean allowMissing
  ) throws IOException {
    Optional<Element> personsElement = this.getChildElement(eventElement, elementName, allowMissing);
    if (personsElement.isEmpty()) {
      return;
    }
    for (Element actorElement : this.getChildElements(personsElement.get(), PERSON_TAG)) {
      int id = this.getAttr(actorElement, PERSON_ID_ATTR, Integer::parseInt, null, false);
      try {
        consumer.accept(persons.get(id));
      } catch (IndexOutOfBoundsException e) {
        throw new IOException(e);
      }
    }
  }

  /**
   * Read a child element of an XML element.
   *
   * @param element      XML element to read from.
   * @param name         Name of the element to read.
   * @param allowMissing True to allow the element designated by {@code name} to be missing;
   *                     false to throw an error if missing.
   * @return The read element.
   * @throws IOException If any error occurs.
   */
  private Optional<Element> getChildElement(
      final @NotNull Element element,
      @NotNull String name,
      boolean allowMissing
  ) throws IOException {
    NodeList childNodes = element.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node item = childNodes.item(i);
      if (item.getNodeName().equals(name)) {
        return Optional.of((Element) item);
      }
    }
    if (!allowMissing) {
      throw new IOException("Missing tag %s in tag %s".formatted(name, element.getTagName()));
    }
    return Optional.empty();
  }

  /**
   * Read all child elements with a given name.
   *
   * @param element Element to read from.
   * @param name    Name of child elements.
   * @return List of read elements.
   */
  private List<Element> getChildElements(final @NotNull Element element, @NotNull String name) {
    List<Element> elements = new LinkedList<>();
    NodeList childNodes = element.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node item = childNodes.item(i);
      if (item.getNodeName().equals(name)) {
        elements.add((Element) item);
      }
    }
    return elements;
  }

  /**
   * Read the value of an element’s attribute.
   *
   * @param element              Element to read from.
   * @param name                 Attribute’s name.
   * @param valueConverter       Function to convert raw value.
   * @param defaultValueSupplier Function that supplies a default value in case attribute is missing.
   *                             If argument is null, an error is thrown if the attribute is missing.
   * @param strip                Whether to strip leading and trailing whitespace before passing
   *                             the raw value to the converter.
   * @param <T>                  Type of the returned value.
   * @return The converted value.
   * @throws IOException If any error occurs.
   */
  private <T> T getAttr(
      final @NotNull Element element,
      @NotNull String name,
      @NotNull Function<String, T> valueConverter,
      Supplier<T> defaultValueSupplier,
      boolean strip
  ) throws IOException {
    String rawValue = element.getAttribute(name);
    if (strip) {
      rawValue = rawValue.strip();
    }
    if (rawValue.isEmpty()) {
      if (defaultValueSupplier == null) {
        throw new IOException("Missing or empty attribute %s on element %s".formatted(name, element.getTagName()));
      }
      return defaultValueSupplier.get();
    }
    try {
      return valueConverter.apply(rawValue);
    } catch (RuntimeException e) {
      throw new IOException(e);
    }
  }

  /**
   * Read a file from disk and return an XML document.
   *
   * @param file File to read.
   * @return The corresponding XML document.
   * @throws IOException If any error occurs.
   */
  private Document readFile(@NotNull File file) throws IOException {
    try {
      return this.newDocumentBuilder().parse(file);
    } catch (SAXException e) {
      throw new IOException(e);
    }
  }
}