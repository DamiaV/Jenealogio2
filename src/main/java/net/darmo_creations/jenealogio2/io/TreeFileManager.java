package net.darmo_creations.jenealogio2.io;

/**
 * Base class for {@code .jtree} readers/writers. These are ZIP files that have the following structure:
 * <ul>
 * <li>{@code tree.xml}: XML file containing the tree’s data.</li>
 * <li>{@code images/}: folder containing all images.</li>
 * </ul>
 */
public abstract class TreeFileManager {
  protected static final String TREE_FILE = "tree.xml";
  protected static final String IMAGES_DIR = "images";
  /**
   * Tree files extension.
   */
  public static final String EXTENSION = ".jtree";
}
