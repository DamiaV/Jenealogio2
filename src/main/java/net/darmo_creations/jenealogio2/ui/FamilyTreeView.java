package net.darmo_creations.jenealogio2.ui;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import net.darmo_creations.jenealogio2.App;
import net.darmo_creations.jenealogio2.config.Language;
import net.darmo_creations.jenealogio2.model.Person;
import net.darmo_creations.jenealogio2.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * JavaFX component displaying the list of all persons in a family tree.
 */
public class FamilyTreeView extends FamilyTreeComponent {
  private final TextField searchField = new TextField();
  private final TreeView<Object> treeView = new TreeView<>();
  private final TreeItem<Object> personsItem;

  private boolean internalSelectionChange;

  /**
   * Create an empty family tree view.
   */
  public FamilyTreeView() {
    Language language = App.config().language();

    VBox vBox = new VBox(4);
    AnchorPane.setTopAnchor(vBox, 0.0);
    AnchorPane.setBottomAnchor(vBox, 0.0);
    AnchorPane.setLeftAnchor(vBox, 0.0);
    AnchorPane.setRightAnchor(vBox, 0.0);
    this.getChildren().add(vBox);

    this.searchField.setPromptText(language.translate("treeview.search"));
    this.searchField.setOnKeyTyped(this::onSearchChanged);
    vBox.getChildren().add(this.searchField);

    this.treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    this.treeView.setShowRoot(false);
    TreeItem<Object> root = new TreeItem<>();
    this.personsItem = new TreeItem<>(language.translate("treeview.persons"));
    root.getChildren().add(this.personsItem);
    this.treeView.setRoot(root);
    VBox.setVgrow(this.treeView, Priority.ALWAYS);
    vBox.getChildren().add(this.treeView);
    this.treeView.getSelectionModel().selectedItemProperty().addListener(this::onSelectionChange);
  }

  private void onSearchChanged(KeyEvent keyEvent) {
    this.personsItem.getChildren().forEach(item -> {
      Person person = (Person) item.getValue();
      Optional<String> filter = StringUtils.stripNullable(this.searchField.getText());
      boolean visible = filter.isEmpty() || person.toString().toLowerCase().contains(filter.get().toLowerCase());
      // TODO set tree item visibility
    });
  }

  public void refresh() {
    this.personsItem.getChildren().clear();
    this.familyTree().ifPresent(familyTree -> familyTree.persons().stream()
        .sorted((p1, p2) -> {
          int c1 = p1.getLastName().orElse("")
              .compareTo(p2.getLastName().orElse(""));
          if (c1 != 0) {
            return c1;
          }
          int c2 = p1.getFirstNames().orElse("")
              .compareTo(p2.getFirstNames().orElse(""));
          if (c2 != 0) {
            return c2;
          }
          return p1.disambiguationID().orElse(-1)
              .compareTo(p2.disambiguationID().orElse(-1));
        })
        .forEach(person -> this.personsItem.getChildren().add(new TreeItem<>(person)))
    );
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

  private void onSelectionChange(ObservableValue<? extends TreeItem<Object>> observable, TreeItem<Object> oldValue, TreeItem<Object> newValue) {
    if (!this.internalSelectionChange && newValue != null && newValue.getValue() instanceof Person person) {
      this.firePersonClickEvent(person, 1);
    }
  }
}
