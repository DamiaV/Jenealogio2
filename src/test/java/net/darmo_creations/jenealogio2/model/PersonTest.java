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

  @BeforeEach
  void setUp() {
    this.person = new Person();
    new FamilyTree("tree").addPerson(this.person);
  }

  @Test
  void setFamilyTree() {
    FamilyTree t = new FamilyTree("t");
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
    DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    LifeEvent l = new LifeEvent(date, new LifeEventTypeRegistry().getEntry(new RegistryEntryKey("builtin:death")));
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
  @Disabled("Not implemented yet")
  void matchesName() {
    // TODO
  }

  @Test
  void setGender() {
    Gender gender = this.person.familyTree().genderRegistry().getEntry(new RegistryEntryKey("builtin:female"));
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
  @Disabled("Not implemented yet")
  void parents() {
    // TODO
  }

  @Test
  @Disabled("Not implemented yet")
  void getParentIndex() {
    // TODO
  }

  @Test
  @Disabled("Not implemented yet")
  void hasSameParents() {
    // TODO
  }

  @Test
  @Disabled("Not implemented yet")
  void hasAnyParents() {
    // TODO
  }

  @Test
  @Disabled("Not implemented yet")
  void hasBothParents() {
    // TODO
  }

  @Test
  @Disabled("Not implemented yet")
  void setParent() {
    // TODO
  }

  @Test
  @Disabled("Not implemented yet")
  void removeParent() {
    // TODO
  }

  @Test
  @Disabled("Not implemented yet")
  void children() {
    // TODO
  }

  @Test
  @Disabled("Not implemented yet")
  void getPartnersAndChildren() {
    // TODO
  }

  @Test
  @Disabled("Not implemented yet")
  void getSameParentsSiblings() {
    // TODO
  }

  @Test
  @Disabled("Not implemented yet")
  void getAllSiblings() {
    // TODO
  }

  @Test
  @Disabled("Not implemented yet")
  void getRelatives() {
    // TODO
  }

  @Test
  @Disabled("Not implemented yet")
  void addRelative() {
    // TODO
  }

  @Test
  @Disabled("Not implemented yet")
  void removeRelative() {
    // TODO
  }

  @Test
  @Disabled("Not implemented yet")
  void nonBiologicalChildren() {
    // TODO
  }

  @Test
  @Disabled("Not implemented yet")
  void getBirthDate() {
    // TODO
  }

  @Test
  @Disabled("Not implemented yet")
  void getDeathDate() {
    // TODO
  }

  @Test
  @Disabled("Not implemented yet")
  void lifeEventsIsSortedByDates() {
    // TODO
  }

  @Test
  @Disabled("Not implemented yet")
  void getLifeEventsAsActor() {
    // TODO
  }

  @Test
  @Disabled("Not implemented yet")
  void getLifeEventsAsWitness() {
    // TODO
  }

  @Test
  void addLifeEvent() {
    DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    LifeEvent l = new LifeEvent(date, new LifeEventTypeRegistry().getEntry(new RegistryEntryKey("builtin:birth")));
    this.person.addLifeEvent(l);
    assertSame(l, this.person.lifeEvents().get(0));
  }

  @Test
  @Disabled("Not implemented yet")
  void removeLifeEvent() {
    // TODO
  }

  @Test
  @Disabled("Not implemented yet")
  void testToString() {
    // TODO
  }
}