package net.darmo_creations.jenealogio2.io;

import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.model.datetime.DateTime;
import net.darmo_creations.jenealogio2.model.datetime.DateTimeAlternative;
import net.darmo_creations.jenealogio2.model.datetime.DateTimeRange;
import net.darmo_creations.jenealogio2.model.datetime.DateTimeWithPrecision;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Writes .jtree files.
 */
public class TreeFileWriter extends TreeFileManager {
  private final TransformerFactory transformerFactory = TransformerFactory.newInstance();

  /**
   * Save a family tree to a .jtree file.
   *
   * @param familyTree Family tree object to save.
   * @param file       File to write to.
   * @throws IOException If any error occurs.
   */
  public void saveToFile(final @NotNull FamilyTree familyTree, @NotNull File file) throws IOException {
    Document document = this.newDocumentBuilder().newDocument();

    Element familyTreeElement = (Element) document.appendChild(document.createElement(FAMILY_TREE_TAG));
    this.setAttr(document, familyTreeElement, FAMILY_TREE_VERSION_ATTR, String.valueOf(VERSION));
    this.setAttr(document, familyTreeElement, FAMILY_TREE_NAME_ATTR, familyTree.name());

    Map<Person, Integer> personIDs = new HashMap<>();
    List<Person> persons = new LinkedList<>(familyTree.persons());
    for (int i = 0; i < persons.size(); i++) {
      personIDs.put(persons.get(i), i);
    }
    List<LifeEvent> lifeEvents = new LinkedList<>();

    this.writeUserRegistryEntries(document, familyTreeElement);
    Element peopleElement = (Element) familyTreeElement.appendChild(document.createElement(PEOPLE_TAG));
    this.writePersons(document, familyTreeElement, peopleElement, familyTree, persons, personIDs, lifeEvents);
    Element lifeEventsElement = document.createElement(LIFE_EVENTS_TAG);
    this.writeEvents(document, lifeEventsElement, lifeEvents, personIDs);
    if (lifeEventsElement.hasChildNodes()) {
      familyTreeElement.appendChild(lifeEventsElement);
    }

    this.writeFile(file, document);
  }

  /**
   * Write all user-defined registry entries.
   *
   * @param document          Current XML document.
   * @param familyTreeElement XML element to write into.
   */
  private void writeUserRegistryEntries(@NotNull Document document, @NotNull Element familyTreeElement) {
    Element registriesElement = document.createElement(REGISTRIES_TAG);
    List<Gender> userGenders = Registries.GENDERS.entries().stream()
        .filter(gender -> !gender.isBuiltin() || !gender.color().equals(gender.defaultColor()))
        .toList();
    List<LifeEventType> userLifeEventTypes = Registries.LIFE_EVENT_TYPES.entries().stream()
        .filter(lifeEventType -> !lifeEventType.isBuiltin())
        .toList();

    if (!userGenders.isEmpty()) {
      Element gendersElement = document.createElement(GENDERS_TAG);
      userGenders.forEach(gender -> {
        Element entryElement = (Element) gendersElement.appendChild(document.createElement(REGISTRY_ENTRY_TAG));
        this.setAttr(document, entryElement, REGISTRY_ENTRY_KEY_ATTR, gender.key().fullName());
        if (!gender.isBuiltin()) {
          this.setAttr(document, entryElement, REGISTRY_ENTRY_LABEL_ATTR, Objects.requireNonNull(gender.userDefinedName()));
        }
        this.setAttr(document, entryElement, GENDER_COLOR_ATTR, gender.color());
      });
      if (gendersElement.hasChildNodes()) {
        registriesElement.appendChild(gendersElement);
      }
    }

    if (!userLifeEventTypes.isEmpty()) {
      Element typesElement = document.createElement(LIFE_EVENT_TYPES_TAG);
      userLifeEventTypes.forEach(lifeEventType -> {
        Element entryElement = (Element) typesElement.appendChild(document.createElement("Entry"));
        this.setAttr(document, entryElement, REGISTRY_ENTRY_KEY_ATTR, lifeEventType.key().fullName());
        this.setAttr(document, entryElement, REGISTRY_ENTRY_LABEL_ATTR, Objects.requireNonNull(lifeEventType.userDefinedName()));
        this.setAttr(document, entryElement, LIFE_EVENT_TYPE_GROUP_ATTR, String.valueOf(lifeEventType.group().ordinal()));
        this.setAttr(document, entryElement, LIFE_EVENT_TYPE_INDICATES_DEATH_ATTR, String.valueOf(lifeEventType.indicatesDeath()));
        this.setAttr(document, entryElement, LIFE_EVENT_TYPE_INDICATES_UNION_ATTR, String.valueOf(lifeEventType.indicatesUnion()));
        this.setAttr(document, entryElement, LIFE_EVENT_TYPE_ACTORS_NB_ATTR, String.valueOf(lifeEventType.minActors()));
        this.setAttr(document, entryElement, LIFE_EVENT_TYPE_UNIQUE_ATTR, String.valueOf(lifeEventType.isUnique()));
      });
      if (typesElement.hasChildNodes()) {
        registriesElement.appendChild(typesElement);
      }
    }

    if (registriesElement.hasChildNodes()) {
      familyTreeElement.appendChild(registriesElement);
    }
  }

  /**
   * Write all persons from the tree.
   *
   * @param document          Current XML document.
   * @param familyTreeElement Root element.
   * @param peopleElement     Element to write to.
   * @param familyTree        Family tree object to get root from.
   * @param persons           List of persons to write to the document.
   * @param personIDs         Map to get person IDs from.
   * @param lifeEvents        List of life events to populate.
   */
  private void writePersons(
      @NotNull Document document,
      @NotNull Element familyTreeElement,
      @NotNull Element peopleElement,
      final @NotNull FamilyTree familyTree,
      final @NotNull List<Person> persons,
      final @NotNull Map<Person, Integer> personIDs,
      @NotNull List<LifeEvent> lifeEvents
  ) {
    for (Person person : persons) {
      if (familyTree.isRoot(person)) {
        // Set root ID attribute
        this.setAttr(document, familyTreeElement, "root", String.valueOf(personIDs.get(person)));
      }

      Element personElement = (Element) peopleElement.appendChild(document.createElement(PERSON_TAG));

      // Image
      // TODO serialize image

      // Disambiguation ID
      person.disambiguationID().ifPresent(id -> {
        Element disambiguationIDElement = (Element) personElement.appendChild(document.createElement(DISAMBIGUATION_ID_TAG));
        this.setAttr(document, disambiguationIDElement, DISAMBIG_ID_VALUE_ATTR, String.valueOf(id));
      });

      // Life status
      Element lifeStatusElement = (Element) personElement.appendChild(document.createElement(LIFE_STATUS_TAG));
      this.setAttr(document, lifeStatusElement, LIFE_STATUS_ORDINAL_ATTR, String.valueOf(person.lifeStatus().ordinal()));

      // Legal last name
      person.legalLastName().ifPresent(s -> {
        Element legalLastNameElement = (Element) personElement.appendChild(document.createElement(LEGAL_LAST_NAME_TAG));
        this.setAttr(document, legalLastNameElement, NAME_VALUE_ATTR, s);
      });

      // Legal first names
      this.writeNames(document, personElement, LEGAL_FIRST_NAMES_TAG, person.legalFirstNames());

      // Public last name
      person.publicLastName().ifPresent(s -> {
        Element publicLastNameElement = (Element) personElement.appendChild(document.createElement(PUBLIC_LAST_NAME_TAG));
        this.setAttr(document, publicLastNameElement, NAME_VALUE_ATTR, s);
      });

      // Public first names
      this.writeNames(document, personElement, PUBLIC_FIRST_NAMES_TAG, person.publicFirstNames());

      // Nicknames
      this.writeNames(document, personElement, NICKNAMES_TAG, person.nicknames());

      // Gender
      person.gender().ifPresent(gender -> {
        Element genderElement = (Element) personElement.appendChild(document.createElement(GENDER_TAG));
        this.setAttr(document, genderElement, GENDER_KEY_ATTR, gender.key().fullName());
      });

      // Main occupation
      person.mainOccupation().ifPresent(occupation -> {
        Element occupationElement = (Element) personElement.appendChild(document.createElement(MAIN_OCCUPATION_TAG));
        this.setAttr(document, occupationElement, MAIN_OCCUPATION_VALUE_ATTR, occupation);
      });

      // Parents
      var parents = person.parents();
      Element parentsElement = document.createElement(PARENTS_TAG);
      Function<Integer, Consumer<Optional<Person>>> writeParent = index -> p ->
          this.setAttr(document, parentsElement, "id" + index, p.map(p_ -> String.valueOf(personIDs.get(p_))).orElse(""));
      Optional<Person> leftParent = parents.left();
      Optional<Person> rightParent = parents.right();
      writeParent.apply(1).accept(leftParent);
      writeParent.apply(2).accept(rightParent);
      if (leftParent.isPresent() || rightParent.isPresent()) {
        personElement.appendChild(parentsElement);
      }

      // Relatives
      Element relativesElement = document.createElement(RELATIVES_TAG);
      for (Person.RelativeType type : Person.RelativeType.values()) {
        Set<Person> relatives = person.getRelatives(type);
        if (relatives.isEmpty()) {
          continue;
        }
        Element groupElement = (Element) relativesElement.appendChild(document.createElement(GROUP_TAG));
        this.setAttr(document, groupElement, GROUP_ORDINAL_ATTR, String.valueOf(type.ordinal()));
        for (Person relative : relatives) {
          Element relativeElement = (Element) groupElement.appendChild(document.createElement(RELATIVE_TAG));
          this.setAttr(document, relativeElement, RELATIVE_ID_ATTR, String.valueOf(personIDs.get(relative)));
        }
      }
      if (relativesElement.hasChildNodes()) {
        personElement.appendChild(relativesElement);
      }

      // Life events
      Element lifeEventsElement = document.createElement(LIFE_EVENTS_TAG);
      for (LifeEvent lifeEvent : person.lifeEvents()) {
        if (!lifeEvents.contains(lifeEvent)) {
          lifeEvents.add(lifeEvent);
        }
      }
      if (lifeEventsElement.hasChildNodes()) {
        personElement.appendChild(lifeEventsElement);
      }

      // Notes
      person.notes().ifPresent(notes -> {
        Element notesElement = (Element) personElement.appendChild(document.createElement(NOTES_TAG));
        notesElement.setTextContent(notes);
      });

      // Sources
      person.sources().ifPresent(sources -> {
        Element sourcesElement = (Element) personElement.appendChild(document.createElement(SOURCES_TAG));
        sourcesElement.setTextContent(sources);
      });
    }
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
      this.setAttr(document, nameElement, NAME_VALUE_ATTR, name);
    });
  }

  /**
   * Write a list of life events.
   *
   * @param document          Current XML document.
   * @param lifeEventsElement Element to write to.
   * @param lifeEvents        List of life events to write.
   * @param personIDs         Map to extract person IDs from.
   */
  private void writeEvents(
      @NotNull Document document,
      @NotNull Element lifeEventsElement,
      final @NotNull List<LifeEvent> lifeEvents,
      final @NotNull Map<Person, Integer> personIDs
  ) {
    for (LifeEvent lifeEvent : lifeEvents) {
      Element lifeEventElement = (Element) lifeEventsElement.appendChild(document.createElement(LIFE_EVENT_TAG));

      // Date
      Element dateElement = (Element) lifeEventElement.appendChild(document.createElement(DATE_TAG));
      DateTime date = lifeEvent.date();
      String dateType;
      if (date instanceof DateTimeWithPrecision d) {
        dateType = DATE_WITH_PRECISION;
        this.setAttr(document, dateElement, DATE_DATE_ATTR, d.date().toString());
        this.setAttr(document, dateElement, DATE_PRECISION_ATTR, String.valueOf(d.precision().ordinal()));
      } else if (date instanceof DateTimeRange d) {
        dateType = DATE_RANGE;
        this.setAttr(document, dateElement, DATE_START_ATTR, d.startDate().toString());
        this.setAttr(document, dateElement, DATE_END_ATTR, d.endDate().toString());
      } else if (date instanceof DateTimeAlternative d) {
        dateType = DATE_ALTERNATIVE;
        this.setAttr(document, dateElement, DATE_EARLIEST_ATTR, d.earliestDate().toString());
        this.setAttr(document, dateElement, DATE_LATEST_ATTR, d.latestDate().toString());
      } else {
        throw new IllegalArgumentException("Unsupported date type: " + date.getClass());
      }
      this.setAttr(document, dateElement, DATE_TYPE_ATTR, dateType);

      // Type
      Element typeElement = (Element) lifeEventElement.appendChild(document.createElement(TYPE_TAG));
      this.setAttr(document, typeElement, "key", lifeEvent.type().key().fullName());

      // Place
      lifeEvent.place().ifPresent(place -> {
        Element placeElement = (Element) lifeEventElement.appendChild(document.createElement(PLACE_TAG));
        this.setAttr(document, placeElement, "value", place);
      });

      // Actors
      Element actorsElement = (Element) lifeEventElement.appendChild(document.createElement(ACTORS_TAG));
      lifeEvent.actors().forEach(person -> {
        Element personElement = (Element) actorsElement.appendChild(document.createElement(PERSON_TAG));
        this.setAttr(document, personElement, PERSON_ID_ATTR, String.valueOf(personIDs.get(person)));
      });

      // Witnesses
      Element witnessesElement = document.createElement(WITNESSES_TAG);
      lifeEvent.witnesses().forEach(person -> {
        Element personElement = (Element) witnessesElement.appendChild(document.createElement(PERSON_TAG));
        this.setAttr(document, personElement, PERSON_ID_ATTR, String.valueOf(personIDs.get(person)));
      });
      if (witnessesElement.hasChildNodes()) {
        lifeEventElement.appendChild(witnessesElement);
      }

      // Notes
      lifeEvent.notes().ifPresent(notes -> {
        Element notesElement = (Element) lifeEventElement.appendChild(document.createElement(NOTES_TAG));
        notesElement.setTextContent(notes);
      });

      // Sources
      lifeEvent.sources().ifPresent(sources -> {
        Element sourcesElement = (Element) lifeEventElement.appendChild(document.createElement(SOURCES_TAG));
        sourcesElement.setTextContent(sources);
      });
    }
  }

  /**
   * Set attribute value of an element.
   *
   * @param document Current XML document.
   * @param element  Element to set attribute of.
   * @param name     Attribute’s name.
   * @param value    Attribute’s value.
   */
  private void setAttr(
      @NotNull Document document,
      @NotNull Element element,
      @NotNull String name,
      @NotNull String value
  ) {
    Attr attr = document.createAttribute(name);
    attr.setValue(value);
    element.setAttributeNode(attr);
  }

  /**
   * Write an XML document to a file.
   *
   * @param file     File to write to.
   * @param document XML document to write to.
   * @throws IOException If any error occurs.
   */
  private void writeFile(@NotNull File file, final @NotNull Document document) throws IOException {
    Transformer tr;
    try {
      tr = this.transformerFactory.newTransformer();
    } catch (TransformerConfigurationException e) {
      throw new RuntimeException(e);
    }
    tr.setOutputProperty(OutputKeys.INDENT, "yes");
    tr.setOutputProperty(OutputKeys.METHOD, "xml");
    tr.setOutputProperty(OutputKeys.STANDALONE, "yes");
    tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    try (FileOutputStream outputStream = new FileOutputStream(file)) {
      tr.transform(new DOMSource(document), new StreamResult(outputStream));
    } catch (TransformerException e) {
      throw new RuntimeException(e);
    }
  }
}
