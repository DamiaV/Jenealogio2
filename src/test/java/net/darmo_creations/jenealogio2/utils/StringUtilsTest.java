package net.darmo_creations.jenealogio2.utils;

import javafx.scene.paint.*;
import javafx.scene.text.*;
import net.darmo_creations.jenealogio2.io.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("OptionalGetWithoutIsPresent")
class StringUtilsTest {
  @Test
  void testStripNullableEmpty() {
    assertTrue(StringUtils.stripNullable("").isEmpty());
  }

  @ParameterizedTest
  @ValueSource(strings = { " ", "\t", "\n", "\r", "\f", "\u000b" })
  void testStripNullableBlank(String s) {
    assertTrue(StringUtils.stripNullable(s).isEmpty());
  }

  @Test
  void testStripNullableBlankLeading() {
    assertEquals("a", StringUtils.stripNullable(" a").get());
  }

  @Test
  void testStripNullableBlankTrailing() {
    assertEquals("a", StringUtils.stripNullable("a ").get());
  }

  @Test
  void testStripNullableBlankLeadingTrailing() {
    assertEquals("a", StringUtils.stripNullable(" a ").get());
  }

  @Test
  void testStripNullableNoBlank() {
    assertEquals("a", StringUtils.stripNullable("a").get());
  }

  @Test
  void testFormatOneArg() {
    assertEquals("aba", StringUtils.format("a{s}a", new FormatArg("s", "b")));
  }

  @Test
  void testFormatOneArgTwice() {
    assertEquals("ababa", StringUtils.format("a{s}a{s}a", new FormatArg("s", "b")));
  }

  @Test
  void testFormatSeveralArgs() {
    assertEquals("abaca", StringUtils.format("a{s1}a{s2}a",
        new FormatArg("s1", "b"), new FormatArg("s2", "c")));
  }

  @Test
  void testFormatDuplicateArgs() {
    assertThrows(IllegalArgumentException.class, () -> StringUtils.format("a{s}a",
        new FormatArg("s", "b"), new FormatArg("s", "c")));
  }

  @Test
  void testFormatMissingArg() {
    assertEquals("a{s}a", StringUtils.format("a{s}a", new FormatArg("c", "b")));
  }

  @Test
  void testFormatUnnecessaryArg() {
    assertEquals("aa", StringUtils.format("aa", new FormatArg("s", "b")));
  }

  @Test
  void testParseTextOneLine() {
    assertTextsEqual(
        List.of("this is some text"),
        StringUtils.parseText("this is some text", url -> {
        })
    );
  }

  @Test
  void testParseTextMultipleLines() {
    assertTextsEqual(
        List.of("line 1\n", "line 2"),
        StringUtils.parseText("line 1\nline 2", url -> {
        })
    );
  }

  @Test
  void testParseTextLF() {
    assertTextsEqual(
        List.of("line 1\n", "line 2"),
        StringUtils.parseText("line 1\nline 2", url -> {
        })
    );
  }

  @Test
  void testParseTextCR() {
    assertTextsEqual(
        List.of("line 1\n", "line 2"),
        StringUtils.parseText("line 1\rline 2", url -> {
        })
    );
  }

  @Test
  void testParseTextCRLF() {
    assertTextsEqual(
        List.of("line 1\n", "line 2"),
        StringUtils.parseText("line 1\r\nline 2", url -> {
        })
    );
  }

  @Test
  void testParseTextUrlAtStart() {
    assertTextsEqual(
        List.of("http://example.com", " blabla"),
        StringUtils.parseText("http://example.com blabla", url -> {
        })
    );
  }

  @Test
  void testParseTextUrlAtEnd() {
    assertTextsEqual(
        List.of("blabla ", "http://example.com"),
        StringUtils.parseText("blabla http://example.com", url -> {
        })
    );
  }

  @Test
  void testParseTextOneUrl() {
    assertTextsEqual(
        List.of("address: ", "http://example.com", " blabla"),
        StringUtils.parseText("address: http://example.com blabla", url -> {
        })
    );
  }

  @Test
  void testParseTextTwoUrls() {
    assertTextsEqual(
        List.of("address: ", "http://example.com", " ", "http://exemple.com", " blabla"),
        StringUtils.parseText("address: http://example.com http://exemple.com blabla", url -> {
        })
    );
  }

  @Test
  void testParseTextTwoUrlsTwoLines() {
    assertTextsEqual(
        List.of("address: ", "http://example.com", "\n", "http://exemple.com", " blabla"),
        StringUtils.parseText("address: http://example.com\nhttp://exemple.com blabla", url -> {
        })
    );
  }

  @Test
  void testParseTextInvalidUrl() {
    assertTextsEqual(
        List.of("address: http://exam blabla"),
        StringUtils.parseText("address: http://exam blabla", url -> {
        })
    );
  }

  private static void assertTextsEqual(List<String> expected, List<Text> actual) {
    assertEquals(expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) {
      assertEquals(expected.get(i), actual.get(i).getText());
    }
  }

  @Test
  void testColorToCSSHex() {
    assertEquals("#0080ff", StringUtils.colorToCSSHex(Color.color(0, 0.5, 1)));
  }

  @Test
  void testColorToCSSHexTransparent() {
    assertEquals("#0080ff40", StringUtils.colorToCSSHex(Color.color(0, 0.5, 1, 0.25)));
  }

  @Test
  void splitExtension() {
    assertEquals(new Pair<>("a", Optional.of(".png")), FileUtils.splitExtension("a.png"));
  }

  @Test
  void splitExtensionMultipleDots() {
    assertEquals(new Pair<>("a.b", Optional.of(".png")), FileUtils.splitExtension("a.b.png"));
  }

  @Test
  void splitExtensionNoExtension() {
    assertEquals(new Pair<>("a", Optional.empty()), FileUtils.splitExtension("a"));
  }
}