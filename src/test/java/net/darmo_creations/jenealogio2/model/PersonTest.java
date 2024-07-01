package net.darmo_creations.jenealogio2.model;

import net.darmo_creations.jenealogio2.model.datetime.*;
import net.darmo_creations.jenealogio2.model.datetime.calendar.Calendar;
import net.darmo_creations.jenealogio2.model.datetime.calendar.GregorianCalendar;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("DataFlowIssue")
class PersonTest {
  private Person person;
  private Person parent1;
  private Person parent2;

  @BeforeEach
  void setUp() {
    final FamilyTree tree = new FamilyTree("tree");
    tree.addPerson(this.person = new Person());
    this.person.setDisambiguationID(1);
    tree.addPerson(this.parent1 = new Person());
    this.parent1.setDisambiguationID(2);
    tree.addPerson(this.parent2 = new Person());
    this.parent2.setDisambiguationID(3);
  }

  @Test
  void setFamilyTree() {
    final FamilyTree t = new FamilyTree("t");
    this.person.setFamilyTree(t);
    assertSame(t, this.person.familyTree());
  }

  @Test
  void setFamilyTreeThrowsIfNull() {
    assertThrows(NullPointerException.class, () -> this.person.setFamilyTree(null));
  }

  @Test
  void setDisambiguationID() {
    this.person.setDisambiguationID(1);
    //noinspection OptionalGetWithoutIsPresent
    assertEquals(1, this.person.disambiguationID().get());
  }

  @Test
  void setDisambiguationIDAcceptsNull() {
    this.person.setDisambiguationID(null);
    assertTrue(this.person.disambiguationID().isEmpty());
  }

  @ParameterizedTest
  @ValueSource(ints = { 0, -1, -2, -3 })
  void setDisambiguationIDThrowsIfNotPositive(int i) {
    assertThrows(IllegalArgumentException.class, () -> this.person.setDisambiguationID(i));
  }

  @Test
  void lifeStatusIsLivingByDefault() {
    assertEquals(LifeStatus.LIVING, this.person.lifeStatus());
  }

  @Test
  void setLifeStatus() {
    this.person.setLifeStatus(LifeStatus.PROBABLY_DECEASED);
    assertEquals(LifeStatus.PROBABLY_DECEASED, this.person.lifeStatus());
  }

  @Test
  void setLifeStatusThrowsIfHasDeathEvent() {
    final DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    final LifeEvent l = new LifeEvent(date, new LifeEventTypeRegistry().getEntry(new RegistryEntryKey("builtin:death")));
    l.setActors(Set.of(this.person));
    assertThrows(IllegalArgumentException.class, () -> this.person.setLifeStatus(LifeStatus.LIVING));
  }

  @Test
  void legalFirstNamesKeepsOrder() {
    this.person.setLegalFirstNames(List.of("Alice", "Bob"));
    assertEquals(List.of("Alice", "Bob"), this.person.legalFirstNames());
  }

  @Test
  void legalFirstNamesReturnsCopy() {
    this.person.setLegalFirstNames(List.of("Alice", "Bob"));
    this.person.legalFirstNames().clear();
    assertEquals(2, this.person.legalFirstNames().size());
  }

  @Test
  void getJoinedLegalFirstNames() {
    this.person.setLegalFirstNames(List.of("Alice", "Bob"));
    //noinspection OptionalGetWithoutIsPresent
    assertEquals("Alice Bob", this.person.getJoinedLegalFirstNames().get());
  }

  @Test
  void getJoinedLegalFirstNamesIsEmptyIfNoFirstNames() {
    assertTrue(this.person.getJoinedLegalFirstNames().isEmpty());
  }

  @Test
  void legalLastName() {
    this.person.setLegalLastName("Charlie");
    //noinspection OptionalGetWithoutIsPresent
    assertEquals("Charlie", this.person.legalLastName().get());
  }

  @Test
  void legalLastNameIsEmptyIfNoLastName() {
    assertTrue(this.person.legalLastName().isEmpty());
  }

  @Test
  void publicFirstNamesKeepsOrder() {
    this.person.setPublicFirstNames(List.of("Alice", "Bob"));
    assertEquals(List.of("Alice", "Bob"), this.person.publicFirstNames());
  }

  @Test
  void publicFirstNamesReturnsCopy() {
    this.person.setPublicFirstNames(List.of("Alice", "Bob"));
    this.person.publicFirstNames().clear();
    assertEquals(2, this.person.publicFirstNames().size());
  }

  @Test
  void getJoinedPublicFirstNames() {
    this.person.setPublicFirstNames(List.of("Alice", "Bob"));
    //noinspection OptionalGetWithoutIsPresent
    assertEquals("Alice Bob", this.person.getJoinedPublicFirstNames().get());
  }

  @Test
  void getJoinedPublicFirstNamesIsEmptyIfNoFirstNames() {
    assertTrue(this.person.getJoinedPublicFirstNames().isEmpty());
  }

  @Test
  void publicLastName() {
    this.person.setPublicLastName("Charlie");
    //noinspection OptionalGetWithoutIsPresent
    assertEquals("Charlie", this.person.publicLastName().get());
  }

  @Test
  void publicLastNameIsEmptyIfNoLastName() {
    assertTrue(this.person.publicLastName().isEmpty());
  }

  @Test
  void nicknamesKeepsOrder() {
    this.person.setNicknames(List.of("Alice", "Bob"));
    assertEquals(List.of("Alice", "Bob"), this.person.nicknames());
  }

  @Test
  void nicknamesReturnsCopy() {
    this.person.setNicknames(List.of("Alice", "Bob"));
    this.person.nicknames().clear();
    assertEquals(2, this.person.nicknames().size());
  }

  @Test
  void getJoinedNicknames() {
    this.person.setNicknames(List.of("Alice", "Bob"));
    //noinspection OptionalGetWithoutIsPresent
    assertEquals("Alice Bob", this.person.getJoinedNicknames().get());
  }

  @Test
  void getJoinedNicknamesIsEmptyIfNoFirstNames() {
    assertTrue(this.person.getJoinedNicknames().isEmpty());
  }

  @Test
  void getLastNameReturnsLegalNameEvenIfPublicNameIsDefined() {
    this.person.setLegalLastName("Alice");
    this.person.setPublicLastName("Bob");
    //noinspection OptionalGetWithoutIsPresent
    assertEquals("Alice", this.person.getLastName().get());
  }

  @Test
  void getLastNameReturnsPublicNameIfLegalNameIsUndefined() {
    this.person.setPublicLastName("Bob");
    //noinspection OptionalGetWithoutIsPresent
    assertEquals("Bob", this.person.getLastName().get());
  }

  @Test
  void getLastNameReturnsIsEmptyIfNoLastNames() {
    assertTrue(this.person.getLastName().isEmpty());
  }

  @Test
  void getFirstNamesReturnsLegalNameEvenIfPublicNameIsDefined() {
    this.person.setLegalFirstNames(List.of("Alice", "Bob"));
    this.person.setPublicFirstNames(List.of("Charlie", "Dick"));
    //noinspection OptionalGetWithoutIsPresent
    assertEquals("Alice Bob", this.person.getFirstNames().get());
  }

  @Test
  void getFirstNamesReturnsPublicNameIfLegalNameIsUndefined() {
    this.person.setPublicFirstNames(List.of("Charlie", "Dick"));
    //noinspection OptionalGetWithoutIsPresent
    assertEquals("Charlie Dick", this.person.getFirstNames().get());
  }

  @Test
  void getFirstNamesReturnsIsEmptyIfNoFirstNames() {
    assertTrue(this.person.getFirstNames().isEmpty());
  }

  @Test
  void setGender() {
    final Gender gender = this.person.familyTree().genderRegistry().getEntry(new RegistryEntryKey("builtin:female"));
    this.person.setGender(gender);
    //noinspection OptionalGetWithoutIsPresent
    assertEquals(gender, this.person.gender().get());
  }

  @Test
  void setMainOccupation() {
    this.person.setMainOccupation("a");
    //noinspection OptionalGetWithoutIsPresent
    assertEquals("a", this.person.mainOccupation().get());
  }

  @Test
  void parentsBothDefined() {
    this.person.setParent(0, this.parent1);
    this.person.setParent(1, this.parent2);
    assertEquals(new Parents(this.parent1, this.parent2), this.person.parents());
  }

  @Test
  void parentsFirstDefined() {
    this.person.setParent(0, this.parent1);
    assertEquals(new Parents(this.parent1, null), this.person.parents());
  }

  @Test
  void parentsSecondDefined() {
    this.person.setParent(1, this.parent2);
    assertEquals(new Parents(null, this.parent2), this.person.parents());
  }

  @Test
  void parentsNoneDefined() {
    assertEquals(new Parents(), this.person.parents());
  }

  @Test
  void getParentIndex() {
    this.person.setParent(0, this.parent1);
    this.person.setParent(1, this.parent2);
    //noinspection OptionalGetWithoutIsPresent
    assertEquals(0, this.person.getParentIndex(this.parent1).get());
    //noinspection OptionalGetWithoutIsPresent
    assertEquals(1, this.person.getParentIndex(this.parent2).get());
  }

  @Test
  void getParentIndexUndefined0() {
    this.person.setParent(1, this.parent2);
    assertTrue(this.person.getParentIndex(this.parent1).isEmpty());
    //noinspection OptionalGetWithoutIsPresent
    assertEquals(1, this.person.getParentIndex(this.parent2).get());
  }

  @Test
  void getParentIndexUndefined1() {
    this.person.setParent(0, this.parent1);
    //noinspection OptionalGetWithoutIsPresent
    assertEquals(0, this.person.getParentIndex(this.parent1).get());
    assertTrue(this.person.getParentIndex(this.parent2).isEmpty());
  }

  @Test
  void hasSameParentsSameIndices() {
    this.person.setParent(0, this.parent1);
    this.person.setParent(1, this.parent2);
    final Person person2 = new Person();
    person2.setParent(0, this.parent1);
    person2.setParent(1, this.parent2);
    assertTrue(this.person.hasSameParents(person2));
  }

  @Test
  void hasSameParentsSwappedIndices() {
    this.person.setParent(1, this.parent1);
    this.person.setParent(0, this.parent2);
    final Person person2 = new Person();
    person2.setParent(0, this.parent1);
    person2.setParent(1, this.parent2);
    assertTrue(this.person.hasSameParents(person2));
  }

  @Test
  void hasSameParentsFalseIfOnlyOneSame() {
    this.person.setParent(0, this.parent1);
    this.person.setParent(1, this.parent2);
    final Person person2 = new Person();
    person2.setParent(0, this.parent1);
    person2.setParent(1, new Person());
    assertFalse(this.person.hasSameParents(person2));
  }

  @Test
  void hasSameParentsFalseIfOnlyNoneSame() {
    this.person.setParent(0, this.parent1);
    this.person.setParent(1, this.parent2);
    final Person person2 = new Person();
    person2.setParent(0, new Person());
    person2.setParent(1, new Person());
    assertFalse(this.person.hasSameParents(person2));
  }

  @Test
  void hasAnyParentsBothDefined() {
    this.person.setParent(0, this.parent1);
    this.person.setParent(1, this.parent2);
    assertTrue(this.person.hasAnyParents());
  }

  @Test
  void hasAnyParentsFirstDefined() {
    this.person.setParent(0, this.parent1);
    assertTrue(this.person.hasAnyParents());
  }

  @Test
  void hasAnyParentsSecondDefined() {
    this.person.setParent(1, this.parent1);
    assertTrue(this.person.hasAnyParents());
  }

  @Test
  void hasBothParents() {
    this.person.setParent(0, this.parent1);
    this.person.setParent(1, this.parent2);
    assertTrue(this.person.hasBothParents());
  }

  @Test
  void hasBothParentsFirstDefined() {
    this.person.setParent(0, this.parent1);
    assertFalse(this.person.hasBothParents());
  }

  @Test
  void hasBothParentsSecondDefined() {
    this.person.setParent(1, this.parent1);
    assertFalse(this.person.hasBothParents());
  }

  @Test
  void setParentUpdatesPerson0() {
    this.person.setParent(0, this.parent1);
    //noinspection OptionalGetWithoutIsPresent
    assertSame(this.parent1, this.person.parents().parent1().get());
  }

  @Test
  void setParentUpdatesPerson1() {
    this.person.setParent(1, this.parent1);
    //noinspection OptionalGetWithoutIsPresent
    assertSame(this.parent1, this.person.parents().parent2().get());
  }

  @Test
  void setParentUpdatesParent() {
    this.person.setParent(0, this.parent1);
    assertSame(this.person, this.parent1.children().iterator().next());
  }

  @Test
  void setParentUpdatesPreviousParent() {
    this.person.setParent(0, this.parent1);
    this.person.setParent(0, this.parent2);
    assertTrue(this.parent1.children().isEmpty());
  }

  @Test
  void removeParent0() {
    this.person.setParent(0, this.parent1);
    this.person.removeParent(this.parent1);
    assertTrue(this.person.parents().parent1().isEmpty());
    assertTrue(this.person.parents().parent2().isEmpty());
  }

  @Test
  void removeParent1() {
    this.person.setParent(1, this.parent1);
    this.person.removeParent(this.parent1);
    assertTrue(this.person.parents().parent1().isEmpty());
    assertTrue(this.person.parents().parent2().isEmpty());
  }

  @Test
  void getPartnersAndChildrenExactSameParents() {
    this.person.setParent(0, this.parent1);
    this.person.setParent(1, this.parent2);
    final Person person2 = new Person();
    person2.setParent(0, this.parent1);
    person2.setParent(1, this.parent2);
    assertEquals(Map.of(
        Optional.of(this.parent2),
        Set.of(person2, this.person)
    ), this.parent1.getPartnersAndChildren());
  }

  @Test
  void getPartnersAndChildrenNoChildrenWithPartner() {
    final DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    final LifeEvent l = new LifeEvent(date, new LifeEventTypeRegistry().getEntry(new RegistryEntryKey("builtin:marriage")));
    l.setActors(Set.of(this.parent1, this.parent2));
    assertEquals(Map.of(
        Optional.of(this.parent2),
        Set.of()
    ), this.parent1.getPartnersAndChildren());
  }

  @Test
  void getPartnersAndChildrenOneCommonParents() {
    this.person.setParent(0, this.parent1);
    this.person.setParent(1, this.parent2);
    final Person person2 = new Person();
    person2.setParent(0, this.parent1);
    final Person parent3 = new Person();
    person2.setParent(1, parent3);
    assertEquals(Map.of(
        Optional.of(this.parent2),
        Set.of(this.person),
        Optional.of(parent3),
        Set.of(person2)
    ), this.parent1.getPartnersAndChildren());
  }

  @Test
  void getPartnersAndChildrenNoPartner() {
    this.person.setParent(0, this.parent1);
    final Person person2 = new Person();
    person2.setParent(0, this.parent1);
    assertEquals(Map.of(
        Optional.empty(),
        Set.of(person2, this.person)
    ), this.parent1.getPartnersAndChildren());
  }

  @Test
  void getSameParentsSiblings() {
    this.person.setParent(0, this.parent1);
    this.person.setParent(1, this.parent2);
    final Person person2 = new Person();
    person2.setParent(0, this.parent1);
    person2.setParent(1, this.parent2);
    assertEquals(Set.of(person2), this.person.getSameParentsSiblings());
    assertEquals(Set.of(this.person), person2.getSameParentsSiblings());
  }

  @Test
  void getSameParentsSiblingsEmptyIfOnlyOneCommon() {
    this.person.setParent(0, this.parent1);
    this.person.setParent(1, this.parent2);
    final Person person2 = new Person();
    person2.setParent(0, this.parent1);
    person2.setParent(1, new Person());
    assertTrue(this.person.getSameParentsSiblings().isEmpty());
    assertTrue(person2.getSameParentsSiblings().isEmpty());
  }

  @Test
  void getSameParentsSiblingsOneNull() {
    this.person.setParent(0, this.parent1);
    final Person person2 = new Person();
    person2.setParent(0, this.parent1);
    assertEquals(Set.of(person2), this.person.getSameParentsSiblings());
    assertEquals(Set.of(this.person), person2.getSameParentsSiblings());
  }

  @Test
  void getAllSiblings() {
    this.person.setParent(0, this.parent1);
    this.person.setParent(1, this.parent2);
    final Person person2 = new Person();
    person2.setParent(0, this.parent1);
    final Person parent3 = new Person();
    person2.setParent(1, parent3);
    final Person person3 = new Person();
    person3.setParent(0, parent3);
    person3.setParent(1, this.parent2);
    assertEquals(Map.of(
        new Parents(this.parent1, parent3),
        Set.of(person2),
        new Parents(parent3, this.parent2),
        Set.of(person3)
    ), this.person.getAllSiblings());
  }

  @ParameterizedTest
  @EnumSource(Person.RelativeType.class)
  void addRelativeUpdatesPerson(Person.RelativeType type) {
    this.person.addRelative(this.parent1, type);
    assertEquals(Set.of(this.parent1), this.person.getRelatives(type));
  }

  @ParameterizedTest
  @EnumSource(Person.RelativeType.class)
  void addRelativeUpdatesRelative(Person.RelativeType type) {
    this.person.addRelative(this.parent1, type);
    assertEquals(Set.of(this.person), this.parent1.nonBiologicalChildren(type));
  }

  @ParameterizedTest
  @EnumSource(Person.RelativeType.class)
  void removeRelativeUpdatesPerson(Person.RelativeType type) {
    this.person.addRelative(this.parent1, type);
    this.person.removeRelative(this.parent1, type);
    assertTrue(this.person.getRelatives(type).isEmpty());
  }

  @ParameterizedTest
  @EnumSource(Person.RelativeType.class)
  void removeRelativeUpdatesRelative(Person.RelativeType type) {
    this.person.addRelative(this.parent1, type);
    this.person.removeRelative(this.parent1, type);
    assertTrue(this.parent1.nonBiologicalChildren(type).isEmpty());
  }

  @Test
  void getBirthDateNoEvents() {
    assertTrue(this.person.getBirthDate().isEmpty());
  }

  @Test
  void getBirthDateNonBirthEvent() {
    final DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    final LifeEvent l = new LifeEvent(date, new LifeEventTypeRegistry().getEntry(new RegistryEntryKey("builtin:diploma")));
    l.setActors(Set.of(this.person));
    assertTrue(this.person.getBirthDate().isEmpty());
  }

  @Test
  void getBirthDateBirthEvent() {
    final DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    final LifeEvent l = new LifeEvent(date, new LifeEventTypeRegistry().getEntry(new RegistryEntryKey("builtin:birth")));
    l.setActors(Set.of(this.person));
    //noinspection OptionalGetWithoutIsPresent
    assertEquals(date, this.person.getBirthDate().get());
  }

  @Test
  void getDeathDateNoEvents() {
    assertTrue(this.person.getDeathDate().isEmpty());
  }

  @Test
  void getDeathDateNonDeathEvent() {
    final DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    final LifeEvent l = new LifeEvent(date, new LifeEventTypeRegistry().getEntry(new RegistryEntryKey("builtin:diploma")));
    l.setActors(Set.of(this.person));
    assertTrue(this.person.getDeathDate().isEmpty());
  }

  @Test
  void getDeathDateDeathEvent() {
    final DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    final LifeEvent l = new LifeEvent(date, new LifeEventTypeRegistry().getEntry(new RegistryEntryKey("builtin:death")));
    l.setActors(Set.of(this.person));
    //noinspection OptionalGetWithoutIsPresent
    assertEquals(date, this.person.getDeathDate().get());
  }

  @Test
  void lifeEventsAreSortedByDates() {
    final DateTime date2 = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2084, 1, 1, 0, 0), DateTimePrecision.EXACT);
    final LifeEvent l2 = new LifeEvent(date2, new LifeEventTypeRegistry().getEntry(new RegistryEntryKey("builtin:death")));
    l2.setActors(Set.of(this.person));
    final DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    final LifeEvent l = new LifeEvent(date, new LifeEventTypeRegistry().getEntry(new RegistryEntryKey("builtin:birth")));
    l.setActors(Set.of(this.person));
    assertEquals(List.of(l, l2), this.person.lifeEvents());
  }

  @Test
  void getLifeEventsAsActor() {
    final DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    final LifeEvent l = new LifeEvent(date, new LifeEventTypeRegistry().getEntry(new RegistryEntryKey("builtin:birth")));
    l.setActors(Set.of(this.person));
    assertEquals(List.of(l), this.person.getLifeEventsAsActor());
  }

  @Test
  void getLifeEventsAsActorIgnoresWitnessed() {
    final DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    final LifeEvent l = new LifeEvent(date, new LifeEventTypeRegistry().getEntry(new RegistryEntryKey("builtin:birth")));
    l.addWitness(this.person);
    assertTrue(this.person.getLifeEventsAsActor().isEmpty());
  }

  @Test
  void getLifeEventsAsWitness() {
    final DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    final LifeEvent l = new LifeEvent(date, new LifeEventTypeRegistry().getEntry(new RegistryEntryKey("builtin:birth")));
    l.addWitness(this.person);
    assertEquals(List.of(l), this.person.getLifeEventsAsWitness());
  }

  @Test
  void getLifeEventsAsWitnessIgnoresActed() {
    final DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    final LifeEvent l = new LifeEvent(date, new LifeEventTypeRegistry().getEntry(new RegistryEntryKey("builtin:birth")));
    l.setActors(Set.of(this.person));
    assertTrue(this.person.getLifeEventsAsWitness().isEmpty());
  }

  @Test
  void addLifeEvent() {
    final DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    final LifeEvent l = new LifeEvent(date, new LifeEventTypeRegistry().getEntry(new RegistryEntryKey("builtin:birth")));
    this.person.addLifeEvent(l);
    assertSame(l, this.person.lifeEvents().get(0));
  }

  @Test
  void toStringNoNames() {
    this.person.setDisambiguationID(null);
    assertEquals("? ?", this.person.toString());
  }

  @Test
  void toStringShowsID() {
    assertEquals("? ? (#1)", this.person.toString());
  }

  @Test
  void toStringOnlyLegalFirstNames() {
    this.person.setDisambiguationID(null);
    this.person.setLegalFirstNames(List.of("a", "b"));
    assertEquals("a b ?", this.person.toString());
  }

  @Test
  void toStringOnlyPublicFirstNames() {
    this.person.setDisambiguationID(null);
    this.person.setPublicFirstNames(List.of("a", "b"));
    assertEquals("a b ?", this.person.toString());
  }

  @Test
  void toStringOnlyBothFirstNames() {
    this.person.setDisambiguationID(null);
    this.person.setLegalFirstNames(List.of("a", "b"));
    this.person.setPublicFirstNames(List.of("c", "d"));
    assertEquals("a b ?", this.person.toString());
  }

  @Test
  void toStringOnlyLegalLastName() {
    this.person.setDisambiguationID(null);
    this.person.setLegalLastName("a");
    assertEquals("? a", this.person.toString());
  }

  @Test
  void toStringOnlyPublicLastName() {
    this.person.setDisambiguationID(null);
    this.person.setPublicLastName("a");
    assertEquals("? a", this.person.toString());
  }

  @Test
  void toStringOnlyBothLastNames() {
    this.person.setDisambiguationID(null);
    this.person.setLegalLastName("a");
    this.person.setPublicLastName("b");
    assertEquals("? a", this.person.toString());
  }

  @Test
  void toStringFullName() {
    this.person.setDisambiguationID(null);
    this.person.setLegalFirstNames(List.of("a", "b"));
    this.person.setLegalLastName("c");
    assertEquals("a b c", this.person.toString());
  }

  @Test
  void toStringFullNameAndID() {
    this.person.setLegalFirstNames(List.of("a", "b"));
    this.person.setLegalLastName("c");
    assertEquals("a b c (#1)", this.person.toString());
  }

  @Test
  void toStringNicknamesNotShown() {
    this.person.setDisambiguationID(null);
    this.person.setNicknames(List.of("a", "b"));
    assertEquals("? ?", this.person.toString());
  }
}