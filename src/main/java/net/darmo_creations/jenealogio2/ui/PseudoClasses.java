package net.darmo_creations.jenealogio2.ui;

import javafx.css.*;
import javafx.scene.control.*;
import net.darmo_creations.jenealogio2.ui.components.*;

/**
 * This class declares CSS pseudo-classes used throughout the app.
 */
public final class PseudoClasses {
  /**
   * Marks a form component as containing invalid data.
   */
  public static final PseudoClass INVALID = PseudoClass.getPseudoClass("invalid");
  /**
   * Marks an item within the {@link FamilyMembersTreeView} as matching the search filter.
   */
  public static final PseudoClass SEARCH_MATCH = PseudoClass.getPseudoClass("search-match");
  /**
   * Marks a {@link PersonWidget} as being selected.
   */
  public static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");
  /**
   * Marks a {@link TableCell} as being disabled.
   */
  public static final PseudoClass DISABLED = PseudoClass.getPseudoClass("disabled");

  private PseudoClasses() {
  }
}
