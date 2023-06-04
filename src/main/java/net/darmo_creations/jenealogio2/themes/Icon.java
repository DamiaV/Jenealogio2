package net.darmo_creations.jenealogio2.themes;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Enumeration of all icons used throughout the app.
 */
public enum Icon {
  NEW_FILE("chart_organisation_add"),
  OPEN_FILE("folder_vertical_open"),
  SAVE("diskette"),
  SAVE_AS("save_as"),
  SETTINGS("cog"),
  QUIT("door_in"),

  UNDO("undo"),
  REDO("redo"),
  ADD_PERSON("user_add"),
  EDIT_PERSON("user_edit"),
  REMOVE_PERSON("user_delete"),
  ADD_CHILD("kids_add"),
  ADD_SIBLING("users_3_add"),
  EDIT_PARENTS("group_edit"),
  EDIT_PARTNERS("heart_edit"),
  SET_PICTURE("camera_edit"),

  CALCULATE_RELATIONSHIPS("link_go"),
  BIRTHDAYS("cake"),
  MAP("map"),
  CHECK_INCONSISTENCIES("error_go"),

  ABOUT("information"),

  HELP("help"),

  CLEAR_TEXT("draw_eraser"),
  SYNC_TREE("arrow_refresh"),

  ADD_EVENT("plus"),
  ADD_WITNESS("plus"),
  REMOVE_WITNESS("delete"),
  ;

  private final String baseName;

  Icon(@NotNull String baseName) {
    this.baseName = Objects.requireNonNull(baseName);
  }

  public String baseName() {
    return this.baseName;
  }

  public enum Size {
    SMALL(16),
    BIG(32),
    ;

    private final int pixels;

    Size(int pixels) {
      this.pixels = pixels;
    }

    public int pixels() {
      return this.pixels;
    }
  }
}
