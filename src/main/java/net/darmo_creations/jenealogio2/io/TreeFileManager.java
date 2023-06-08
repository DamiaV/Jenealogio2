package net.darmo_creations.jenealogio2.io;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Base class for tree files managers.
 */
public abstract class TreeFileManager {
  /**
   * Current version of tree files.
   */
  public static final int VERSION = 1;
  /**
   * Tree files’ extension.
   */
  public static final String EXTENSION = ".jtree";

  private final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

  public TreeFileManager() {
    this.documentBuilderFactory.setIgnoringComments(true);
  }

  /**
   * Return a new document builder.
   */
  protected DocumentBuilder newDocumentBuilder() {
    try {
      return this.documentBuilderFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e); // Should never happen
    }
  }
}
