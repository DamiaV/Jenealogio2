package net.darmo_creations.jenealogio2;

import javafx.application.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.io.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.themes.*;
import net.darmo_creations.jenealogio2.ui.*;
import net.darmo_creations.jenealogio2.ui.dialogs.*;
import net.darmo_creations.jenealogio2.ui.events.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Application’s main controller.
 */
public class AppController {
  /**
   * The stage associated to this controller.
   */
  private final Stage stage;

  private final MenuItem newFileMenuItem = new MenuItem();
  private final MenuItem openFileMenuItem = new MenuItem();
  private final MenuItem saveMenuItem = new MenuItem();
  private final MenuItem saveAsMenuItem = new MenuItem();
  private final MenuItem settingsMenuItem = new MenuItem();
  private final MenuItem quitMenuItem = new MenuItem();
  private final MenuItem undoMenuItem = new MenuItem();
  private final MenuItem redoMenuItem = new MenuItem();
  private final MenuItem editRegistriesMenuItem = new MenuItem();
  private final MenuItem renameTreeMenuItem = new MenuItem();
  private final MenuItem setAsRootMenuItem = new MenuItem();
  private final MenuItem addPersonMenuItem = new MenuItem();
  private final MenuItem editPersonMenuItem = new MenuItem();
  private final MenuItem removePersonMenuItem = new MenuItem();
  private final MenuItem addChildMenuItem = new MenuItem();
  private final MenuItem addSiblingMenuItem = new MenuItem();
  private final MenuItem editParentsMenuItem = new MenuItem();
  private final MenuItem editLifeEventsMenuItem = new MenuItem();
  private final MenuItem setPictureMenuItem = new MenuItem();
  private final MenuItem calculateRelationshipsMenuItem = new MenuItem();
  private final MenuItem birthdaysMenuItem = new MenuItem();
  private final MenuItem mapMenuItem = new MenuItem();
  private final MenuItem checkInconsistenciesMenuItem = new MenuItem();
  private final MenuItem aboutMenuItem = new MenuItem();

  private final Button newToolbarButton = new Button();
  private final Button openToolbarButton = new Button();
  private final Button saveToolbarButton = new Button();
  private final Button saveAsToolbarButton = new Button();
  private final Button undoToolbarButton = new Button();
  private final Button redoToolbarButton = new Button();
  private final Button setAsRootToolbarButton = new Button();
  private final Button addPersonToolbarButton = new Button();
  private final Button addChildToolbarButton = new Button();
  private final Button addSiblingToolbarButton = new Button();
  private final Button editParentsToolbarButton = new Button();
  private final Button editLifeEventsToolbarButton = new Button();
  private final Button setPictureToolbarButton = new Button();
  private final Button calculateRelationshipsToolbarButton = new Button();
  private final Button birthdaysToolbarButton = new Button();
  private final Button mapToolbarButton = new Button();
  private final Button checkInconsistenciesToolbarButton = new Button();

  // File managers
  private final TreeFileReader treeFileReader = new TreeFileReader();
  private final TreeFileWriter treeFileWriter = new TreeFileWriter();

  // Tree components
  private FamilyTreeComponent focusedComponent;
  private final FamilyTreeView familyTreeView = new FamilyTreeView();
  private final FamilyTreePane familyTreePane = new FamilyTreePane();

  private final PersonDetailsView personDetailsView = new PersonDetailsView();

  // Dialogs
  private final RegistriesDialog editRegistriesDialog = new RegistriesDialog();
  private final EditPersonDialog editPersonDialog = new EditPersonDialog();
  private final BirthdaysDialog birthdaysDialog = new BirthdaysDialog();
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
   * Create the app’s controller.
   *
   * @param stage App’s main stage.
   */
  public AppController(@NotNull Stage stage) {
    this.stage = Objects.requireNonNull(stage);
    URL url = this.getClass().getResource(App.IMAGES_PATH + "app-icon.png");
    if (url != null) {
      stage.getIcons().add(new Image(url.toExternalForm()));
    } else {
      App.LOGGER.warn("Could not load app icon!");
    }
    stage.setMinWidth(300);
    stage.setMinHeight(200);
    stage.setTitle(App.NAME);
    stage.setMaximized(true);
    Scene scene = new Scene(new VBox(this.createMenuBar(), this.createToolBar(), this.createContent()));
    stage.setScene(scene);
    App.config().theme().getStyleSheets()
        .forEach(path -> scene.getStylesheets().add(path.toExternalForm()));

    this.birthdaysDialog.personClickListeners()
        .add(event -> this.onPersonClick(event, null));

    // Files drag-and-drop
    scene.setOnDragOver(event -> {
      if (event.getGestureSource() == null // From another application
          && this.isDragAndDropValid(event.getDragboard())) {
        event.acceptTransferModes(TransferMode.COPY);
      }
      event.consume();
    });
    scene.setOnDragDropped(event -> {
      Dragboard db = event.getDragboard();
      boolean success = this.isDragAndDropValid(db);
      if (success) {
        if (!this.defaultEmptyTree && this.unsavedChanges) {
          boolean open = Alerts.confirmation(
              "alert.unsaved_changes.header", "alert.unsaved_changes.content", null);
          if (!open) {
            success = false;
          }
        }
      }
      if (success) {
        this.loadFile(db.getFiles().get(0));
      }
      event.setDropCompleted(success);
      event.consume();
    });
  }

  private boolean isDragAndDropValid(final @NotNull Dragboard dragboard) {
    List<File> files = dragboard.getFiles();
    return dragboard.hasFiles()
        && files.size() == 1
        && files.get(0).getName().endsWith(TreeFileReader.EXTENSION);
  }

  private MenuBar createMenuBar() {
    Config config = App.config();
    Language language = config.language();
    Theme theme = config.theme();

    //

    Menu fileMenu = new Menu(language.translate("menu.file"));

    this.newFileMenuItem.setText(language.translate("menu.file.new"));
    this.newFileMenuItem.setGraphic(theme.getIcon(Icon.NEW_FILE, Icon.Size.SMALL));
    this.newFileMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
    this.newFileMenuItem.setOnAction(event -> this.onNewFileAction());
    fileMenu.getItems().add(this.newFileMenuItem);

    this.openFileMenuItem.setText(language.translate("menu.file.open"));
    this.openFileMenuItem.setGraphic(theme.getIcon(Icon.OPEN_FILE, Icon.Size.SMALL));
    this.openFileMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
    this.openFileMenuItem.setOnAction(event -> this.onOpenFileAction());
    fileMenu.getItems().add(this.openFileMenuItem);

    fileMenu.getItems().add(new SeparatorMenuItem());

    this.saveMenuItem.setText(language.translate("menu.file.save"));
    this.saveMenuItem.setGraphic(theme.getIcon(Icon.SAVE, Icon.Size.SMALL));
    this.saveMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
    this.saveMenuItem.setOnAction(event -> this.onSaveAction());
    fileMenu.getItems().add(this.saveMenuItem);

    this.saveAsMenuItem.setText(language.translate("menu.file.save_as"));
    this.saveAsMenuItem.setGraphic(theme.getIcon(Icon.SAVE_AS, Icon.Size.SMALL));
    this.saveAsMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
    this.saveAsMenuItem.setOnAction(event -> this.onSaveAsAction());
    fileMenu.getItems().add(this.saveAsMenuItem);

    fileMenu.getItems().add(new SeparatorMenuItem());

    this.settingsMenuItem.setText(language.translate("menu.file.settings"));
    this.settingsMenuItem.setGraphic(theme.getIcon(Icon.SETTINGS, Icon.Size.SMALL));
    this.settingsMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHIFT_DOWN, KeyCombination.ALT_DOWN));
    this.settingsMenuItem.setOnAction(event -> this.onSettingsAction());
    fileMenu.getItems().add(this.settingsMenuItem);

    this.quitMenuItem.setText(language.translate("menu.file.quit"));
    this.quitMenuItem.setGraphic(theme.getIcon(Icon.QUIT, Icon.Size.SMALL));
    this.quitMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
    this.quitMenuItem.setOnAction(event -> this.onQuitAction());
    fileMenu.getItems().add(this.quitMenuItem);

    //

    Menu editMenu = new Menu(language.translate("menu.edit"));

    this.undoMenuItem.setText(language.translate("menu.edit.undo"));
    this.undoMenuItem.setGraphic(theme.getIcon(Icon.UNDO, Icon.Size.SMALL));
    this.undoMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));
    this.undoMenuItem.setDisable(true); // TEMP disabled until implemented
    editMenu.getItems().add(this.undoMenuItem);

    this.redoMenuItem.setText(language.translate("menu.edit.redo"));
    this.redoMenuItem.setGraphic(theme.getIcon(Icon.REDO, Icon.Size.SMALL));
    this.redoMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN));
    this.redoMenuItem.setDisable(true); // TEMP disabled until implemented
    editMenu.getItems().add(this.redoMenuItem);

    editMenu.getItems().add(new SeparatorMenuItem());

    this.editRegistriesMenuItem.setText(language.translate("menu.edit.edit_registries"));
    this.editRegistriesMenuItem.setGraphic(theme.getIcon(Icon.EDIT_REGISTRIES, Icon.Size.SMALL));
    this.editRegistriesMenuItem.setOnAction(event -> this.onEditRegistriesAction());
    editMenu.getItems().add(this.editRegistriesMenuItem);

    editMenu.getItems().add(new SeparatorMenuItem());

    this.renameTreeMenuItem.setText(language.translate("menu.edit.rename_tree"));
    this.renameTreeMenuItem.setGraphic(theme.getIcon(Icon.RENAME_TREE, Icon.Size.SMALL));
    this.renameTreeMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
    this.renameTreeMenuItem.setOnAction(event -> this.onRenameTreeAction());
    editMenu.getItems().add(this.renameTreeMenuItem);

    this.setAsRootMenuItem.setText(language.translate("menu.edit.set_as_root"));
    this.setAsRootMenuItem.setGraphic(theme.getIcon(Icon.SET_AS_ROOT, Icon.Size.SMALL));
    this.setAsRootMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN));
    this.setAsRootMenuItem.setOnAction(event -> this.onSetAsRootAction());
    editMenu.getItems().add(this.setAsRootMenuItem);

    this.addPersonMenuItem.setText(language.translate("menu.edit.add_person"));
    this.addPersonMenuItem.setGraphic(theme.getIcon(Icon.ADD_PERSON, Icon.Size.SMALL));
    this.addPersonMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN));
    this.addPersonMenuItem.setOnAction(event -> this.onAddPersonAction());
    editMenu.getItems().add(this.addPersonMenuItem);

    this.editPersonMenuItem.setText(language.translate("menu.edit.edit_person"));
    this.editPersonMenuItem.setGraphic(theme.getIcon(Icon.EDIT_PERSON, Icon.Size.SMALL));
    this.editPersonMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN));
    this.editPersonMenuItem.setOnAction(event -> this.onEditPersonAction());
    editMenu.getItems().add(this.editPersonMenuItem);

    this.removePersonMenuItem.setText(language.translate("menu.edit.remove_person"));
    this.removePersonMenuItem.setGraphic(theme.getIcon(Icon.REMOVE_PERSON, Icon.Size.SMALL));
    this.removePersonMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));
    this.removePersonMenuItem.setOnAction(event -> this.onRemovePersonAction());
    editMenu.getItems().add(this.removePersonMenuItem);

    editMenu.getItems().add(new SeparatorMenuItem());

    this.addChildMenuItem.setText(language.translate("menu.edit.add_child"));
    this.addChildMenuItem.setGraphic(theme.getIcon(Icon.ADD_CHILD, Icon.Size.SMALL));
    this.addChildMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
    this.addChildMenuItem.setOnAction(event -> this.onAddChildAction());
    editMenu.getItems().add(this.addChildMenuItem);

    this.addSiblingMenuItem.setText(language.translate("menu.edit.add_sibling"));
    this.addSiblingMenuItem.setGraphic(theme.getIcon(Icon.ADD_SIBLING, Icon.Size.SMALL));
    this.addSiblingMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.B, KeyCombination.CONTROL_DOWN));
    this.addSiblingMenuItem.setOnAction(event -> this.onAddSiblingAction());
    editMenu.getItems().add(this.addSiblingMenuItem);

    this.editParentsMenuItem.setText(language.translate("menu.edit.edit_parents"));
    this.editParentsMenuItem.setGraphic(theme.getIcon(Icon.EDIT_PARENTS, Icon.Size.SMALL));
    this.editParentsMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
    this.editParentsMenuItem.setOnAction(event -> this.onEditParentsAction());
    editMenu.getItems().add(this.editParentsMenuItem);

    this.editLifeEventsMenuItem.setText(language.translate("menu.edit.edit_life_events"));
    this.editLifeEventsMenuItem.setGraphic(theme.getIcon(Icon.EDIT_LIFE_EVENTS, Icon.Size.SMALL));
    this.editLifeEventsMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.SHIFT_DOWN, KeyCombination.ALT_DOWN));
    this.editLifeEventsMenuItem.setOnAction(event -> this.onEditLifeEventsAction());
    editMenu.getItems().add(this.editLifeEventsMenuItem);

    editMenu.getItems().add(new SeparatorMenuItem());

    this.setPictureMenuItem.setText(language.translate("menu.edit.set_picture"));
    this.setPictureMenuItem.setGraphic(theme.getIcon(Icon.SET_PICTURE, Icon.Size.SMALL));
    this.setPictureMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN));
    this.setPictureMenuItem.setOnAction(event -> this.onEditPicturesAction());
    editMenu.getItems().add(this.setPictureMenuItem);

    //

    Menu toolsMenu = new Menu(language.translate("menu.tools"));

    this.calculateRelationshipsMenuItem.setText(language.translate("menu.tools.calculate_relationships"));
    this.calculateRelationshipsMenuItem.setGraphic(theme.getIcon(Icon.CALCULATE_RELATIONSHIPS, Icon.Size.SMALL));
    this.calculateRelationshipsMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN));
    this.calculateRelationshipsMenuItem.setDisable(true); // TEMP disabled until implemented
    toolsMenu.getItems().add(this.calculateRelationshipsMenuItem);

    this.birthdaysMenuItem.setText(language.translate("menu.tools.birthdays"));
    this.birthdaysMenuItem.setGraphic(theme.getIcon(Icon.BIRTHDAYS, Icon.Size.SMALL));
    this.birthdaysMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.B, KeyCombination.CONTROL_DOWN));
    this.birthdaysMenuItem.setOnAction(event -> this.onShowBirthdaysDialog());
    toolsMenu.getItems().add(this.birthdaysMenuItem);

    this.mapMenuItem.setText(language.translate("menu.tools.map"));
    this.mapMenuItem.setGraphic(theme.getIcon(Icon.MAP, Icon.Size.SMALL));
    this.mapMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN));
    this.mapMenuItem.setDisable(true); // TEMP disabled until implemented
    toolsMenu.getItems().add(this.mapMenuItem);

    toolsMenu.getItems().add(new SeparatorMenuItem());

    this.checkInconsistenciesMenuItem.setText(language.translate("menu.tools.check_inconsistencies"));
    this.checkInconsistenciesMenuItem.setGraphic(theme.getIcon(Icon.CHECK_INCONSISTENCIES, Icon.Size.SMALL));
    this.checkInconsistenciesMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
    this.checkInconsistenciesMenuItem.setDisable(true); // TEMP disabled until implemented
    toolsMenu.getItems().add(this.checkInconsistenciesMenuItem);

    //

    Menu helpMenu = new Menu(language.translate("menu.help"));

    this.aboutMenuItem.setText(language.translate("menu.help.about"));
    this.aboutMenuItem.setGraphic(theme.getIcon(Icon.ABOUT, Icon.Size.SMALL));
    this.aboutMenuItem.setOnAction(event -> this.onAboutAction());
    helpMenu.getItems().add(this.aboutMenuItem);

    return new MenuBar(fileMenu, editMenu, toolsMenu, helpMenu);
  }

  private ToolBar createToolBar() {
    Config config = App.config();
    Language language = config.language();
    Theme theme = config.theme();

    ToolBar toolbar = new ToolBar();

    //

    this.newToolbarButton.setTooltip(new Tooltip(language.translate("toolbar.new")));
    this.newToolbarButton.setGraphic(theme.getIcon(Icon.NEW_FILE, Icon.Size.BIG));
    this.newToolbarButton.setOnAction(event -> this.onNewFileAction());
    toolbar.getItems().add(this.newToolbarButton);

    this.openToolbarButton.setTooltip(new Tooltip(language.translate("toolbar.open")));
    this.openToolbarButton.setGraphic(theme.getIcon(Icon.OPEN_FILE, Icon.Size.BIG));
    this.openToolbarButton.setOnAction(event -> this.onOpenFileAction());
    toolbar.getItems().add(this.openToolbarButton);

    toolbar.getItems().add(new Separator(Orientation.VERTICAL));

    this.saveToolbarButton.setTooltip(new Tooltip(language.translate("toolbar.save")));
    this.saveToolbarButton.setGraphic(theme.getIcon(Icon.SAVE, Icon.Size.BIG));
    this.saveToolbarButton.setOnAction(event -> this.onSaveAction());
    toolbar.getItems().add(this.saveToolbarButton);

    this.saveAsToolbarButton.setTooltip(new Tooltip(language.translate("toolbar.save_as")));
    this.saveAsToolbarButton.setGraphic(theme.getIcon(Icon.SAVE_AS, Icon.Size.BIG));
    this.saveAsToolbarButton.setOnAction(event -> this.onSaveAsAction());
    toolbar.getItems().add(this.saveAsToolbarButton);

    toolbar.getItems().add(new Separator(Orientation.VERTICAL));

    //

    this.undoToolbarButton.setTooltip(new Tooltip(language.translate("toolbar.undo")));
    this.undoToolbarButton.setGraphic(theme.getIcon(Icon.UNDO, Icon.Size.BIG));
    this.undoToolbarButton.setDisable(true); // TEMP disabled until implemented
    toolbar.getItems().add(this.undoToolbarButton);

    this.redoToolbarButton.setTooltip(new Tooltip(language.translate("toolbar.redo")));
    this.redoToolbarButton.setGraphic(theme.getIcon(Icon.REDO, Icon.Size.BIG));
    this.redoToolbarButton.setDisable(true); // TEMP disabled until implemented
    toolbar.getItems().add(this.redoToolbarButton);

    toolbar.getItems().add(new Separator(Orientation.VERTICAL));

    this.setAsRootToolbarButton.setTooltip(new Tooltip(language.translate("toolbar.set_as_root")));
    this.setAsRootToolbarButton.setGraphic(theme.getIcon(Icon.SET_AS_ROOT, Icon.Size.BIG));
    this.setAsRootToolbarButton.setOnAction(event -> this.onSetAsRootAction());
    toolbar.getItems().add(this.setAsRootToolbarButton);

    this.addPersonToolbarButton.setTooltip(new Tooltip(language.translate("toolbar.add_person")));
    this.addPersonToolbarButton.setGraphic(theme.getIcon(Icon.ADD_PERSON, Icon.Size.BIG));
    this.addPersonToolbarButton.setOnAction(event -> this.onAddPersonAction());
    toolbar.getItems().add(this.addPersonToolbarButton);

    this.addChildToolbarButton.setTooltip(new Tooltip(language.translate("toolbar.add_child")));
    this.addChildToolbarButton.setGraphic(theme.getIcon(Icon.ADD_CHILD, Icon.Size.BIG));
    this.addChildToolbarButton.setOnAction(event -> this.onAddChildAction());
    toolbar.getItems().add(this.addChildToolbarButton);

    this.addSiblingToolbarButton.setTooltip(new Tooltip(language.translate("toolbar.add_sibling")));
    this.addSiblingToolbarButton.setGraphic(theme.getIcon(Icon.ADD_SIBLING, Icon.Size.BIG));
    this.addSiblingToolbarButton.setOnAction(event -> this.onAddSiblingAction());
    toolbar.getItems().add(this.addSiblingToolbarButton);

    this.editParentsToolbarButton.setTooltip(new Tooltip(language.translate("toolbar.edit_parents")));
    this.editParentsToolbarButton.setGraphic(theme.getIcon(Icon.EDIT_PARENTS, Icon.Size.BIG));
    this.editParentsToolbarButton.setOnAction(event -> this.onEditParentsAction());
    toolbar.getItems().add(this.editParentsToolbarButton);

    this.editLifeEventsToolbarButton.setTooltip(new Tooltip(language.translate("toolbar.edit_life_events")));
    this.editLifeEventsToolbarButton.setGraphic(theme.getIcon(Icon.EDIT_LIFE_EVENTS, Icon.Size.BIG));
    this.editLifeEventsToolbarButton.setOnAction(event -> this.onEditLifeEventsAction());
    toolbar.getItems().add(this.editLifeEventsToolbarButton);

    this.setPictureToolbarButton.setTooltip(new Tooltip(language.translate("toolbar.set_picture")));
    this.setPictureToolbarButton.setGraphic(theme.getIcon(Icon.SET_PICTURE, Icon.Size.BIG));
    this.setPictureToolbarButton.setOnAction(event -> this.onEditPicturesAction());
    toolbar.getItems().add(this.setPictureToolbarButton);

    toolbar.getItems().add(new Separator(Orientation.VERTICAL));

    //

    this.calculateRelationshipsToolbarButton.setTooltip(new Tooltip(language.translate("toolbar.calculate_relationships")));
    this.calculateRelationshipsToolbarButton.setGraphic(theme.getIcon(Icon.CALCULATE_RELATIONSHIPS, Icon.Size.BIG));
    this.calculateRelationshipsToolbarButton.setDisable(true); // TEMP disabled until implemented
    toolbar.getItems().add(this.calculateRelationshipsToolbarButton);

    this.birthdaysToolbarButton.setTooltip(new Tooltip(language.translate("toolbar.birthdays")));
    this.birthdaysToolbarButton.setGraphic(theme.getIcon(Icon.BIRTHDAYS, Icon.Size.BIG));
    this.birthdaysToolbarButton.setOnAction(event -> this.onShowBirthdaysDialog());
    toolbar.getItems().add(this.birthdaysToolbarButton);

    this.mapToolbarButton.setTooltip(new Tooltip(language.translate("toolbar.map")));
    this.mapToolbarButton.setGraphic(theme.getIcon(Icon.MAP, Icon.Size.BIG));
    this.mapToolbarButton.setDisable(true); // TEMP disabled until implemented
    toolbar.getItems().add(this.mapToolbarButton);

    toolbar.getItems().add(new Separator(Orientation.VERTICAL));

    this.checkInconsistenciesToolbarButton.setTooltip(new Tooltip(language.translate("toolbar.check_inconsistencies")));
    this.checkInconsistenciesToolbarButton.setGraphic(theme.getIcon(Icon.CHECK_INCONSISTENCIES, Icon.Size.BIG));
    this.checkInconsistenciesToolbarButton.setDisable(true); // TEMP disabled until implemented
    toolbar.getItems().add(this.checkInconsistenciesToolbarButton);

    return toolbar;
  }

  private SplitPane createContent() {
    SplitPane splitPane = new SplitPane();

    this.familyTreeView.personClickListeners()
        .add(event -> this.onPersonClick(event, this.familyTreeView));
    splitPane.getItems().add(this.familyTreeView);

    this.familyTreePane.personClickListeners()
        .add(event -> this.onPersonClick(event, this.familyTreePane));
    this.familyTreePane.newParentClickListeners().add(this::onNewParentClick);
    this.familyTreePane.setMaxHeight(App.config().maxTreeHeight());
    splitPane.getItems().add(this.familyTreePane);

    this.personDetailsView.personClickListeners()
        .add(event -> this.onPersonClick(event, null));
    this.personDetailsView.newParentClickListeners()
        .add(this::onNewParentClick);
    splitPane.getItems().add(this.personDetailsView);

    splitPane.setDividerPositions(0.1, 0.9);
    return splitPane;
  }

  /**
   * Show the stage.
   * <p>
   * Hooks callbacks to the stage’s close event and load the specified tree or the default one.
   *
   * @param file File to load. May be null.
   */
  public void show(File file) {
    this.stage.show();
    this.stage.setOnCloseRequest(event -> {
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

  public void onConfigUpdate() {
    Config config = App.config();
    this.familyTreePane.setMaxHeight(config.maxTreeHeight());
    this.familyTreePane.refresh();
    this.familyTreeView.refresh();
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
    this.personDetailsView.setPerson(null, this.familyTree);
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
        "alert.tree_name.header", "alert.tree_name.label", null, this.familyTree.name());
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
    Optional<File> f = FileChoosers.showTreeFileChooser(this.stage, null);
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
      Optional<File> f = FileChoosers.showTreeFileSaver(this.stage, this.familyTree.name());
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
    Optional<File> f = FileChoosers.showTreeFileSaver(this.stage, this.familyTree.name());
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
    this.openEditPersonDialog(null, List.of(), null, null, EditPersonDialog.TAB_PROFILE);
  }

  /**
   * Open person edit dialog to edit the selected person.
   */
  private void onEditPersonAction() {
    this.openEditPersonDialogOnTab(EditPersonDialog.TAB_PROFILE);
  }

  /**
   * Open person edit dialog to edit the parents of the selected person.
   */
  private void onEditParentsAction() {
    this.openEditPersonDialogOnTab(EditPersonDialog.TAB_PARENTS);
  }

  /**
   * Open person edit dialog to edit the life events of the selected person.
   */
  private void onEditLifeEventsAction() {
    this.openEditPersonDialogOnTab(EditPersonDialog.TAB_EVENTS);
  }

  /**
   * Open person edit dialog to edit the life events of the selected person.
   */
  private void onEditPicturesAction() {
    this.openEditPersonDialogOnTab(EditPersonDialog.TAB_PICTURES);
  }

  /**
   * Open person edit dialog to edit the specified tab.
   */
  private void openEditPersonDialogOnTab(int tabIndex) {
    this.getSelectedPerson().ifPresent(
        person -> this.openEditPersonDialog(person, List.of(), null, null, tabIndex));
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
      this.openEditPersonDialog(null, List.of(), parent1, parent2, EditPersonDialog.TAB_PROFILE);
    });
  }

  /**
   * Open person edit dialog to create a new child of the selected person.
   * <p>
   * Prompts the user to select the partner with whom the selected person had the child with.
   */
  private void onAddChildAction() {
    this.getSelectedPerson().ifPresent(person -> {
      List<Person> persons = this.familyTree.persons().stream()
          .filter(p -> p != person)
          .sorted(Person.lastThenFirstNamesComparator())
          .toList();
      Person partner = null;
      if (!persons.isEmpty()) {
        Optional<Person> selection = Alerts.chooser("alert.partner_chooser.header",
            "alert.partner_chooser.label", "alert.partner_chooser.title", persons);
        if (selection.isEmpty()) {
          return;
        }
        partner = selection.get();
      }
      this.openEditPersonDialog(null, List.of(), person, partner, EditPersonDialog.TAB_PROFILE);
    });
  }

  /**
   * Open person edit dialog to create a parent of the given child.
   *
   * @param childInfo Information about the children of the parent to create.
   */
  private void onNewParentClick(@NotNull List<ChildInfo> childInfo) {
    this.openEditPersonDialog(null, childInfo, null, null, EditPersonDialog.TAB_PROFILE);
  }

  /**
   * Update display when a person is clicked.
   *
   * @param event    The event that was fired.
   * @param fromNode The {@link FamilyTreeComponent} in which the click occurred. Null if another node fired it.
   */
  private void onPersonClick(@NotNull PersonClickEvent event, FamilyTreeComponent fromNode) {
    if (fromNode == null) {
      // Click occurred outside of tree pane and tree view, select person in tree pane
      this.updateSelection(event, this.familyTreePane);
      fromNode = this.familyTreePane;
    }
    this.focusedComponent = fromNode;
    if (App.config().shouldSyncTreeWithMainPane()) {
      if (fromNode == this.familyTreeView) {
        this.updateSelection(event, this.familyTreePane);
      } else {
        this.updateSelection(event, this.familyTreeView);
      }
    }
    Person person = event instanceof PersonClickedEvent e ? e.person() : null;
    this.personDetailsView.setPerson(person, this.familyTree);
    if (event instanceof PersonClickedEvent e && e.action() == PersonClickedEvent.Action.EDIT) {
      this.openEditPersonDialog(person, List.of(), null, null, EditPersonDialog.TAB_PROFILE);
    }
    this.updateUI();
  }

  /**
   * Update the selection of a {@link FamilyTreeComponent} depending on the given {@link PersonClickEvent}.
   */
  private void updateSelection(@NotNull PersonClickEvent event, @NotNull FamilyTreeComponent component) {
    if (event instanceof DeselectPersonsEvent) {
      component.deselectAll();
    } else if (event instanceof PersonClickedEvent e) {
      component.select(e.person(), e.action().shouldUpdateTarget());
    }
  }

  /**
   * Open the dialog to edit registries.
   */
  private void onEditRegistriesAction() {
    this.editRegistriesDialog.refresh(this.familyTree);
    Optional<ButtonType> result = this.editRegistriesDialog.showAndWait();
    if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
      this.familyTreeView.refresh();
      this.familyTreePane.refresh();
      this.defaultEmptyTree = false;
      this.unsavedChanges = true;
      this.updateUI();
    }
  }

  /**
   * Open person edit dialog.
   *
   * @param person    Person to edit. Null to create a new person.
   * @param childInfo Optional information about the person’s currently visible children.
   * @param tabIndex  Index of the tab to show.
   */
  private void openEditPersonDialog(
      Person person, final @NotNull List<ChildInfo> childInfo, Person parent1, Person parent2, int tabIndex) {
    this.editPersonDialog.setPerson(person, childInfo, this.familyTree);
    if (parent1 != null || parent2 != null) {
      this.editPersonDialog.setParents(parent1, parent2);
    }
    this.editPersonDialog.selectTab(tabIndex);
    Optional<Person> p = this.editPersonDialog.showAndWait();
    boolean present = p.isPresent();
    if (present) {
      this.familyTreeView.refresh();
      this.familyTreePane.refresh();
      if (person == null && childInfo.isEmpty()) {
        this.onPersonClick(new PersonClickedEvent(p.get(), PersonClickedEvent.Action.SET_AS_TARGET), null);
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
    this.stage.setTitle("%s%s – %s%s".formatted(
        App.NAME,
        App.config().isDebug() ? " (Debug)" : "",
        this.familyTree.name(),
        this.unsavedChanges ? "*" : ""
    ));

    if (this.birthdaysDialog.isShowing()) {
      this.birthdaysDialog.refresh(this.familyTree);
    }

    Optional<Person> selectedPerson = this.getSelectedPerson();
    boolean selection = selectedPerson.isPresent();
    boolean hasBothParents = selection && selectedPerson.get().hasBothParents();
    boolean selectedIsRoot = selection && selectedPerson.map(this.familyTree::isRoot).orElse(false);

    this.saveMenuItem.setDisable(!this.unsavedChanges);
    this.setAsRootMenuItem.setDisable(!selection || selectedIsRoot);
    this.editPersonMenuItem.setDisable(!selection);
    this.removePersonMenuItem.setDisable(!selection || selectedIsRoot);
    this.addChildMenuItem.setDisable(!selection);
    this.addSiblingMenuItem.setDisable(!hasBothParents);
    this.editParentsMenuItem.setDisable(!selection);
    this.editLifeEventsMenuItem.setDisable(!selection);
    this.setPictureMenuItem.setDisable(!selection);

    this.saveToolbarButton.setDisable(!this.unsavedChanges);
    this.setAsRootToolbarButton.setDisable(!selection || selectedIsRoot);
    this.addChildToolbarButton.setDisable(!selection);
    this.addSiblingToolbarButton.setDisable(!hasBothParents);
    this.editParentsToolbarButton.setDisable(!selection);
    this.editLifeEventsToolbarButton.setDisable(!selection);
    this.setPictureToolbarButton.setDisable(!selection);
  }

  /**
   * Open birthdays dialog.
   */
  private void onShowBirthdaysDialog() {
    if (this.birthdaysDialog.isShowing()) {
      return;
    }
    this.birthdaysDialog.refresh(this.familyTree);
    this.birthdaysDialog.show();
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
