package net.darmo_creations.jenealogio2.io;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public abstract class TreeFileManager {
  public static final int VERSION = 1;
  public static final String EXTENSION = ".jtree";

  private final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

  public TreeFileManager() {
    this.documentBuilderFactory.setIgnoringComments(true);
  }

  protected DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
    return this.documentBuilderFactory.newDocumentBuilder();
  }
}
