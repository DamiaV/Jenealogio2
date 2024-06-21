package net.darmo_creations.jenealogio2.ui;

import javafx.beans.binding.*;
import javafx.collections.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import net.darmo_creations.jenealogio2.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.themes.*;
import net.darmo_creations.jenealogio2.ui.events.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.util.*;

/**
 * JavaFX component displaying the list of all persons in a family tree.
 */
public class FamilyTreeView extends FamilyTreeComponent {
  private final Config config;
  private final TextField searchField = new TextField();
  private final ObservableSet<TreeItem<Object>> searchMatches = FXCollections.observableSet(new HashSet<>());
  private final TreeView<Object> treeView = new TreeView<>();
  private final TreeItem<Object> personsItem;
  private final Button clearButton;
  private final ToggleButton syncTreeButton;

  private boolean internalSelectionChange;

  /**
   * Create an empty family tree view.
   *
   * @param config The app’s config.
   */
  public FamilyTreeView(final @NotNull Config config) {
    this.config = config;
    Language language = this.config.language();
    Theme theme = this.config.theme();

    VBox vBox = new VBox(4);
    AnchorPane.setTopAnchor(vBox, 4.0);
    AnchorPane.setBottomAnchor(vBox, 4.0);
    AnchorPane.setLeftAnchor(vBox, 4.0);
    AnchorPane.setRightAnchor(vBox, 4.0);
    this.getChildren().add(vBox);

    // Search bar

    HBox hBox = new HBox(4);
    vBox.getChildren().add(hBox);

    this.searchField.setPromptText(language.translate("treeview.search"));
    this.searchField.textProperty().addListener(
        (observable, oldValue, newValue) -> this.onSearchFilterChange(newValue));
    HBox.setHgrow(this.searchField, Priority.ALWAYS);
    hBox.getChildren().add(this.searchField);

    this.clearButton = new Button("", theme.getIcon(Icon.CLEAR_TEXT, Icon.Size.SMALL));
    this.clearButton.setOnAction(event -> {
      this.searchField.clear();
      this.searchField.requestFocus();
    });
    this.clearButton.setTooltip(new Tooltip(language.translate("treeview.erase_search")));
    this.clearButton.setDisable(true);
    hBox.getChildren().add(this.clearButton);

    this.syncTreeButton = new ToggleButton("", theme.getIcon(Icon.SYNC_TREE, Icon.Size.SMALL));
    this.syncTreeButton.setTooltip(new Tooltip(language.translate("treeview.sync_tree")));
    this.syncTreeButton.setSelected(this.config.shouldSyncTreeWithMainPane());
    this.syncTreeButton.selectedProperty().addListener(
        (observable, oldValue, newValue) -> {
          this.config.setShouldSyncTreeWithMainPane(newValue);
          try {
            this.config.save();
          } catch (IOException e) {
            App.LOGGER.exception(e);
          }
        });
    hBox.getChildren().add(this.syncTreeButton);

    // Tree view

    VBox.setVgrow(this.treeView, Priority.ALWAYS);
    vBox.getChildren().add(this.treeView);
    this.treeView.setCellFactory(tree -> new SearchHighlightingTreeCell());
    this.treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    this.treeView.setShowRoot(false);
    TreeItem<Object> root = new TreeItem<>();
    this.personsItem = new TreeItem<>(language.translate("treeview.persons"));
    root.getChildren().add(this.personsItem);
    this.treeView.setRoot(root);
    this.treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      if (!this.internalSelectionChange) {
        if (newValue != null && newValue.getValue() instanceof Person person) {
          this.firePersonClickEvent(new PersonClickedEvent(person, PersonClickedEvent.Action.SELECT));
        } else {
          this.firePersonClickEvent(new DeselectPersonsEvent());
        }
      }
    });
    this.treeView.setOnMouseClicked(event -> {
      TreeItem<Object> selectedItem = this.treeView.getSelectionModel().getSelectedItem();
      if (selectedItem != null && selectedItem.getValue() instanceof Person person) {
        var clickType = PersonClickedEvent.getClickType(event.getClickCount(), event.getButton());
        this.firePersonClickEvent(new PersonClickedEvent(person, clickType));
      }
    });
  }

  @Override
  public void refresh() {
    this.personsItem.getChildren().clear();
    this.searchField.clear();
    this.familyTree().ifPresent(familyTree -> {
      familyTree.persons().stream()
          .sorted(Person.lastThenFirstNamesComparator())
          .forEach(person -> this.personsItem.getChildren().add(new TreeItem<>(person)));
      this.personsItem.setExpanded(true);
    });
    // Option may have been updated from elsewhere
    this.syncTreeButton.setSelected(this.config.shouldSyncTreeWithMainPane());
  }

  @Override
  public Optional<Person> getSelectedPerson() {
    TreeItem<Object> selectedItem = this.treeView.getSelectionModel().getSelectedItem();
    return selectedItem != null && selectedItem.getValue() instanceof Person person
        ? Optional.of(person)
        : Optional.empty();
  }

  @Override
  public void deselectAll() {
    this.internalSelectionChange = true;
    this.treeView.getSelectionModel().clearSelection();
    this.internalSelectionChange = false;
  }

  @Override
  public void select(@NotNull Person person, boolean updateTarget) {
    Objects.requireNonNull(person);
    for (int i = 0; i < this.personsItem.getChildren().size(); i++) {
      TreeItem<Object> item = this.personsItem.getChildren().get(i);
      if (item.getValue() == person) {
        this.internalSelectionChange = true;
        this.treeView.getSelectionModel().select(item);
        this.internalSelectionChange = false;
        break;
      }
    }
  }

  /**
   * Called whenever the search filter changes.
   *
   * @param text The filter text.
   */
  private void onSearchFilterChange(String text) {
    this.searchMatches.clear();
    Optional<String> filter = StringUtils.stripNullable(text);
    this.clearButton.setDisable(filter.isEmpty());
    if (filter.isEmpty()) {
      return;
    }
    Set<TreeItem<Object>> matches = new HashSet<>();
    this.searchMatchingItems(this.personsItem, matches, filter.get());
    this.searchMatches.addAll(matches);
  }

  /**
   * Search for tree items matching the given search query.
   *
   * @param searchNode   Tree item to search children of.
   * @param matches      Set to populate with matches.
   * @param searchFilter Search filter.
   */
  private void searchMatchingItems(
      @NotNull TreeItem<Object> searchNode, @NotNull Set<TreeItem<Object>> matches, @NotNull String searchFilter) {
    for (TreeItem<Object> item : searchNode.getChildren()) {
      if (item.getValue().toString().toLowerCase().contains(searchFilter.toLowerCase())) {
        matches.add(item);
      }
    }
  }

  /**
   * Tree cell class that allows highlighting of tree items matching a query filter.
   * <p>
   * From https://stackoverflow.com/a/34914538/3779986
   */
  private class SearchHighlightingTreeCell extends TreeCell<Object> {
    // Cannot be local or else it would be garbage-collected
    @SuppressWarnings("FieldCanBeLocal")
    private final BooleanBinding matchesSearch;

    public SearchHighlightingTreeCell() {
      this.matchesSearch = Bindings.createBooleanBinding(
          () -> FamilyTreeView.this.searchMatches.contains(this.getTreeItem()),
          this.treeItemProperty(),
          FamilyTreeView.this.searchMatches
      );
      this.matchesSearch.addListener((obs, didMatchSearch, nowMatchesSearch) ->
          this.pseudoClassStateChanged(PseudoClasses.SEARCH_MATCH, nowMatchesSearch));
    }

    @Override
    protected void updateItem(Object item, boolean empty) {
      // Update the text when the displayed item changes
      super.updateItem(item, empty);
      this.setText(empty ? null : item.toString());
      Optional<FamilyTree> familyTree = FamilyTreeView.this.familyTree();
      if (familyTree.isPresent() && item instanceof Person p && familyTree.get().isRoot(p)) {
        this.setGraphic(FamilyTreeView.this.config.theme().getIcon(Icon.TREE_ROOT, Icon.Size.SMALL));
      } else {
        this.setGraphic(null);
      }
    }
  }
}
