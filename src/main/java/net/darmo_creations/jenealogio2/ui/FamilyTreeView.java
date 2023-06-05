package net.darmo_creations.jenealogio2.ui;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import net.darmo_creations.jenealogio2.App;
import net.darmo_creations.jenealogio2.config.Config;
import net.darmo_creations.jenealogio2.config.Language;
import net.darmo_creations.jenealogio2.model.Person;
import net.darmo_creations.jenealogio2.themes.Icon;
import net.darmo_creations.jenealogio2.themes.Theme;
import net.darmo_creations.jenealogio2.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * JavaFX component displaying the list of all persons in a family tree.
 */
public class FamilyTreeView extends FamilyTreeComponent {
  private final TextField searchField = new TextField();
  private final ObservableSet<TreeItem<Object>> searchMatches = FXCollections.observableSet(new HashSet<>());
  private final TreeView<Object> treeView = new TreeView<>();
  private final TreeItem<Object> personsItem;
  private final Button clearButton;

  private boolean internalSelectionChange;

  /**
   * Create an empty family tree view.
   */
  public FamilyTreeView() {
    Config config = App.config();
    Language language = config.language();
    Theme theme = config.theme();

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
    this.searchField.textProperty().addListener(this::onSearchFilterChange);
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

    ToggleButton syncTreeButton = new ToggleButton("", theme.getIcon(Icon.SYNC_TREE, Icon.Size.SMALL));
    syncTreeButton.setTooltip(new Tooltip(language.translate("treeview.sync_tree")));
    syncTreeButton.setSelected(config.shouldSyncTreeWithMainPane());
    syncTreeButton.selectedProperty().addListener(
        (observable, oldValue, newValue) -> {
          config.setShouldSyncTreeWithMainPane(newValue);
          try {
            config.save();
          } catch (IOException e) {
            App.LOGGER.exception(e);
          }
        });
    hBox.getChildren().add(syncTreeButton);

    // Tree view

    VBox.setVgrow(this.treeView, Priority.ALWAYS);
    vBox.getChildren().add(this.treeView);
    this.treeView.setCellFactory(tree -> new SearchHighlightingTreeCell(this.searchMatches));
    this.treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    this.treeView.setShowRoot(false);
    TreeItem<Object> root = new TreeItem<>();
    this.personsItem = new TreeItem<>(language.translate("treeview.persons"));
    root.getChildren().add(this.personsItem);
    this.treeView.setRoot(root);
    this.treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      if (!this.internalSelectionChange && newValue != null && newValue.getValue() instanceof Person person) {
        this.firePersonClickEvent(person, 1);
      }
    });
    this.treeView.setOnMouseClicked(event -> {
      TreeItem<Object> selectedItem = this.treeView.getSelectionModel().getSelectedItem();
      if (event.getClickCount() != 1 && selectedItem != null && selectedItem.getValue() instanceof Person person) {
        this.firePersonClickEvent(person, event.getClickCount());
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
  }

  @Override
  public Optional<Person> getSelectedPerson() {
    TreeItem<Object> selectedItem = this.treeView.getSelectionModel().getSelectedItem();
    return selectedItem != null && selectedItem.getValue() instanceof Person person
        ? Optional.of(person)
        : Optional.empty();
  }

  @Override
  protected void deselectAll() {
    this.internalSelectionChange = true;
    this.treeView.getSelectionModel().clearSelection();
    this.internalSelectionChange = false;
  }

  @Override
  protected void select(@NotNull Person person) {
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

  private void onSearchFilterChange(ObservableValue<? extends String> observable, String oldValue, String newValue) {
    this.searchMatches.clear();
    Optional<String> filter = StringUtils.stripNullable(newValue);
    this.clearButton.setDisable(filter.isEmpty());
    if (filter.isEmpty()) {
      return;
    }
    Set<TreeItem<Object>> matches = new HashSet<>();
    this.searchMatchingItems(this.personsItem, matches, filter.get());
    this.searchMatches.addAll(matches);
  }

  private void searchMatchingItems(
      @NotNull TreeItem<Object> searchNode, @NotNull Set<TreeItem<Object>> matches, @NotNull String searchValue) {
    for (TreeItem<Object> item : searchNode.getChildren()) {
      if (item.getValue().toString().toLowerCase().contains(searchValue.toLowerCase())) {
        matches.add(item);
      }
    }
  }

  // From https://stackoverflow.com/a/34914538/3779986
  private static class SearchHighlightingTreeCell extends TreeCell<Object> {
    // Cannot be local or else it would be garbage-collected
    @SuppressWarnings("FieldCanBeLocal")
    private final BooleanBinding matchesSearch;

    public SearchHighlightingTreeCell(ObservableSet<TreeItem<Object>> searchMatches) {
      this.matchesSearch = Bindings.createBooleanBinding(
          () -> searchMatches.contains(this.getTreeItem()),
          this.treeItemProperty(),
          searchMatches
      );
      this.matchesSearch.addListener((obs, didMatchSearch, nowMatchesSearch) ->
          this.pseudoClassStateChanged(PseudoClasses.SEARCH_MATCH, nowMatchesSearch));
    }

    @Override
    protected void updateItem(Object item, boolean empty) {
      // Update the text when the displayed item changes
      super.updateItem(item, empty);
      this.setText(empty ? null : item.toString());
    }
  }
}
