package net.darmo_creations.jenealogio2.io;

import net.darmo_creations.jenealogio2.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;
import org.w3c.dom.*;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.function.*;

public final class TreesManager {
  private static final String METADATA_FILE_NAME = "metadata.xml";

  private static final String TREES_TAG = "Trees";
  private static final String METADATA_TAG = "Metadata";
  private static final String TREE_METADATA_TAG = "TreeMetadata";
  private static final String DIRECTORY_NAME_ATTR = "directoryName";
  private static final String LAST_OPENING_DATE_ATTR = "lastOpeningDate";

  private final Map<String, TreeMetadata> trees = new HashMap<>();

  public TreesManager() {
    try (var files = Files.walk(App.USER_DATA_DIR, 1)) {
      var openingDates = this.readMetadataFile();
      var iterator = files
          .filter(path -> Files.isDirectory(path) && Files.exists(path.resolve(TreeFileManager.TREE_FILE_NAME)))
          .iterator();
      while (iterator.hasNext()) {
        Path path = iterator.next();
        String dirName = path.getFileName().toString();
        FamilyTree tree;
        try (var in = Files.newInputStream(path.resolve(TreeFileManager.TREE_FILE_NAME))) {
          tree = new TreeXMLReader().readFromStream(
              in, (name, description, date) -> new Picture(null, path.resolve(name), description, date));
        } catch (IOException e) {
          App.LOGGER.exception(e);
          continue;
        }
        LocalDateTime lastOpeningDate = Optional.ofNullable(openingDates.get(dirName))
            .flatMap(Function.identity())
            .orElse(null);
        this.trees.put(dirName, new TreeMetadata(tree.name(), dirName, lastOpeningDate));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Map<String, Optional<LocalDateTime>> readMetadataFile() {
    Map<String, Optional<LocalDateTime>> extracted = new HashMap<>();
    Path metadataFile = App.CURRENT_DIR.resolve(METADATA_FILE_NAME);
    if (Files.exists(metadataFile)) {
      try (var metadata = Files.newInputStream(metadataFile)) {
        Document document = XmlUtils.readFile(metadata);
        NodeList childNodes = document.getChildNodes();
        if (childNodes.getLength() != 1)
          return extracted;
        Element root = (Element) childNodes.item(0);
        Optional<Element> trees = XmlUtils.getChildElement(root, TREES_TAG, true);
        if (trees.isPresent()) {
          for (Element treeElement : XmlUtils.getChildElements(trees.get(), TREE_METADATA_TAG)) {
            try {
              String dirName = XmlUtils.getAttr(
                  treeElement, DIRECTORY_NAME_ATTR, Function.identity(), null, false);
              LocalDateTime lastOpeningDate = XmlUtils.getAttr(
                  treeElement, LAST_OPENING_DATE_ATTR, LocalDateTime::parse, () -> null, false);
              extracted.put(dirName, Optional.ofNullable(lastOpeningDate));
            } catch (IOException e) {
              App.LOGGER.exception(e);
            }
          }
        }
      } catch (IOException e) {
        App.LOGGER.exception(e);
      }
    }
    return extracted;
  }

  /**
   * Update the metadata.xml file when the given {@link FamilyTree} is opened.
   *
   * @param tree          The family tree that was opened.
   * @param directoryName The name of the directory the tree was loaded from.
   * @param config        The app’s config.
   */
  public void onTreeOpened(
      final @NotNull FamilyTree tree,
      @NotNull String directoryName,
      final @NotNull Config config
  ) {
    this.trees.put(directoryName, new TreeMetadata(tree.name(), directoryName, LocalDateTime.now()));
    Document document = XmlUtils.newDocumentBuilder().newDocument();
    Element root = (Element) document.appendChild(document.createElement(METADATA_TAG));
    Element treesElement = (Element) root.appendChild(document.createElement(TREES_TAG));
    for (TreeMetadata metadata : this.trees.values()) {
      Element treeMetadata = document.createElement(TREE_METADATA_TAG);
      XmlUtils.setAttr(document, treeMetadata, DIRECTORY_NAME_ATTR, metadata.directoryName());
      LocalDateTime date = directoryName.equals(metadata.directoryName()) ? LocalDateTime.now() : metadata.lastOpenDate();
      if (date != null) {
        date = date.withNano(0);
        XmlUtils.setAttr(document, treeMetadata, LAST_OPENING_DATE_ATTR, date.toString());
      }
      treesElement.appendChild(treeMetadata);
    }
    try (var out = Files.newOutputStream(App.CURRENT_DIR.resolve(METADATA_FILE_NAME))) {
      XmlUtils.writeFile(out, document, config);
    } catch (IOException e) {
      App.LOGGER.exception(e);
    }
  }

  /**
   * Remove the tree metadata for the given tree directory.
   *
   * @param directoryName Name of the tree directory to wipe the metadata of.
   */
  public void removeEntry(String directoryName) {
    this.trees.remove(directoryName);
  }

  /**
   * An unmodifiable view of the available family trees’ metadata.
   */
  public Map<String, TreeMetadata> treesMetadata() {
    return Collections.unmodifiableMap(this.trees);
  }
}
