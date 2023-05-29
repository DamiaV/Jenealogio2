package net.darmo_creations.jenealogio2.model;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Registry<E extends RegistryEntry, A> {
  public static final String BUILTIN_NS = "builtin";
  public static final String USER_NS = "user";

  private final String name;
  private final Map<RegistryEntryKey, E> entries = new HashMap<>();
  private final List<E> defaults;
  private final EntryFactory<E, A> entryFactory;

  // TODO use vararg for defaults
  Registry(@NotNull String name, final @NotNull List<BuiltinEntry<A>> defaults, @NotNull EntryFactory<E, A> entryFactory) {
    this.name = Objects.requireNonNull(name);
    this.entryFactory = Objects.requireNonNull(entryFactory);
    this.defaults = defaults.stream()
        .map(e -> this.registerEntry(new RegistryEntryKey(BUILTIN_NS, e.name()), e.args(), true))
        .toList();
  }

  /**
   * The name of this registry.
   */
  public String name() {
    return this.name;
  }

  /**
   * A list of this registry’s entries, in no particular order.
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
   * @param key  Key of the entry to register.
   * @param args Additional arguments to pass to the new entry’s constructor.
   * @return The newly created instance.
   * @throws IllegalArgumentException If the specified key is already used
   *                                  or the key is in the builtin namespace.
   */
  public E registerEntry(@NotNull RegistryEntryKey key, A args) {
    return this.registerEntry(key, args, false);
  }

  /**
   * Register a new entry.
   *
   * @param key  Key of the entry to register.
   * @param args Additional arguments to pass to the new entry’s constructor.
   * @return The newly created instance.
   * @throws IllegalArgumentException If the specified key is already used
   *                                  or allowBuiltin and the key is in the builtin namespace.
   */
  private E registerEntry(@NotNull RegistryEntryKey key, A args, boolean allowBuiltin) {
    Objects.requireNonNull(this.name);
    if (!allowBuiltin && key.namespace().equals(BUILTIN_NS)) {
      throw new IllegalArgumentException("cannot register entries in the '%s' namespace.".formatted(BUILTIN_NS));
    }
    if (this.entries.containsKey(key)) {
      throw new IllegalArgumentException("key '%s' already exists".formatted(key));
    }
    E entry = this.entryFactory.apply(key, args);
    this.entries.put(key, entry);
    return entry;
  }

  /**
   * Wrapper class to provide builtin entries when creating a registry.
   *
   * @param name Entry’s internal name.
   * @param args Entry’s additonal arguments.
   */
  public record BuiltinEntry<A>(@NotNull String name, A args) {
    public BuiltinEntry {
      Objects.requireNonNull(name);
    }

    public BuiltinEntry(@NotNull String name) {
      this(name, null);
    }
  }

  @FunctionalInterface
  public interface EntryFactory<E extends RegistryEntry, A> {
    E apply(@NotNull RegistryEntryKey key, A args);
  }
}
