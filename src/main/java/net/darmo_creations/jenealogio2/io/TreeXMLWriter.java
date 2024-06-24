package net.darmo_creations.jenealogio2.io;

import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.model.datetime.*;
import net.darmo_creations.jenealogio2.model.datetime.calendar.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;
import org.w3c.dom.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;

/**
 * Serializes {@link FamilyTree} objects to XML data.
 */
public class TreeXMLWriter extends TreeXMLManager {
  // region Public methods

  /**
   * Save a family tree to an output stream.
   *
   * @param familyTree   Family tree object to save.
   * @param outputStream Stream to write to.
   * @param config       The app’s config.
   */
  public void writeToStream(final @NotNull FamilyTree familyTree, @NotNull OutputStream outputStream, final @NotNull Config config) {
    Document document = this.newDocumentBuilder().newDocument();

    Element familyTreeElement = (Element) document.appendChild(document.createElement(FAMILY_TREE_TAG));
    XmlUtils.setAttr(document, familyTreeElement, FAMILY_TREE_VERSION_ATTR, String.valueOf(VERSION));
    XmlUtils.setAttr(document, familyTreeElement, FAMILY_TREE_NAME_ATTR, familyTree.name());

    Map<Person, Integer> personIDs = new HashMap<>();
    List<Person> persons = new LinkedList<>(familyTree.persons());
    for (int i = 0; i < persons.size(); i++) {
      personIDs.put(persons.get(i), i);
    }

    this.writeUserRegistryEntries(document, familyTreeElement, familyTree, null);
    Element peopleElement = (Element) familyTreeElement.appendChild(document.createElement(PEOPLE_TAG));
    this.writePersons(document, familyTreeElement, peopleElement, familyTree, persons, personIDs);
    Element lifeEventsElement = document.createElement(LIFE_EVENTS_TAG);
    this.writeEvents(document, lifeEventsElement, familyTree.lifeEvents(), personIDs);
    if (lifeEventsElement.hasChildNodes()) {
      familyTreeElement.appendChild(lifeEventsElement);
    }

    XmlUtils.writeFile(outputStream, document, config);
  }

  /**
   * Save registries from a {@link FamilyTree} to a {@code .reg} file.
   *
   * @param file       File to write to.
   * @param familyTree Family tree object containing entries to save.
   * @param keep       Lists of {@link RegistryEntryKey} of entries to save.
   * @param config     The app’s config.
   * @throws IOException If any error occurs.
   */
  public void saveRegistriesToFile(
      @NotNull Path file,
      final @NotNull FamilyTree familyTree,
      final @NotNull Pair<List<RegistryEntryKey>, List<RegistryEntryKey>> keep,
      final @NotNull Config config
  ) throws IOException {
    Document document = this.newDocumentBuilder().newDocument();
    Element dummyElement = document.createElement("dummy");
    this.writeUserRegistryEntries(document, dummyElement, familyTree, keep);
    Element registriesElement = (Element) dummyElement.getChildNodes().item(0);
    document.appendChild(registriesElement);
    XmlUtils.setAttr(document, registriesElement, REGISTRIES_VERSION_ATTR, String.valueOf(VERSION));
    try (var outputStream = new FileOutputStream(file.toFile())) {
      XmlUtils.writeFile(outputStream, document, config);
    }
  }

  // endregion
  // region User registries

  /**
   * Write all user-defined registry entries.
   *
   * @param document          Current XML document.
   * @param familyTreeElement XML element to write into.
   * @param familyTree        Tree to get entries from.
   * @param keep              Lists of {@link RegistryEntryKey} of entries to save.
   *                          If null, all eligible entries are kept
   */
  private void writeUserRegistryEntries(
      @NotNull Document document,
      @NotNull Element familyTreeElement,
      final @NotNull FamilyTree familyTree,
      final Pair<List<RegistryEntryKey>, List<RegistryEntryKey>> keep
  ) {
    Element registriesElement = document.createElement(REGISTRIES_TAG);
    List<LifeEventType> userLifeEventTypes = familyTree.lifeEventTypeRegistry().serializableEntries().stream()
        .filter(entry -> keep == null || keep.left().contains(entry.key())).toList();
    List<Gender> userGenders = familyTree.genderRegistry().serializableEntries().stream()
        .filter(entry -> keep == null || keep.right().contains(entry.key())).toList();

    if (!userGenders.isEmpty()) {
      Element gendersElement = document.createElement(GENDERS_TAG);
      userGenders.forEach(gender -> {
        Element entryElement = (Element) gendersElement.appendChild(document.createElement(REGISTRY_ENTRY_TAG));
        XmlUtils.setAttr(document, entryElement, REGISTRY_ENTRY_KEY_ATTR, gender.key().fullName());
        if (!gender.isBuiltin()) {
          XmlUtils.setAttr(document, entryElement, REGISTRY_ENTRY_LABEL_ATTR, Objects.requireNonNull(gender.userDefinedName()));
        }
        XmlUtils.setAttr(document, entryElement, GENDER_COLOR_ATTR, gender.color());
      });
      if (gendersElement.hasChildNodes()) {
        registriesElement.appendChild(gendersElement);
      }
    }

    if (!userLifeEventTypes.isEmpty()) {
      Element typesElement = document.createElement(LIFE_EVENT_TYPES_TAG);
      userLifeEventTypes.forEach(lifeEventType -> {
        Element entryElement = (Element) typesElement.appendChild(document.createElement("Entry"));
        XmlUtils.setAttr(document, entryElement, REGISTRY_ENTRY_KEY_ATTR, lifeEventType.key().fullName());
        XmlUtils.setAttr(document, entryElement, REGISTRY_ENTRY_LABEL_ATTR, Objects.requireNonNull(lifeEventType.userDefinedName()));
        XmlUtils.setAttr(document, entryElement, LIFE_EVENT_TYPE_GROUP_ATTR, String.valueOf(lifeEventType.group().ordinal()));
        XmlUtils.setAttr(document, entryElement, LIFE_EVENT_TYPE_INDICATES_DEATH_ATTR, String.valueOf(lifeEventType.indicatesDeath()));
        XmlUtils.setAttr(document, entryElement, LIFE_EVENT_TYPE_INDICATES_UNION_ATTR, String.valueOf(lifeEventType.indicatesUnion()));
        XmlUtils.setAttr(document, entryElement, LIFE_EVENT_TYPE_ACTORS_NB_ATTR, String.valueOf(lifeEventType.minActors()));
        XmlUtils.setAttr(document, entryElement, LIFE_EVENT_TYPE_UNIQUE_ATTR, String.valueOf(lifeEventType.isUnique()));
      });
      if (typesElement.hasChildNodes()) {
        registriesElement.appendChild(typesElement);
      }
    }

    if (registriesElement.hasChildNodes()) {
      familyTreeElement.appendChild(registriesElement);
    }

    Collection<Picture> pictures = familyTree.pictures();
    if (!pictures.isEmpty()) {
      Element picturesElement = document.createElement(PICTURES_TAG);
      pictures.forEach(picture -> {
        Element pictureElement = (Element) picturesElement.appendChild(document.createElement(PICTURE_TAG));
        XmlUtils.setAttr(document, pictureElement, PICTURE_NAME_ATTR, picture.fileName());
        Element descElement = document.createElement(PICTURE_DESC_TAG);
        picture.description().ifPresent(descElement::setTextContent);
        picture.date().ifPresent(date -> this.writeDateTag(document, pictureElement, date));
        pictureElement.appendChild(descElement);
      });
      familyTreeElement.appendChild(picturesElement);
    }
  }

  // endregion
  // region Persons

  /**
   * Write all persons from the tree.
   *
   * @param document          Current XML document.
   * @param familyTreeElement Root element.
   * @param peopleElement     Element to write to.
   * @param familyTree        Family tree object to get root from.
   * @param persons           List of persons to write to the document.
   * @param personIDs         Map to get person IDs from.
   */
  private void writePersons(
      @NotNull Document document,
      @NotNull Element familyTreeElement,
      @NotNull Element peopleElement,
      final @NotNull FamilyTree familyTree,
      final @NotNull List<Person> persons,
      final @NotNull Map<Person, Integer> personIDs
  ) {
    for (Person person : persons) {
      if (familyTree.isRoot(person)) {
        // Set root ID attribute
        XmlUtils.setAttr(document, familyTreeElement, "root", String.valueOf(personIDs.get(person)));
      }

      Element personElement = (Element) peopleElement.appendChild(document.createElement(PERSON_TAG));

      this.writePicturesTag(document, personElement, person);
      this.writeDisambiguationIdTag(document, personElement, person);
      this.writeLifeStatusTag(document, personElement, person);
      this.writeLegalLastNameTag(document, personElement, person);
      // Legal first names
      this.writeNames(document, personElement, LEGAL_FIRST_NAMES_TAG, person.legalFirstNames());
      this.writePublicLastNameTag(document, personElement, person);
      // Public first names
      this.writeNames(document, personElement, PUBLIC_FIRST_NAMES_TAG, person.publicFirstNames());
      // Nicknames
      this.writeNames(document, personElement, NICKNAMES_TAG, person.nicknames());
      this.writeGenderTag(document, personElement, person);
      this.writeMainOccupationTag(document, personElement, person);
      this.writeParentsTag(document, personElement, person, personIDs);
      this.writeRelativesTag(document, personElement, person, personIDs);
      this.writeNotesTag(document, personElement, person);
      this.writeSourcesTag(document, personElement, person);
    }
  }

  private void writePicturesTag(
      @NotNull Document document,
      @NotNull Element element,
      final @NotNull GenealogyObject<?> o
  ) {
    Collection<Picture> pictures = o.pictures();
    if (!pictures.isEmpty()) {
      Element picturesElement = document.createElement(PICTURES_TAG);
      pictures.forEach(picture -> {
        Element pictureElement = (Element) picturesElement.appendChild(document.createElement(PICTURE_TAG));
        XmlUtils.setAttr(document, pictureElement, PICTURE_NAME_ATTR, picture.fileName());
        if (o.mainPicture().map(p -> p == picture).orElse(false)) {
          XmlUtils.setAttr(document, pictureElement, PICTURE_MAIN_ATTR, "true");
        }
      });
      element.appendChild(picturesElement);
    }
  }

  private void writeDisambiguationIdTag(
      @NotNull Document document,
      @NotNull Element personElement,
      final @NotNull Person person
  ) {
    person.disambiguationID().ifPresent(id -> {
      Element disambiguationIDElement = (Element) personElement.appendChild(document.createElement(DISAMBIGUATION_ID_TAG));
      XmlUtils.setAttr(document, disambiguationIDElement, DISAMBIG_ID_VALUE_ATTR, String.valueOf(id));
    });
  }

  private void writeLifeStatusTag(
      @NotNull Document document,
      @NotNull Element personElement,
      final @NotNull Person person
  ) {
    Element lifeStatusElement = (Element) personElement.appendChild(document.createElement(LIFE_STATUS_TAG));
    XmlUtils.setAttr(document, lifeStatusElement, LIFE_STATUS_ORDINAL_ATTR, String.valueOf(person.lifeStatus().ordinal()));
  }

  private void writeLegalLastNameTag(
      @NotNull Document document,
      @NotNull Element personElement,
      final @NotNull Person person
  ) {
    person.legalLastName().ifPresent(s -> {
      Element legalLastNameElement = (Element) personElement.appendChild(document.createElement(LEGAL_LAST_NAME_TAG));
      XmlUtils.setAttr(document, legalLastNameElement, NAME_VALUE_ATTR, s);
    });
  }

  private void writePublicLastNameTag(
      @NotNull Document document,
      @NotNull Element personElement,
      final @NotNull Person person
  ) {
    person.publicLastName().ifPresent(s -> {
      Element publicLastNameElement = (Element) personElement.appendChild(document.createElement(PUBLIC_LAST_NAME_TAG));
      XmlUtils.setAttr(document, publicLastNameElement, NAME_VALUE_ATTR, s);
    });
  }

  private void writeGenderTag(
      @NotNull Document document,
      @NotNull Element personElement,
      final @NotNull Person person
  ) {
    person.gender().ifPresent(gender -> {
      Element genderElement = (Element) personElement.appendChild(document.createElement(GENDER_TAG));
      XmlUtils.setAttr(document, genderElement, GENDER_KEY_ATTR, gender.key().fullName());
    });
  }

  private void writeMainOccupationTag(
      @NotNull Document document,
      @NotNull Element personElement,
      final @NotNull Person person
  ) {
    person.mainOccupation().ifPresent(occupation -> {
      Element occupationElement = (Element) personElement.appendChild(document.createElement(MAIN_OCCUPATION_TAG));
      XmlUtils.setAttr(document, occupationElement, MAIN_OCCUPATION_VALUE_ATTR, occupation);
    });
  }

  private void writeParentsTag(
      @NotNull Document document,
      @NotNull Element personElement,
      final @NotNull Person person,
      final @NotNull Map<Person, Integer> personIDs
  ) {
    var parents = person.parents();
    Element parentsElement = document.createElement(PARENTS_TAG);
    Function<Integer, Consumer<Optional<Person>>> writeParent = index -> p ->
        XmlUtils.setAttr(document, parentsElement, "id" + index, p.map(p_ -> String.valueOf(personIDs.get(p_))).orElse(""));
    Optional<Person> leftParent = parents.left();
    Optional<Person> rightParent = parents.right();
    writeParent.apply(1).accept(leftParent);
    writeParent.apply(2).accept(rightParent);
    if (leftParent.isPresent() || rightParent.isPresent()) {
      personElement.appendChild(parentsElement);
    }
  }

  private void writeRelativesTag(
      @NotNull Document document,
      @NotNull Element personElement,
      final @NotNull Person person,
      final @NotNull Map<Person, Integer> personIDs
  ) {
    Element relativesElement = document.createElement(RELATIVES_TAG);
    for (Person.RelativeType type : Person.RelativeType.values()) {
      Set<Person> relatives = person.getRelatives(type);
      if (relatives.isEmpty()) {
        continue;
      }
      Element groupElement = (Element) relativesElement.appendChild(document.createElement(GROUP_TAG));
      XmlUtils.setAttr(document, groupElement, GROUP_ORDINAL_ATTR, String.valueOf(type.ordinal()));
      for (Person relative : relatives) {
        Element relativeElement = (Element) groupElement.appendChild(document.createElement(RELATIVE_TAG));
        XmlUtils.setAttr(document, relativeElement, RELATIVE_ID_ATTR, String.valueOf(personIDs.get(relative)));
      }
    }
    if (relativesElement.hasChildNodes()) {
      personElement.appendChild(relativesElement);
    }
  }

  private void writeNotesTag(
      @NotNull Document document,
      @NotNull Element element,
      final @NotNull GenealogyObject<?> o
  ) {
    o.notes().ifPresent(notes -> {
      Element notesElement = (Element) element.appendChild(document.createElement(NOTES_TAG));
      notesElement.setTextContent(notes);
    });
  }

  private void writeSourcesTag(
      @NotNull Document document,
      @NotNull Element element,
      final @NotNull GenealogyObject<?> o
  ) {
    o.sources().ifPresent(sources -> {
      Element sourcesElement = (Element) element.appendChild(document.createElement(SOURCES_TAG));
      sourcesElement.setTextContent(sources);
    });
  }

  /**
   * Write a person’s names.
   *
   * @param document      Current XML document.
   * @param personElement Element to write into.
   * @param elementName   Name of the element to create.
   * @param names         List of names to write.
   */
  private void writeNames(
      @NotNull Document document,
      @NotNull Element personElement,
      @NotNull String elementName,
      final @NotNull List<String> names
  ) {
    if (names.isEmpty()) {
      return;
    }
    Element legalFirstNamesElement = (Element) personElement.appendChild(document.createElement(elementName));
    names.forEach(name -> {
      Element nameElement = (Element) legalFirstNamesElement.appendChild(document.createElement(NAME_TAG));
      XmlUtils.setAttr(document, nameElement, NAME_VALUE_ATTR, name);
    });
  }

  // endregion
  // region Life events

  /**
   * Write a list of life events.
   *
   * @param document          Current XML document.
   * @param lifeEventsElement Element to write to.
   * @param lifeEvents        Set of life events to write.
   * @param personIDs         Map to extract person IDs from.
   */
  private void writeEvents(
      @NotNull Document document,
      @NotNull Element lifeEventsElement,
      final @NotNull Set<LifeEvent> lifeEvents,
      final @NotNull Map<Person, Integer> personIDs
  ) {
    for (LifeEvent lifeEvent : lifeEvents) {
      Element lifeEventElement = (Element) lifeEventsElement.appendChild(document.createElement(LIFE_EVENT_TAG));

      this.writePicturesTag(document, lifeEventElement, lifeEvent);
      this.writeDateTag(document, lifeEventElement, lifeEvent.date());
      this.writeLifeEventTypeTag(document, lifeEventElement, lifeEvent);
      this.writePlace(document, lifeEventElement, lifeEvent);
      this.writeActorsTag(document, lifeEventElement, lifeEvent, personIDs);
      this.writeWitnessesTag(document, lifeEventElement, lifeEvent, personIDs);
      this.writeNotesTag(document, lifeEventElement, lifeEvent);
      this.writeSourcesTag(document, lifeEventElement, lifeEvent);
    }
  }

  private void writeDateTag(
      @NotNull Document document,
      @NotNull Element element,
      final @NotNull DateTime date
  ) {
    Element dateElement = (Element) element.appendChild(document.createElement(DATE_TAG));
    String dateType;
    if (date instanceof DateTimeWithPrecision d) {
      dateType = DATE_WITH_PRECISION;
      XmlUtils.setAttr(document, dateElement, DATE_DATE_ATTR, this.serializeDate(d.date()));
      XmlUtils.setAttr(document, dateElement, DATE_PRECISION_ATTR, String.valueOf(d.precision().ordinal()));
    } else if (date instanceof DateTimeRange d) {
      dateType = DATE_RANGE;
      XmlUtils.setAttr(document, dateElement, DATE_START_ATTR, this.serializeDate(d.startDate()));
      XmlUtils.setAttr(document, dateElement, DATE_END_ATTR, this.serializeDate(d.endDate()));
    } else if (date instanceof DateTimeAlternative d) {
      dateType = DATE_ALTERNATIVE;
      XmlUtils.setAttr(document, dateElement, DATE_EARLIEST_ATTR, this.serializeDate(d.earliestDate()));
      XmlUtils.setAttr(document, dateElement, DATE_LATEST_ATTR, this.serializeDate(d.latestDate()));
    } else {
      throw new IllegalArgumentException("Unsupported date type: " + date.getClass());
    }
    XmlUtils.setAttr(document, dateElement, DATE_TYPE_ATTR, dateType);
  }

  private String serializeDate(final @NotNull CalendarSpecificDateTime d) {
    return d + ";" + d.calendar().name();
  }

  private void writeLifeEventTypeTag(
      @NotNull Document document,
      @NotNull Element lifeEventElement,
      final @NotNull LifeEvent lifeEvent
  ) {
    Element typeElement = (Element) lifeEventElement.appendChild(document.createElement(TYPE_TAG));
    XmlUtils.setAttr(document, typeElement, "key", lifeEvent.type().key().fullName());
  }

  private void writePlace(
      @NotNull Document document,
      @NotNull Element lifeEventElement,
      final @NotNull LifeEvent lifeEvent
  ) {
    lifeEvent.place().ifPresent(place -> {
      Element placeElement = (Element) lifeEventElement.appendChild(document.createElement(PLACE_TAG));
      XmlUtils.setAttr(document, placeElement, PLACE_ADDRESS_ATTR, place.address());
      place.latLon().ifPresent(
          latLon -> XmlUtils.setAttr(document, placeElement, PLACE_LATLON_ATTR, latLon.toString()));
    });
  }

  private void writeActorsTag(
      @NotNull Document document,
      @NotNull Element lifeEventElement,
      final @NotNull LifeEvent lifeEvent,
      final @NotNull Map<Person, Integer> personIDs
  ) {
    Element actorsElement = (Element) lifeEventElement.appendChild(document.createElement(ACTORS_TAG));
    lifeEvent.actors().forEach(person -> {
      Element personElement = (Element) actorsElement.appendChild(document.createElement(PERSON_TAG));
      XmlUtils.setAttr(document, personElement, PERSON_ID_ATTR, String.valueOf(personIDs.get(person)));
    });
  }

  private void writeWitnessesTag(
      @NotNull Document document,
      @NotNull Element lifeEventElement,
      final @NotNull LifeEvent lifeEvent,
      final @NotNull Map<Person, Integer> personIDs
  ) {
    Element witnessesElement = document.createElement(WITNESSES_TAG);
    lifeEvent.witnesses().forEach(person -> {
      Element personElement = (Element) witnessesElement.appendChild(document.createElement(PERSON_TAG));
      XmlUtils.setAttr(document, personElement, PERSON_ID_ATTR, String.valueOf(personIDs.get(person)));
    });
    if (witnessesElement.hasChildNodes()) {
      lifeEventElement.appendChild(witnessesElement);
    }
  }

  // endregion
}
