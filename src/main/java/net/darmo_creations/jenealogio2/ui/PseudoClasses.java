package net.darmo_creations.jenealogio2.ui;

import javafx.css.PseudoClass;
import javafx.scene.control.TableCell;
import net.darmo_creations.jenealogio2.ui.components.PersonWidget;

/**
 * This class declares CSS pseudo-classes used throughout the app.
 */
public final class PseudoClasses {
  /**
   * Marks a form component as containing invalid data.
   */
  public static final PseudoClass INVALID = PseudoClass.getPseudoClass("invalid");
  /**
   * Marks an item within the {@link FamilyTreeView} as matching the search filter.
   */
  public static final PseudoClass SEARCH_MATCH = PseudoClass.getPseudoClass("search-match");
  /**
   * Mark a {@link PersonWidget} as being selected.
   */
  public static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");
  /**
   * Mark a {@link TableCell} as being disabled.
   */
  public static final PseudoClass DISABLED = PseudoClass.getPseudoClass("disabled");

  private PseudoClasses() {
  }
}
