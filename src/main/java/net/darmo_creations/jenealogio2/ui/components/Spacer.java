package net.darmo_creations.jenealogio2.ui.components;

import javafx.geometry.*;
import javafx.scene.layout.*;
import org.jetbrains.annotations.*;

/**
 * An empty component that fills as much space as available in the given orientation.
 */
public class Spacer extends Pane {
  /**
   * Create a spacer for the given orientation.
   *
   * @param orientation The spacer’s orientation.
   */
  public Spacer(@NotNull Orientation orientation) {
    switch (orientation) {
      case HORIZONTAL -> HBox.setHgrow(this, Priority.ALWAYS);
      case VERTICAL -> VBox.setVgrow(this, Priority.ALWAYS);
    }
  }
}
