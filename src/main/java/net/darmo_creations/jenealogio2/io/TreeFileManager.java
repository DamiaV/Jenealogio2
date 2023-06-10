package net.darmo_creations.jenealogio2.io;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Base class for tree files managers.
 */
public abstract class TreeFileManager {
  // Tags and attributes names
  public static final String FAMILY_TREE_TAG = "FamilyTree";
  public static final String FAMILY_TREE_VERSION_ATTR = "version";
  public static final String FAMILY_TREE_NAME_ATTR = "name";
  public static final String FAMILY_TREE_ROOT_ATTR = "root";
  public static final String REGISTRIES_TAG = "Registries";
  public static final String PEOPLE_TAG = "People";
  public static final String LIFE_EVENTS_TAG = "LifeEvents";
  public static final String GENDERS_TAG = "Genders";
  public static final String REGISTRY_ENTRY_TAG = "Entry";
  public static final String REGISTRY_ENTRY_NAME_ATTR = "name";
  public static final String GENDER_COLOR_ATTR = "color";
  public static final String LIFE_EVENT_TYPES_TAG = "LifeEventTypes";
  public static final String LIFE_EVENT_TYPE_GROUP_ATTR = "group";
  public static final String LIFE_EVENT_TYPE_INDICATES_DEATH_ATTR = "indicatesDeath";
  public static final String LIFE_EVENT_TYPE_INDICATES_UNION_ATTR = "indicatesUnion";
  public static final String LIFE_EVENT_TYPE_MIN_ACTORS_ATTR = "minActors";
  public static final String LIFE_EVENT_TYPE_MAX_ACTORS_ATTR = "maxActors";
  public static final String LIFE_EVENT_TYPE_UNIQUE_ATTR = "unique";
  public static final String DISAMBIGUATION_ID_TAG = "DisambiguationID";
  public static final String DISAMBIG_ID_VALUE_ATTR = "value";
  public static final String LIFE_STATUS_TAG = "LifeStatus";
  public static final String LIFE_STATUS_ORDINAL_ATTR = "ordinal";
  public static final String LEGAL_LAST_NAME_TAG = "LegalLastName";
  public static final String LEGAL_FIRST_NAMES_TAG = "LegalFirstNames";
  public static final String PUBLIC_LAST_NAME_TAG = "PublicLastName";
  public static final String PUBLIC_FIRST_NAMES_TAG = "PublicFirstNames";
  public static final String NICKNAMES_TAG = "Nicknames";
  public static final String NAME_TAG = "Name";
  public static final String NAME_VALUE_ATTR = "value";
  public static final String MAIN_OCCUPATION_TAG = "MainOccupation";
  public static final String MAIN_OCCUPATION_VALUE_ATTR = "value";
  public static final String GENDER_TAG = "Gender";
  public static final String GENDER_KEY_ATTR = "key";
  public static final String PARENTS_TAG = "Parents";
  public static final String PARENT_ID_1_ATTR = "id1";
  public static final String PARENT_ID_2_ATTR = "id2";
  public static final String RELATIVES_TAG = "Relatives";
  public static final String GROUP_TAG = "Group";
  public static final String GROUP_ORDINAL_ATTR = "ordinal";
  public static final String RELATIVE_TAG = "Relative";
  public static final String RELATIVE_ID_ATTR = "id";
  public static final String NOTES_TAG = "Notes";
  public static final String SOURCES_TAG = "Sources";
  public static final String LIFE_EVENT_TAG = "LifeEvent";
  public static final String DATE_TAG = "Date";
  public static final String DATE_TYPE_ATTR = "type";
  public static final String DATE_PRECISION_ATTR = "precision";
  public static final String DATE_DATE_ATTR = "date";
  public static final String DATE_START_ATTR = "start";
  public static final String DATE_END_ATTR = "end";
  public static final String DATE_EARLIEST_ATTR = "earliest";
  public static final String DATE_LATEST_ATTR = "latest";
  public static final String TYPE_TAG = "Type";
  public static final String TYPE_KEY_ATTR = "key";
  public static final String PLACE_TAG = "Place";
  public static final String PLACE_VALUE_ATTR = "value";
  public static final String ACTORS_TAG = "Actors";
  public static final String WITNESSES_TAG = "Witnesses";
  public static final String PERSON_TAG = "Person";
  public static final String PERSON_ID_ATTR = "id";

  public static final String DATE_WITH_PRECISION = "with_precision";
  public static final String DATE_RANGE = "range";
  public static final String DATE_ALTERNATIVE = "alternative";

  /**
   * Current version of tree files.
   */
  public static final int VERSION = 1;
  /**
   * Tree files’ extension.
   */
  public static final String EXTENSION = ".jtree";

  private final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

  public TreeFileManager() {
    this.documentBuilderFactory.setIgnoringComments(true);
  }

  /**
   * Return a new document builder.
   */
  protected DocumentBuilder newDocumentBuilder() {
    try {
      return this.documentBuilderFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e); // Should never happen
    }
  }
}
