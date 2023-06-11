package net.darmo_creations.jenealogio2.themes;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

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

  MORE("plus"),

  GO_TO("arrow_right"),
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
