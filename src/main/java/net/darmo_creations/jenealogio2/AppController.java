package net.darmo_creations.jenealogio2;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import net.darmo_creations.jenealogio2.model.FamilyTree;
import net.darmo_creations.jenealogio2.model.LifeEvent;
import net.darmo_creations.jenealogio2.model.Person;
import net.darmo_creations.jenealogio2.themes.Icon;
import net.darmo_creations.jenealogio2.themes.Theme;
import net.darmo_creations.jenealogio2.ui.ChildInfo;
import net.darmo_creations.jenealogio2.ui.FamilyTreeComponent;
import net.darmo_creations.jenealogio2.ui.FamilyTreePane;
import net.darmo_creations.jenealogio2.ui.FamilyTreeView;
import net.darmo_creations.jenealogio2.ui.dialogs.AboutDialog;
import net.darmo_creations.jenealogio2.ui.dialogs.Alerts;
import net.darmo_creations.jenealogio2.ui.dialogs.EditPersonDialog;
import net.darmo_creations.jenealogio2.ui.dialogs.SettingsDialog;
import net.darmo_creations.jenealogio2.utils.FormatArg;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Optional;

// TODO add right panel to display summary of selected person’s data
public class AppController {
  @FXML
  private MenuItem newFileMenuItem;
  @FXML
  private MenuItem openFileMenuItem;
  @FXML
  private MenuItem saveMenuItem;
  @FXML
  private MenuItem saveAsMenuItem;
  @FXML
  private MenuItem settingsMenuItem;
  @FXML
  private MenuItem quitMenuItem;
  @FXML
  private MenuItem undoMenuItem;
  @FXML
  private MenuItem redoMenuItem;
  @FXML
  private MenuItem setAsRootMenuItem;
  @FXML
  private MenuItem addPersonMenuItem;
  @FXML
  private MenuItem editPersonMenuItem;
  @FXML
  private MenuItem removePersonMenuItem;
  @FXML
  private MenuItem addChildMenuItem;
  @FXML
  private MenuItem addSiblingMenuItem;
  @FXML
  private MenuItem editParentsMenuItem;
  @FXML
  private MenuItem editLifeEventsMenuItem;
  @FXML
  private MenuItem setPictureMenuItem;
  @FXML
  private MenuItem calculateRelationshipsMenuItem;
  @FXML
  private MenuItem birthdaysMenuItem;
  @FXML
  private MenuItem mapMenuItem;
  @FXML
  private MenuItem checkInconsistenciesMenuItem;
  @FXML
  private MenuItem aboutMenuItem;

  @FXML
  private Button newToolbarButton;
  @FXML
  private Button openToolbarButton;
  @FXML
  private Button saveToolbarButton;
  @FXML
  private Button saveAsToolbarButton;
  @FXML
  private Button undoToolbarButton;
  @FXML
  private Button redoToolbarButton;
  @FXML
  private Button setAsRootToolbarButton;
  @FXML
  private Button addPersonToolbarButton;
  @FXML
  private Button addChildToolbarButton;
  @FXML
  private Button addSiblingToolbarButton;
  @FXML
  private Button editParentsToolbarButton;
  @FXML
  private Button editLifeEventsToolbarButton;
  @FXML
  private Button setPictureToolbarButton;
  @FXML
  private Button calculateRelationshipsToolbarButton;
  @FXML
  private Button birthdaysToolbarButton;
  @FXML
  private Button mapToolbarButton;
  @FXML
  private Button checkInconsistenciesToolbarButton;

  @FXML
  private AnchorPane sideTreeView;
  @FXML
  private AnchorPane mainPane;

  private final FamilyTreeView familyTreeView = new FamilyTreeView();
  private final FamilyTreePane familyTreePane = new FamilyTreePane();
  private FamilyTreeComponent focusedComponent;

  private final EditPersonDialog editPersonDialog = new EditPersonDialog();
  private final SettingsDialog settingsDialog = new SettingsDialog();
  private final AboutDialog aboutDialog = new AboutDialog();

  private FamilyTree familyTree;

  public void initialize() {
    Theme theme = App.config().theme();

    // Menu items
    this.newFileMenuItem.setGraphic(theme.getIcon(Icon.NEW_FILE, Icon.Size.SMALL));
    this.openFileMenuItem.setGraphic(theme.getIcon(Icon.OPEN_FILE, Icon.Size.SMALL));
    this.saveMenuItem.setGraphic(theme.getIcon(Icon.SAVE, Icon.Size.SMALL));
    this.saveAsMenuItem.setGraphic(theme.getIcon(Icon.SAVE_AS, Icon.Size.SMALL));
    this.settingsMenuItem.setGraphic(theme.getIcon(Icon.SETTINGS, Icon.Size.SMALL));
    this.settingsMenuItem.setOnAction(event -> this.onSettingsAction());
    this.quitMenuItem.setGraphic(theme.getIcon(Icon.QUIT, Icon.Size.SMALL));
    this.quitMenuItem.setOnAction(event -> this.onQuitAction());

    this.undoMenuItem.setGraphic(theme.getIcon(Icon.UNDO, Icon.Size.SMALL));
    this.redoMenuItem.setGraphic(theme.getIcon(Icon.REDO, Icon.Size.SMALL));
    this.setAsRootMenuItem.setGraphic(theme.getIcon(Icon.SET_AS_ROOT, Icon.Size.SMALL));
    this.setAsRootMenuItem.setOnAction(event -> this.onSetAsRootAction());
    this.addPersonMenuItem.setGraphic(theme.getIcon(Icon.ADD_PERSON, Icon.Size.SMALL));
    this.addPersonMenuItem.setOnAction(event -> this.onAddPersonAction());
    this.editPersonMenuItem.setGraphic(theme.getIcon(Icon.EDIT_PERSON, Icon.Size.SMALL));
    this.editPersonMenuItem.setOnAction(event -> this.onEditPersonAction());
    this.removePersonMenuItem.setGraphic(theme.getIcon(Icon.REMOVE_PERSON, Icon.Size.SMALL));
    this.removePersonMenuItem.setOnAction(event -> this.onRemovePersonAction());
    this.addChildMenuItem.setGraphic(theme.getIcon(Icon.ADD_CHILD, Icon.Size.SMALL));
    this.addSiblingMenuItem.setGraphic(theme.getIcon(Icon.ADD_SIBLING, Icon.Size.SMALL));
    this.editParentsMenuItem.setGraphic(theme.getIcon(Icon.EDIT_PARENTS, Icon.Size.SMALL));
    this.editParentsMenuItem.setOnAction(event -> this.onEditParentsAction());
    this.editLifeEventsMenuItem.setGraphic(theme.getIcon(Icon.EDIT_LIFE_EVENTS, Icon.Size.SMALL));
    this.editLifeEventsMenuItem.setOnAction(event -> this.onEditLifeEventsAction());
    this.setPictureMenuItem.setGraphic(theme.getIcon(Icon.SET_PICTURE, Icon.Size.SMALL));

    this.calculateRelationshipsMenuItem.setGraphic(theme.getIcon(Icon.CALCULATE_RELATIONSHIPS, Icon.Size.SMALL));
    this.birthdaysMenuItem.setGraphic(theme.getIcon(Icon.BIRTHDAYS, Icon.Size.SMALL));
    this.mapMenuItem.setGraphic(theme.getIcon(Icon.MAP, Icon.Size.SMALL));
    this.checkInconsistenciesMenuItem.setGraphic(theme.getIcon(Icon.CHECK_INCONSISTENCIES, Icon.Size.SMALL));

    this.aboutMenuItem.setGraphic(theme.getIcon(Icon.ABOUT, Icon.Size.SMALL));
    this.aboutMenuItem.setOnAction(event -> this.onAboutAction());

    // Toolbar buttons
    this.newToolbarButton.setGraphic(theme.getIcon(Icon.NEW_FILE, Icon.Size.BIG));
    this.openToolbarButton.setGraphic(theme.getIcon(Icon.OPEN_FILE, Icon.Size.BIG));
    this.saveToolbarButton.setGraphic(theme.getIcon(Icon.SAVE, Icon.Size.BIG));
    this.saveAsToolbarButton.setGraphic(theme.getIcon(Icon.SAVE_AS, Icon.Size.BIG));

    this.undoToolbarButton.setGraphic(theme.getIcon(Icon.UNDO, Icon.Size.BIG));
    this.redoToolbarButton.setGraphic(theme.getIcon(Icon.REDO, Icon.Size.BIG));
    this.setAsRootToolbarButton.setGraphic(theme.getIcon(Icon.SET_AS_ROOT, Icon.Size.BIG));
    this.setAsRootToolbarButton.setOnAction(event -> this.onSetAsRootAction());
    this.addPersonToolbarButton.setGraphic(theme.getIcon(Icon.ADD_PERSON, Icon.Size.BIG));
    this.addPersonToolbarButton.setOnAction(event -> this.onAddPersonAction());
    this.addChildToolbarButton.setGraphic(theme.getIcon(Icon.ADD_CHILD, Icon.Size.BIG));
    this.addSiblingToolbarButton.setGraphic(theme.getIcon(Icon.ADD_SIBLING, Icon.Size.BIG));
    this.editParentsToolbarButton.setGraphic(theme.getIcon(Icon.EDIT_PARENTS, Icon.Size.BIG));
    this.editParentsToolbarButton.setOnAction(event -> this.onEditParentsAction());
    this.editLifeEventsToolbarButton.setGraphic(theme.getIcon(Icon.EDIT_LIFE_EVENTS, Icon.Size.BIG));
    this.editLifeEventsToolbarButton.setOnAction(event -> this.onEditLifeEventsAction());
    this.setPictureToolbarButton.setGraphic(theme.getIcon(Icon.SET_PICTURE, Icon.Size.BIG));

    this.calculateRelationshipsToolbarButton.setGraphic(theme.getIcon(Icon.CALCULATE_RELATIONSHIPS, Icon.Size.BIG));
    this.birthdaysToolbarButton.setGraphic(theme.getIcon(Icon.BIRTHDAYS, Icon.Size.BIG));
    this.mapToolbarButton.setGraphic(theme.getIcon(Icon.MAP, Icon.Size.BIG));
    this.checkInconsistenciesToolbarButton.setGraphic(theme.getIcon(Icon.CHECK_INCONSISTENCIES, Icon.Size.BIG));

    AnchorPane.setTopAnchor(this.familyTreeView, 0.0);
    AnchorPane.setBottomAnchor(this.familyTreeView, 0.0);
    AnchorPane.setLeftAnchor(this.familyTreeView, 0.0);
    AnchorPane.setRightAnchor(this.familyTreeView, 0.0);
    this.sideTreeView.getChildren().add(this.familyTreeView);
    this.familyTreeView.personClickListeners()
        .add((person, clickCount) -> this.onPersonClick(person, clickCount, true));

    AnchorPane.setTopAnchor(this.familyTreePane, 0.0);
    AnchorPane.setBottomAnchor(this.familyTreePane, 0.0);
    AnchorPane.setLeftAnchor(this.familyTreePane, 0.0);
    AnchorPane.setRightAnchor(this.familyTreePane, 0.0);
    this.mainPane.getChildren().add(this.familyTreePane);
    this.familyTreePane.personClickListeners()
        .add((person, clickCount) -> this.onPersonClick(person, clickCount, false));
    this.familyTreePane.newParentClickListeners().add(this::onNewParentClick);

    // TEMP
    this.familyTree = new FamilyTree();
    this.familyTreeView.setFamilyTree(this.familyTree);
    this.familyTreePane.setFamilyTree(this.familyTree);

    this.updateButtons();
  }

  public void onShown() {
    this.familyTreePane.refresh();
  }

  private Optional<Person> getSelectedPerson() {
    if (this.focusedComponent != null) {
      return this.focusedComponent.getSelectedPerson();
    }
    return Optional.empty();
  }

  private void onSetAsRootAction() {
    this.getSelectedPerson().ifPresent(root -> {
      this.familyTree.setRoot(root);
      this.updateButtons();
    });
  }

  private void onAddPersonAction() {
    this.openEditPersonDialog(null, null, EditPersonDialog.TAB_PROFILE);
  }

  private void onEditPersonAction() {
    this.getSelectedPerson().ifPresent(
        person -> this.openEditPersonDialog(person, null, EditPersonDialog.TAB_PROFILE));
  }

  private void onEditParentsAction() {
    this.getSelectedPerson().ifPresent(
        person -> this.openEditPersonDialog(person, null, EditPersonDialog.TAB_PARENTS));
  }

  private void onEditLifeEventsAction() {
    this.getSelectedPerson().ifPresent(
        person -> this.openEditPersonDialog(person, null, EditPersonDialog.TAB_EVENTS));
  }

  private void onRemovePersonAction() {
    this.getSelectedPerson().ifPresent(person -> {
      if (this.familyTree.isRoot(person)) {
        Alerts.warning(
            "alert.cannot_delete_root.header",
            "alert.cannot_delete_root.content",
            null,
            new FormatArg("person", person)
        );
        return;
      }
      boolean delete = Alerts.confirmation(
          "alert.delete_person.header", null, "alert.delete_person.title");
      if (delete) {
        for (LifeEvent lifeEvent : person.getLifeEventsAsActor()) {
          if (lifeEvent.actors().size() <= lifeEvent.type().minActors()) {
            lifeEvent.actors().forEach(a -> a.removeLifeEvent(lifeEvent));
            lifeEvent.witnesses().forEach(w -> w.removeLifeEvent(lifeEvent));
          } else {
            person.removeLifeEvent(lifeEvent);
          }
        }
        for (LifeEvent lifeEvent : person.getLifeEventsAsWitness()) {
          lifeEvent.removeWitness(person);
        }
        for (Person child : new HashSet<>(person.children())) {
          for (Person.RelativeType type : Person.RelativeType.values()) {
            child.removeRelative(person, type);
          }
          child.removeParent(person);
        }
        this.familyTree.removePerson(person);
        this.familyTreePane.refresh();
        this.familyTreeView.refresh();
        this.updateButtons();
      }
    });
  }

  private void onNewParentClick(@NotNull ChildInfo childInfo) {
    this.openEditPersonDialog(null, childInfo, EditPersonDialog.TAB_PROFILE);
  }

  private void onPersonClick(Person person, int clickCount, boolean inTree) {
    this.focusedComponent = inTree ? this.familyTreeView : this.familyTreePane;
    if (App.config().shouldSyncTreeWithMainPane()) {
      if (inTree) {
        this.familyTreePane.selectPerson(person);
      } else {
        this.familyTreeView.selectPerson(person);
      }
    }
    if (clickCount == 2) {
      this.openEditPersonDialog(person, null, EditPersonDialog.TAB_PROFILE);
    }
    this.updateButtons();
  }

  private void openEditPersonDialog(Person person, ChildInfo childInfo, int tabIndex) {
    this.editPersonDialog.setPerson(person, childInfo, this.familyTree);
    this.editPersonDialog.selectTab(tabIndex);
    Optional<Person> person_ = this.editPersonDialog.showAndWait();
    if (person_.isPresent()) {
      this.familyTreeView.refresh();
      this.familyTreePane.refresh();
      if (person == null && childInfo == null) {
        this.familyTreePane.selectPerson(person_.get());
      }
      this.updateButtons();
    }
  }

  private void updateButtons() {
    Optional<Person> selectedPerson = this.getSelectedPerson();
    boolean selection = selectedPerson.isPresent();
    boolean hasBothParents = selection && selectedPerson.get().hasBothParents();
    boolean selectedIsRoot = selection && selectedPerson.map(this.familyTree::isRoot).orElse(false);

    this.setAsRootMenuItem.setDisable(!selection || selectedIsRoot);
    this.editPersonMenuItem.setDisable(!selection);
    this.removePersonMenuItem.setDisable(!selection || selectedIsRoot);
    this.addChildMenuItem.setDisable(!selection);
    this.addSiblingMenuItem.setDisable(!hasBothParents);
    this.editParentsMenuItem.setDisable(!selection);
    this.editLifeEventsMenuItem.setDisable(!selection);
    this.setPictureMenuItem.setDisable(!selection);

    this.setAsRootToolbarButton.setDisable(!selection || selectedIsRoot);
    this.addChildToolbarButton.setDisable(!selection);
    this.addSiblingToolbarButton.setDisable(!hasBothParents);
    this.editParentsToolbarButton.setDisable(!selection);
    this.editLifeEventsToolbarButton.setDisable(!selection);
    this.setPictureToolbarButton.setDisable(!selection);
  }

  private void onSettingsAction() {
    this.settingsDialog.resetLocalConfig();
    this.settingsDialog.showAndWait();
  }

  private void onAboutAction() {
    this.aboutDialog.showAndWait();
  }

  private void onQuitAction() {
    Platform.exit();
  }
}
