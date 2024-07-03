package net.darmo_creations.jenealogio2.utils.text_parser;

import org.jetbrains.annotations.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import java.util.stream.*;

import static org.junit.jupiter.api.Assertions.*;

class TextParserTest {
  private TextParser parser;

  @BeforeEach
  void setUp() {
    this.parser = new TextParser();
  }

  @Test
  void parseTextOneLine() {
    assertEquals(
        new PlainTextNode("this is some text"),
        this.parser.parseTree("this is some text")
    );
  }

  @Test
  void parseTextMultipleLines() {
    assertEquals(
        new PlainTextNode("line 1\nline 2"),
        this.parser.parseTree("line 1\nline 2")
    );
  }

  @Test
  void parseTextLF() {
    assertEquals(
        new PlainTextNode("line 1\nline 2"),
        this.parser.parseTree("line 1\nline 2")
    );
  }

  @Test
  void parseTextCR() {
    assertEquals(
        new PlainTextNode("line 1\nline 2"),
        this.parser.parseTree("line 1\rline 2")
    );
  }

  @Test
  void parseTextCRLF() {
    assertEquals(
        new PlainTextNode("line 1\nline 2"),
        this.parser.parseTree("line 1\r\nline 2")
    );
  }

  @Test
  void parseTextNonBracedUrlTreatedAsPlainText() {
    assertEquals(
        new PlainTextNode("http://example.com blabla"),
        this.parser.parseTree("http://example.com blabla")
    );
  }

  @Test
  void parseTextOpenChevronInUrl() {
    assertEquals(
        newSequenceNode(TextStyle.NONE, new LinkNode("http://example.com/<y"), new PlainTextNode(" blabla")),
        this.parser.parseTree("<http://example.com/<y> blabla")
    );
  }

  @Test
  void parseTextUrlWithLineFeedTreatedAsPlainText() {
    assertEquals(
        new PlainTextNode("<http://example.com\n/yo> blabla"),
        this.parser.parseTree("<http://example.com\n/yo> blabla")
    );
  }

  @Test
  void parseTextUrlWithWhitespaceTreatedAsPlainText() {
    assertEquals(
        new PlainTextNode("<http://exampl e.com> blabla"),
        this.parser.parseTree("<http://exampl e.com> blabla")
    );
    assertEquals(
        new PlainTextNode("<http://example.com > blabla"),
        this.parser.parseTree("<http://example.com > blabla")
    );
    assertEquals(
        new PlainTextNode("< http://example.com> blabla"),
        this.parser.parseTree("< http://example.com> blabla")
    );
  }

  @Test
  void parseTextChevronsWithoutUrlIsPlainText() {
    assertEquals(
        new PlainTextNode("<ab> blabla"),
        this.parser.parseTree("<ab> blabla")
    );
  }

  @Test
  void parseTextUrlAtStart() {
    assertEquals(
        newSequenceNode(TextStyle.NONE, new LinkNode("http://example.com"), new PlainTextNode(" blabla")),
        this.parser.parseTree("<http://example.com> blabla")
    );
  }

  @Test
  void parseTextUrlAtEnd() {
    assertEquals(
        newSequenceNode(TextStyle.NONE, new PlainTextNode("blabla "), new LinkNode("http://example.com")),
        this.parser.parseTree("blabla <http://example.com>")
    );
  }

  @Test
  void parseTextOneUrl() {
    assertEquals(
        newSequenceNode(TextStyle.NONE, new PlainTextNode("address: "), new LinkNode("http://example.com"), new PlainTextNode(" blabla")),
        this.parser.parseTree("address: <http://example.com> blabla")
    );
  }

  @Test
  void parseTextTwoUrls() {
    assertEquals(
        newSequenceNode(TextStyle.NONE, new PlainTextNode("address: "), new LinkNode("http://example.com"), new PlainTextNode(" "), new LinkNode("http://exemple.com"), new PlainTextNode(" blabla")),
        this.parser.parseTree("address: <http://example.com> <http://exemple.com> blabla")
    );
  }

  @Test
  void parseTextTwoUrlsTwoLines() {
    assertEquals(
        newSequenceNode(TextStyle.NONE, new PlainTextNode("address: "), new LinkNode("http://example.com"), new PlainTextNode("\n"), new LinkNode("http://exemple.com"), new PlainTextNode(" blabla")),
        this.parser.parseTree("address: <http://example.com>\n<http://exemple.com> blabla")
    );
  }

  @Test
  void parseTextInvalidUrl() {
    assertEquals(
        new PlainTextNode("address: <http://exam> blabla"),
        this.parser.parseTree("address: <http://exam> blabla")
    );
  }

  @ParameterizedTest
  @MethodSource("tagsSource")
  void parseFormatTag(TextStyle style, String tag) {
    assertEquals(
        newSequenceNode(TextStyle.NONE, new PlainTextNode("a "), newSequenceNode(style, new PlainTextNode("test")), new PlainTextNode(" b")),
        this.parser.parseTree("a %1$stest%1$s b".formatted(tag))
    );
  }

  @ParameterizedTest
  @MethodSource("tagsSource")
  void parseFormatTagEndOfLine(TextStyle style, String tag) {
    assertEquals(
        newSequenceNode(TextStyle.NONE, new PlainTextNode("aa "), newSequenceNode(style, new PlainTextNode("test"))),
        this.parser.parseTree("aa %1$stest%1$s".formatted(tag))
    );
  }

  @ParameterizedTest
  @MethodSource("tagsSource")
  void parseFormatTagStartOfLine(TextStyle style, String tag) {
    assertEquals(
        newSequenceNode(TextStyle.NONE, newSequenceNode(style, new PlainTextNode("test")), new PlainTextNode(" aa")),
        this.parser.parseTree("%1$stest%1$s aa".formatted(tag))
    );
  }

  @ParameterizedTest
  @MethodSource("tagsSource")
  void parseFormatTagTwice(TextStyle style, String tag) {
    assertEquals(
        newSequenceNode(TextStyle.NONE, new PlainTextNode("aa "), newSequenceNode(style, new PlainTextNode("test")), newSequenceNode(style, new PlainTextNode("test1")), new PlainTextNode(" bb")),
        this.parser.parseTree("aa %1$stest%1$s%1$stest1%1$s bb".formatted(tag))
    );
  }

  @ParameterizedTest
  @MethodSource("tagsSource")
  void parseFormatTagUnclosed(TextStyle ignoredStyle, String tag) {
    assertEquals(
        new PlainTextNode("%1$stest".formatted(tag)),
        this.parser.parseTree("%1$stest".formatted(tag))
    );
  }

  @ParameterizedTest
  @MethodSource("tagsSource")
  void parseFormatTagEOL(TextStyle ignoredStyle, String tag) {
    assertEquals(
        new PlainTextNode("%1$stest\n%1$s".formatted(tag)),
        this.parser.parseTree("%1$stest\n%1$s".formatted(tag))
    );
  }

  @ParameterizedTest
  @MethodSource("tagsSource")
  void parseEmptyTag(TextStyle ignoredStyle, String tag) {
    assertEquals(
        new PlainTextNode("a%1$s%1$sb".formatted(tag)),
        this.parser.parseTree("a%1$s%1$sb".formatted(tag))
    );
  }

  @Test
  void parseConsecutiveOpeningTags() {
    assertEquals(
        newSequenceNode(TextStyle.BOLD, newSequenceNode(TextStyle.UNDERLINE, new PlainTextNode("test")), new PlainTextNode("test1")),
        this.parser.parseTree("*_test_test1*")
    );
  }

  @Test
  void parseConsecutiveClosingTags() {
    assertEquals(
        newSequenceNode(TextStyle.BOLD, new PlainTextNode("test"), newSequenceNode(TextStyle.UNDERLINE, new PlainTextNode("test1"))),
        this.parser.parseTree("*test_test1_*")
    );
  }

  @Test
  void parse2InterwovenTags() {
    assertEquals(
        newSequenceNode(TextStyle.NONE, newSequenceNode(TextStyle.BOLD, new PlainTextNode("test_test1")), new PlainTextNode("test2_test3")),
        this.parser.parseTree("*test_test1*test2_test3")
    );
  }

  @Test
  void parse3InterwovenTags() {
    assertEquals(
        newSequenceNode(TextStyle.NONE, newSequenceNode(TextStyle.BOLD, new PlainTextNode("test_te/st1")), new PlainTextNode("tes/t2_test3")),
        this.parser.parseTree("*test_te/st1*tes/t2_test3")
    );
  }

  @ParameterizedTest
  @MethodSource("tagsSource")
  void parseEscapeOpeningTag(TextStyle ignoredStyle, String tag) {
    assertEquals(
        new PlainTextNode("a%1$sb%1$sc".formatted(tag)),
        this.parser.parseTree("a\\%1$sb%1$sc".formatted(tag))
    );
  }

  @ParameterizedTest
  @MethodSource("tagsSource")
  void parseEscapeClosingTag(TextStyle ignoredStyle, String tag) {
    assertEquals(
        new PlainTextNode("a%1$sb%1$sc".formatted(tag)),
        this.parser.parseTree("a%1$sb\\%1$sc".formatted(tag))
    );
  }

  @ParameterizedTest
  @MethodSource("tagsSource")
  void parseEscapeBackslash(TextStyle style, String tag) {
    assertEquals(
        newSequenceNode(TextStyle.NONE, new PlainTextNode("a\\"), newSequenceNode(style, new PlainTextNode("b\\")), new PlainTextNode("c")),
        this.parser.parseTree("a\\\\%1$sb\\\\%1$sc".formatted(tag))
    );
  }

  @Test
  void parseEscapeNonSpecial() {
    assertEquals(
        new PlainTextNode("a\\b"),
        this.parser.parseTree("a\\b")
    );
  }

  @Test
  void parseEscapeEOL() {
    assertEquals(
        new PlainTextNode("a\\\nb"),
        this.parser.parseTree("a\\\nb")
    );
  }

  @Test
  void parseEmptyString() {
    assertEquals(
        new PlainTextNode(""),
        this.parser.parseTree("")
    );
  }

  private static SequenceNode newSequenceNode(@NotNull TextStyle style, @NotNull Node @NotNull ... nodes) {
    final SequenceNode sequenceNode = new SequenceNode(style);
    for (final Node node : nodes)
      sequenceNode.addChild(node);
    return sequenceNode;
  }

  public static Stream<Arguments> tagsSource() {
    return Stream.of(
        Arguments.of(TextStyle.BOLD, "*"),
        Arguments.of(TextStyle.ITALIC, "/"),
        Arguments.of(TextStyle.UNDERLINE, "_"),
        Arguments.of(TextStyle.STRIKETHROUGH, "~")
    );
  }
}