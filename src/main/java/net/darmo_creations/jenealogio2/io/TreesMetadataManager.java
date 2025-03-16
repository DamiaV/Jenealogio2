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

/**
 * This class manages the metadata of trees in {@link App#USER_DATA_DIR}.
 */
public final class TreesMetadataManager {
  private static final String METADATA_FILE_NAME = "metadata.xml";

  private static final String TREES_TAG = "Trees";
  private static final String METADATA_TAG = "Metadata";
  private static final String TREE_METADATA_TAG = "TreeMetadata";
  private static final String DIRECTORY_NAME_ATTR = "directoryName";
  private static final String LAST_OPENING_DATE_ATTR = "lastOpeningDate";

  private final Map<String, TreeMetadata> trees = new HashMap<>();

  public TreesMetadataManager() {
    if (!Files.exists(App.USER_DATA_DIR))
      try {
        Files.createDirectory(App.USER_DATA_DIR);
      } catch (final IOException e) {
        throw new RuntimeException(e);
      }

    try (final var files = Files.newDirectoryStream(App.USER_DATA_DIR)) {
      final var openingDates = this.readMetadataFile();
      for (final var path : files) {
        if (!Files.isDirectory(path) || !Files.exists(path.resolve(TreeFileManager.TREE_FILE_NAME)))
          continue;
        final String dirName = path.getFileName().toString();
        final FamilyTree tree;
        try (final var in = Files.newInputStream(path.resolve(TreeFileManager.TREE_FILE_NAME))) {
          tree = new TreeXMLReader().readFromStream(
              in,
              (name, description, date) -> {
                final Path filePath = path.resolve(name);
                final Optional<String> ext = FileUtils.splitExtension(name).extension();
                if (ext.isPresent() && Picture.FILE_EXTENSIONS.contains(ext.get().toLowerCase()))
                  return new Picture(null, filePath, description, date);
                return new AttachedDocument(filePath, description, date);
              }
          );
        } catch (final IOException e) {
          App.LOGGER.exception(e);
          continue;
        }
        final LocalDateTime lastOpeningDate = Optional.ofNullable(openingDates.get(dirName))
            .flatMap(Function.identity())
            .orElse(null);
        this.trees.put(dirName, new TreeMetadata(tree.name(), dirName, lastOpeningDate));
      }
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Map<String, Optional<LocalDateTime>> readMetadataFile() {
    final Map<String, Optional<LocalDateTime>> extracted = new HashMap<>();
    final Path metadataFile = App.CURRENT_DIR.resolve(METADATA_FILE_NAME);
    if (Files.exists(metadataFile)) {
      try (final var metadata = Files.newInputStream(metadataFile)) {
        final Document document = XmlUtils.readFile(metadata);
        final NodeList childNodes = document.getChildNodes();
        if (childNodes.getLength() != 1)
          return extracted;
        final Element root = (Element) childNodes.item(0);
        final Optional<Element> trees = XmlUtils.getChildElement(root, TREES_TAG, true);
        if (trees.isPresent())
          for (final Element treeElement : XmlUtils.getChildrenElements(trees.get(), TREE_METADATA_TAG))
            try {
              final String dirName = XmlUtils.getAttr(
                  treeElement,
                  DIRECTORY_NAME_ATTR,
                  Function.identity(),
                  null,
                  false
              );
              final LocalDateTime lastOpeningDate = XmlUtils.getAttr(
                  treeElement,
                  LAST_OPENING_DATE_ATTR,
                  LocalDateTime::parse,
                  () -> null,
                  false
              );
              extracted.put(dirName, Optional.ofNullable(lastOpeningDate));
            } catch (final IOException e) {
              App.LOGGER.exception(e);
            }
      } catch (final IOException e) {
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
    final Document document = XmlUtils.newDocumentBuilder().newDocument();
    final Element root = (Element) document.appendChild(document.createElement(METADATA_TAG));
    final Element treesElement = (Element) root.appendChild(document.createElement(TREES_TAG));

    for (final TreeMetadata metadata : this.trees.values()) {
      final Element treeMetadata = document.createElement(TREE_METADATA_TAG);
      XmlUtils.setAttr(document, treeMetadata, DIRECTORY_NAME_ATTR, metadata.directoryName());
      final LocalDateTime date = directoryName.equals(metadata.directoryName())
          ? LocalDateTime.now()
          : metadata.lastOpenDate();
      if (date != null)
        XmlUtils.setAttr(
            document,
            treeMetadata,
            LAST_OPENING_DATE_ATTR,
            date.withNano(0).toString()
        );
      treesElement.appendChild(treeMetadata);
    }

    try (final var out = Files.newOutputStream(App.CURRENT_DIR.resolve(METADATA_FILE_NAME))) {
      XmlUtils.writeFile(out, document, config);
    } catch (final IOException e) {
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
