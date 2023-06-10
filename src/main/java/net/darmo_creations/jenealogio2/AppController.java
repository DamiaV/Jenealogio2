package net.darmo_creations.jenealogio2;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import net.darmo_creations.jenealogio2.io.TreeFileReader;
import net.darmo_creations.jenealogio2.io.TreeFileWriter;
import net.darmo_creations.jenealogio2.model.FamilyTree;
import net.darmo_creations.jenealogio2.model.LifeEvent;
import net.darmo_creations.jenealogio2.model.Person;
import net.darmo_creations.jenealogio2.themes.Icon;
import net.darmo_creations.jenealogio2.themes.Theme;
import net.darmo_creations.jenealogio2.ui.ChildInfo;
import net.darmo_creations.jenealogio2.ui.FamilyTreeComponent;
import net.darmo_creations.jenealogio2.ui.FamilyTreePane;
import net.darmo_creations.jenealogio2.ui.FamilyTreeView;
import net.darmo_creations.jenealogio2.ui.dialogs.*;
import net.darmo_creations.jenealogio2.utils.FormatArg;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

/**
 * Application’s main controller.
 */
// TODO add right panel to display summary of selected person’s data
public class AppController {
  /**
   * Mouse button used to select the target in the tree pane.
   */
  public static final MouseButton TARGET_UPDATE_BUTTON = MouseButton.SECONDARY;

  /**
   * The stage associated to this controller.
   */
  private Stage stage;

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
  private MenuItem renameTreeMenuItem;
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

  // File managers
  private final TreeFileReader treeFileReader = new TreeFileReader();
  private final TreeFileWriter treeFileWriter = new TreeFileWriter();

  // Tree components
  private FamilyTreeComponent focusedComponent;
  private final FamilyTreeView familyTreeView = new FamilyTreeView();
  private final FamilyTreePane familyTreePane = new FamilyTreePane();

  // Dialogs
  private final EditPersonDialog editPersonDialog = new EditPersonDialog();
  private final SettingsDialog settingsDialog = new SettingsDialog();
  private final AboutDialog aboutDialog = new AboutDialog();

  /**
   * Currently loaded family tree.
   */
  private FamilyTree familyTree;
  /**
   * File the current tree has been loaded from, will be null for new trees.
   */
  private File loadedFile;
  /**
   * Indicate whether there are any unsaved changes.
   */
  private boolean unsavedChanges;
  /**
   * Indicate whether the current tree is the default one.
   */
  private boolean defaultEmptyTree;

  /**
   * Initialize this controller.
   * <p>
   * Automatically called by JavaFX.
   */
  public void initialize() {
    Theme theme = App.config().theme();

    // Menu items
    this.newFileMenuItem.setGraphic(theme.getIcon(Icon.NEW_FILE, Icon.Size.SMALL));
    this.newFileMenuItem.setOnAction(event -> this.onNewFileAction());
    this.openFileMenuItem.setGraphic(theme.getIcon(Icon.OPEN_FILE, Icon.Size.SMALL));
    this.openFileMenuItem.setOnAction(event -> this.onOpenFileAction());
    this.saveMenuItem.setGraphic(theme.getIcon(Icon.SAVE, Icon.Size.SMALL));
    this.saveMenuItem.setOnAction(event -> this.onSaveAction());
    this.saveAsMenuItem.setGraphic(theme.getIcon(Icon.SAVE_AS, Icon.Size.SMALL));
    this.saveAsMenuItem.setOnAction(event -> this.onSaveAsAction());
    this.settingsMenuItem.setGraphic(theme.getIcon(Icon.SETTINGS, Icon.Size.SMALL));
    this.settingsMenuItem.setOnAction(event -> this.onSettingsAction());
    this.quitMenuItem.setGraphic(theme.getIcon(Icon.QUIT, Icon.Size.SMALL));
    this.quitMenuItem.setOnAction(event -> this.onQuitAction());

    this.undoMenuItem.setGraphic(theme.getIcon(Icon.UNDO, Icon.Size.SMALL));
    this.redoMenuItem.setGraphic(theme.getIcon(Icon.REDO, Icon.Size.SMALL));
    this.renameTreeMenuItem.setGraphic(theme.getIcon(Icon.RENAME_TREE, Icon.Size.SMALL));
    this.renameTreeMenuItem.setOnAction(event -> this.onRenameTreeAction());
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
    this.addSiblingMenuItem.setOnAction(event -> this.onAddSiblingAction());
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
    this.newToolbarButton.setOnAction(event -> this.onNewFileAction());
    this.openToolbarButton.setGraphic(theme.getIcon(Icon.OPEN_FILE, Icon.Size.BIG));
    this.openToolbarButton.setOnAction(event -> this.onOpenFileAction());
    this.saveToolbarButton.setGraphic(theme.getIcon(Icon.SAVE, Icon.Size.BIG));
    this.saveToolbarButton.setOnAction(event -> this.onSaveAction());
    this.saveAsToolbarButton.setGraphic(theme.getIcon(Icon.SAVE_AS, Icon.Size.BIG));
    this.saveAsToolbarButton.setOnAction(event -> this.onSaveAsAction());

    this.undoToolbarButton.setGraphic(theme.getIcon(Icon.UNDO, Icon.Size.BIG));
    this.redoToolbarButton.setGraphic(theme.getIcon(Icon.REDO, Icon.Size.BIG));
    this.setAsRootToolbarButton.setGraphic(theme.getIcon(Icon.SET_AS_ROOT, Icon.Size.BIG));
    this.setAsRootToolbarButton.setOnAction(event -> this.onSetAsRootAction());
    this.addPersonToolbarButton.setGraphic(theme.getIcon(Icon.ADD_PERSON, Icon.Size.BIG));
    this.addPersonToolbarButton.setOnAction(event -> this.onAddPersonAction());
    this.addChildToolbarButton.setGraphic(theme.getIcon(Icon.ADD_CHILD, Icon.Size.BIG));
    this.addSiblingToolbarButton.setGraphic(theme.getIcon(Icon.ADD_SIBLING, Icon.Size.BIG));
    this.addSiblingToolbarButton.setOnAction(event -> this.onAddSiblingAction());
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
        .add((person, clickCount, button) -> this.onPersonClick(person, clickCount, button, true));

    AnchorPane.setTopAnchor(this.familyTreePane, 0.0);
    AnchorPane.setBottomAnchor(this.familyTreePane, 0.0);
    AnchorPane.setLeftAnchor(this.familyTreePane, 0.0);
    AnchorPane.setRightAnchor(this.familyTreePane, 0.0);
    this.mainPane.getChildren().add(this.familyTreePane);
    this.familyTreePane.personClickListeners()
        .add((person, clickCount, button) -> this.onPersonClick(person, clickCount, button, false));
    this.familyTreePane.newParentClickListeners().add(this::onNewParentClick);
  }

  /**
   * Called when the app’s main stage is shown.
   * <p>
   * Hooks callbacks to the stage’s close event and load the specified tree or the default one.
   *
   * @param stage App’s main stage.
   * @param file  File to load. May be null.
   */
  public void onShown(@NotNull Stage stage, File file) {
    this.stage = Objects.requireNonNull(stage);
    stage.setOnCloseRequest(event -> {
      event.consume();
      this.onQuitAction();
    });
    if (file != null && this.loadFile(file)) {
      return;
    }
    this.defaultEmptyTree = true;
    String defaultName = App.config().language().translate("app_title.undefined_tree_name");
    this.setFamilyTree(new FamilyTree(defaultName), null);
  }

  /**
   * Set the current family tree.
   *
   * @param tree Family tree to use.
   * @param file Optional file the tree has been loaded from.
   */
  private void setFamilyTree(@NotNull FamilyTree tree, File file) {
    this.familyTree = tree;
    this.familyTreeView.setFamilyTree(this.familyTree);
    this.familyTreePane.setFamilyTree(this.familyTree);
    this.familyTreeView.refresh();
    this.familyTreePane.refresh();
    this.loadedFile = file;
    this.unsavedChanges = file == null;
    this.updateUI();
  }

  /**
   * Open an alert dialog to rename the current tree.
   */
  private void onRenameTreeAction() {
    Optional<String> name = Alerts.textInput(
        "alert.tree_name.header", "alert.tree_name.label", null, null);
    if (name.isEmpty()) {
      return;
    }
    this.familyTree.setName(name.get());
    this.defaultEmptyTree = false;
    this.unsavedChanges = true;
    this.updateUI();
  }

  /**
   * Open an alert dialog to create a new tree.
   * <p>
   * Checks for any unsaved changes.
   */
  private void onNewFileAction() {
    if (!this.defaultEmptyTree && this.unsavedChanges) {
      boolean open = Alerts.confirmation(
          "alert.unsaved_changes.header", "alert.unsaved_changes.content", null);
      if (!open) {
        return;
      }
    }
    Optional<String> name = Alerts.textInput(
        "alert.tree_name.header", "alert.tree_name.label", null, null);
    if (name.isEmpty()) {
      return;
    }
    this.defaultEmptyTree = false;
    this.setFamilyTree(new FamilyTree(name.get()), null);
  }

  /**
   * Open a file chooser dialog to open a tree file.
   * <p>
   * Checks for any unsaved changes.
   */
  private void onOpenFileAction() {
    if (!this.defaultEmptyTree && this.unsavedChanges) {
      boolean open = Alerts.confirmation(
          "alert.unsaved_changes.header", "alert.unsaved_changes.content", null);
      if (!open) {
        return;
      }
    }
    Optional<File> f = FileChoosers.showTreeFileChooser(this.stage);
    if (f.isEmpty()) {
      return;
    }
    this.loadFile(f.get());
  }

  /**
   * Load a tree from a file.
   *
   * @param file File to read.
   * @return The tree read from the file.
   */
  private boolean loadFile(@NotNull File file) {
    App.LOGGER.info("Loading tree from %s…".formatted(file));
    FamilyTree familyTree;
    try {
      familyTree = this.treeFileReader.loadFile(file);
    } catch (IOException e) {
      App.LOGGER.exception(e);
      Alerts.error(
          "alert.load_error.header",
          "alert.load_error.content",
          "alert.load_error.title",
          new FormatArg("trace", e.getMessage())
      );
      return false;
    }
    App.LOGGER.info("Done");

    this.defaultEmptyTree = false;
    this.setFamilyTree(familyTree, file);
    return true;
  }

  /**
   * Save the current tree.
   * <p>
   * If the tree has not yet been saved, a file saver dialog is shown.
   */
  private void onSaveAction() {
    File file;
    if (this.loadedFile == null) {
      Optional<File> f = FileChoosers.showTreeFileSaver(this.stage);
      if (f.isEmpty()) {
        return;
      }
      file = f.get();
      // File existence is already checked by file chooser
    } else {
      file = this.loadedFile;
    }
    if (!this.saveFile(file)) {
      return;
    }
    if (this.loadedFile == null) {
      this.loadedFile = file;
    }
    this.unsavedChanges = false;
    this.updateUI();
  }

  /**
   * Open a file saver dialog to save the current tree to.
   */
  private void onSaveAsAction() {
    Optional<File> f = FileChoosers.showTreeFileSaver(this.stage);
    if (f.isEmpty()) {
      return;
    }
    File file = f.get();
    if (!this.saveFile(file)) {
      return;
    }
    this.loadedFile = file;
    this.unsavedChanges = false;
    this.updateUI();
  }

  /**
   * Save the current tree to a file.
   *
   * @param file File to save to.
   * @return True if save succeeded, false otherwise.
   */
  private boolean saveFile(@NotNull File file) {
    App.LOGGER.info("Saving tree to %s…".formatted(file));
    try {
      this.treeFileWriter.saveToFile(this.familyTree, file);
    } catch (IOException e) {
      App.LOGGER.exception(e);
      Alerts.error(
          "alert.save_error.header",
          "alert.save_error.content",
          "alert.save_error.title",
          new FormatArg("trace", e.getMessage())
      );
      return false;
    }
    App.LOGGER.info("Done");
    return true;
  }

  /**
   * Return the currently selected person in the focused tree component.
   */
  private Optional<Person> getSelectedPerson() {
    if (this.focusedComponent != null) {
      return this.focusedComponent.getSelectedPerson();
    }
    return Optional.empty();
  }

  /**
   * Set the selected person as the root of the current tree.
   */
  private void onSetAsRootAction() {
    this.getSelectedPerson().ifPresent(root -> {
      this.familyTree.setRoot(root);
      this.defaultEmptyTree = false;
      this.unsavedChanges = true;
      this.updateUI();
    });
  }

  /**
   * Open person edit dialog to create a new person.
   */
  private void onAddPersonAction() {
    this.openEditPersonDialog(null, null, null, null, EditPersonDialog.TAB_PROFILE);
  }

  /**
   * Open person edit dialog to edit the selected person.
   */
  private void onEditPersonAction() {
    this.getSelectedPerson().ifPresent(
        person -> this.openEditPersonDialog(person, null, null, null, EditPersonDialog.TAB_PROFILE));
  }

  /**
   * Open person edit dialog to edit the parents of the selected person.
   */
  private void onEditParentsAction() {
    this.getSelectedPerson().ifPresent(
        person -> this.openEditPersonDialog(person, null, null, null, EditPersonDialog.TAB_PARENTS));
  }

  /**
   * Open person edit dialog to edit the life events of the selected person.
   */
  private void onEditLifeEventsAction() {
    this.getSelectedPerson().ifPresent(
        person -> this.openEditPersonDialog(person, null, null, null, EditPersonDialog.TAB_EVENTS));
  }

  /**
   * Delete the currently selected person.
   * <p>
   * Person is removed from any life event it is an actor or witness of.
   */
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
        // Unlink life events from person
        for (LifeEvent lifeEvent : person.getLifeEventsAsActor()) {
          if (lifeEvent.actors().size() <= lifeEvent.type().minActors()) {
            lifeEvent.actors().forEach(a -> a.removeLifeEvent(lifeEvent));
            lifeEvent.witnesses().forEach(w -> w.removeLifeEvent(lifeEvent));
          } else {
            person.removeLifeEvent(lifeEvent);
          }
        }
        // Unlink person from life events
        for (LifeEvent lifeEvent : person.getLifeEventsAsWitness()) {
          lifeEvent.removeWitness(person);
        }

        // Unlink person’s parents
        person.setParent(0, null);
        person.setParent(1, null);
        // Unlink person’s children
        for (Person child : person.children()) {
          child.removeParent(person);
        }

        // Unlink person’s non-biological children and parents
        for (Person.RelativeType type : Person.RelativeType.values()) {
          for (Person nonBiologicalChild : person.nonBiologicalChildren(type)) {
            nonBiologicalChild.removeRelative(person, type);
          }
          for (Person relative : person.getRelatives(type)) {
            person.removeRelative(relative, type);
          }
        }
        // Remove person from tree
        this.familyTree.removePerson(person);

        // Update UI
        this.familyTreePane.refresh();
        this.familyTreeView.refresh();
        this.defaultEmptyTree = false;
        this.unsavedChanges = true;
        this.updateUI();
      }
    });
  }

  /**
   * Open person edit dialog to create a new sibling of the selected person.
   */
  private void onAddSiblingAction() {
    this.getSelectedPerson().ifPresent(person -> {
      var parents = person.parents();
      Person parent1 = parents.left().orElse(null);
      Person parent2 = parents.right().orElse(null);
      this.openEditPersonDialog(null, null, parent1, parent2, EditPersonDialog.TAB_PROFILE);
    });
  }

  /**
   * Open person edit dialog to create a parent of the given child.
   *
   * @param childInfo Information about the child of the parent to create.
   */
  private void onNewParentClick(@NotNull ChildInfo childInfo) {
    this.openEditPersonDialog(null, childInfo, null, null, EditPersonDialog.TAB_PROFILE);
  }

  /**
   * Update display when a person is clicked.
   *
   * @param person     Person that was clicked. May be null if none was.
   * @param clickCount Number of clicks.
   * @param button     Clicked mouse button.
   * @param inTree     True if the click occured inside the side tree view;
   *                   false if it occured inside the pane view.
   */
  private void onPersonClick(Person person, int clickCount, MouseButton button, boolean inTree) {
    this.focusedComponent = inTree ? this.familyTreeView : this.familyTreePane;
    if (App.config().shouldSyncTreeWithMainPane()) {
      if (inTree) {
        this.familyTreePane.selectPerson(person, button == TARGET_UPDATE_BUTTON);
      } else {
        this.familyTreeView.selectPerson(person, button == TARGET_UPDATE_BUTTON);
      }
    }
    if (clickCount == 2 && button == MouseButton.PRIMARY) {
      this.openEditPersonDialog(person, null, null, null, EditPersonDialog.TAB_PROFILE);
    }
    this.updateUI();
  }

  /**
   * Open person edit dialog.
   *
   * @param person    Person to edit. Null to create a new person.
   * @param childInfo Optional information about the person’s currently visible child.
   * @param tabIndex  Index of the tab to show.
   */
  private void openEditPersonDialog(Person person, ChildInfo childInfo, Person parent1, Person parent2, int tabIndex) {
    this.editPersonDialog.setPerson(person, childInfo, this.familyTree);
    if (parent1 != null || parent2 != null) {
      this.editPersonDialog.setParents(parent1, parent2);
    }
    this.editPersonDialog.selectTab(tabIndex);
    Optional<Person> p = this.editPersonDialog.showAndWait();
    if (p.isPresent()) {
      this.familyTreeView.refresh();
      this.familyTreePane.refresh();
      if (person == null && childInfo == null) {
        this.familyTreePane.selectPerson(p.get(), false);
      }
      this.defaultEmptyTree = false;
      this.unsavedChanges = true;
      this.updateUI();
    }
  }

  /**
   * Update the UI, i.e. menu items, toolbar buttons and stage’s title.
   */
  private void updateUI() {
    this.stage.setTitle("%s – %s%s".formatted(App.NAME, this.familyTree.name(), this.unsavedChanges ? "*" : ""));

    boolean emptyTree = this.familyTree.persons().isEmpty();
    Optional<Person> selectedPerson = this.getSelectedPerson();
    boolean selection = selectedPerson.isPresent();
    boolean hasBothParents = selection && selectedPerson.get().hasBothParents();
    boolean selectedIsRoot = selection && selectedPerson.map(this.familyTree::isRoot).orElse(false);

    this.saveMenuItem.setDisable(!this.unsavedChanges || emptyTree);
    this.saveAsMenuItem.setDisable(emptyTree);
    this.setAsRootMenuItem.setDisable(!selection || selectedIsRoot);
    this.editPersonMenuItem.setDisable(!selection);
    this.removePersonMenuItem.setDisable(!selection || selectedIsRoot);
    this.addChildMenuItem.setDisable(!selection);
    this.addSiblingMenuItem.setDisable(!hasBothParents);
    this.editParentsMenuItem.setDisable(!selection);
    this.editLifeEventsMenuItem.setDisable(!selection);
    this.setPictureMenuItem.setDisable(!selection);

    this.saveToolbarButton.setDisable(!this.unsavedChanges || emptyTree);
    this.saveAsToolbarButton.setDisable(emptyTree);
    this.setAsRootToolbarButton.setDisable(!selection || selectedIsRoot);
    this.addChildToolbarButton.setDisable(!selection);
    this.addSiblingToolbarButton.setDisable(!hasBothParents);
    this.editParentsToolbarButton.setDisable(!selection);
    this.editLifeEventsToolbarButton.setDisable(!selection);
    this.setPictureToolbarButton.setDisable(!selection);
  }

  /**
   * Open settings dialog.
   */
  private void onSettingsAction() {
    this.settingsDialog.resetLocalConfig();
    this.settingsDialog.showAndWait();
  }

  /**
   * Open about dialog.
   */
  private void onAboutAction() {
    this.aboutDialog.showAndWait();
  }

  /**
   * Open alert dialog in there are any unsaved changes before closing the app.
   */
  private void onQuitAction() {
    if (!this.defaultEmptyTree && this.unsavedChanges) {
      boolean close = Alerts.confirmation(
          "alert.unsaved_changes.header", "alert.unsaved_changes.content", null);
      if (!close) {
        return;
      }
    }
    Platform.exit();
  }
}
