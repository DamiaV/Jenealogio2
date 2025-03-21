package net.darmo_creations.jenealogio2.io;

import javafx.scene.image.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.model.datetime.*;
import net.darmo_creations.jenealogio2.model.datetime.calendar.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;
import org.w3c.dom.*;

import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

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
  public void writeToStream(
      final @NotNull FamilyTree familyTree,
      @NotNull OutputStream outputStream,
      final @NotNull Config config
  ) {
    final Document document = this.newDocumentBuilder().newDocument();

    final Element familyTreeElement = (Element) document
        .appendChild(document.createElement(FAMILY_TREE_TAG));
    XmlUtils.setAttr(
        document,
        familyTreeElement,
        FAMILY_TREE_VERSION_ATTR,
        String.valueOf(VERSION)
    );
    XmlUtils.setAttr(
        document,
        familyTreeElement,
        FAMILY_TREE_NAME_ATTR,
        familyTree.name()
    );

    final Map<Person, Integer> personIDs = new HashMap<>();
    final Map<LifeEvent, Integer> eventIDs = new HashMap<>();
    final List<Person> persons = new LinkedList<>(familyTree.persons());
    for (int i = 0; i < persons.size(); i++)
      personIDs.put(persons.get(i), i);
    final List<LifeEvent> events = new LinkedList<>(familyTree.lifeEvents());
    for (int i = 0; i < events.size(); i++)
      eventIDs.put(events.get(i), i);

    this.writeUserRegistryEntries(document, familyTreeElement, familyTree, null);
    this.writeDocuments(document, familyTreeElement, familyTree, personIDs, eventIDs);
    final Element peopleElement = (Element) familyTreeElement
        .appendChild(document.createElement(PEOPLE_TAG));
    this.writePersons(document, familyTreeElement, peopleElement, familyTree, persons, personIDs);
    final Element lifeEventsElement = document.createElement(LIFE_EVENTS_TAG);
    this.writeEvents(document, lifeEventsElement, familyTree.lifeEvents(), personIDs);
    if (lifeEventsElement.hasChildNodes())
      familyTreeElement.appendChild(lifeEventsElement);

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
      final @NotNull RegistriesValues keep,
      final @NotNull Config config
  ) throws IOException {
    final Document document = this.newDocumentBuilder().newDocument();
    final Element dummyElement = document.createElement("dummy");
    this.writeUserRegistryEntries(document, dummyElement, familyTree, keep);
    final Element registriesElement = (Element) dummyElement.getChildNodes().item(0);
    document.appendChild(registriesElement);
    XmlUtils.setAttr(
        document,
        registriesElement,
        REGISTRIES_VERSION_ATTR,
        String.valueOf(VERSION)
    );
    try (final var outputStream = new FileOutputStream(file.toFile())) {
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
   * @param keep              The {@link RegistryEntry} values to save.
   *                          If null, all eligible entries are kept.
   */
  private void writeUserRegistryEntries(
      @NotNull Document document,
      @NotNull Element familyTreeElement,
      final @NotNull FamilyTree familyTree,
      final RegistriesValues keep
  ) {
    final Element registriesElement = document.createElement(REGISTRIES_TAG);
    final List<LifeEventType> userLifeEventTypes = familyTree.lifeEventTypeRegistry()
        .serializableEntries()
        .stream()
        .filter(entry -> keep == null || keep.lifeEventTypeKeys().contains(entry.key()))
        .toList();
    final List<Gender> userGenders = familyTree.genderRegistry()
        .serializableEntries()
        .stream()
        .filter(entry -> keep == null || keep.genderKeys().contains(entry.key()))
        .toList();

    if (!userGenders.isEmpty()) {
      final Element gendersElement = document.createElement(GENDERS_TAG);
      for (final Gender gender : userGenders) {
        final Element entryElement = (Element) gendersElement
            .appendChild(document.createElement(REGISTRY_ENTRY_TAG));
        XmlUtils.setAttr(
            document,
            entryElement,
            REGISTRY_ENTRY_KEY_ATTR,
            gender.key().fullName()
        );
        XmlUtils.setAttr(
            document,
            entryElement,
            REGISTRY_ENTRY_LABEL_ATTR,
            Objects.requireNonNull(gender.userDefinedName())
        );
        XmlUtils.setAttr(
            document,
            entryElement,
            GENDER_ICON_ATTR,
            imageToBase64(gender.icon())
        );
      }
      if (gendersElement.hasChildNodes())
        registriesElement.appendChild(gendersElement);
    }

    if (!userLifeEventTypes.isEmpty()) {
      final Element typesElement = document.createElement(LIFE_EVENT_TYPES_TAG);
      userLifeEventTypes.forEach(lifeEventType -> {
        final Element entryElement = (Element) typesElement
            .appendChild(document.createElement("Entry"));
        XmlUtils.setAttr(
            document,
            entryElement,
            REGISTRY_ENTRY_KEY_ATTR,
            lifeEventType.key().fullName()
        );
        XmlUtils.setAttr(
            document,
            entryElement,
            REGISTRY_ENTRY_LABEL_ATTR,
            Objects.requireNonNull(lifeEventType.userDefinedName())
        );
        XmlUtils.setAttr(
            document,
            entryElement,
            LIFE_EVENT_TYPE_GROUP_ATTR,
            String.valueOf(lifeEventType.group().ordinal())
        );
        XmlUtils.setAttr(
            document,
            entryElement,
            LIFE_EVENT_TYPE_INDICATES_DEATH_ATTR,
            String.valueOf(lifeEventType.indicatesDeath())
        );
        XmlUtils.setAttr(
            document,
            entryElement,
            LIFE_EVENT_TYPE_INDICATES_UNION_ATTR,
            String.valueOf(lifeEventType.indicatesUnion())
        );
        XmlUtils.setAttr(
            document,
            entryElement,
            LIFE_EVENT_TYPE_ACTORS_NB_ATTR,
            String.valueOf(lifeEventType.minActors())
        );
        XmlUtils.setAttr(
            document,
            entryElement,
            LIFE_EVENT_TYPE_UNIQUE_ATTR,
            String.valueOf(lifeEventType.isUnique())
        );
      });
      if (typesElement.hasChildNodes())
        registriesElement.appendChild(typesElement);
    }

    if (registriesElement.hasChildNodes())
      familyTreeElement.appendChild(registriesElement);
  }

  /**
   * Convert an {@link Image}’s ARGB pixel data to a Base64 string.
   *
   * @param image The image to convert.
   * @return The computed Base64.
   */
  private static String imageToBase64(@NotNull Image image) {
    final int w = (int) image.getWidth();
    final int h = (int) image.getHeight();
    final var bb = ByteBuffer.allocate(2 + 4 * (w * h));
    bb.put((byte) w);
    bb.put((byte) h);
    final int[] buffer = new int[w * h];
    image.getPixelReader().getPixels(0, 0, w, h, PixelFormat.getIntArgbInstance(), buffer, 0, w);
    for (final int argb : buffer)
      bb.putInt(argb);
    return Base64.getEncoder().encodeToString(bb.array());
  }

  // endregion
  // region Documents

  private void writeDocuments(
      @NotNull Document document,
      @NotNull Element familyTreeElement,
      @NotNull FamilyTree familyTree,
      final @NotNull Map<Person, Integer> personIDs,
      final @NotNull Map<LifeEvent, Integer> eventIDs
  ) {
    final Collection<AttachedDocument> documents = familyTree.documents();
    if (!documents.isEmpty()) {
      final Element documentsElement = document.createElement(DOCUMENTS_TAG);
      documents.forEach(doc -> {
        final Element documentElement = (Element) documentsElement
            .appendChild(document.createElement(DOCUMENT_TAG));
        XmlUtils.setAttr(document, documentElement, DOCUMENT_NAME_ATTR, doc.fileName());
        if (doc.description().isPresent()) {
          final Element descElement = document.createElement(DOCUMENT_DESC_TAG);
          descElement.setTextContent(doc.description().get());
          documentElement.appendChild(descElement);
        }
        doc.date().ifPresent(date -> this.writeDateTag(document, documentElement, date));

        if (!doc.authors().isEmpty()) {
          final Element authorsElement = (Element) documentElement
              .appendChild(document.createElement(AUTHORS_TAG));
          XmlUtils.setAttr(
              document,
              authorsElement,
              AUTHORS_IDS_ATTR,
              doc.authors().stream()
                  .map(p -> String.valueOf(personIDs.get(p)))
                  .collect(Collectors.joining(","))
          );
        }

        final Element annotationsElement = document.createElement(DOCUMENT_ANNOTATIONS_TAG);
        for (final var annotationType : AnnotationType.values()) {
          final var annotations = doc.annotatedObjects(annotationType);
          if (annotations.isEmpty()) continue;

          final Element docAnnotationsElement = (Element) annotationsElement
              .appendChild(document.createElement(StringUtils.capitalize(annotationType.name())));
          annotations.forEach((o, note) -> {
            final Element objectElement;
            final int id;
            if (o instanceof Person p) {
              objectElement = (Element) docAnnotationsElement
                  .appendChild(document.createElement(PERSON_ANNOTATION_TAG));
              id = personIDs.get(p);
            } else if (o instanceof LifeEvent e) {
              objectElement = (Element) docAnnotationsElement
                  .appendChild(document.createElement(LIFE_EVENT_ANNOTATION_TAG));
              id = eventIDs.get(e);
            } else throw new IllegalArgumentException("invalid object type: " + o);

            XmlUtils.setAttr(
                document,
                objectElement,
                ANNOTATION_OBJECT_ID_ATTR,
                String.valueOf(id)
            );
            note.ifPresent(s -> XmlUtils.setAttr(
                document,
                objectElement,
                ANNOTATION_NOTE_ATTR,
                s
            ));
          });
        }
        if (annotationsElement.hasChildNodes())
          documentElement.appendChild(annotationsElement);
      });
      familyTreeElement.appendChild(documentsElement);
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
    for (final Person person : persons) {
      // Set root ID attribute
      if (familyTree.isRoot(person))
        XmlUtils.setAttr(
            document,
            familyTreeElement,
            FAMILY_TREE_ROOT_ATTR,
            String.valueOf(personIDs.get(person))
        );

      final Element personElement = (Element) peopleElement
          .appendChild(document.createElement(PERSON_TAG));

      this.writeMainPictureTag(document, personElement, person);
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
      this.writeGenderTag(document, personElement, AGAB_TAG, AGAB_KEY_ATTR, person::assignedGenderAtBirth);
      if (!person.assignedGenderAtBirth().equals(person.gender()))
        this.writeGenderTag(document, personElement, GENDER_TAG, GENDER_KEY_ATTR, person::gender);
      this.writeMainOccupationTag(document, personElement, person);
      this.writeParentsTag(document, personElement, person, personIDs);
      this.writeNotesTag(document, personElement, person);
      this.writeSourcesTag(document, personElement, person);
    }
  }

  private void writeMainPictureTag(
      @NotNull Document document,
      @NotNull Element element,
      final @NotNull GenealogyObject<?> o
  ) {
    final var mainPicture = o.mainPicture();
    if (mainPicture.isEmpty()) return;

    final Element mainPictureElement = (Element) element
        .appendChild(document.createElement(MAIN_PICTURE_TAG));
    XmlUtils.setAttr(
        document,
        mainPictureElement,
        MAIN_PICTURE_NAME_ATTR,
        mainPicture.get().fileName()
    );
  }

  private void writeDisambiguationIdTag(
      @NotNull Document document,
      @NotNull Element personElement,
      final @NotNull Person person
  ) {
    person.disambiguationID().ifPresent(id -> {
      final Element disambiguationIDElement = (Element) personElement
          .appendChild(document.createElement(DISAMBIGUATION_ID_TAG));
      XmlUtils.setAttr(
          document,
          disambiguationIDElement,
          DISAMBIG_ID_VALUE_ATTR,
          String.valueOf(id)
      );
    });
  }

  private void writeLifeStatusTag(
      @NotNull Document document,
      @NotNull Element personElement,
      final @NotNull Person person
  ) {
    final Element lifeStatusElement = (Element) personElement
        .appendChild(document.createElement(LIFE_STATUS_TAG));
    XmlUtils.setAttr(
        document,
        lifeStatusElement,
        LIFE_STATUS_ORDINAL_ATTR,
        String.valueOf(person.lifeStatus().ordinal())
    );
  }

  private void writeLegalLastNameTag(
      @NotNull Document document,
      @NotNull Element personElement,
      final @NotNull Person person
  ) {
    person.legalLastName().ifPresent(s -> {
      final Element legalLastNameElement = (Element) personElement
          .appendChild(document.createElement(LEGAL_LAST_NAME_TAG));
      XmlUtils.setAttr(
          document,
          legalLastNameElement,
          NAME_VALUE_ATTR,
          s
      );
    });
  }

  private void writePublicLastNameTag(
      @NotNull Document document,
      @NotNull Element personElement,
      final @NotNull Person person
  ) {
    person.publicLastName().ifPresent(s -> {
      final Element publicLastNameElement = (Element) personElement
          .appendChild(document.createElement(PUBLIC_LAST_NAME_TAG));
      XmlUtils.setAttr(
          document,
          publicLastNameElement,
          NAME_VALUE_ATTR,
          s
      );
    });
  }

  /**
   * Write a gender tag.
   *
   * @param document       The document to write to.
   * @param personElement  The <Person> tag to write into.
   * @param tagName        The name of the gender tag to write.
   * @param attrName       The attribute to add to the gender tag.
   * @param genderSupplier A function that returns an optional {@link Gender} object.
   */
  private void writeGenderTag(
      @NotNull Document document,
      @NotNull Element personElement,
      final @NotNull String tagName,
      final @NotNull String attrName,
      final @NotNull Supplier<Optional<Gender>> genderSupplier
  ) {
    genderSupplier.get().ifPresent(gender -> {
      final Element genderElement = (Element) personElement
          .appendChild(document.createElement(tagName));
      XmlUtils.setAttr(
          document,
          genderElement,
          attrName,
          gender.key().fullName()
      );
    });
  }

  private void writeMainOccupationTag(
      @NotNull Document document,
      @NotNull Element personElement,
      final @NotNull Person person
  ) {
    person.mainOccupation().ifPresent(occupation -> {
      final Element occupationElement = (Element) personElement
          .appendChild(document.createElement(MAIN_OCCUPATION_TAG));
      XmlUtils.setAttr(
          document,
          occupationElement,
          MAIN_OCCUPATION_VALUE_ATTR,
          occupation
      );
    });
  }

  private void writeParentsTag(
      @NotNull Document document,
      @NotNull Element personElement,
      final @NotNull Person person,
      final @NotNull Map<Person, Integer> personIDs
  ) {
    final Element parentsElement = document.createElement(PARENTS_TAG);
    for (final var type : ParentalRelationType.values()) {
      final Set<Person> parents = person.parents(type);
      if (parents.isEmpty())
        continue;
      final Element groupElement = (Element) parentsElement
          .appendChild(document.createElement(PARENT_GROUP_TAG));
      XmlUtils.setAttr(
          document,
          groupElement,
          PARENT_GROUP_ORDINAL_ATTR,
          String.valueOf(type.ordinal())
      );
      for (final Person parent : parents) {
        final Element parentElement = (Element) groupElement
            .appendChild(document.createElement(PARENT_TAG));
        XmlUtils.setAttr(
            document,
            parentElement,
            PARENT_ID_ATTR,
            String.valueOf(personIDs.get(parent))
        );
      }
    }
    if (parentsElement.hasChildNodes())
      personElement.appendChild(parentsElement);
  }

  private void writeNotesTag(
      @NotNull Document document,
      @NotNull Element element,
      final @NotNull GenealogyObject<?> o
  ) {
    o.notes().ifPresent(notes -> {
      final Element notesElement = (Element) element
          .appendChild(document.createElement(NOTES_TAG));
      notesElement.setTextContent(notes);
    });
  }

  private void writeSourcesTag(
      @NotNull Document document,
      @NotNull Element element,
      final @NotNull GenealogyObject<?> o
  ) {
    o.sources().ifPresent(sources -> {
      final Element sourcesElement = (Element) element
          .appendChild(document.createElement(SOURCES_TAG));
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
    final Element legalFirstNamesElement = (Element) personElement
        .appendChild(document.createElement(elementName));
    names.forEach(name -> {
      final Element nameElement = (Element) legalFirstNamesElement
          .appendChild(document.createElement(NAME_TAG));
      XmlUtils.setAttr(
          document,
          nameElement,
          NAME_VALUE_ATTR,
          name
      );
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
    for (final LifeEvent lifeEvent : lifeEvents) {
      final Element lifeEventElement = (Element) lifeEventsElement
          .appendChild(document.createElement(LIFE_EVENT_TAG));

      this.writeMainPictureTag(document, lifeEventElement, lifeEvent);
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
    final Element dateElement = (Element) element
        .appendChild(document.createElement(DATE_TAG));
    final String dateType;
    if (date instanceof DateTimeWithPrecision d) {
      dateType = DATE_WITH_PRECISION;
      XmlUtils.setAttr(
          document,
          dateElement,
          DATE_DATE_ATTR,
          this.serializeDate(d.date())
      );
      XmlUtils.setAttr(
          document,
          dateElement,
          DATE_PRECISION_ATTR,
          String.valueOf(d.precision().ordinal())
      );
    } else if (date instanceof DateTimeRange d) {
      dateType = DATE_RANGE;
      XmlUtils.setAttr(
          document,
          dateElement,
          DATE_START_ATTR,
          this.serializeDate(d.startDate())
      );
      XmlUtils.setAttr(
          document,
          dateElement,
          DATE_END_ATTR,
          this.serializeDate(d.endDate())
      );
    } else if (date instanceof DateTimeAlternative d) {
      dateType = DATE_ALTERNATIVE;
      final var dates = d.dates();
      for (int i = 0; i < dates.size(); i++)
        XmlUtils.setAttr(
            document,
            dateElement,
            "date" + (i + 1),
            this.serializeDate(dates.get(i))
        );
    } else
      throw new IllegalArgumentException("Unsupported date type: " + date.getClass());
    XmlUtils.setAttr(
        document,
        dateElement,
        DATE_TYPE_ATTR,
        dateType
    );
  }

  private String serializeDate(final @NotNull CalendarSpecificDateTime d) {
    return d + ";" + d.calendar().name();
  }

  private void writeLifeEventTypeTag(
      @NotNull Document document,
      @NotNull Element lifeEventElement,
      final @NotNull LifeEvent lifeEvent
  ) {
    final Element typeElement = (Element) lifeEventElement
        .appendChild(document.createElement(TYPE_TAG));
    XmlUtils.setAttr(
        document,
        typeElement,
        "key",
        lifeEvent.type().key().fullName()
    );
  }

  private void writePlace(
      @NotNull Document document,
      @NotNull Element lifeEventElement,
      final @NotNull LifeEvent lifeEvent
  ) {
    lifeEvent.place().ifPresent(place -> {
      final Element placeElement = (Element) lifeEventElement
          .appendChild(document.createElement(PLACE_TAG));
      XmlUtils.setAttr(
          document,
          placeElement,
          PLACE_ADDRESS_ATTR,
          place.address()
      );
      place.latLon().ifPresent(latLon -> XmlUtils.setAttr(
          document,
          placeElement,
          PLACE_LATLON_ATTR,
          latLon.toString()
      ));
    });
  }

  private void writeActorsTag(
      @NotNull Document document,
      @NotNull Element lifeEventElement,
      final @NotNull LifeEvent lifeEvent,
      final @NotNull Map<Person, Integer> personIDs
  ) {
    final Element actorsElement = (Element) lifeEventElement
        .appendChild(document.createElement(ACTORS_TAG));
    lifeEvent.actors().forEach(person -> {
      final Element personElement = (Element) actorsElement
          .appendChild(document.createElement(PERSON_TAG));
      XmlUtils.setAttr(
          document,
          personElement,
          PERSON_ID_ATTR,
          String.valueOf(personIDs.get(person))
      );
    });
  }

  private void writeWitnessesTag(
      @NotNull Document document,
      @NotNull Element lifeEventElement,
      final @NotNull LifeEvent lifeEvent,
      final @NotNull Map<Person, Integer> personIDs
  ) {
    final Element witnessesElement = document.createElement(WITNESSES_TAG);
    lifeEvent.witnesses().forEach(person -> {
      final Element personElement = (Element) witnessesElement
          .appendChild(document.createElement(PERSON_TAG));
      XmlUtils.setAttr(
          document,
          personElement,
          PERSON_ID_ATTR,
          String.valueOf(personIDs.get(person))
      );
    });
    if (witnessesElement.hasChildNodes())
      lifeEventElement.appendChild(witnessesElement);
  }

  // endregion
}
