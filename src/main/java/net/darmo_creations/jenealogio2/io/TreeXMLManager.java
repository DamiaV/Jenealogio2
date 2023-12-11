package net.darmo_creations.jenealogio2.io;

import net.darmo_creations.jenealogio2.model.*;
import org.jetbrains.annotations.*;

import javax.xml.parsers.*;
import java.util.*;

/**
 * Base class for tree XML serializers.
 */
public abstract class TreeXMLManager {
  // Tags and attributes names
  protected static final String FAMILY_TREE_TAG = "FamilyTree";
  protected static final String FAMILY_TREE_VERSION_ATTR = "version";
  protected static final String REGISTRIES_VERSION_ATTR = "version";
  protected static final String FAMILY_TREE_NAME_ATTR = "name";
  protected static final String FAMILY_TREE_ROOT_ATTR = "root";
  protected static final String REGISTRIES_TAG = "Registries";
  protected static final String PEOPLE_TAG = "People";
  protected static final String LIFE_EVENTS_TAG = "LifeEvents";
  protected static final String PICTURES_TAG = "Pictures";
  protected static final String GENDERS_TAG = "Genders";
  protected static final String REGISTRY_ENTRY_TAG = "Entry";
  protected static final String REGISTRY_ENTRY_KEY_ATTR = "key";
  protected static final String REGISTRY_ENTRY_LABEL_ATTR = "label";
  protected static final String GENDER_COLOR_ATTR = "color";
  protected static final String LIFE_EVENT_TYPES_TAG = "LifeEventTypes";
  protected static final String LIFE_EVENT_TYPE_GROUP_ATTR = "group";
  protected static final String LIFE_EVENT_TYPE_INDICATES_DEATH_ATTR = "indicatesDeath";
  protected static final String LIFE_EVENT_TYPE_INDICATES_UNION_ATTR = "indicatesUnion";
  protected static final String LIFE_EVENT_TYPE_ACTORS_NB_ATTR = "actorsNumber";
  protected static final String LIFE_EVENT_TYPE_UNIQUE_ATTR = "unique";
  protected static final String DISAMBIGUATION_ID_TAG = "DisambiguationID";
  protected static final String DISAMBIG_ID_VALUE_ATTR = "value";
  protected static final String LIFE_STATUS_TAG = "LifeStatus";
  protected static final String LIFE_STATUS_ORDINAL_ATTR = "ordinal";
  protected static final String LEGAL_LAST_NAME_TAG = "LegalLastName";
  protected static final String LEGAL_FIRST_NAMES_TAG = "LegalFirstNames";
  protected static final String PUBLIC_LAST_NAME_TAG = "PublicLastName";
  protected static final String PUBLIC_FIRST_NAMES_TAG = "PublicFirstNames";
  protected static final String NICKNAMES_TAG = "Nicknames";
  protected static final String NAME_TAG = "Name";
  protected static final String NAME_VALUE_ATTR = "value";
  protected static final String MAIN_OCCUPATION_TAG = "MainOccupation";
  protected static final String MAIN_OCCUPATION_VALUE_ATTR = "value";
  protected static final String GENDER_TAG = "Gender";
  protected static final String GENDER_KEY_ATTR = "key";
  protected static final String PARENTS_TAG = "Parents";
  protected static final String PARENT_ID_1_ATTR = "id1";
  protected static final String PARENT_ID_2_ATTR = "id2";
  protected static final String RELATIVES_TAG = "Relatives";
  protected static final String GROUP_TAG = "Group";
  protected static final String GROUP_ORDINAL_ATTR = "ordinal";
  protected static final String RELATIVE_TAG = "Relative";
  protected static final String RELATIVE_ID_ATTR = "id";
  protected static final String NOTES_TAG = "Notes";
  protected static final String SOURCES_TAG = "Sources";
  protected static final String LIFE_EVENT_TAG = "LifeEvent";
  protected static final String DATE_TAG = "Date";
  protected static final String DATE_TYPE_ATTR = "type";
  protected static final String DATE_PRECISION_ATTR = "precision";
  protected static final String DATE_DATE_ATTR = "date";
  protected static final String DATE_START_ATTR = "start";
  protected static final String DATE_END_ATTR = "end";
  protected static final String DATE_EARLIEST_ATTR = "earliest";
  protected static final String DATE_LATEST_ATTR = "latest";
  protected static final String TYPE_TAG = "Type";
  protected static final String TYPE_KEY_ATTR = "key";
  protected static final String PLACE_TAG = "Place";
  protected static final String PLACE_VALUE_ATTR = "value";
  protected static final String ACTORS_TAG = "Actors";
  protected static final String WITNESSES_TAG = "Witnesses";
  protected static final String PERSON_TAG = "Person";
  protected static final String PERSON_ID_ATTR = "id";
  protected static final String PICTURE_TAG = "Picture";
  protected static final String PICTURE_NAME_ATTR = "name";
  protected static final String PICTURE_MAIN_ATTR = "main";
  protected static final String PICTURE_DESC_TAG = "Description";

  protected static final String DATE_WITH_PRECISION = "with_precision";
  protected static final String DATE_RANGE = "range";
  protected static final String DATE_ALTERNATIVE = "alternative";

  /**
   * Current version of tree/registries files.
   */
  public static final int VERSION = 1;
  /**
   * Registries files extension.
   */
  public static final String REG_FILE_EXTENSION = ".jtreereg";

  private final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

  public TreeXMLManager() {
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

  public record RegistriesWrapper(
      @NotNull List<LifeEventType> lifeEventTypes,
      @NotNull List<Gender> genders
  ) {
    public RegistriesWrapper {
      Objects.requireNonNull(lifeEventTypes);
      Objects.requireNonNull(genders);
    }
  }
}
