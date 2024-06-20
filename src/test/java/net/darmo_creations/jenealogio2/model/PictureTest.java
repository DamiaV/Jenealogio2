package net.darmo_creations.jenealogio2.model;

import javafx.scene.image.*;
import net.darmo_creations.jenealogio2.model.datetime.*;
import net.darmo_creations.jenealogio2.model.datetime.calendar.*;
import org.junit.jupiter.api.*;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({ "OptionalGetWithoutIsPresent", "DataFlowIssue" })
class PictureTest {
  private Picture p;

  @BeforeEach
  void setUp() throws IOException {
    Image image = getImage("/net/darmo_creations/jenealogio2/images/app_icon.png");
    this.p = new Picture(
        image,
        "app_icon.png",
        "description",
        new DateTimeWithPrecision(Calendars.GREGORIAN.getDate(1234, 5, 6, 7, 8), DateTimePrecision.EXACT)
    );
  }

  @Test
  void testNullImageThrowsError() {
    DateTimeWithPrecision d = new DateTimeWithPrecision(Calendars.GREGORIAN.getDate(1234, 5, 6, 7, 8), DateTimePrecision.EXACT);
    assertThrows(NullPointerException.class, () -> new Picture(null, "a", "b", d));
  }

  @Test
  void testNullNameThrowsError() throws IOException {
    Image image = getImage("/net/darmo_creations/jenealogio2/images/app_icon.png");
    DateTimeWithPrecision d = new DateTimeWithPrecision(Calendars.GREGORIAN.getDate(1234, 5, 6, 7, 8), DateTimePrecision.EXACT);
    assertThrows(NullPointerException.class, () -> new Picture(image, null, "b", d));
  }

  @Test
  void testImage() {
    assertNotNull(this.p.image());
  }

  @Test
  void testName() {
    assertEquals("app_icon.png", this.p.name());
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
    DateTimeWithPrecision d = new DateTimeWithPrecision(Calendars.GREGORIAN.getDate(1234, 5, 6, 7, 8), DateTimePrecision.EXACT);
    assertEquals(d, this.p.date().get());
  }

  @Test
  void testSetDate() {
    DateTimeWithPrecision d = new DateTimeWithPrecision(Calendars.GREGORIAN.getDate(8765, 4, 3, 2, 1), DateTimePrecision.EXACT);
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
    Image image = getImage("/net/darmo_creations/jenealogio2/images/app_icon.png");
    Picture pp = new Picture(
        image,
        "app_icon.png",
        "description",
        new DateTimeWithPrecision(Calendars.GREGORIAN.getDate(1234, 5, 6, 7, 8), DateTimePrecision.EXACT)
    );
    assertEquals(this.p, pp);
  }

  @Test
  void testEqualsAllDifferentButName() throws IOException {
    Image image = getImage("/net/darmo_creations/jenealogio2/images/add_person_image.png");
    Picture pp = new Picture(
        image,
        "app_icon.png",
        "desc",
        new DateTimeWithPrecision(Calendars.GREGORIAN.getDate(8765, 4, 3, 2, 1), DateTimePrecision.EXACT)
    );
    assertEquals(this.p, pp);
  }

  @Test
  void testEqualsAllSameButName() throws IOException {
    Image image = getImage("/net/darmo_creations/jenealogio2/images/app_icon.png");
    Picture pp = new Picture(
        image,
        "appicon.png",
        "description",
        new DateTimeWithPrecision(Calendars.GREGORIAN.getDate(1234, 5, 6, 7, 8), DateTimePrecision.EXACT)
    );
    assertNotEquals(this.p, pp);
  }

  public static Image getImage(String path) throws IOException {
    Image image;
    try (var stream = PictureTest.class.getResourceAsStream(path)) {
      if (stream == null) {
        fail("Missing image: " + path);
      }
      image = new Image(stream);
    }
    return image;
  }
}