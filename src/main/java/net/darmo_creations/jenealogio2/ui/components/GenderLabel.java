package net.darmo_creations.jenealogio2.ui.components;

import javafx.scene.control.*;
import javafx.scene.image.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class GenderLabel extends Label {
  private final Config config;
  private final boolean showText;

  public GenderLabel(Gender gender, boolean showText, @NotNull Config config) {
    this.showText = showText;
    this.config = Objects.requireNonNull(config);
    this.setGender(gender);
  }

  public void setGender(Gender gender) {
    String text = null;
    if (gender != null) {
      final RegistryEntryKey key = gender.key();
      if (key.isBuiltin())
        text = this.config.language().translate("genders." + key.name());
      else
        text = gender.userDefinedName();
    }
    if (this.showText)
      this.setText(text);
    else
      this.setTooltip(text != null ? new Tooltip(text) : null);
    this.setGraphic(gender != null ? new ImageView(gender.icon()) : null);
  }
}
