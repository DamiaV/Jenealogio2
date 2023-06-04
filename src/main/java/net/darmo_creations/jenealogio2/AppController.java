package net.darmo_creations.jenealogio2;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import net.darmo_creations.jenealogio2.model.FamilyTree;
import net.darmo_creations.jenealogio2.model.Person;
import net.darmo_creations.jenealogio2.themes.Icon;
import net.darmo_creations.jenealogio2.themes.Theme;
import net.darmo_creations.jenealogio2.ui.FamilyTreeComponent;
import net.darmo_creations.jenealogio2.ui.FamilyTreePane;
import net.darmo_creations.jenealogio2.ui.FamilyTreeView;
import net.darmo_creations.jenealogio2.ui.dialogs.AboutDialog;
import net.darmo_creations.jenealogio2.ui.dialogs.EditPersonDialog;
import net.darmo_creations.jenealogio2.ui.dialogs.SettingsDialog;

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
  private MenuItem editPartnersMenuItem;
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
  private Button addPersonToolbarButton;
  @FXML
  private Button addChildToolbarButton;
  @FXML
  private Button addSiblingToolbarButton;
  @FXML
  private Button editParentsToolbarButton;
  @FXML
  private Button editPartnersToolbarButton;
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
    this.addPersonMenuItem.setGraphic(theme.getIcon(Icon.ADD_PERSON, Icon.Size.SMALL));
    this.editPersonMenuItem.setGraphic(theme.getIcon(Icon.EDIT_PERSON, Icon.Size.SMALL));
    this.editPersonMenuItem.setOnAction(event -> this.onEditPersonAction());
    this.removePersonMenuItem.setGraphic(theme.getIcon(Icon.REMOVE_PERSON, Icon.Size.SMALL));
    this.addChildMenuItem.setGraphic(theme.getIcon(Icon.ADD_CHILD, Icon.Size.SMALL));
    this.addSiblingMenuItem.setGraphic(theme.getIcon(Icon.ADD_SIBLING, Icon.Size.SMALL));
    this.editParentsMenuItem.setGraphic(theme.getIcon(Icon.EDIT_PARENTS, Icon.Size.SMALL));
    this.editPartnersMenuItem.setGraphic(theme.getIcon(Icon.EDIT_PARTNERS, Icon.Size.SMALL));
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
    this.addPersonToolbarButton.setGraphic(theme.getIcon(Icon.ADD_PERSON, Icon.Size.BIG));
    this.addChildToolbarButton.setGraphic(theme.getIcon(Icon.ADD_CHILD, Icon.Size.BIG));
    this.addSiblingToolbarButton.setGraphic(theme.getIcon(Icon.ADD_SIBLING, Icon.Size.BIG));
    this.editParentsToolbarButton.setGraphic(theme.getIcon(Icon.EDIT_PARENTS, Icon.Size.BIG));
    this.editPartnersToolbarButton.setGraphic(theme.getIcon(Icon.EDIT_PARTNERS, Icon.Size.BIG));
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

    // TEMP
    this.familyTree = new FamilyTree();
    this.familyTreeView.setFamilyTree(this.familyTree);
    this.familyTreePane.setFamilyTree(this.familyTree);

    this.editPersonDialog.resultProperty().addListener((observable, oldValue, newValue) -> this.onPersonEdited());

    this.updateButtons(null);
  }

  private void onEditPersonAction() {
    if (this.focusedComponent != null) {
      this.focusedComponent.getSelectedPerson().ifPresent(this::openEditPersonDialog);
    }
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
      this.openEditPersonDialog(person);
    }
    this.updateButtons(person);
  }

  private void openEditPersonDialog(Person person) {
    this.editPersonDialog.setPerson(person, this.familyTree);
    this.editPersonDialog.show();
  }

  private void onPersonEdited() {
    this.editPersonDialog.getResult().ifPresent(result -> {
      if (result.isPersonCreated()) {
        this.familyTree.persons().add(result.person());
      }
      this.familyTreeView.refresh();
      this.familyTreePane.refresh();
    });
  }

  private void updateButtons(final Person selectedPerson) {
    boolean selection = selectedPerson != null;
    boolean hasParents = selection && selectedPerson.hasBothParents();

    this.editPersonMenuItem.setDisable(!selection);
    this.removePersonMenuItem.setDisable(!selection);
    this.addChildMenuItem.setDisable(!selection);
    this.addSiblingMenuItem.setDisable(!hasParents);
    this.editParentsMenuItem.setDisable(!selection);
    this.editPartnersMenuItem.setDisable(!selection);
    this.setPictureMenuItem.setDisable(!selection);

    this.addChildToolbarButton.setDisable(!selection);
    this.addSiblingToolbarButton.setDisable(!hasParents);
    this.editParentsToolbarButton.setDisable(!selection);
    this.editPartnersToolbarButton.setDisable(!selection);
    this.setPictureToolbarButton.setDisable(!selection);
  }

  private void onSettingsAction() {
    this.settingsDialog.resetLocalConfig();
    this.settingsDialog.show();
  }

  private void onAboutAction() {
    this.aboutDialog.show();
  }

  private void onQuitAction() {
    Platform.exit();
  }
}
