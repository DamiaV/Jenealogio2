package net.darmo_creations.jenealogio2.model;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Registry<E extends RegistryEntry, A> {
  private final String name;
  private final Map<Integer, E> entries = new HashMap<>();
  private final List<E> defaults;
  private final int customIDsStart;
  private int customGlobalID;
  private final EntryFactory<E, A> entryFactory;

  Registry(String name, final @NotNull List<BuiltinEntry<A>> defaults, int customIDsStart, @NotNull EntryFactory<E, A> entryFactory) {
    this.name = name;
    this.defaults = defaults.stream().map(e -> entryFactory.apply(e.id(), e.name(), true, e.args())).toList();
    this.customIDsStart = customIDsStart;
    this.customGlobalID = customIDsStart;
    this.entryFactory = Objects.requireNonNull(entryFactory);
  }

  /**
   * The name of this registry.
   */
  public String name() {
    return this.name;
  }

  /**
   * The first ID to use for user-defined entries.
   */
  public int customIDsStart() {
    return this.customIDsStart;
  }

  /**
   * A list of this registry’s entries, in no particular order.
   */
  public List<E> entries() {
    return new LinkedList<>(this.entries.values());
  }

  /**
   * Return the entry with the given ID.
   *
   * @param id ID of the entry to get.
   * @return The entry or null if none matched the ID.
   */
  public E getEntry(int id) {
    return this.entries.get(id);
  }

  /**
   * Indicate whether this registry has the given entry ID.
   *
   * @param id ID to check.
   * @return True if the ID is already registered, false otherwise.
   */
  public boolean hasEntryID(int id) {
    return this.entries.containsKey(id);
  }

  /**
   * Reset this registry, i.e. empty it and fill it back with default entries only.
   */
  public void reset() {
    this.entries.clear();
    this.defaults.forEach(g -> this.entries.put(g.id(), g));
    this.customGlobalID = this.customIDsStart;
  }

  /**
   * Create a new user-defined entry with an auto-generated ID.
   *
   * @param name Name of the entry to create.
   * @param args Additional arguments to pass to the entry factory.
   * @return The newly created instance.
   */
  public E createEntry(@NotNull String name, A args) {
    return this.createEntry(this.customGlobalID++, name, args);
  }

  /**
   * Register a user-defined entry.
   *
   * @param id   ID of the entry to register.
   * @param name Name of the entry to register.
   * @param args Additional arguments to pass to the entry factory.
   * @return The newly created instance.
   * @throws IllegalArgumentException If the specified ID is already used or is less than {@link #customIDsStart()}.
   */
  public E createEntry(int id, @NotNull String name, A args) {
    Objects.requireNonNull(name);
    if (id < this.customIDsStart || this.entries.containsKey(id)) {
      throw new IllegalArgumentException("ID %d already exists".formatted(id));
    }
    // Update to avoid conflicts when calling createCustom()
    this.customGlobalID = Math.max(id, this.customGlobalID);
    E entry = this.entryFactory.apply(id, name, false, args);
    this.entries.put(entry.id(), entry);
    return entry;
  }

  /**
   * Wrapper class to provide builtin entries when creating a registry.
   *
   * @param id   Entry’s ID.
   * @param name Entry’s name.
   * @param args Entry’s additonal arguments.
   */
  public record BuiltinEntry<A>(int id, @NotNull String name, A args) {
    public BuiltinEntry {
      Objects.requireNonNull(name);
    }

    public BuiltinEntry(int id, @NotNull String name) {
      this(id, name, null);
    }
  }

  @FunctionalInterface
  public interface EntryFactory<E extends RegistryEntry, A> {
    E apply(int id, @NotNull String name, boolean builtin, A args);
  }
}
