package net.darmo_creations.jenealogio2.model;

import net.darmo_creations.jenealogio2.io.*;
import net.darmo_creations.jenealogio2.model.datetime.*;
import net.darmo_creations.jenealogio2.model.datetime.calendar.*;
import net.darmo_creations.jenealogio2.model.datetime.calendar.Calendar;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AttachedDocumentTest {
  private static final Path IMG_PATH = Path.of("/net/darmo_creations/jenealogio2/test.png");
  private AttachedDocument doc, docWithAuthor, docWithAnnotations;
  private Person author, annotatedPerson;
  private LifeEvent annotatedEvent;

  @BeforeEach
  void setUp() {
    this.doc = new AttachedDocument(IMG_PATH, null, null);

    this.docWithAuthor = new AttachedDocument(IMG_PATH, null, null);
    this.author = new Person();
    this.author.setDisambiguationID(1);
    this.docWithAuthor.addAuthor(this.author, 0);

    this.docWithAnnotations = new AttachedDocument(IMG_PATH, null, null);
    this.annotatedPerson = new Person();
    this.annotatedPerson.setDisambiguationID(2);
    final DateTime date = new DateTimeWithPrecision(Calendar.forName(GregorianCalendarSystem.NAME).getDate(null, 2024, 1, 1, 0, 0), DateTimePrecision.EXACT);
    this.annotatedEvent = new LifeEvent(date, new LifeEventTypeRegistry().getEntry(new RegistryEntryKey("builtin:marriage")));
    for (final AnnotationType annotationType : AnnotationType.values()) {
      this.docWithAnnotations.annotateObject(annotationType, this.annotatedPerson, "note 1 " + annotationType);
      this.docWithAnnotations.annotateObject(annotationType, this.annotatedEvent, "note 2 " + annotationType);
    }
  }

  @Test
  void path() {
    assertEquals(IMG_PATH, this.doc.path());
  }

  @Test
  void setPathDoesNotUpdateDocumentProperties() {
    this.doc.setPath(Path.of("/net/darmo_creations/jenealogio2/images/app_icon.png"));
    assertEquals("test.png", this.doc.fileName());
    assertEquals("test", this.doc.name());
  }

  @Test
  void fileName() {
    assertEquals("test.png", this.doc.fileName());
  }

  @Test
  void normalizedFileExtension() {
    assertEquals(Optional.of(".png"), this.doc.normalizedFileExtension());
  }

  @Test
  void name() {
    assertEquals("test", this.doc.name());
  }

  @Test
  void setNameUpdatesNameAndFileName() {
    this.doc.setName("test");
    assertEquals("test", this.doc.name());
    assertEquals("test.png", this.doc.fileName());
  }

  @Test
  void setNameKeepsExtension() {
    this.doc.setName("test.jpg");
    assertEquals("test.jpg.png", this.doc.fileName());
  }

  @Test
  void setNameDoesNotUpdatePath() {
    this.doc.setName("test");
    assertEquals(IMG_PATH, this.doc.path());
  }

  @Test
  void authors() {
    assertTrue(this.doc.authors().isEmpty());
    assertEquals(List.of(this.author), this.docWithAuthor.authors());
  }

  @Test
  void addAuthorAddsAuthor() {
    final Person p1 = new Person();
    p1.setDisambiguationID(1);
    final Person p2 = new Person();
    p2.setDisambiguationID(2);
    this.doc.addAuthor(p1, 0);
    this.doc.addAuthor(p2, 1);
    assertEquals(List.of(p1, p2), this.doc.authors());
  }

  @Test
  void addAuthorAddsDocumentToAuthor() {
    final Person p = new Person();
    this.doc.addAuthor(p, 0);
    assertEquals(Set.of(this.doc), p.authoredDocuments());
  }

  @Test
  void addAuthorThrowsIfInvalidIndex() {
    assertThrows(IndexOutOfBoundsException.class, () -> this.doc.addAuthor(new Person(), -1));
    assertThrows(IndexOutOfBoundsException.class, () -> this.doc.addAuthor(new Person(), 1));
  }

  @Test
  void removeAuthorRemovesAuthor() {
    this.docWithAuthor.removeAuthor(this.author);
    assertTrue(this.docWithAuthor.authors().isEmpty());
  }

  @Test
  void removeAuthorRemovesDocumentFromAuthor() {
    this.docWithAuthor.removeAuthor(this.author);
    assertTrue(this.author.authoredDocuments().isEmpty());
  }

  @Test
  void clearAuthorsRemovesAuthors() {
    this.docWithAuthor.clearAuthors();
    assertTrue(this.docWithAuthor.authors().isEmpty());
  }

  @Test
  void clearAuthorsRemovesDocumentFromAuthors() {
    this.docWithAuthor.clearAuthors();
    assertTrue(this.author.authoredDocuments().isEmpty());
  }

  @ParameterizedTest
  @EnumSource(AnnotationType.class)
  void annotatedObjects(AnnotationType annotationType) {
    assertEquals(
        Map.of(
            this.annotatedPerson, Optional.of("note 1 " + annotationType),
            this.annotatedEvent, Optional.of("note 2 " + annotationType)
        ),
        this.docWithAnnotations.annotatedObjects(annotationType)
    );
  }

  @ParameterizedTest
  @EnumSource(AnnotationType.class)
  void annotatedPersons(AnnotationType annotationType) {
    assertEquals(Map.of(this.annotatedPerson, Optional.of("note 1 " + annotationType)),
        this.docWithAnnotations.annotatedPersons(annotationType));
  }

  @ParameterizedTest
  @EnumSource(AnnotationType.class)
  void annotatedEvents(AnnotationType annotationType) {
    assertEquals(Map.of(this.annotatedEvent, Optional.of("note 2 " + annotationType)),
        this.docWithAnnotations.annotatedEvents(annotationType));
  }

  @ParameterizedTest
  @EnumSource(AnnotationType.class)
  void annotateObjectAddsObject(AnnotationType annotationType) {
    final Person p = new Person();
    this.doc.annotateObject(annotationType, p, "note");
    assertEquals(Map.of(p, Optional.of("note")), this.doc.annotatedObjects(annotationType));
  }

  @ParameterizedTest
  @EnumSource(AnnotationType.class)
  void annotateObjectAddsDocumentToObject(AnnotationType annotationType) {
    final Person p = new Person();
    this.doc.annotateObject(annotationType, p, "note");
    assertEquals(Set.of(this.doc), p.getAnnotatedInDocuments(annotationType));
  }

  @ParameterizedTest
  @EnumSource(AnnotationType.class)
  void removeObjectRemovesObjectAnnotation(AnnotationType annotationType) {
    this.docWithAnnotations.removeObjectAnnotation(annotationType, this.annotatedPerson);
    assertEquals(Map.of(this.annotatedEvent, Optional.of("note 2 " + annotationType)),
        this.docWithAnnotations.annotatedObjects(annotationType));
  }

  @ParameterizedTest
  @EnumSource(AnnotationType.class)
  void removeObjectRemovesDocumentFromObjectAnnotation(AnnotationType annotationType) {
    this.docWithAnnotations.removeObjectAnnotation(annotationType, this.annotatedPerson);
    assertEquals(Map.of(this.annotatedEvent, Optional.of("note 2 " + annotationType)),
        this.docWithAnnotations.annotatedObjects(annotationType));
  }

  @Test
  void removeObjectOnlyImpactsAnnotationType() {
    this.docWithAnnotations.removeObjectAnnotation(AnnotationType.MENTION, this.annotatedPerson);
    assertEquals(
        Map.of(
            this.annotatedPerson, Optional.of("note 1 VISIBLE"),
            this.annotatedEvent, Optional.of("note 2 VISIBLE")
        ),
        this.docWithAnnotations.annotatedObjects(AnnotationType.VISIBLE)
    );
  }

  @ParameterizedTest
  @EnumSource(AnnotationType.class)
  void clearObjectAnnotationsRemovesAnnotations(AnnotationType annotationType) {
    this.docWithAnnotations.clearObjectAnnotations();
    assertTrue(this.docWithAnnotations.annotatedObjects(annotationType).isEmpty());
  }

  @ParameterizedTest
  @EnumSource(AnnotationType.class)
  void clearObjectAnnotationsRemovesDocumentFromObjects(AnnotationType annotationType) {
    this.docWithAnnotations.clearObjectAnnotations();
    assertTrue(this.annotatedPerson.getAnnotatedInDocuments(annotationType).isEmpty());
    assertTrue(this.annotatedEvent.getAnnotatedInDocuments(annotationType).isEmpty());
  }
}