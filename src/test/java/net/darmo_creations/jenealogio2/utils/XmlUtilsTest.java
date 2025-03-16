package net.darmo_creations.jenealogio2.utils;

import org.junit.jupiter.api.*;
import org.w3c.dom.*;

import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class XmlUtilsTest {
  private Document doc;
  private Element root;

  @BeforeEach
  void setUp() {
    this.doc = XmlUtils.newDocumentBuilder().newDocument();
    this.root = this.doc.createElement("Root");
    this.doc.appendChild(this.root);
    final Element tag1 = this.doc.createElement("Tag");
    this.root.appendChild(tag1);
    tag1.setAttribute("id", "a");
    final Element tag2 = this.doc.createElement("Tag");
    this.root.appendChild(tag2);
    tag2.setAttribute("id", "b");
    final Element tag3 = this.doc.createElement("Test");
    this.root.appendChild(tag3);
    tag3.setAttribute("value", "4");
    final Element tag4 = this.doc.createElement("Whitespace");
    this.root.appendChild(tag4);
    tag4.setAttribute("value", "  \t1  \t");
    final Element tag5 = this.doc.createElement("Child");
    tag1.appendChild(tag5);
    tag5.setAttribute("value", "1");
    final Element tag6 = this.doc.createElement("Test");
    tag1.appendChild(tag6);
    tag6.setAttribute("value", "1");
  }

  @Test
  void getChildElement() throws IOException {
    //noinspection OptionalGetWithoutIsPresent
    final Element element = XmlUtils.getChildElement(this.root, "Test", false).get();
    assertEquals("Test", element.getTagName());
    assertEquals("4", element.getAttribute("value"));
  }

  @Test
  void getChildElementOnlyReturnsDirectChild() throws IOException {
    assertTrue(XmlUtils.getChildElement(this.root, "Child", true).isEmpty());
  }

  @Test
  void getChildElementReturnsFirstOccurence() throws IOException {
    //noinspection OptionalGetWithoutIsPresent
    final Element element = XmlUtils.getChildElement(this.root, "Tag", false).get();
    assertEquals("Tag", element.getTagName());
    assertEquals("a", element.getAttribute("id"));
  }

  @Test
  void getChildElementReturnsEmptyIfMissingAndAllowed() throws IOException {
    assertTrue(XmlUtils.getChildElement(this.root, "Missing", true).isEmpty());
  }

  @Test
  void getChildElementThrowsIfMissingAndNotAllowed() {
    assertThrows(IOException.class, () -> XmlUtils.getChildElement(this.root, "Root", false));
  }

  @Test
  void getChildrenElementsKeepsOrder() {
    final List<Element> elements = XmlUtils.getChildrenElements(this.root, "Tag");
    assertEquals(2, elements.size());
    assertEquals("Tag", elements.get(0).getTagName());
    assertEquals("a", elements.get(0).getAttribute("id"));
    assertEquals("Tag", elements.get(1).getTagName());
    assertEquals("b", elements.get(1).getAttribute("id"));
  }

  @Test
  void getChildrenElementsReturnsOnlyDirectChildren() {
    final List<Element> elements = XmlUtils.getChildrenElements(this.root, "Test");
    assertEquals("Test", elements.get(0).getTagName());
    assertEquals("4", elements.get(0).getAttribute("value"));
  }

  @Test
  void getChildrenElementsReturnsEmptyListIfNoMatch() {
    assertTrue(XmlUtils.getChildrenElements(this.root, "Missing").isEmpty());
  }

  @Test
  void getAttr() throws IOException {
    final Element element = (Element) this.root.getElementsByTagName("Test").item(1);
    assertEquals("4", XmlUtils.getAttr(
        element,
        "value",
        s -> s,
        null,
        false
    ));
  }

  @Test
  void getAttrSuppliesDefaultValueIfMissing() throws IOException {
    final Element element = (Element) this.root.getElementsByTagName("Test").item(1);
    assertEquals("default", XmlUtils.getAttr(
        element,
        "missing",
        s -> s,
        () -> "default",
        false
    ));
  }

  @Test
  void getAttrConvertsValue() throws IOException {
    final Element element = (Element) this.root.getElementsByTagName("Test").item(1);
    assertEquals((Integer) 4, XmlUtils.getAttr(
        element,
        "value",
        Integer::parseInt,
        null,
        false
    ));
  }

  @Test
  void getAttrThrowsIfConversionError() {
    final Element element = (Element) this.root.getElementsByTagName("Tag").item(0);
    assertThrows(IOException.class, () -> XmlUtils.getAttr(
        element,
        "value",
        Integer::parseInt,
        null,
        false
    ));
  }

  @Test
  void getAttrKeepsWhitespace() throws IOException {
    final Element element = (Element) this.root.getElementsByTagName("Whitespace").item(0);
    assertEquals("  \t1  \t", XmlUtils.getAttr(
        element,
        "value",
        s -> s,
        null,
        false
    ));
  }

  @Test
  void getAttrStripsIfSpecified() throws IOException {
    final Element element = (Element) this.root.getElementsByTagName("Whitespace").item(0);
    assertEquals("1", XmlUtils.getAttr(
        element,
        "value",
        s -> s,
        null,
        true
    ));
  }

  @Test
  void setAttr() {
    final Element test = this.doc.createElement("Test");
    XmlUtils.setAttr(this.doc, test, "attr", "value");
    assertEquals("value", test.getAttribute("attr"));
  }
}
