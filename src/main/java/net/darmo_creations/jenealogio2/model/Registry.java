package net.darmo_creations.jenealogio2.model;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A registry is a container that holds any number of {@link RegistryEntry} objects.
 * <p>
 * Two types of entries can be registered: builtin und user defined.
 *
 * @param <E> Type of registry entries.
 * @param <A> Type of arguments to pass to {@link #registerEntry(RegistryEntryKey, String, Object)}
 *            to build and register a new {@link RegistryEntry} object.
 */
public class Registry<E extends RegistryEntry, A> {
  public static final String BUILTIN_NS = "builtin";
  public static final String USER_NS = "user";

  private final String name;
  private final Map<RegistryEntryKey, E> entries = new HashMap<>();
  private final List<E> defaults;
  private final EntryFactory<E, A> entryFactory;

  /**
   * Create a new registry.
   *
   * @param name         Registry’s name.
   * @param entryFactory Function that takes a registry key and argument object to create a new {@link RegistryEntry}.
   * @param defaults     List of default entries’ data to be registered.
   */
  @SafeVarargs
  Registry(@NotNull String name, @NotNull EntryFactory<E, A> entryFactory, final @NotNull BuiltinEntry<A>... defaults) {
    this.name = Objects.requireNonNull(name);
    this.entryFactory = Objects.requireNonNull(entryFactory);
    this.defaults = Arrays.stream(defaults)
        .map(e -> this.registerEntry(new RegistryEntryKey(BUILTIN_NS, e.name()), null, e.args(), true))
        .toList();
  }

  /**
   * Name of this registry.
   */
  public String name() {
    return this.name;
  }

  /**
   * A copy of the list of this registry’s entries, in no particular order.
   */
  public List<E> entries() {
    return new LinkedList<>(this.entries.values());
  }

  /**
   * Return the entry with the given key.
   *
   * @param key Key of the entry to get.
   * @return The entry or null if none matched the key.
   */
  public E getEntry(@NotNull RegistryEntryKey key) {
    return this.entries.get(Objects.requireNonNull(key));
  }

  /**
   * Indicate whether this registry has the given entry key.
   *
   * @param key Key to check.
   * @return True if the key is already registered, false otherwise.
   */
  public boolean containsKey(@NotNull RegistryEntryKey key) {
    return this.entries.containsKey(Objects.requireNonNull(key));
  }

  /**
   * Reset this registry, i.e. empty it and fill it back with default entries only.
   */
  public void reset() {
    this.entries.clear();
    this.defaults.forEach(g -> this.entries.put(g.key(), g));
  }

  /**
   * Register a new user-defined entry.
   *
   * @param key   Key of the entry to register.
   * @param label Entry’s display text.
   * @param args  Additional arguments to pass to the new entry’s constructor.
   * @throws IllegalArgumentException If the specified key is already used
   *                                  or the key is in the builtin namespace.
   */
  public void registerEntry(@NotNull RegistryEntryKey key, @NotNull String label, A args) {
    this.registerEntry(key, Objects.requireNonNull(label), args, false);
  }

  /**
   * Register a new entry.
   *
   * @param key   Key of the entry to register.
   * @param label Entry’s display text if not builtin.
   * @param args  Additional arguments to pass to the new entry’s constructor.
   * @return The newly created instance.
   * @throws IllegalArgumentException If the specified key is already used
   *                                  or allowBuiltin and the key is in the builtin namespace.
   */
  private E registerEntry(@NotNull RegistryEntryKey key, String label, A args, boolean allowBuiltin) {
    Objects.requireNonNull(this.name);
    if (!allowBuiltin && key.isBuiltin()) {
      throw new IllegalArgumentException("cannot register entries in the '%s' namespace.".formatted(BUILTIN_NS));
    }
    if (this.entries.containsKey(key)) {
      throw new IllegalArgumentException("key '%s' already exists".formatted(key));
    }
    if (!key.isBuiltin() && label.isEmpty()) {
      throw new IllegalArgumentException("label is empty for non-builtin key '%s'".formatted(key));
    }
    E entry = this.entryFactory.apply(key, label, args);
    this.entries.put(key, entry);
    return entry;
  }

  /**
   * Delete the given entry.
   *
   * @param entry Entry to delete.
   * @throws IllegalArgumentException If the entry is in {@link #BUILTIN_NS}.
   */
  public void removeEntry(E entry) {
    if (entry.isBuiltin()) {
      throw new IllegalArgumentException("cannot delete builtin entry '%s'".formatted(entry.key()));
    }
    this.entries.remove(entry.key());
  }

  /**
   * Wrapper class to provide builtin entries when creating a registry.
   *
   * @param name Entry’s internal name.
   * @param args Entry’s additonal constructor arguments.
   */
  public record BuiltinEntry<A>(@NotNull String name, A args) {
    public BuiltinEntry {
      Objects.requireNonNull(name);
    }
  }

  /**
   * A factory that takes in a registry key and an object containing arguments
   * to be passed to the registry entry’s concreet constructor.
   *
   * @param <E> Type of registry entries.
   * @param <A> Type of arguments to pass to the registry entry’s concrete constructor.
   */
  @FunctionalInterface
  public interface EntryFactory<E extends RegistryEntry, A> {
    E apply(@NotNull RegistryEntryKey key, String label, A args);
  }
}
