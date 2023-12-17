package net.darmo_creations.jenealogio2.themes;

import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Enumeration of all icons used throughout the app.
 */
public enum Icon {
  NEW_FILE("tree_add"),
  OPEN_FILE("folder_vertical_open"),
  SAVE("diskette"),
  SAVE_AS("save_as"),
  SETTINGS("cog"),
  QUIT("door_in"),

  UNDO("undo"),
  REDO("redo"),
  EDIT_REGISTRIES("tag_blue_edit"),
  RENAME_TREE("tree_textfield"),
  SET_AS_ROOT("anchor"),
  ADD_PERSON("user_add"),
  EDIT_PERSON("user_edit"),
  REMOVE_PERSON("user_delete"),
  ADD_CHILD("kids_add"),
  ADD_SIBLING("users_3_add"),
  EDIT_PARENTS("group_edit"),
  EDIT_LIFE_EVENTS("time_edit"),
  SET_PICTURE("camera_edit"),

  CALCULATE_RELATIONSHIPS("link_go"),
  BIRTHDAYS("cake"),
  MAP("map"),
  CHECK_INCONSISTENCIES("error_go"),

  ABOUT("information"),

  HELP("help"),

  CLEAR_TEXT("draw_eraser"),
  SYNC_TREE("arrow_refresh"),

  BIRTH("emotion_baby"),
  DEATH("emotion_flower_dead"),

  ADD_EVENT("plus"),
  DELETE_EVENT("cross"),
  ADD_WITNESS("plus"),
  REMOVE_WITNESS("delete"),

  ADD_PARENT("plus"),
  REMOVE_PARENT("delete"),

  REMOVE_MAIN_IMAGE("arrow_down"),
  ADD_IMAGE("plus"),
  SET_AS_MAIN_IMAGE("arrow_up"),
  REMOVE_IMAGE("delete"),
  EDIT_IMAGE_DESC("pencil"),

  OPEN_IMAGE_FILE("folder_picture"),

  TREE_ROOT("anchor"),
  MORE("plus"),

  GO_TO("arrow_right"),
  CLOSE_LIFE_EVENT("arrow_left"),

  UNCERTAIN("help"),

  IMPORT_REGISTRIES("tag_blue_down"),
  IMPORT_REGISTRIES_FROM_TREE("tree_down"),
  EXPORT_REGISTRIES("tag_blue_up"),

  ADD_ENTRY("plus"),
  DELETE_ENTRY("cross"),
  RESET_ENTRY("undo"),

  INFO("information"),

  PROFILE_TAB("user_silhouette"),
  LIFE_EVENTS_TAB("clock"),
  FAMILY_TAB("users_5"),
  FOSTER_PARENTS_TAB("users_3"),
  IMAGES_TAB("pictures"),
  ;

  private final String baseName;

  Icon(@NotNull String baseName) {
    this.baseName = Objects.requireNonNull(baseName);
  }

  /**
   * The base name of this icon.
   */
  public String baseName() {
    return this.baseName;
  }

  /**
   * Enumeration of possible icon sizes.
   */
  public enum Size {
    /**
     * 16x16 pixels size.
     */
    SMALL(16),
    /**
     * 32x32 pixels size.
     */
    BIG(32),
    ;

    private final int pixels;

    Size(int pixels) {
      this.pixels = pixels;
    }

    /**
     * The width/height in pixels.
     */
    public int pixels() {
      return this.pixels;
    }
  }
}
