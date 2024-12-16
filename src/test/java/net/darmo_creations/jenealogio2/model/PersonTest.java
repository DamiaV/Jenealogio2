package net.darmo_creations.jenealogio2.model;

import net.darmo_creations.jenealogio2.model.datetime.*;
import net.darmo_creations.jenealogio2.model.datetime.calendar.Calendar;
import net.darmo_creations.jenealogio2.model.datetime.calendar.GregorianCalendar;
import net.darmo_creations.jenealogio2.utils.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import java.util.*;
import java.util.stream.*;

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
    assertEquals(1, this.person.disambiguationID().orElseThrow());
  }

  @Test
  void setDisambiguationIDAcceptsNull() {
    this.person.setDisambiguationID(null);
    assertTrue(this.person.disambiguationID().isEmpty());
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1, -2, -3})
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
    assertEquals("Alice Bob", this.person.getJoinedLegalFirstNames().orElseThrow());
  }

  @Test
  void getJoinedLegalFirstNamesIsEmptyIfNoFirstNames() {
    assertTrue(this.person.getJoinedLegalFirstNames().isEmpty());
  }

  @Test
  void legalLastName() {
    this.person.setLegalLastName("Charlie");
    assertEquals("Charlie", this.person.legalLastName().orElseThrow());
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
    assertEquals("Alice Bob", this.person.getJoinedPublicFirstNames().orElseThrow());
  }

  @Test
  void getJoinedPublicFirstNamesIsEmptyIfNoFirstNames() {
    assertTrue(this.person.getJoinedPublicFirstNames().isEmpty());
  }

  @Test
  void publicLastName() {
    this.person.setPublicLastName("Charlie");
    assertEquals("Charlie", this.person.publicLastName().orElseThrow());
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
    assertEquals("Alice Bob", this.person.getJoinedNicknames().orElseThrow());
  }

  @Test
  void getJoinedNicknamesIsEmptyIfNoFirstNames() {
    assertTrue(this.person.getJoinedNicknames().isEmpty());
  }

  @Test
  void getLastNameReturnsLegalNameEvenIfPublicNameIsDefined() {
    this.person.setLegalLastName("Alice");
    this.person.setPublicLastName("Bob");
    assertEquals("Alice", this.person.getLastName().orElseThrow());
  }

  @Test
  void getLastNameReturnsPublicNameIfLegalNameIsUndefined() {
    this.person.setPublicLastName("Bob");
    assertEquals("Bob", this.person.getLastName().orElseThrow());
  }

  @Test
  void getLastNameReturnsIsEmptyIfNoLastNames() {
    assertTrue(this.person.getLastName().isEmpty());
  }

  @Test
  void getFirstNamesReturnsLegalNameEvenIfPublicNameIsDefined() {
    this.person.setLegalFirstNames(List.of("Alice", "Bob"));
    this.person.setPublicFirstNames(List.of("Charlie", "Dick"));
    assertEquals("Alice Bob", this.person.getFirstNames().orElseThrow());
  }

  @Test
  void getFirstNamesReturnsPublicNameIfLegalNameIsUndefined() {
    this.person.setPublicFirstNames(List.of("Charlie", "Dick"));
    assertEquals("Charlie Dick", this.person.getFirstNames().orElseThrow());
  }

  @Test
  void getFirstNamesReturnsIsEmptyIfNoFirstNames() {
    assertTrue(this.person.getFirstNames().isEmpty());
  }

  @Test
  void setAssignedGenderAtBirth() {
    final Gender gender = this.person.familyTree().genderRegistry().getEntry(new RegistryEntryKey("builtin:female"));
    this.person.setAssignedGenderAtBirth(gender);
    assertEquals(gender, this.person.assignedGenderAtBirth().orElseThrow());
  }

  @Test
  void setGender() {
    final Gender gender = this.person.familyTree().genderRegistry().getEntry(new RegistryEntryKey("builtin:female"));
    this.person.setGender(gender);
    assertEquals(gender, this.person.gender().orElseThrow());
  }

  @Test
  void genderReturnsAgabIfNotSet() {
    final Gender gender = this.person.familyTree().genderRegistry().getEntry(new RegistryEntryKey("builtin:female"));
    this.person.setAssignedGenderAtBirth(gender);
    assertEquals(gender, this.person.gender().orElseThrow());
  }

  @Test
  void setMainOccupation() {
    this.person.setMainOccupation("a");
    assertEquals("a", this.person.mainOccupation().orElseThrow());
  }

  @ParameterizedTest
  @EnumSource(ParentalRelationType.class)
  void hasAnyParents(ParentalRelationType type) {
    this.person.addParent(this.parent1, type);
    assertTrue(this.person.hasAnyParents());
  }

  @ParameterizedTest
  @EnumSource(ParentalRelationType.class)
  void addParent(ParentalRelationType type) {
    this.person.addParent(this.parent1, type);
    assertEquals(Set.of(this.parent1), this.person.parents(type));
  }

  @ParameterizedTest
  @EnumSource(ParentalRelationType.class)
  void addParentFailsIfMoreThanAllowed(ParentalRelationType type) {
    if (type.maxParentsCount().isPresent()) {
      for (int i = 0; i < type.maxParentsCount().orElseThrow(); i++)
        this.person.addParent(new Person(), type);
      assertThrows(IllegalArgumentException.class, () -> this.person.addParent(new Person(), type));
    }
  }

  @Test
  void addParentFailsIfParentAlreadyAdded_sameType() {
    this.person.addParent(this.parent1, ParentalRelationType.NON_BIOLOGICAL_PARENT);
    assertThrows(IllegalArgumentException.class,
        () -> this.person.addParent(this.parent1, ParentalRelationType.NON_BIOLOGICAL_PARENT));
  }

  @Test
  void addParentFailsIfParentAlreadyAdded_differentTypes() {
    this.person.addParent(this.parent1, ParentalRelationType.NON_BIOLOGICAL_PARENT);
    assertThrows(IllegalArgumentException.class,
        () -> this.person.addParent(this.parent1, ParentalRelationType.ADOPTIVE_PARENT));
  }

  @ParameterizedTest
  @MethodSource("provideArgsFor_addParentFailsIfAlready2GeneticParents")
  void addParentFailsIfAlready2GeneticParents(ParentalRelationType type1, ParentalRelationType type2, ParentalRelationType type3) {
    this.person.addParent(this.parent1, type1);
    this.person.addParent(this.parent2, type2);
    assertThrows(IllegalArgumentException.class, () -> this.person.addParent(new Person(), type3));
  }

  static Stream<Arguments> provideArgsFor_addParentFailsIfAlready2GeneticParents() {
    return Stream.of(
        Arguments.of(ParentalRelationType.BIOLOGICAL_PARENT, ParentalRelationType.BIOLOGICAL_PARENT, ParentalRelationType.EGG_DONOR),
        Arguments.of(ParentalRelationType.BIOLOGICAL_PARENT, ParentalRelationType.BIOLOGICAL_PARENT, ParentalRelationType.SPERM_DONOR),
        Arguments.of(ParentalRelationType.EGG_DONOR, ParentalRelationType.BIOLOGICAL_PARENT, ParentalRelationType.BIOLOGICAL_PARENT),
        Arguments.of(ParentalRelationType.EGG_DONOR, ParentalRelationType.BIOLOGICAL_PARENT, ParentalRelationType.SPERM_DONOR),
        Arguments.of(ParentalRelationType.SPERM_DONOR, ParentalRelationType.BIOLOGICAL_PARENT, ParentalRelationType.BIOLOGICAL_PARENT),
        Arguments.of(ParentalRelationType.SPERM_DONOR, ParentalRelationType.BIOLOGICAL_PARENT, ParentalRelationType.EGG_DONOR),

        Arguments.of(ParentalRelationType.BIOLOGICAL_PARENT, ParentalRelationType.BIOLOGICAL_PARENT, ParentalRelationType.EGG_DONOR),
        Arguments.of(ParentalRelationType.BIOLOGICAL_PARENT, ParentalRelationType.BIOLOGICAL_PARENT, ParentalRelationType.SPERM_DONOR),
        Arguments.of(ParentalRelationType.BIOLOGICAL_PARENT, ParentalRelationType.EGG_DONOR, ParentalRelationType.BIOLOGICAL_PARENT),
        Arguments.of(ParentalRelationType.BIOLOGICAL_PARENT, ParentalRelationType.EGG_DONOR, ParentalRelationType.SPERM_DONOR),
        Arguments.of(ParentalRelationType.BIOLOGICAL_PARENT, ParentalRelationType.SPERM_DONOR, ParentalRelationType.BIOLOGICAL_PARENT),
        Arguments.of(ParentalRelationType.BIOLOGICAL_PARENT, ParentalRelationType.SPERM_DONOR, ParentalRelationType.EGG_DONOR)
    );
  }

  @ParameterizedTest
  @EnumSource(ParentalRelationType.class)
  void addParentUpdatesParent(ParentalRelationType type) {
    this.person.addParent(this.parent1, type);
    assertSame(this.person, this.parent1.children(type).iterator().next());
  }

  @ParameterizedTest
  @EnumSource(ParentalRelationType.class)
  void removeParentUpdatesParent(ParentalRelationType type) {
    this.person.addParent(this.parent1, type);
    this.person.removeParent(this.parent1);
    assertTrue(this.parent1.children(type).isEmpty());
  }

  @Test
  void getGeneticParentsReturnsOnlyGeneticParents_0() {
    assertEquals(Set.of(), this.person.getGeneticParents());
  }

  @ParameterizedTest
  @MethodSource("provideArgs_oneGeneticType")
  void getGeneticParentsReturnsOnlyGeneticParents_1(ParentalRelationType type) {
    this.person.addParent(this.parent1, type);
    assertEquals(Set.of(this.parent1), this.person.getGeneticParents());
  }

  static Stream<Arguments> provideArgs_oneGeneticType() {
    return Arrays.stream(ParentalRelationType.GENETIC_RELATIONS).map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource("provideArgs_twoGeneticTypes")
  void getGeneticParentsReturnsOnlyGeneticParents_2(ParentalRelationType type1, ParentalRelationType type2) {
    this.person.addParent(this.parent1, type1);
    this.person.addParent(this.parent2, type2);
    assertEquals(Set.of(this.parent1, this.parent2), this.person.getGeneticParents());
  }

  static Stream<Arguments> provideArgs_twoGeneticTypes() {
    return Stream.of(
        Arguments.of(ParentalRelationType.BIOLOGICAL_PARENT, ParentalRelationType.BIOLOGICAL_PARENT),
        Arguments.of(ParentalRelationType.EGG_DONOR, ParentalRelationType.BIOLOGICAL_PARENT),
        Arguments.of(ParentalRelationType.SPERM_DONOR, ParentalRelationType.BIOLOGICAL_PARENT),
        Arguments.of(ParentalRelationType.SPERM_DONOR, ParentalRelationType.EGG_DONOR)
    );
  }

  @ParameterizedTest
  @MethodSource("provideArgsfor_getPartnersAndChildren")
  void getPartnersAndChildren_groupsByType(ParentalRelationType type1, ParentalRelationType type2, ParentalRelationType type3) {
    this.person.addParent(this.parent1, type1);
    this.person.addParent(this.parent2, type2);
    final Person person2 = new Person();
    person2.addParent(this.parent1, type1);
    person2.addParent(this.parent2, type2);
    person2.setDisambiguationID(4);
    final Person person3 = new Person();
    person3.addParent(this.parent1, type3);
    person3.setDisambiguationID(5);
    assertEquals(Set.of(new Pair<>(
        Set.of(this.parent2),
        Set.of(person2, this.person)
    ), new Pair<>(
        Set.of(),
        Set.of(person3)
    )), new HashSet<>(this.parent1.getPartnersAndChildren()));
  }

  @ParameterizedTest
  @MethodSource("provideArgsfor_getPartnersAndChildren")
  void getPartnersAndChildren_oneParent(ParentalRelationType type1, ParentalRelationType type2, ParentalRelationType ignored) {
    this.person.addParent(this.parent1, type1);
    final Person person2 = new Person();
    person2.addParent(this.parent1, type2);
    assertEquals(List.of(new Pair<>(
        Set.of(),
        Set.of(person2, this.person)
    )), this.parent1.getPartnersAndChildren());
  }

  static Stream<Arguments> provideArgsfor_getPartnersAndChildren() {
    return Stream.of(
        Arguments.of(ParentalRelationType.BIOLOGICAL_PARENT, ParentalRelationType.BIOLOGICAL_PARENT, ParentalRelationType.ADOPTIVE_PARENT),
        Arguments.of(ParentalRelationType.BIOLOGICAL_PARENT, ParentalRelationType.NON_BIOLOGICAL_PARENT, ParentalRelationType.FOSTER_PARENT),
        Arguments.of(ParentalRelationType.NON_BIOLOGICAL_PARENT, ParentalRelationType.BIOLOGICAL_PARENT, ParentalRelationType.GODPARENT),
        Arguments.of(ParentalRelationType.NON_BIOLOGICAL_PARENT, ParentalRelationType.NON_BIOLOGICAL_PARENT, ParentalRelationType.ADOPTIVE_PARENT),
        Arguments.of(ParentalRelationType.ADOPTIVE_PARENT, ParentalRelationType.ADOPTIVE_PARENT, ParentalRelationType.BIOLOGICAL_PARENT),
        Arguments.of(ParentalRelationType.FOSTER_PARENT, ParentalRelationType.FOSTER_PARENT, ParentalRelationType.NON_BIOLOGICAL_PARENT),
        Arguments.of(ParentalRelationType.GODPARENT, ParentalRelationType.GODPARENT, ParentalRelationType.BIOLOGICAL_PARENT)
    );
  }

  @ParameterizedTest
  @MethodSource("provideArgs_twoGeneticTypes")
  void getPartnersAndGeneticChildren_exactSameParents_sameType(ParentalRelationType type1, ParentalRelationType type2) {
    this.person.addParent(this.parent1, type1);
    this.person.addParent(this.parent2, type2);
    final Person person2 = new Person();
    person2.addParent(this.parent1, type1);
    person2.addParent(this.parent2, type2);
    assertEquals(Map.of(
        Optional.of(this.parent2),
        Set.of(person2, this.person)
    ), this.parent1.getPartnersAndGeneticChildren());
  }

  @ParameterizedTest
  @MethodSource("provideArgs_twoGeneticTypes")
  void getPartnersAndGeneticChildren_exactSameParents_differentTypes(ParentalRelationType type1, ParentalRelationType type2) {
    this.person.addParent(this.parent1, type1);
    this.person.addParent(this.parent2, type2);
    final Person person2 = new Person();
    person2.addParent(this.parent1, type2);
    person2.addParent(this.parent2, type1);
    assertEquals(Map.of(
        Optional.of(this.parent2),
        Set.of(person2, this.person)
    ), this.parent1.getPartnersAndGeneticChildren());
  }

  @Test
  void getPartnersAndGeneticChildren_noChildrenNoPartner() {
    assertEquals(Map.of(), this.person.getPartnersAndGeneticChildren());
  }

  @Test
  void getPartnersAndGeneticChildren_noChildrenWithPartner() {
    final DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendar.NAME).getDate(2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    final LifeEvent l = new LifeEvent(date, new LifeEventTypeRegistry().getEntry(new RegistryEntryKey("builtin:marriage")));
    l.setActors(Set.of(this.parent1, this.parent2));
    assertEquals(Map.of(), this.parent1.getPartnersAndGeneticChildren());
  }

  @Test
  void getPartnersAndGeneticChildren_onlyNonGeneticChildrenWithPartner() {
    this.person.addParent(this.parent1, ParentalRelationType.NON_BIOLOGICAL_PARENT);
    this.person.addParent(this.parent2, ParentalRelationType.NON_BIOLOGICAL_PARENT);
    assertEquals(Map.of(), this.parent1.getPartnersAndGeneticChildren());
  }

  @ParameterizedTest
  @MethodSource("provideArgs_twoGeneticTypes")
  void getPartnersAndGeneticChildren_oneCommonParent(ParentalRelationType type1, ParentalRelationType type2) {
    this.person.addParent(this.parent1, type1);
    this.person.addParent(this.parent2, type2);
    final Person person2 = new Person();
    person2.addParent(this.parent1, type1);
    final Person parent3 = new Person();
    person2.addParent(parent3, type2);
    assertEquals(Map.of(
        Optional.of(this.parent2),
        Set.of(this.person),
        Optional.of(parent3),
        Set.of(person2)
    ), this.parent1.getPartnersAndGeneticChildren());
  }

  @ParameterizedTest
  @MethodSource("provideArgs_oneGeneticType")
  void getPartnersAndGeneticChildren_noPartner(ParentalRelationType type) {
    this.person.addParent(this.parent1, type);
    final Person person2 = new Person();
    person2.addParent(this.parent1, type);
    assertEquals(Map.of(
        Optional.empty(),
        Set.of(person2, this.person)
    ), this.parent1.getPartnersAndGeneticChildren());
  }

  @ParameterizedTest
  @MethodSource("provideArgs_twoGeneticTypes")
  void getSameGeneticParentsSiblings(ParentalRelationType type1, ParentalRelationType type2) {
    this.person.addParent(this.parent1, type1);
    this.person.addParent(this.parent2, type2);
    final Person person2 = new Person();
    person2.addParent(this.parent1, type1);
    person2.addParent(this.parent2, type2);
    assertEquals(Set.of(person2), this.person.getSameGeneticParentsSiblings());
    assertEquals(Set.of(this.person), person2.getSameGeneticParentsSiblings());
  }

  @ParameterizedTest
  @MethodSource("provideArgs_twoGeneticTypes")
  void getSameGeneticParentsSiblings_emptyIfOnlyOneCommon(ParentalRelationType type1, ParentalRelationType type2) {
    this.person.addParent(this.parent1, type1);
    this.person.addParent(this.parent2, type2);
    final Person person2 = new Person();
    person2.addParent(this.parent1, type1);
    person2.addParent(new Person(), type2);
    assertTrue(this.person.getSameGeneticParentsSiblings().isEmpty());
    assertTrue(person2.getSameGeneticParentsSiblings().isEmpty());
  }

  @ParameterizedTest
  @MethodSource("provideArgs_twoGeneticTypes")
  void getSameGeneticParentsSiblings_singleParent(ParentalRelationType type1, ParentalRelationType type2) {
    this.person.addParent(this.parent1, type1);
    final Person person2 = new Person();
    person2.addParent(this.parent1, type2);
    assertEquals(Set.of(person2), this.person.getSameGeneticParentsSiblings());
    assertEquals(Set.of(this.person), person2.getSameGeneticParentsSiblings());
  }

  @ParameterizedTest
  @MethodSource("provideArgsFor_getSiblings")
  void getSiblings(ParentalRelationType type1, ParentalRelationType type2, ParentalRelationType type3, ParentalRelationType type4) {
    this.person.addParent(this.parent1, type1);
    this.person.addParent(this.parent2, type2);
    final Person person2 = new Person();
    person2.setDisambiguationID(5);
    person2.addParent(this.parent1, type3);
    final Person parent3 = new Person();
    parent3.setDisambiguationID(6);
    person2.addParent(parent3, type4);
    final Person person3 = new Person();
    person3.setDisambiguationID(7);
    person3.addParent(parent3, type1);
    person3.addParent(this.parent2, type3);
    assertEquals(Set.of(
        new Pair<>(Set.of(this.parent1, parent3), Set.of(person2)),
        new Pair<>(Set.of(parent3, this.parent2), Set.of(person3))
    ), new HashSet<>(this.person.getSiblings()));
  }

  static Stream<Arguments> provideArgsFor_getSiblings() {
    return Stream.of( // Non-exhaustive permutations list
        Arguments.of(ParentalRelationType.BIOLOGICAL_PARENT, ParentalRelationType.BIOLOGICAL_PARENT, ParentalRelationType.NON_BIOLOGICAL_PARENT, ParentalRelationType.ADOPTIVE_PARENT),
        Arguments.of(ParentalRelationType.BIOLOGICAL_PARENT, ParentalRelationType.BIOLOGICAL_PARENT, ParentalRelationType.FOSTER_PARENT, ParentalRelationType.ADOPTIVE_PARENT),
        Arguments.of(ParentalRelationType.BIOLOGICAL_PARENT, ParentalRelationType.NON_BIOLOGICAL_PARENT, ParentalRelationType.NON_BIOLOGICAL_PARENT, ParentalRelationType.BIOLOGICAL_PARENT),
        Arguments.of(ParentalRelationType.ADOPTIVE_PARENT, ParentalRelationType.BIOLOGICAL_PARENT, ParentalRelationType.NON_BIOLOGICAL_PARENT, ParentalRelationType.FOSTER_PARENT),
        Arguments.of(ParentalRelationType.FOSTER_PARENT, ParentalRelationType.BIOLOGICAL_PARENT, ParentalRelationType.NON_BIOLOGICAL_PARENT, ParentalRelationType.FOSTER_PARENT)
    );
  }

  @ParameterizedTest
  @MethodSource("provideArgsFor_getSiblings_ignoresDonorsSurrogatesAndGodparents")
  void getSiblings_ignoresDonorsSurrogatesAndGodparents(ParentalRelationType type) {
    this.person.addParent(this.parent1, ParentalRelationType.BIOLOGICAL_PARENT);
    final Person person2 = new Person();
    person2.addParent(this.parent1, type);
    assertEquals(List.of(), this.person.getSiblings());
  }

  static Stream<Arguments> provideArgsFor_getSiblings_ignoresDonorsSurrogatesAndGodparents() {
    return Stream.of(
        Arguments.of(ParentalRelationType.SURROGATE_PARENT),
        Arguments.of(ParentalRelationType.SPERM_DONOR),
        Arguments.of(ParentalRelationType.EGG_DONOR),
        Arguments.of(ParentalRelationType.GODPARENT)
    );
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
    assertEquals(date, this.person.getBirthDate().orElseThrow());
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
    assertEquals(date, this.person.getDeathDate().orElseThrow());
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