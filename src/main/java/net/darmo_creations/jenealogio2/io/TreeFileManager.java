package net.darmo_creations.jenealogio2.io;

import net.darmo_creations.jenealogio2.model.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Base class for {@link FamilyTree} readers/writers.
 * <p>
 * The file structure is the following:
 * <ul>
 * <li>{@code tree.xml}: XML file containing the tree’s data.</li>
 * <li>{@code files/}: folder containing all files.</li>
 * </ul>
 */
public abstract class TreeFileManager {
  public static final String TREE_FILE_NAME = "tree.xml";
  public static final String FILES_DIR = "files";

  public static final @Unmodifiable List<Character> INVALID_PATH_CHARS;

  static {
    List<Character> invalidChars = new LinkedList<>();
    "<>:\"/\\|?*".chars().forEach(c -> invalidChars.add((char) c));
    for (int i = 0; i < 32; i++)
      invalidChars.add((char) i);
    INVALID_PATH_CHARS = Collections.unmodifiableList(invalidChars);
  }
}
