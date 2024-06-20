package net.darmo_creations.jenealogio2.model;

import net.darmo_creations.jenealogio2.model.datetime.*;
import net.darmo_creations.jenealogio2.model.datetime.calendar.Calendar;
import net.darmo_creations.jenealogio2.model.datetime.calendar.GregorianCalendar;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("DataFlowIssue")
class FamilyTreeTest {
  private static final String IMG_PATH = "/net/darmo_creations/jenealogio2/images/app_icon.png";
  private static LifeEventTypeRegistry typeReg;
  private FamilyTree tree;

  @BeforeAll
  static void setUpClass() {
    typeReg = new LifeEventTypeRegistry();
  }

  @BeforeEach
  void setUp() {
    this.tree = new FamilyTree("tree");
  }

  @Test
  void testGetName() {
    assertEquals("tree", this.tree.name());
  }

  @Test
  void testSetName() {
    this.tree.setName("TREE");
    assertEquals("TREE", this.tree.name());
  }

  @Test
  void testSetNameNullError() {
    assertThrows(NullPointerException.class, () -> this.tree.setName(null));
  }

  @Test
  void testGetPersonsEmpty() {
    assertTrue(this.tree.persons().isEmpty());
  }

  @Test
  void testGetPersonsNotEmpty() {
    this.tree.addPerson(new Person());
    this.tree.addPerson(new Person());
    assertEquals(2, this.tree.persons().size());
  }

  @Test
  void testGetLifeEventsEmpty() {
    assertTrue(this.tree.lifeEvents().isEmpty());
  }

  @Test
  void testGetLifeEventsNotEmptyOnePerson() {
    Person person = new Person();
    LifeEvent event = new LifeEvent(new DateTimeWithPrecision(Calendar.forName("gregorian").getDate(1970, 1, 1, 1, 0), DateTimePrecision.EXACT), typeReg.getEntry(new RegistryEntryKey("builtin:birth")));
    this.tree.setLifeEventActors(event, Set.of(person));
    assertEquals(1, this.tree.lifeEvents().size());
  }

  @Test
  void testGetLifeEventsNotEmptyTwoPersons() {
    Person person1 = new Person();
    LifeEvent event1 = new LifeEvent(new DateTimeWithPrecision(Calendar.forName("gregorian")
        .getDate(1970, 1, 1, 1, 0), DateTimePrecision.EXACT),
        typeReg.getEntry(new RegistryEntryKey("builtin:birth")));
    this.tree.setLifeEventActors(event1, Set.of(person1));
    Person person2 = new Person();
    LifeEvent event2 = new LifeEvent(new DateTimeWithPrecision(Calendar.forName("gregorian")
        .getDate(1970, 1, 1, 1, 0), DateTimePrecision.EXACT),
        typeReg.getEntry(new RegistryEntryKey("builtin:birth")));
    this.tree.setLifeEventActors(event2, Set.of(person2));
    assertEquals(2, this.tree.lifeEvents().size());
  }

  @Test
  void testGetPicturesEmpty() {
    assertTrue(this.tree.pictures().isEmpty());
  }

  @Test
  void testGetPictureEmpty() {
    assertTrue(this.tree.getPicture("a").isEmpty());
  }

  @Test
  void addPerson() {
    Person person = new Person();
    this.tree.addPerson(person);
    assertSame(person, this.tree.persons().iterator().next());
  }

  @Test
  void addPersonSetsRootIfEmtpy() {
    Person person = new Person();
    this.tree.addPerson(person);
    //noinspection OptionalGetWithoutIsPresent
    assertSame(person, this.tree.root().get());
  }

  @Test
  void addPersonSetsTree() {
    Person person = new Person();
    this.tree.addPerson(person);
    //noinspection OptionalGetWithoutIsPresent
    assertSame(this.tree, this.tree.root().get().familyTree());
  }

  @Test
  void removePerson() {
    Person p1 = new Person();
    Person p2 = new Person();
    this.tree.addPerson(p1);
    this.tree.addPerson(p2);
    this.tree.removePerson(p2);
    assertEquals(1, this.tree.persons().size());
    assertSame(p1, this.tree.persons().iterator().next());
  }

  @Test
  void removePersonThrowsIfRoot() {
    Person p = new Person();
    this.tree.addPerson(p);
    assertThrows(IllegalArgumentException.class, () -> this.tree.removePerson(p));
  }

  @Test
  void removePersonUpdatesParents() {
    Person p = new Person();
    Person p1 = new Person();
    Person p2 = new Person();
    p.setParent(0, p1);
    p.setParent(1, p2);
    this.tree.addPerson(new Person()); // So that "p" is not root
    this.tree.addPerson(p);
    this.tree.removePerson(p);
    assertTrue(p1.children().isEmpty());
    assertTrue(p2.children().isEmpty());
  }

  @Test
  void removePersonUpdatesChildren() {
    Person p = new Person();
    Person p1 = new Person();
    p.setParent(0, p1);
    this.tree.addPerson(new Person()); // So that "p1" is not root
    this.tree.addPerson(p1);
    this.tree.removePerson(p1);
    assertTrue(p.parents().left().isEmpty());
    assertTrue(p.parents().right().isEmpty());
  }

  @Test
  void removePersonUpdatesLifeEventActors() {
    DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    LifeEventTypeRegistry reg = new LifeEventTypeRegistry();
    reg.registerEntry(new RegistryEntryKey("user:test"), "Test", new LifeEventTypeRegistry.RegistryArgs(LifeEventType.Group.ADMIN, false, false, 1, 2, false));
    LifeEvent l = new LifeEvent(date, reg.getEntry(new RegistryEntryKey("user:test")));
    Person p1 = new Person();
    Person p2 = new Person();
    l.setActors(Set.of(p1, p2));
    this.tree.addPerson(new Person()); // So that "p1" is not root
    this.tree.addPerson(p1);
    this.tree.removePerson(p1);
    assertFalse(l.hasActor(p1));
    assertTrue(l.hasActor(p2));
  }

  @Test
  void removePersonUpdatesLifeEventWitnesses() {
    DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    LifeEvent l = new LifeEvent(date, new LifeEventTypeRegistry().getEntry(new RegistryEntryKey("builtin:birth")));
    Person p1 = new Person();
    Person p2 = new Person();
    l.addWitness(p1);
    l.setActors(Set.of(p2));
    this.tree.addPerson(new Person()); // So that "p1" is not root
    this.tree.addPerson(p1);
    this.tree.removePerson(p1);
    assertFalse(l.hasWitness(p1));
  }

  @ParameterizedTest
  @EnumSource(Person.RelativeType.class)
  void removePersonUpdatesRelatives(Person.RelativeType relativeType) {
    Person p = new Person();
    Person parent = new Person();
    p.addRelative(parent, relativeType);
    this.tree.addPerson(new Person()); // So that "p" is not root
    this.tree.addPerson(p);
    this.tree.removePerson(p);
    assertTrue(p.getRelatives(relativeType).isEmpty());
    assertTrue(parent.nonBiologicalChildren(relativeType).isEmpty());
  }

  @Test
  void setLifeEventActorsAddsEventToTree() {
    DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    LifeEvent l = new LifeEvent(date, new LifeEventTypeRegistry().getEntry(new RegistryEntryKey("builtin:birth")));
    Person p = new Person();
    this.tree.setLifeEventActors(l, Set.of(p));
    assertSame(l, this.tree.lifeEvents().iterator().next());
  }

  @Test
  void setLifeEventActorsAddsEventToActors() {
    DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    LifeEvent l = new LifeEvent(date, new LifeEventTypeRegistry().getEntry(new RegistryEntryKey("builtin:birth")));
    Person p = new Person();
    this.tree.setLifeEventActors(l, Set.of(p));
    assertSame(p, l.actors().iterator().next());
  }

  @Test
  void setLifeEventActorsRemovesEventFromPreviousActors() {
    DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    LifeEvent l = new LifeEvent(date, new LifeEventTypeRegistry().getEntry(new RegistryEntryKey("builtin:birth")));
    Person p = new Person();
    this.tree.setLifeEventActors(l, Set.of(p));
    this.tree.setLifeEventActors(l, Set.of(new Person()));
    assertNotSame(p, l.actors().iterator().next());
  }

  @Test
  void setLifeEventActorsThrowsIfActorIsWitness() {
    DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    LifeEvent l = new LifeEvent(date, new LifeEventTypeRegistry().getEntry(new RegistryEntryKey("builtin:birth")));
    Person p1 = new Person();
    l.addWitness(p1);
    assertThrows(IllegalArgumentException.class, () -> this.tree.setLifeEventActors(l, Set.of(p1)));
  }

  @Test
  void setLifeEventActorsThrowsIfInvalidActorsNumber() {
    DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    LifeEvent l = new LifeEvent(date, new LifeEventTypeRegistry().getEntry(new RegistryEntryKey("builtin:birth")));
    assertThrows(IllegalArgumentException.class, () -> this.tree.setLifeEventActors(l, Set.of()));
    assertThrows(IllegalArgumentException.class, () -> this.tree.setLifeEventActors(l, Set.of(new Person(), new Person())));
  }

  @Test
  void removeActorFromLifeEventRemovesActor() {
    var registry = new LifeEventTypeRegistry();
    var key = new RegistryEntryKey(Registry.USER_NS, "test");
    registry.registerEntry(key, "test", new LifeEventTypeRegistry.RegistryArgs(LifeEventType.Group.OTHER, false, false, 1, 2, false));
    LifeEventType type = registry.getEntry(key);
    DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    LifeEvent l = new LifeEvent(date, type);
    Person p1 = new Person();
    Person p2 = new Person();
    this.tree.setLifeEventActors(l, Set.of(p1, p2));
    this.tree.removeActorFromLifeEvent(l, p1);
    assertEquals(1, l.actors().size());
    assertSame(p2, l.actors().iterator().next());
  }

  @Test
  void removeActorFromLifeEventRemovesEventIfNotEnoughActors() {
    DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    LifeEvent l = new LifeEvent(date, new LifeEventTypeRegistry().getEntry(new RegistryEntryKey("builtin:birth")));
    Person p1 = new Person();
    this.tree.setLifeEventActors(l, Set.of(p1));
    this.tree.removeActorFromLifeEvent(l, p1);
    assertTrue(this.tree.lifeEvents().isEmpty());
  }

  @Test
  void removeActorFromLifeEventDissociatesActorsIfNotEnoughActors() {
    DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    LifeEvent l = new LifeEvent(date, new LifeEventTypeRegistry().getEntry(new RegistryEntryKey("builtin:marriage")));
    Person p1 = new Person();
    Person p2 = new Person();
    this.tree.setLifeEventActors(l, Set.of(p1, p2));
    this.tree.removeActorFromLifeEvent(l, p1);
    assertTrue(p1.getLifeEventsAsActor().stream().noneMatch(le -> le.hasActor(p1)));
    assertTrue(p2.getLifeEventsAsActor().stream().noneMatch(le -> le.hasActor(p2)));
  }

  @Test
  void removeActorFromLifeEventDissociatesWitnessesIfNotEnoughActors() {
    DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    LifeEvent l = new LifeEvent(date, new LifeEventTypeRegistry().getEntry(new RegistryEntryKey("builtin:marriage")));
    Person p1 = new Person();
    Person p2 = new Person();
    Person w = new Person();
    this.tree.setLifeEventActors(l, Set.of(p1, p2));
    l.addWitness(w);
    this.tree.removeActorFromLifeEvent(l, p1);
    assertTrue(w.getLifeEventsAsWitness().stream().noneMatch(le -> le.hasWitness(w)));
  }

  @Test
  void addWitnessToLifeEventAddsWitness() {
    DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    LifeEvent l = new LifeEvent(date, new LifeEventTypeRegistry().getEntry(new RegistryEntryKey("builtin:birth")));
    l.setActors(Set.of(new Person()));
    Person w = new Person();
    this.tree.addWitnessToLifeEvent(l, w);
    assertSame(w, l.witnesses().iterator().next());
  }

  @Test
  void addWitnessToLifeEventAddsEventToWitness() {
    DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    LifeEvent l = new LifeEvent(date, new LifeEventTypeRegistry().getEntry(new RegistryEntryKey("builtin:birth")));
    l.setActors(Set.of(new Person()));
    Person w = new Person();
    this.tree.addWitnessToLifeEvent(l, w);
    assertSame(l, w.getLifeEventsAsWitness().iterator().next());
  }

  @Test
  void removeWitnessFromLifeEventRemovesWitness() {
    DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    LifeEvent l = new LifeEvent(date, new LifeEventTypeRegistry().getEntry(new RegistryEntryKey("builtin:birth")));
    l.setActors(Set.of(new Person()));
    Person w = new Person();
    this.tree.addWitnessToLifeEvent(l, w);
    this.tree.removeWitnessFromLifeEvent(l, w);
    assertTrue(l.witnesses().isEmpty());
  }

  @Test
  void removeWitnessFromLifeEventRemovesEventFromWitness() {
    DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    LifeEvent l = new LifeEvent(date, new LifeEventTypeRegistry().getEntry(new RegistryEntryKey("builtin:birth")));
    l.setActors(Set.of(new Person()));
    Person w = new Person();
    this.tree.addWitnessToLifeEvent(l, w);
    this.tree.removeWitnessFromLifeEvent(l, w);
    assertFalse(w.getLifeEventsAsWitness().iterator().hasNext());
  }

  @Test
  void addPictureAddsPictureAndReturnsTrue() throws IOException {
    Picture p = new Picture(PictureTest.getImage(IMG_PATH), "test", null, null);
    assertTrue(this.tree.addPicture(p));
    assertSame(p, this.tree.pictures().iterator().next());
  }

  @Test
  void addPictureReturnsFalseIfAlreadyAdded() throws IOException {
    Picture p = new Picture(PictureTest.getImage(IMG_PATH), "test", null, null);
    this.tree.addPicture(p);
    assertFalse(this.tree.addPicture(p));
  }

  @Test
  void removePictureRemovesFromTree() throws IOException {
    Picture p = new Picture(PictureTest.getImage(IMG_PATH), "test", null, null);
    this.tree.addPicture(p);
    this.tree.removePicture(p.name());
    assertTrue(this.tree.pictures().isEmpty());
  }

  @Test
  void removePictureRemovesReturnsPicture() throws IOException {
    Picture p = new Picture(PictureTest.getImage(IMG_PATH), "test", null, null);
    this.tree.addPicture(p);
    assertSame(p, this.tree.removePicture(p.name()));
  }

  @Test
  void removePictureRemovesFromGenealogyObjects() throws IOException {
    Picture pic = new Picture(PictureTest.getImage(IMG_PATH), "test", null, null);
    this.tree.addPicture(pic);
    Person p = new Person();
    p.addPicture(pic);
    DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    LifeEvent l = new LifeEvent(date, new LifeEventTypeRegistry().getEntry(new RegistryEntryKey("builtin:birth")));
    l.addPicture(pic);
    this.tree.addPerson(p);
    this.tree.setLifeEventActors(l, Set.of(p));
    this.tree.removePicture(pic.name());
    assertTrue(p.pictures().isEmpty());
    assertTrue(l.pictures().isEmpty());
  }

  @Test
  void addPictureToObjectAddsPicture() throws IOException {
    Picture pic = new Picture(PictureTest.getImage(IMG_PATH), "test", null, null);
    this.tree.addPicture(pic);
    Person p = new Person();
    this.tree.addPerson(p);
    this.tree.addPictureToObject("test", p);
    assertSame(pic, p.pictures().iterator().next());
  }

  @Test
  void addPictureToObjectThrowsIfInvalidName() throws IOException {
    Picture pic = new Picture(PictureTest.getImage(IMG_PATH), "test", null, null);
    this.tree.addPicture(pic);
    Person p = new Person();
    this.tree.addPerson(p);
    assertThrows(NoSuchElementException.class, () -> this.tree.addPictureToObject("invalid", p));
  }

  @Test
  void removePictureFromObjectRemovesPicture() throws IOException {
    Picture pic = new Picture(PictureTest.getImage(IMG_PATH), "test", null, null);
    this.tree.addPicture(pic);
    Person p = new Person();
    this.tree.addPerson(p);
    this.tree.removePictureFromObject("test", p);
    assertTrue(p.pictures().isEmpty());
  }

  @Test
  void setMainPictureOfObjectSetsMainPicture() throws IOException {
    Picture pic = new Picture(PictureTest.getImage(IMG_PATH), "test", null, null);
    this.tree.addPicture(pic);
    Person p = new Person();
    this.tree.addPerson(p);
    this.tree.addPictureToObject("test", p);
    this.tree.setMainPictureOfObject("test", p);
    //noinspection OptionalGetWithoutIsPresent
    assertSame(pic, p.mainPicture().get());
  }

  @Test
  void setMainPictureOfObjectThrowsIfNotAddedToObject() throws IOException {
    Picture pic = new Picture(PictureTest.getImage(IMG_PATH), "test", null, null);
    this.tree.addPicture(pic);
    Person p = new Person();
    this.tree.addPerson(p);
    assertThrows(IllegalArgumentException.class, () -> this.tree.setMainPictureOfObject("test", p));
  }

  @Test
  void setRoot() {
    this.tree.addPerson(new Person());
    Person p = new Person();
    this.tree.addPerson(p);
    this.tree.setRoot(p);
    //noinspection OptionalGetWithoutIsPresent
    assertSame(p, this.tree.root().get());
  }

  @Test
  void setRootThrowsIfPersonNotInTree() {
    this.tree.addPerson(new Person());
    assertThrows(NoSuchElementException.class, () -> this.tree.setRoot(new Person()));
  }
}
