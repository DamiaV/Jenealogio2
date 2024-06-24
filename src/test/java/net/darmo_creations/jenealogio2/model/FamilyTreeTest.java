package net.darmo_creations.jenealogio2.model;

import net.darmo_creations.jenealogio2.model.datetime.*;
import net.darmo_creations.jenealogio2.model.datetime.calendar.Calendar;
import net.darmo_creations.jenealogio2.model.datetime.calendar.GregorianCalendar;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import java.io.*;
import java.nio.file.*;
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
  void getName() {
    assertEquals("tree", this.tree.name());
  }

  @Test
  void setName() {
    this.tree.setName("TREE");
    assertEquals("TREE", this.tree.name());
  }

  @Test
  void setNameNullError() {
    assertThrows(NullPointerException.class, () -> this.tree.setName(null));
  }

  @Test
  void getPersonsEmpty() {
    assertTrue(this.tree.persons().isEmpty());
  }

  @Test
  void getPersonsNotEmpty() {
    this.tree.addPerson(new Person());
    this.tree.addPerson(new Person());
    assertEquals(2, this.tree.persons().size());
  }

  @Test
  void getLifeEventsEmpty() {
    assertTrue(this.tree.lifeEvents().isEmpty());
  }

  @Test
  void getLifeEventsNotEmptyOnePerson() {
    Person person = new Person();
    LifeEvent event = new LifeEvent(new DateTimeWithPrecision(Calendar.forName("gregorian").getDate(1970, 1, 1, 1, 0), DateTimePrecision.EXACT), typeReg.getEntry(new RegistryEntryKey("builtin:birth")));
    this.tree.setLifeEventActors(event, Set.of(person));
    assertEquals(1, this.tree.lifeEvents().size());
  }

  @Test
  void getLifeEventsNotEmptyTwoPersons() {
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
  void documentsEmptyByDefault() {
    assertTrue(this.tree.documents().isEmpty());
  }

  @Test
  void getDocument() {
    AttachedDocument doc = new AttachedDocument(Path.of("doc.pdf"), null, null);
    this.tree.addDocument(doc);
    //noinspection OptionalGetWithoutIsPresent
    assertSame(doc, this.tree.getDocument("doc.pdf").get());
  }

  @Test
  void getDocumentEmptyIfPictureNotRegistered() {
    assertTrue(this.tree.getDocument("a").isEmpty());
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
  void addDocumentAddsDocumentAndReturnsTrue() throws IOException {
    Picture p = new Picture(PictureTest.getImage(IMG_PATH), Path.of("app_icon.png"), null, null);
    assertTrue(this.tree.addDocument(p));
    assertSame(p, this.tree.documents().iterator().next());
  }

  @Test
  void addDocumentReturnsFalseIfAlreadyAdded() throws IOException {
    Picture p = new Picture(PictureTest.getImage(IMG_PATH), Path.of("app_icon.png"), null, null);
    this.tree.addDocument(p);
    assertFalse(this.tree.addDocument(p));
  }

  @Test
  void removeDocumentRemovesFromTree() throws IOException {
    Picture p = new Picture(PictureTest.getImage(IMG_PATH), Path.of("app_icon.png"), null, null);
    this.tree.addDocument(p);
    this.tree.removeDocument(p.fileName());
    assertTrue(this.tree.documents().isEmpty());
  }

  @Test
  void removeDocumentReturnsDocument() throws IOException {
    Picture p = new Picture(PictureTest.getImage(IMG_PATH), Path.of("app_icon.png"), null, null);
    this.tree.addDocument(p);
    assertSame(p, this.tree.removeDocument(p.fileName()));
  }

  @Test
  void removeDocumentRemovesFromGenealogyObjects() throws IOException {
    Picture pic = new Picture(PictureTest.getImage(IMG_PATH), Path.of("app_icon.png"), null, null);
    this.tree.addDocument(pic);
    Person p = new Person();
    p.addDocument(pic);
    DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    LifeEvent l = new LifeEvent(date, new LifeEventTypeRegistry().getEntry(new RegistryEntryKey("builtin:birth")));
    l.addDocument(pic);
    this.tree.addPerson(p);
    this.tree.setLifeEventActors(l, Set.of(p));
    this.tree.removeDocument(pic.fileName());
    assertTrue(p.documents().isEmpty());
    assertTrue(l.documents().isEmpty());
  }

  @Test
  void renameDocumentRenamesDocument() throws IOException {
    Picture pic = new Picture(PictureTest.getImage(IMG_PATH), Path.of("test.png"), null, null);
    this.tree.addDocument(pic);
    this.tree.renameDocument("test.png", "test1");
    assertEquals("test1.png", pic.fileName());
  }

  @Test
  void renameDocumentRenamesDocumentInTree() throws IOException {
    Picture pic = new Picture(PictureTest.getImage(IMG_PATH), Path.of("test.png"), null, null);
    this.tree.addDocument(pic);
    this.tree.renameDocument("test.png", "test1");
    //noinspection OptionalGetWithoutIsPresent
    assertSame(pic, this.tree.getDocument("test1.png").get());
    assertTrue(this.tree.getDocument("test.png").isEmpty());
  }

  @Test
  void renameDocumentThrowsIfBothArgsEqual() throws IOException {
    Picture pic = new Picture(PictureTest.getImage(IMG_PATH), Path.of("app_icon.png"), null, null);
    this.tree.addDocument(pic);
    assertThrows(IllegalArgumentException.class, () -> this.tree.renameDocument("app_icon.png", "app_icon"));
  }

  @Test
  void renameDocumentThrowsIfOldNameNotRegistered() throws IOException {
    Picture pic = new Picture(PictureTest.getImage(IMG_PATH), Path.of("test.png"), null, null);
    this.tree.addDocument(pic);
    assertThrows(IllegalArgumentException.class, () -> this.tree.renameDocument("test1.png", "test2"));
  }

  @Test
  void renameDocumentThrowsIfNewNameAlreadyRegistered() throws IOException {
    Picture pic = new Picture(PictureTest.getImage(IMG_PATH), Path.of("test.png"), null, null);
    this.tree.addDocument(pic);
    Picture pic1 = new Picture(PictureTest.getImage(IMG_PATH), Path.of("test1.png"), null, null);
    this.tree.addDocument(pic1);
    assertThrows(IllegalArgumentException.class, () -> this.tree.renameDocument("test.png", "test1"));
  }

  @Test
  void addDocumentToObjectAddsDocument() throws IOException {
    Picture pic = new Picture(PictureTest.getImage(IMG_PATH), Path.of("app_icon.png"), null, null);
    this.tree.addDocument(pic);
    Person p = new Person();
    this.tree.addPerson(p);
    this.tree.addDocumentToObject("app_icon.png", p);
    assertSame(pic, p.documents().iterator().next());
  }

  @Test
  void addDocumentToObjectThrowsIfInvalidName() throws IOException {
    Picture pic = new Picture(PictureTest.getImage(IMG_PATH), Path.of("app_icon.png"), null, null);
    this.tree.addDocument(pic);
    Person p = new Person();
    this.tree.addPerson(p);
    assertThrows(NoSuchElementException.class, () -> this.tree.addDocumentToObject("invalid", p));
  }

  @Test
  void removeDocumentFromObjectRemovesDocument() throws IOException {
    Picture pic = new Picture(PictureTest.getImage(IMG_PATH), Path.of("app_icon.png"), null, null);
    this.tree.addDocument(pic);
    Person p = new Person();
    this.tree.addPerson(p);
    this.tree.removeDocumentFromObject("app_icon.png", p);
    assertTrue(p.documents().isEmpty());
  }

  @Test
  void setMainPictureOfObjectSetsMainPicture() throws IOException {
    Picture pic = new Picture(PictureTest.getImage(IMG_PATH), Path.of("app_icon.png"), null, null);
    this.tree.addDocument(pic);
    Person p = new Person();
    this.tree.addPerson(p);
    this.tree.addDocumentToObject("app_icon.png", p);
    this.tree.setMainPictureOfObject("app_icon.png", p);
    //noinspection OptionalGetWithoutIsPresent
    assertSame(pic, p.mainPicture().get());
  }

  @Test
  void setMainPictureOfObjectThrowsIfNotAddedToObject() throws IOException {
    Picture pic = new Picture(PictureTest.getImage(IMG_PATH), Path.of("app_icon.png"), null, null);
    this.tree.addDocument(pic);
    Person p = new Person();
    this.tree.addPerson(p);
    assertThrows(IllegalArgumentException.class, () -> this.tree.setMainPictureOfObject("invalid", p));
  }

  @Test
  void setMainPictureOfObjectThrowsIfNotPicture() {
    AttachedDocument doc = new AttachedDocument(Path.of("doc.pdf"), null, null);
    this.tree.addDocument(doc);
    Person p = new Person();
    this.tree.addPerson(p);
    this.tree.addDocumentToObject("doc.pdf", p);
    assertThrows(ClassCastException.class, () -> this.tree.setMainPictureOfObject("doc.pdf", p));
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
