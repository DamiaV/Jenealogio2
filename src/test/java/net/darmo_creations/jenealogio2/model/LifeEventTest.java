package net.darmo_creations.jenealogio2.model;

import net.darmo_creations.jenealogio2.model.datetime.*;
import net.darmo_creations.jenealogio2.model.datetime.calendar.Calendar;
import net.darmo_creations.jenealogio2.model.datetime.calendar.GregorianCalendar;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class LifeEventTest {
  private LifeEvent lifeEvent;
  private Person p1;
  private Person p2;

  @BeforeEach
  void setUp() {
    DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    this.lifeEvent = new LifeEvent(date, new LifeEventTypeRegistry().getEntry(new RegistryEntryKey("builtin:marriage")));
    this.p1 = new Person();
    this.p1.setLegalFirstNames(List.of("Alice")).setPublicLastName("Yo");
    this.p2 = new Person();
    this.p2.setLegalFirstNames(List.of("Bob")).setPublicLastName("Ya");
  }

  @Test
  void setActorsUpdatesEvent() {
    this.lifeEvent.setActors(Set.of(this.p1, this.p2));
    assertEquals(Set.of(this.p1, this.p2), this.lifeEvent.actors());
  }

  @Test
  void setActorsUpdatesNewActors() {
    this.lifeEvent.setActors(Set.of(this.p1, this.p2));
    assertSame(this.lifeEvent, this.p1.getLifeEventsAsActor().get(0));
    assertSame(this.lifeEvent, this.p2.getLifeEventsAsActor().get(0));
  }

  @Test
  void setActorsUpdatesPreviousActors() {
    Person p = new Person();
    this.lifeEvent.setActors(Set.of(p, this.p2));
    this.lifeEvent.setActors(Set.of(this.p1, this.p2));
    assertTrue(p.getLifeEventsAsActor().isEmpty());
  }

  @Test
  void setActorsThrowsIfTooFewActors() {
    assertThrows(IllegalArgumentException.class, () -> this.lifeEvent.setActors(Set.of(this.p1)));
  }

  @Test
  void setActorsThrowsIfTooManyActors() {
    assertThrows(IllegalArgumentException.class, () -> this.lifeEvent.setActors(Set.of(this.p1, this.p2, new Person())));
  }

  @Test
  void setActorsThrowsIfDuplicateUniqueEvents() {
    DateTime date1 = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    var birth1 = new LifeEvent(date1, new LifeEventTypeRegistry().getEntry(new RegistryEntryKey("builtin:birth")));
    DateTime date2 = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 2, 1, 0, 0), DateTimePrecision.EXACT);
    var birth2 = new LifeEvent(date2, new LifeEventTypeRegistry().getEntry(new RegistryEntryKey("builtin:birth")));
    birth1.setActors(Set.of(this.p1));
    assertThrows(IllegalArgumentException.class, () -> birth2.setActors(Set.of(this.p1)));
  }

  @Test
  void setActorsThrowsIfWitness() {
    this.lifeEvent.addWitness(this.p1);
    assertThrows(IllegalArgumentException.class, () -> this.lifeEvent.setActors(Set.of(this.p1, this.p2)));
  }

  @Test
  void removeActorUpdatesEvent() {
    var registry = new LifeEventTypeRegistry();
    var key = new RegistryEntryKey(Registry.USER_NS, "test");
    registry.registerEntry(key, "test", new LifeEventTypeRegistry.RegistryArgs(LifeEventType.Group.OTHER, false, false, 1, 2, false));
    LifeEventType type = registry.getEntry(key);
    DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    LifeEvent l = new LifeEvent(date, type);
    l.setActors(Set.of(this.p1, this.p2));
    l.removeActor(this.p1);
    assertEquals(Set.of(this.p2), l.actors());
  }

  @Test
  void removeActorUpdatesRemovedActor() {
    var registry = new LifeEventTypeRegistry();
    var key = new RegistryEntryKey(Registry.USER_NS, "test");
    registry.registerEntry(key, "test", new LifeEventTypeRegistry.RegistryArgs(LifeEventType.Group.OTHER, false, false, 1, 2, false));
    LifeEventType type = registry.getEntry(key);
    DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    LifeEvent l = new LifeEvent(date, type);
    l.setActors(Set.of(this.p1, this.p2));
    l.removeActor(this.p1);
    assertTrue(this.p1.getLifeEventsAsActor().isEmpty());
  }

  @Test
  void removeActorThrowsIfNotEnoughActors() {
    var registry = new LifeEventTypeRegistry();
    var key = new RegistryEntryKey(Registry.USER_NS, "test");
    registry.registerEntry(key, "test", new LifeEventTypeRegistry.RegistryArgs(LifeEventType.Group.OTHER, false, false, 1, 2, false));
    LifeEventType type = registry.getEntry(key);
    DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    LifeEvent l = new LifeEvent(date, type);
    l.setActors(Set.of(this.p1));
    assertThrows(IllegalStateException.class, () -> l.removeActor(this.p1));
  }

  @Test
  void hasActor() {
    this.lifeEvent.setActors(Set.of(this.p1, this.p2));
    assertTrue(this.lifeEvent.hasActor(this.p1));
    assertTrue(this.lifeEvent.hasActor(this.p2));
    assertFalse(this.lifeEvent.hasActor(new Person()));
  }

  @Test
  void addWitnessUpdatesEvent() {
    this.lifeEvent.addWitness(this.p1);
    assertEquals(Set.of(this.p1), this.lifeEvent.witnesses());
  }

  @Test
  void addWitnessUpdatesWitness() {
    this.lifeEvent.addWitness(this.p1);
    assertSame(this.lifeEvent, this.p1.getLifeEventsAsWitness().get(0));
  }

  @Test
  void addWitnessThrowsIfActor() {
    this.lifeEvent.setActors(Set.of(this.p1, this.p2));
    assertThrows(IllegalArgumentException.class, () -> this.lifeEvent.addWitness(this.p1));
  }

  @Test
  void removeWitnessUpdatesEvent() {
    this.lifeEvent.addWitness(this.p1);
    this.lifeEvent.removeWitness(this.p1);
    assertTrue(this.lifeEvent.witnesses().isEmpty());
  }

  @Test
  void removeWitnessUpdatesPreviousWitness() {
    this.lifeEvent.addWitness(this.p1);
    this.lifeEvent.removeWitness(this.p1);
    assertTrue(this.p1.getLifeEventsAsWitness().isEmpty());
  }

  @Test
  void hasWitness() {
    this.lifeEvent.addWitness(this.p1);
    assertTrue(this.lifeEvent.hasWitness(this.p1));
    assertFalse(this.lifeEvent.hasWitness(new Person()));
  }
}