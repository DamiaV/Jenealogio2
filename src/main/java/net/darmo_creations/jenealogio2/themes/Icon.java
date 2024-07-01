package net.darmo_creations.jenealogio2.themes;

import net.darmo_creations.jenealogio2.io.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Enumeration of all icons used throughout the app.
 */
public enum Icon {
  NEW_FILE("tree_add"),
  OPEN_TREE("folder_vertical_open"),
  MANAGE_TREES("tree_gear"),
  IMPORT_TREE_FILE("tree_down"),
  EXPORT_TREE_FILE("tree_up"),
  SAVE("diskette"),
  SETTINGS("cog"),
  QUIT("door_in"),

  UNDO("undo"),
  REDO("redo"),
  PERSON_BACK("user_back"),
  PERSON_NEXT("user_go"),
  EDIT_REGISTRIES("tag_blue_edit"),
  EDIT_TREE_DOCUMENTS("page_white_stack_edit"),
  RENAME_TREE("tree_textfield"),
  SET_AS_ROOT("anchor"),
  ADD_PERSON("user_add"),
  EDIT_PERSON("user_edit"),
  REMOVE_PERSON("user_delete"),
  ADD_CHILD("kids_add"),
  ADD_SIBLING("users_3_add"),
  EDIT_PARENTS("group_edit"),
  EDIT_LIFE_EVENTS("time_edit"),
  EDIT_DOCUMENTS("page_white_stack_edit"),

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

  SHOW_TREE_IN_EXPLORER("folder_vertical_open"),
  DELETE_TREE("cross"),

  ADD_EVENT("plus"),
  DELETE_EVENT("cross"),
  ADD_WITNESS("plus"),
  REMOVE_WITNESS("minus"),
  EDIT_PARTNER("pencil"),
  GET_LATLON("download_cloud"),
  OPEN_LATLON_SELECTOR("map_pins"),
  LOADING("arrow_refresh"),

  EDIT_PARENT("pencil"),
  SWAP_PARENTS("arrow_refresh"),

  ADD_PARENT("plus"),
  REMOVE_PARENT("minus"),

  REMOVE_MAIN_IMAGE("arrow_down"),
  ADD_DOCUMENT("plus"),
  SET_AS_MAIN_IMAGE("arrow_up"),
  REMOVE_DOCUMENT("minus"),
  DELETE_DOCUMENT("cross"),
  EDIT_DOCUMENT_DESC("pencil"),

  IMPORT_FILE("folder_page_white"),

  TREE_ROOT("anchor"),
  MORE("plus"),

  GO_TO("arrow_right"),
  PLUS("plus"),
  CLOSE_LIFE_EVENT("arrow_left"),

  UNCERTAIN("help"),

  IMPORT_REGISTRIES("tag_blue_down"),
  IMPORT_REGISTRIES_FROM_TREE("tree_down"),
  EXPORT_REGISTRIES("tag_blue_up"),
  EDIT_ICON("picture_edit"),

  ADD_ENTRY("plus"),
  DELETE_ENTRY("cross"),

  INFO("information"),

  SEARCH("map_magnify"),
  BULLET("bullet_black"),

  PROFILE_TAB("user_silhouette"),
  LIFE_EVENTS_TAB("clock"),
  FAMILY_TAB("users_5"),
  FOSTER_PARENTS_TAB("users_3"),

  SHOW_FILE_IN_EXPLORER("folder_vertical_open"),

  SELECT_DATE("date_edit"),
  CLEAR_DATE("draw_eraser"),

  ADD_DATE("date_add"),
  REMOVE_DATE("date_delete"),

  WARNING("error"),

  // File extensions
  UNKNOWN_FILE_EXT("page_white"),
  FILE_EXT_SWF("file_extension_swf"),
  FILE_EXT_PDF("file_extension_pdf"),
  FILE_EXT_SES("file_extension_ses"),
  FILE_EXT_MCD("file_extension_mcd"),
  FILE_EXT_QBB("file_extension_qbb"),
  FILE_EXT_M4P("file_extension_m4p"),
  FILE_EXT_SEA("file_extension_sea"),
  FILE_EXT_AIF("file_extension_aif"),
  FILE_EXT_MID("file_extension_mid"),
  FILE_EXT_MSI("file_extension_msi"),
  FILE_EXT_M4V("file_extension_m4v"),
  FILE_EXT_TORRENT("file_extension_torrent"),
  FILE_EXT_ACE("file_extension_ace"),
  FILE_EXT_THM("file_extension_thm"),
  FILE_EXT_VCD("file_extension_vcd"),
  FILE_EXT_CDR("file_extension_cdr"),
  FILE_EXT_JPEG("file_extension_jpeg"),
  FILE_EXT_HQX("file_extension_hqx"),
  FILE_EXT_7Z("file_extension_7z"),
  FILE_EXT_MP2("file_extension_mp2"),
  FILE_EXT_XLS("file_extension_xls"),
  FILE_EXT_LNK("file_extension_lnk"),
  FILE_EXT_M4A("file_extension_m4a"),
  FILE_EXT_IFO("file_extension_ifo"),
  FILE_EXT_3GP("file_extension_3gp"),
  FILE_EXT_MDB("file_extension_mdb"),
  FILE_EXT_MP4("file_extension_mp4"),
  FILE_EXT_DOC("file_extension_doc"),
  FILE_EXT_MPEG("file_extension_mpeg"),
  FILE_EXT_BMP("file_extension_bmp"),
  FILE_EXT_JPG("file_extension_jpg"),
  FILE_EXT_CHM("file_extension_chm"),
  FILE_EXT_AI("file_extension_ai"),
  FILE_EXT_DLL("file_extension_dll"),
  FILE_EXT_FLA("file_extension_fla"),
  FILE_EXT_GZ("file_extension_gz"),
  FILE_EXT_XPI("file_extension_xpi"),
  FILE_EXT_TGZ("file_extension_tgz"),
  FILE_EXT_CBR("file_extension_cbr"),
  FILE_EXT_HTM("file_extension_htm"),
  FILE_EXT_MPG("file_extension_mpg"),
  FILE_EXT_CAB("file_extension_cab"),
  FILE_EXT_AMR("file_extension_amr"),
  FILE_EXT_TMP("file_extension_tmp"),
  FILE_EXT_DSS("file_extension_dss"),
  FILE_EXT_PTB("file_extension_ptb"),
  FILE_EXT_ZIP("file_extension_zip"),
  FILE_EXT_HTML("file_extension_html"),
  FILE_EXT_RAR("file_extension_rar"),
  FILE_EXT_ISO("file_extension_iso"),
  FILE_EXT_RMVB("file_extension_rmvb"),
  FILE_EXT_WMV("file_extension_wmv"),
  FILE_EXT_DIVX("file_extension_divx"),
  FILE_EXT_RTF("file_extension_rtf"),
  FILE_EXT_LOG("file_extension_log"),
  FILE_EXT_EML("file_extension_eml"),
  FILE_EXT_DWG("file_extension_dwg"),
  FILE_EXT_PUB("file_extension_pub"),
  FILE_EXT_OGG("file_extension_ogg"),
  FILE_EXT_SS("file_extension_ss"),
  FILE_EXT_ASX("file_extension_asx"),
  FILE_EXT_SITX("file_extension_sitx"),
  FILE_EXT_EXE("file_extension_exe"),
  FILE_EXT_INDD("file_extension_indd"),
  FILE_EXT_QBW("file_extension_qbw"),
  FILE_EXT_BUP("file_extension_bup"),
  FILE_EXT_WPS("file_extension_wps"),
  FILE_EXT_BAT("file_extension_bat"),
  FILE_EXT_PNG("file_extension_png"),
  FILE_EXT_TTF("file_extension_ttf"),
  FILE_EXT_MSWMM("file_extension_mswmm"),
  FILE_EXT_ASF("file_extension_asf"),
  FILE_EXT_GIF("file_extension_gif"),
  FILE_EXT_PS("file_extension_ps"),
  FILE_EXT_MOV("file_extension_mov"),
  FILE_EXT_M4B("file_extension_m4b"),
  FILE_EXT_DVF("file_extension_dvf"),
  FILE_EXT_FLV("file_extension_flv"),
  FILE_EXT_AIFF("file_extension_aiff"),
  FILE_EXT_DMG("file_extension_dmg"),
  FILE_EXT_PSD("file_extension_psd"),
  FILE_EXT_WAV("file_extension_wav"),
  FILE_EXT_DAT("file_extension_dat"),
  FILE_EXT_PST("file_extension_pst"),
  FILE_EXT_TIF("file_extension_tif"),
  FILE_EXT_TXT("file_extension_txt"),
  FILE_EXT_RAM("file_extension_ram"),
  FILE_EXT_SIT("file_extension_sit"),
  FILE_EXT_QXD("file_extension_qxd"),
  FILE_EXT_VOB("file_extension_vob"),
  FILE_EXT_CDA("file_extension_cda"),
  FILE_EXT_RM("file_extension_rm"),
  FILE_EXT_EPS("file_extension_eps"),
  FILE_EXT_JAR("file_extension_jar"),
  FILE_EXT_PPS("file_extension_pps"),
  FILE_EXT_WMA("file_extension_wma"),
  FILE_EXT_CDL("file_extension_cdl"),
  FILE_EXT_BIN("file_extension_bin"),
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
   * Return the file extension {@link Icon} that corresponds to the extension of the given file name.
   *
   * @param name A file name.
   * @return The matching file extension icon or {@link #UNKNOWN_FILE_EXT} if none matched.
   */
  public static Icon forFile(@NotNull String name) {
    final Optional<String> ext = FileUtils.splitExtension(name).extension();
    if (ext.isEmpty())
      return UNKNOWN_FILE_EXT;
    final String iconName = "FILE_EXT_" + ext.get().substring(1).toUpperCase();
    for (final Icon icon : values())
      if (icon.name().equals(iconName))
        return icon;
    return UNKNOWN_FILE_EXT;
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
