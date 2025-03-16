package net.darmo_creations.jenealogio2.utils;

import net.darmo_creations.jenealogio2.config.*;
import org.jetbrains.annotations.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import java.io.*;
import java.util.*;
import java.util.function.*;

public final class XmlUtils {
  private static final DocumentBuilderFactory BUILDER_FACTORY = DocumentBuilderFactory.newInstance();

  /**
   * Read a child element of an XML element.
   *
   * @param element      XML element to read from.
   * @param name         Name of the element to read.
   * @param allowMissing True to allow the element designated by {@code name} to be missing;
   *                     false to throw an error if missing.
   * @return The first occurence of the specified child element.
   * @throws IOException If any error occurs.
   */
  public static Optional<Element> getChildElement(
      final @NotNull Element element,
      @NotNull String name,
      boolean allowMissing
  ) throws IOException {
    final NodeList childNodes = element.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      final Node item = childNodes.item(i);
      if (item.getNodeName().equals(name))
        return Optional.of((Element) item);
    }
    if (!allowMissing)
      throw new IOException("Missing tag %s in tag %s".formatted(name, element.getTagName()));
    return Optional.empty();
  }

  /**
   * Read all children elements with a given name.
   *
   * @param element Element to read from.
   * @param name    Name of child elements.
   * @return List of read elements.
   */
  public static List<Element> getChildrenElements(
      final @NotNull Element element,
      @NotNull String name
  ) {
    final List<Element> elements = new LinkedList<>();
    final NodeList childNodes = element.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      final Node item = childNodes.item(i);
      if (item.getNodeName().equals(name))
        elements.add((Element) item);
    }
    return elements;
  }

  /**
   * Read the value of an element’s attribute.
   *
   * @param element              Element to read from.
   * @param name                 Attribute’s name.
   * @param valueConverter       Function to convert raw value.
   * @param defaultValueSupplier Function that supplies a default value in case attribute is missing.
   *                             If argument is null, an error is thrown if the attribute is missing.
   * @param strip                Whether to strip leading and trailing whitespace before passing
   *                             the raw value to the converter.
   * @param <T>                  Type of the returned value.
   * @return The converted value.
   * @throws IOException If any error occurs.
   */
  public static <T> T getAttr(
      final @NotNull Element element,
      @NotNull String name,
      @NotNull Function<String, T> valueConverter,
      Supplier<T> defaultValueSupplier,
      boolean strip
  ) throws IOException {
    String rawValue = element.getAttribute(name);
    if (strip)
      rawValue = rawValue.strip();
    if (rawValue.isEmpty()) {
      if (defaultValueSupplier == null)
        throw new IOException("Missing or empty attribute %s on element %s".formatted(name, element.getTagName()));
      return defaultValueSupplier.get();
    }
    try {
      return valueConverter.apply(rawValue);
    } catch (final RuntimeException e) {
      throw new IOException(e);
    }
  }

  /**
   * Set attribute value of an element.
   *
   * @param document Current XML document.
   * @param element  Element to set attribute of.
   * @param name     Attribute’s name.
   * @param value    Attribute’s value.
   */
  public static void setAttr(
      @NotNull Document document,
      @NotNull Element element,
      @NotNull String name,
      @NotNull String value
  ) {
    final Attr attr = document.createAttribute(name);
    attr.setValue(value);
    element.setAttributeNode(attr);
  }

  /**
   * Write an XML document to a file.
   *
   * @param outputStream Stream to write to.
   * @param document     XML document to write to.
   * @param config       The app’s config.
   */
  public static void writeFile(
      @NotNull OutputStream outputStream,
      final @NotNull Document document,
      final @NotNull Config config
  ) {
    final Transformer tr;
    try {
      tr = TransformerFactory.newInstance().newTransformer();
    } catch (final TransformerConfigurationException e) {
      throw new RuntimeException(e);
    }
    tr.setOutputProperty(OutputKeys.INDENT, config.isDebug() ? "yes" : "no");
    tr.setOutputProperty(OutputKeys.METHOD, "xml");
    tr.setOutputProperty(OutputKeys.STANDALONE, "yes");
    tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    try {
      tr.transform(new DOMSource(document), new StreamResult(outputStream));
    } catch (final TransformerException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Read a file from disk and return an XML document.
   *
   * @param file File to read.
   * @return The corresponding XML document.
   * @throws IOException If any error occurs.
   */
  public static Document readFile(@NotNull InputStream file) throws IOException {
    try {
      return newDocumentBuilder().parse(file);
    } catch (final SAXException e) {
      throw new IOException(e);
    }
  }

  /**
   * Return a new document builder.
   */
  public static DocumentBuilder newDocumentBuilder() {
    try {
      return BUILDER_FACTORY.newDocumentBuilder();
    } catch (final ParserConfigurationException e) {
      throw new RuntimeException(e); // Should never happen
    }
  }

  private XmlUtils() {
  }
}
