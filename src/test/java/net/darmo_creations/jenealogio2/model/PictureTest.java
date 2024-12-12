package net.darmo_creations.jenealogio2.model;

import javafx.scene.image.*;
import net.darmo_creations.jenealogio2.model.datetime.*;
import net.darmo_creations.jenealogio2.model.datetime.calendar.*;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"OptionalGetWithoutIsPresent", "DataFlowIssue"})
class PictureTest {
  private Picture p;

  @BeforeEach
  void setUp() throws IOException {
    final Image image = getImage("/net/darmo_creations/jenealogio2/images/app_icon.png");
    this.p = new Picture(
        image,
        Path.of("app_icon.png"),
        "description",
        new DateTimeWithPrecision(Calendars.GREGORIAN.getDate(1234, 5, 6, 7, 8), DateTimePrecision.EXACT)
    );
  }

  @Test
  void testNullNameThrowsError() throws IOException {
    final Image image = getImage("/net/darmo_creations/jenealogio2/images/app_icon.png");
    final DateTimeWithPrecision d = new DateTimeWithPrecision(Calendars.GREGORIAN.getDate(1234, 5, 6, 7, 8), DateTimePrecision.EXACT);
    assertThrows(NullPointerException.class, () -> new Picture(image, null, "b", d));
  }

  @Test
  void testImage() {
    assertTrue(this.p.image().isPresent());
  }

  @Test
  void testFileName() {
    assertEquals("app_icon.png", this.p.fileName());
  }

  @Test
  void testNormalizedFileExtension() {
    assertEquals(".png", this.p.normalizedFileExtension().get());
  }

  @Test
  void testNormalizedFileExtensionDifferentCase() throws IOException {
    final Image image = getImage("/net/darmo_creations/jenealogio2/images/app_icon.png");
    final Picture p = new Picture(
        image,
        Path.of("app_icon.PNG"),
        "description",
        new DateTimeWithPrecision(Calendars.GREGORIAN.getDate(1234, 5, 6, 7, 8), DateTimePrecision.EXACT)
    );
    assertEquals(".png", p.normalizedFileExtension().get());
  }

  @Test
  void testName() {
    assertEquals("app_icon", this.p.name());
  }

  @Test
  void testSetName() {
    this.p.setName("test");
    assertEquals("test", this.p.name());
  }

  @Test
  void testSetNameUpdatesFileName() {
    this.p.setName("test");
    assertEquals("test.png", this.p.fileName());
  }

  @Test
  void testSetNameKeepsOriginalExtension() throws IOException {
    final Image image = getImage("/net/darmo_creations/jenealogio2/images/app_icon.png");
    final Picture p = new Picture(
        image,
        Path.of("app_icon.PNG"),
        "description",
        new DateTimeWithPrecision(Calendars.GREGORIAN.getDate(1234, 5, 6, 7, 8), DateTimePrecision.EXACT)
    );
    p.setName("test");
    assertEquals("test.PNG", p.fileName());
  }

  @Test
  void testDescription() {
    assertEquals("description", this.p.description().get());
  }

  @Test
  void testSetDescription() {
    this.p.setDescription("desc");
    assertEquals("desc", this.p.description().get());
  }

  @Test
  void testSetDescriptionNull() {
    this.p.setDescription(null);
    assertTrue(this.p.description().isEmpty());
  }

  @Test
  void testDate() {
    final DateTimeWithPrecision d = new DateTimeWithPrecision(Calendars.GREGORIAN.getDate(1234, 5, 6, 7, 8), DateTimePrecision.EXACT);
    assertEquals(d, this.p.date().get());
  }

  @Test
  void testSetDate() {
    final DateTimeWithPrecision d = new DateTimeWithPrecision(Calendars.GREGORIAN.getDate(8765, 4, 3, 2, 1), DateTimePrecision.EXACT);
    this.p.setDate(d);
    assertEquals(d, this.p.date().get());
  }

  @Test
  void testSetDateNull() {
    this.p.setDate(null);
    assertTrue(this.p.date().isEmpty());
  }

  @Test
  void testEqualsAllSame() throws IOException {
    final Image image = getImage("/net/darmo_creations/jenealogio2/images/app_icon.png");
    final Picture pp = new Picture(
        image,
        Path.of("app_icon.png"),
        "description",
        new DateTimeWithPrecision(Calendars.GREGORIAN.getDate(1234, 5, 6, 7, 8), DateTimePrecision.EXACT)
    );
    assertEquals(this.p, pp);
  }

  @Test
  void testEqualsAllDifferentButName() throws IOException {
    final Image image = getImage("/net/darmo_creations/jenealogio2/images/add_person_image.png");
    final Picture pp = new Picture(
        image,
        Path.of("app_icon.png"),
        "desc",
        new DateTimeWithPrecision(Calendars.GREGORIAN.getDate(8765, 4, 3, 2, 1), DateTimePrecision.EXACT)
    );
    assertEquals(this.p, pp);
  }

  @Test
  void testEqualsAllSameButName() throws IOException {
    final Image image = getImage("/net/darmo_creations/jenealogio2/images/app_icon.png");
    final Picture pp = new Picture(
        image,
        Path.of("appicon.png"),
        "description",
        new DateTimeWithPrecision(Calendars.GREGORIAN.getDate(1234, 5, 6, 7, 8), DateTimePrecision.EXACT)
    );
    assertNotEquals(this.p, pp);
  }

  public static Image getImage(String path) throws IOException {
    final Image image;
    try (final var stream = PictureTest.class.getResourceAsStream(path)) {
      if (stream == null) {
        fail("Missing image: " + path);
      }
      image = new Image(stream);
    }
    return image;
  }
}