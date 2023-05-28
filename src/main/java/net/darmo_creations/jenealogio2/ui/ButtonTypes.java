package net.darmo_creations.jenealogio2.ui;

import javafx.scene.control.ButtonType;
import net.darmo_creations.jenealogio2.App;

import java.util.ResourceBundle;

@SuppressWarnings("unused")
public final class ButtonTypes {
  private static final ResourceBundle RES = App.getResourceBundle();

  /**
   * Localized version of {@link ButtonType#APPLY}.
   */
  public static final ButtonType APPLY = new ButtonType(
      RES.getString("dialog.apply.button"), ButtonType.APPLY.getButtonData());

  /**
   * Localized version of {@link ButtonType#OK}.
   */
  public static final ButtonType OK = new ButtonType(
      RES.getString("dialog.ok.button"), ButtonType.OK.getButtonData());

  /**
   * Localized version of {@link ButtonType#CANCEL}.
   */
  public static final ButtonType CANCEL = new ButtonType(
      RES.getString("dialog.cancel.button"), ButtonType.CANCEL.getButtonData());

  /**
   * Localized version of {@link ButtonType#CLOSE}.
   */
  public static final ButtonType CLOSE = new ButtonType(
      RES.getString("dialog.close.button"), ButtonType.CLOSE.getButtonData());

  /**
   * Localized version of {@link ButtonType#YES}.
   */
  public static final ButtonType YES = new ButtonType(
      RES.getString("dialog.yes.button"), ButtonType.YES.getButtonData());

  /**
   * Localized version of {@link ButtonType#NO}.
   */
  public static final ButtonType NO = new ButtonType(
      RES.getString("dialog.no.button"), ButtonType.NO.getButtonData());

  /**
   * Localized version of {@link ButtonType#FINISH}.
   */
  public static final ButtonType FINISH = new ButtonType(
      RES.getString("dialog.finish.button"), ButtonType.FINISH.getButtonData());

  /**
   * Localized version of {@link ButtonType#NEXT}.
   */
  public static final ButtonType NEXT = new ButtonType(
      RES.getString("dialog.next.button"), ButtonType.NEXT.getButtonData());

  /**
   * Localized version of {@link ButtonType#PREVIOUS}.
   */
  public static final ButtonType PREVIOUS = new ButtonType(
      RES.getString("dialog.previous.button"), ButtonType.PREVIOUS.getButtonData());

  private ButtonTypes() {
  }
}
