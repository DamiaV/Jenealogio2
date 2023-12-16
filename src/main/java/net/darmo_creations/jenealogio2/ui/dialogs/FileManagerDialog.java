package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.scene.control.*;
import net.darmo_creations.jenealogio2.model.*;

/**
 * This dialog allows managing of all files for the given {@link FamilyTree}.
 */
public class FileManagerDialog extends DialogBase<ButtonType> {
  public FileManagerDialog() {
    super("file_manager", true, ButtonTypes.CLOSE);
  }

  /*
   * Available I18N strings:
   * alert.delete_images.title
   * alert.delete_images.header
   * alert.delete_images.content
   */
}
