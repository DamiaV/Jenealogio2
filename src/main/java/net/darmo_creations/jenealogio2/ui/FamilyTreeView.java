package net.darmo_creations.jenealogio2.ui;

import javafx.beans.binding.*;
import javafx.collections.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import net.darmo_creations.jenealogio2.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.themes.*;
import net.darmo_creations.jenealogio2.ui.components.*;
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
  private final ErasableTextField searchField;
  private final ObservableSet<TreeItem<Object>> searchMatches = FXCollections.observableSet(new HashSet<>());
  private final TreeView<Object> treeView = new TreeView<>();
  private final TreeItem<Object> personsItem;
  private final ToggleButton syncTreeButton;

  private boolean internalSelectionChange;

  /**
   * Create an empty family tree view.
   *
   * @param config The app’s config.
   */
  public FamilyTreeView(final @NotNull Config config) {
    this.config = config;
    final Language language = this.config.language();
    final Theme theme = this.config.theme();

    final VBox vBox = new VBox(5);
    AnchorPane.setTopAnchor(vBox, 5.0);
    AnchorPane.setBottomAnchor(vBox, 5.0);
    AnchorPane.setLeftAnchor(vBox, 5.0);
    AnchorPane.setRightAnchor(vBox, 5.0);
    this.getChildren().add(vBox);

    // Search bar

    final HBox hBox = new HBox(5);
    vBox.getChildren().add(hBox);

    this.searchField = new ErasableTextField(config);
    this.searchField.textField().setPromptText(language.translate("treeview.search"));
    this.searchField.textField().textProperty().addListener(
        (observable, oldValue, newValue) -> this.onSearchFilterChange(newValue));
    HBox.setHgrow(this.searchField, Priority.ALWAYS);
    hBox.getChildren().add(this.searchField);

    this.syncTreeButton = new ToggleButton("", theme.getIcon(Icon.SYNC_TREE, Icon.Size.SMALL));
    this.syncTreeButton.setTooltip(new Tooltip(language.translate("treeview.sync_tree")));
    this.syncTreeButton.setSelected(this.config.shouldSyncTreeWithMainPane());
    this.syncTreeButton.selectedProperty().addListener(
        (observable, oldValue, newValue) -> {
          this.config.setShouldSyncTreeWithMainPane(newValue);
          try {
            this.config.save();
          } catch (final IOException e) {
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
    final TreeItem<Object> root = new TreeItem<>();
    this.personsItem = new TreeItem<>(language.translate("treeview.persons"));
    root.getChildren().add(this.personsItem);
    this.treeView.setRoot(root);
    this.treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      if (!this.internalSelectionChange) {
        if (newValue != null && newValue.getValue() instanceof Person person)
          this.firePersonClickEvent(new PersonClickedEvent(person, PersonClickedEvent.Action.SELECT));
        else
          this.firePersonClickEvent(new DeselectPersonsEvent());
      }
    });
    this.treeView.setOnMouseClicked(event -> {
      final TreeItem<Object> selectedItem = this.treeView.getSelectionModel().getSelectedItem();
      if (selectedItem != null && selectedItem.getValue() instanceof Person person)
        this.firePersonClickEvent(new PersonClickedEvent(
            person,
            PersonClickedEvent.getClickType(event.getClickCount(), event.getButton())
        ));
    });
  }

  @Override
  public void refresh() {
    this.personsItem.getChildren().clear();
    this.searchField.textField().clear();
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
    final TreeItem<Object> selectedItem = this.treeView.getSelectionModel().getSelectedItem();
    if (selectedItem != null && selectedItem.getValue() instanceof Person person)
      return Optional.of(person);
    return Optional.empty();
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
      final TreeItem<Object> item = this.personsItem.getChildren().get(i);
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
    final Optional<String> filter = StringUtils.stripNullable(text);
    if (filter.isEmpty())
      return;
    final Set<TreeItem<Object>> matches = new HashSet<>();
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
      final @NotNull TreeItem<Object> searchNode,
      @NotNull Set<TreeItem<Object>> matches,
      @NotNull String searchFilter
  ) {
    for (final TreeItem<Object> item : searchNode.getChildren())
      if (item.getValue().toString().toLowerCase().contains(searchFilter.toLowerCase()))
        matches.add(item);
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
      final Optional<FamilyTree> familyTree = FamilyTreeView.this.familyTree();
      if (familyTree.isPresent() && item instanceof Person p && familyTree.get().isRoot(p))
        this.setGraphic(FamilyTreeView.this.config.theme().getIcon(Icon.TREE_ROOT, Icon.Size.SMALL));
      else
        this.setGraphic(null);
    }
  }
}
