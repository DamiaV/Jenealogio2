package net.darmo_creations.jenealogio2.model;

import javafx.scene.image.*;
import net.darmo_creations.jenealogio2.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.util.*;

/**
 * This class represents the gender of a {@link Person}.
 */
public final class Gender extends RegistryEntry {
  private Image icon;

  /**
   * Create a new gender object.
   *
   * @param key             Gender’s registry key.
   * @param userDefinedName Entry’s display text if not builtin.
   * @param icon            Gender’s icon if not builtin.
   */
  Gender(@NotNull RegistryEntryKey key, String userDefinedName, Image icon) {
    super(key, userDefinedName);
    this.icon = key.isBuiltin() ? this.loadIcon() : Objects.requireNonNull(icon);
  }

  private @Nullable Image loadIcon() {
    String iconName = this.key().name();
    String path = "%s%s.png".formatted(App.IMAGES_PATH + "gender_icons/", iconName);
    try (var stream = Gender.class.getResourceAsStream(path)) {
      if (stream == null) {
        App.LOGGER.warn("Missing icon: " + iconName);
        return null;
      }
      return new Image(stream);
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * This gender’s icon.
   */
  public Image icon() {
    return this.icon;
  }

  /**
   * Set this gender’s icon.
   *
   * @param icon The new icon.
   * @throws UnsupportedOperationException If this entry is built-in.
   */
  public void setIcon(@NotNull Image icon) {
    this.ensureNotBuiltin("icon");
    this.icon = Objects.requireNonNull(icon);
  }

  @Override
  public String toString() {
    return "Gender{key=%s}".formatted(this.key());
  }
}
