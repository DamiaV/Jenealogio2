package net.darmo_creations.jenealogio2.io;

import net.darmo_creations.jenealogio2.model.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * This class contains values from {@link LifeEventTypeRegistry}
 * and {@link GenderRegistry} for import/export purposes.
 */
public final class RegistriesValues {
  private final List<LifeEventType> lifeEventTypes;
  private final List<RegistryEntryKey> lifeEventTypeKeys;
  private final List<Gender> genders;
  private final List<RegistryEntryKey> genderKeys;

  public RegistriesValues(final @NotNull List<LifeEventType> lifeEventTypes, final @NotNull List<Gender> genders) {
    this.lifeEventTypes = Collections.unmodifiableList(lifeEventTypes);
    this.lifeEventTypeKeys = lifeEventTypes.stream().map(RegistryEntry::key).toList();
    this.genders = Collections.unmodifiableList(genders);
    this.genderKeys = genders.stream().map(RegistryEntry::key).toList();
  }

  public List<LifeEventType> lifeEventTypes() {
    return this.lifeEventTypes;
  }

  public List<RegistryEntryKey> lifeEventTypeKeys() {
    return this.lifeEventTypeKeys;
  }

  public List<Gender> genders() {
    return this.genders;
  }

  public List<RegistryEntryKey> genderKeys() {
    return this.genderKeys;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this)
      return true;
    if (obj == null || obj.getClass() != this.getClass())
      return false;
    final RegistriesValues that = (RegistriesValues) obj;
    return Objects.equals(this.lifeEventTypes, that.lifeEventTypes)
           && Objects.equals(this.genders, that.genders);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.lifeEventTypes, this.genders);
  }

  @Override
  public String toString() {
    return "RegistriesValues[lifeEventTypes=%s, genders=%s]".formatted(this.lifeEventTypes, this.genders);
  }
}
