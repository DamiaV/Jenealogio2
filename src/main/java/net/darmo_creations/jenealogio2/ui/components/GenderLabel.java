package net.darmo_creations.jenealogio2.ui.components;

import javafx.scene.control.*;
import javafx.scene.image.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * This label shows the icon and localized name of the specified {@link Gender} object.
 */
public class GenderLabel extends Label {
  private final Config config;
  private final boolean showText;

  /**
   * Create a new gender label.
   *
   * @param gender   The gender to show. If null, nothing will be shown.
   * @param showText If true, the localized gender name will be shown; if false, only the icon will be visible.
   * @param config   The app’s current config.
   */
  public GenderLabel(Gender gender, boolean showText, final @NotNull Config config) {
    this.showText = showText;
    this.config = Objects.requireNonNull(config);
    this.setGender(gender);
  }

  /**
   * Set the gender to show.
   *
   * @param gender The gender to show. If null, nothing will be shown.
   */
  public void setGender(Gender gender) {
    String text = null;
    if (gender != null) {
      final RegistryEntryKey key = gender.key();
      if (key.isBuiltin())
        text = this.config.language().translate("genders." + key.name());
      else
        text = gender.userDefinedName();
    }
    if (this.showText) this.setText(text);
    else this.setTooltip(text != null ? new Tooltip(text) : null);
    this.setGraphic(gender != null ? new ImageView(gender.icon()) : null);
  }
}
