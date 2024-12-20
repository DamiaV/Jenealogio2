package net.darmo_creations.jenealogio2;

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
import net.darmo_creations.jenealogio2.ui.components.*;
import net.darmo_creations.jenealogio2.ui.dialogs.*;
import net.darmo_creations.jenealogio2.ui.events.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;

/**
 * Application’s main controller.
 */
public class AppController {
  /**
   * The stage associated to this controller.
   */
  private final Stage stage;
  private final Config config;

  private final Menu openTreeMenu = new Menu();
  private final MenuItem manageTreesMenuItem = new MenuItem();
  private final MenuItem saveMenuItem = new MenuItem();
  private final MenuItem undoMenuItem = new MenuItem();
  private final MenuItem redoMenuItem = new MenuItem();
  private final MenuItem setAsRootMenuItem = new MenuItem();
  private final MenuItem editPersonMenuItem = new MenuItem();
  private final MenuItem removePersonMenuItem = new MenuItem();
  private final MenuItem addChildMenuItem = new MenuItem();
  private final MenuItem addSiblingMenuItem = new MenuItem();
  private final MenuItem editParentsMenuItem = new MenuItem();
  private final MenuItem editLifeEventsMenuItem = new MenuItem();
  private final MenuItem editDocumentsMenuItem = new MenuItem();

  private final Button saveToolbarButton = new Button();
  private final Button undoToolbarButton = new Button();
  private final Button redoToolbarButton = new Button();
  private final Button previousSelectionToolbarButton = new Button();
  private final Button nextSelectionToolbarButton = new Button();
  private final Button setAsRootToolbarButton = new Button();
  private final Button addChildToolbarButton = new Button();
  private final Button addSiblingToolbarButton = new Button();
  private final Button editParentsToolbarButton = new Button();
  private final Button editLifeEventsToolbarButton = new Button();
  private final Button editDocumentsToolbarButton = new Button();

  // File managers
  private final FamilyTreeWriter familyTreeWriter = new FamilyTreeWriter();

  // Tree components
  private FamilyTreeComponent focusedComponent;
  private final FamilyMembersTreeView familyMembersTreeView;
  private final GeneticFamilyTreePane geneticFamilyTreePane;
  private final FamilyMemberFullViewPane familyMemberFullViewPane;

  private final StatisticsPanel statisticsPanel;

  private final PersonDetailsView personDetailsView;

  private final List<Person> selectionHistory = new ArrayList<>();
  private int selectionIndex = -1;

  // Dialogs
  private final TreesManagerDialog treesManagerDialog;
  private final RegistriesDialog editRegistriesDialog;
  private final EditPersonDialog editPersonDialog;
  private final ManageDocumentsDialog editDocumentsDialog;
  private final BirthdaysDialog birthdaysDialog;
  private final MapDialog mapDialog;
  private final SettingsDialog settingsDialog;
  private final AboutDialog aboutDialog;

  /**
   * Currently loaded family tree.
   */
  private FamilyTree familyTree;
  /**
   * File the current tree has been loaded from, will be null for new trees.
   */
  private Path loadedFile;
  /**
   * Indicate whether there are any unsaved changes.
   */
  private boolean unsavedChanges;

  /**
   * Create the app’s controller.
   *
   * @param stage  App’s main stage.
   * @param config The app’s config.
   */
  public AppController(@NotNull Stage stage, @NotNull Config config) {
    this.stage = Objects.requireNonNull(stage);
    this.config = Objects.requireNonNull(config);
    final Theme theme = config.theme();
    final Image icon = theme.getAppIcon();
    if (icon != null)
      stage.getIcons().add(icon);
    stage.setMinWidth(300);
    stage.setMinHeight(200);
    stage.setTitle(App.NAME);
    stage.setMaximized(true);

    this.statisticsPanel = new StatisticsPanel(config);
    this.personDetailsView = new PersonDetailsView(config);
    this.familyMembersTreeView = new FamilyMembersTreeView(config);
    this.geneticFamilyTreePane = new GeneticFamilyTreePane(config);
    this.familyMemberFullViewPane = new FamilyMemberFullViewPane(config);

    this.treesManagerDialog = new TreesManagerDialog(config);
    this.editRegistriesDialog = new RegistriesDialog(config);
    this.editPersonDialog = new EditPersonDialog(config);
    this.editDocumentsDialog = new ManageDocumentsDialog(config);
    this.mapDialog = new MapDialog(config);
    this.settingsDialog = new SettingsDialog(config);
    this.aboutDialog = new AboutDialog(config);

    final Scene scene = new Scene(new VBox(this.createMenuBar(), this.createToolBar(), this.createContent(config)));
    stage.setScene(scene);
    theme.getStyleSheets().forEach(path -> scene.getStylesheets().add(path.toExternalForm()));

    this.birthdaysDialog = new BirthdaysDialog(config);
    this.birthdaysDialog.personClickListeners()
        .add(event -> this.onPersonClick(event, null));

    // Files drag-and-drop
    scene.setOnDragOver(event -> {
      if (event.getGestureSource() == null // From another application
          && this.isDragAndDropValid(event.getDragboard()))
        event.acceptTransferModes(TransferMode.COPY);
      event.consume();
    });
    scene.setOnDragDropped(event -> {
      final Dragboard db = event.getDragboard();
      final boolean success = this.isDragAndDropValid(db) &&
          (!this.unsavedChanges || this.canProceedAfterOptionalSave());
      if (success)
        this.loadTree(db.getFiles().get(0).getName());
      event.setDropCompleted(success);
      event.consume();
    });

    scene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
      if (event.isBackButtonDown())
        this.onNavigateSelectionHistory(-1);
      else if (event.isForwardButtonDown())
        this.onNavigateSelectionHistory(1);
    });

    stage.setOnCloseRequest(event -> {
      if (this.unsavedChanges && !this.canProceedAfterOptionalSave())
        event.consume();
    });
  }

  private boolean isDragAndDropValid(final @NotNull Dragboard dragboard) {
    final List<File> files = dragboard.getFiles();
    return dragboard.hasFiles()
        && files.size() == 1
        && files.get(0).getName().endsWith(".zip");
  }

  private MenuBar createMenuBar() {
    final Language language = this.config.language();
    final Theme theme = this.config.theme();

    //

    final Menu fileMenu = new Menu(language.translate("menu.file"));

    final MenuItem newFileMenuItem = new MenuItem();
    newFileMenuItem.setText(language.translate("menu.file.new"));
    newFileMenuItem.setGraphic(theme.getIcon(Icon.NEW_FILE, Icon.Size.SMALL));
    newFileMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
    newFileMenuItem.setOnAction(event -> this.onNewTreeAction());
    fileMenu.getItems().add(newFileMenuItem);

    this.openTreeMenu.setText(language.translate("menu.file.open"));
    this.openTreeMenu.setGraphic(theme.getIcon(Icon.OPEN_TREE, Icon.Size.SMALL));
    fileMenu.getItems().add(this.openTreeMenu);

    this.manageTreesMenuItem.setText(language.translate("menu.file.open.manage_trees"));
    this.manageTreesMenuItem.setGraphic(theme.getIcon(Icon.MANAGE_TREES, Icon.Size.SMALL));
    this.manageTreesMenuItem.setOnAction(event -> this.onManageTreesAction());

    final MenuItem importTreeMenuItem = new MenuItem();
    importTreeMenuItem.setText(language.translate("menu.file.import"));
    importTreeMenuItem.setGraphic(theme.getIcon(Icon.IMPORT_TREE_FILE, Icon.Size.SMALL));
    importTreeMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
    importTreeMenuItem.setOnAction(event -> this.onImportTreeAction());
    fileMenu.getItems().add(importTreeMenuItem);

    final MenuItem exportTreeMenuItem = new MenuItem();
    exportTreeMenuItem.setText(language.translate("menu.file.export"));
    exportTreeMenuItem.setGraphic(theme.getIcon(Icon.EXPORT_TREE_FILE, Icon.Size.SMALL));
    exportTreeMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
    exportTreeMenuItem.setOnAction(event -> this.onExportTreeAction());
    fileMenu.getItems().add(exportTreeMenuItem);

    fileMenu.getItems().add(new SeparatorMenuItem());

    this.saveMenuItem.setText(language.translate("menu.file.save"));
    this.saveMenuItem.setGraphic(theme.getIcon(Icon.SAVE, Icon.Size.SMALL));
    this.saveMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
    this.saveMenuItem.setOnAction(event -> this.onSaveAction());
    fileMenu.getItems().add(this.saveMenuItem);

    fileMenu.getItems().add(new SeparatorMenuItem());

    final MenuItem settingsMenuItem = new MenuItem();
    settingsMenuItem.setText(language.translate("menu.file.settings"));
    settingsMenuItem.setGraphic(theme.getIcon(Icon.SETTINGS, Icon.Size.SMALL));
    settingsMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHIFT_DOWN, KeyCombination.ALT_DOWN));
    settingsMenuItem.setOnAction(event -> this.onSettingsAction());
    fileMenu.getItems().add(settingsMenuItem);

    final MenuItem quitMenuItem = new MenuItem();
    quitMenuItem.setText(language.translate("menu.file.quit"));
    quitMenuItem.setGraphic(theme.getIcon(Icon.QUIT, Icon.Size.SMALL));
    quitMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
    quitMenuItem.setOnAction(event -> this.stage.close());
    fileMenu.getItems().add(quitMenuItem);

    //

    final Menu editMenu = new Menu(language.translate("menu.edit"));

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

    final MenuItem editRegistriesMenuItem = new MenuItem();
    editRegistriesMenuItem.setText(language.translate("menu.edit.edit_registries"));
    editRegistriesMenuItem.setGraphic(theme.getIcon(Icon.EDIT_REGISTRIES, Icon.Size.SMALL));
    editRegistriesMenuItem.setOnAction(event -> this.onEditRegistriesAction());
    editMenu.getItems().add(editRegistriesMenuItem);

    final MenuItem editTreeDocuments = new MenuItem();
    editTreeDocuments.setText(language.translate("menu.edit.edit_tree_documents"));
    editTreeDocuments.setGraphic(theme.getIcon(Icon.EDIT_TREE_DOCUMENTS, Icon.Size.SMALL));
    editTreeDocuments.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN));
    editTreeDocuments.setOnAction(event -> this.onEditTreeDocumentsAction());
    editMenu.getItems().add(editTreeDocuments);

    editMenu.getItems().add(new SeparatorMenuItem());

    final MenuItem renameTreeMenuItem = new MenuItem();
    renameTreeMenuItem.setText(language.translate("menu.edit.rename_tree"));
    renameTreeMenuItem.setGraphic(theme.getIcon(Icon.RENAME_TREE, Icon.Size.SMALL));
    renameTreeMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
    renameTreeMenuItem.setOnAction(event -> this.onRenameTreeAction());
    editMenu.getItems().add(renameTreeMenuItem);

    this.setAsRootMenuItem.setText(language.translate("menu.edit.set_as_root"));
    this.setAsRootMenuItem.setGraphic(theme.getIcon(Icon.SET_AS_ROOT, Icon.Size.SMALL));
    this.setAsRootMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN));
    this.setAsRootMenuItem.setOnAction(event -> this.onSetAsRootAction());
    editMenu.getItems().add(this.setAsRootMenuItem);

    final MenuItem addPersonMenuItem = new MenuItem();
    addPersonMenuItem.setText(language.translate("menu.edit.add_person"));
    addPersonMenuItem.setGraphic(theme.getIcon(Icon.ADD_PERSON, Icon.Size.SMALL));
    addPersonMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN));
    addPersonMenuItem.setOnAction(event -> this.onAddPersonAction());
    editMenu.getItems().add(addPersonMenuItem);

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

    this.editDocumentsMenuItem.setText(language.translate("menu.edit.edit_documents"));
    this.editDocumentsMenuItem.setGraphic(theme.getIcon(Icon.EDIT_DOCUMENTS, Icon.Size.SMALL));
    this.editDocumentsMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN));
    this.editDocumentsMenuItem.setOnAction(event -> this.onEditObjectDocumentsAction());
    editMenu.getItems().add(this.editDocumentsMenuItem);

    //

    final Menu toolsMenu = new Menu(language.translate("menu.tools"));

    final MenuItem calculateRelationshipsMenuItem = new MenuItem();
    calculateRelationshipsMenuItem.setText(language.translate("menu.tools.calculate_relationships"));
    calculateRelationshipsMenuItem.setGraphic(theme.getIcon(Icon.CALCULATE_RELATIONSHIPS, Icon.Size.SMALL));
    calculateRelationshipsMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN));
    calculateRelationshipsMenuItem.setDisable(true); // TEMP disabled until implemented
    toolsMenu.getItems().add(calculateRelationshipsMenuItem);

    final MenuItem birthdaysMenuItem = new MenuItem();
    birthdaysMenuItem.setText(language.translate("menu.tools.birthdays"));
    birthdaysMenuItem.setGraphic(theme.getIcon(Icon.BIRTHDAYS, Icon.Size.SMALL));
    birthdaysMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.B, KeyCombination.CONTROL_DOWN));
    birthdaysMenuItem.setOnAction(event -> this.onShowBirthdaysDialog());
    toolsMenu.getItems().add(birthdaysMenuItem);

    final MenuItem mapMenuItem = new MenuItem();
    mapMenuItem.setText(language.translate("menu.tools.map"));
    mapMenuItem.setGraphic(theme.getIcon(Icon.MAP, Icon.Size.SMALL));
    mapMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN));
    mapMenuItem.setOnAction(event -> this.onShowMapDialog());
    toolsMenu.getItems().add(mapMenuItem);

    toolsMenu.getItems().add(new SeparatorMenuItem());

    final MenuItem checkInconsistenciesMenuItem = new MenuItem();
    checkInconsistenciesMenuItem.setText(language.translate("menu.tools.check_inconsistencies"));
    checkInconsistenciesMenuItem.setGraphic(theme.getIcon(Icon.CHECK_INCONSISTENCIES, Icon.Size.SMALL));
    checkInconsistenciesMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
    checkInconsistenciesMenuItem.setDisable(true); // TEMP disabled until implemented
    toolsMenu.getItems().add(checkInconsistenciesMenuItem);

    //

    final Menu helpMenu = new Menu(language.translate("menu.help"));

    final MenuItem aboutMenuItem = new MenuItem();
    aboutMenuItem.setText(language.translate("menu.help.about"));
    aboutMenuItem.setGraphic(theme.getIcon(Icon.ABOUT, Icon.Size.SMALL));
    aboutMenuItem.setOnAction(event -> this.onAboutAction());
    helpMenu.getItems().add(aboutMenuItem);

    return new MenuBar(fileMenu, editMenu, toolsMenu, helpMenu);
  }

  private ToolBar createToolBar() {
    final Language language = this.config.language();
    final Theme theme = this.config.theme();

    final ToolBar toolbar = new ToolBar();

    //

    final Button newToolbarButton = new Button();
    newToolbarButton.setTooltip(new Tooltip(language.translate("toolbar.new")));
    newToolbarButton.setGraphic(theme.getIcon(Icon.NEW_FILE, Icon.Size.BIG));
    newToolbarButton.setOnAction(event -> this.onNewTreeAction());
    toolbar.getItems().add(newToolbarButton);

    toolbar.getItems().add(new Separator(Orientation.VERTICAL));

    this.saveToolbarButton.setTooltip(new Tooltip(language.translate("toolbar.save")));
    this.saveToolbarButton.setGraphic(theme.getIcon(Icon.SAVE, Icon.Size.BIG));
    this.saveToolbarButton.setOnAction(event -> this.onSaveAction());
    toolbar.getItems().add(this.saveToolbarButton);

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

    //

    this.previousSelectionToolbarButton.setTooltip(new Tooltip(language.translate("toolbar.previous_selection")));
    this.previousSelectionToolbarButton.setGraphic(theme.getIcon(Icon.PERSON_BACK, Icon.Size.BIG));
    this.previousSelectionToolbarButton.setOnAction(event -> this.onNavigateSelectionHistory(-1));
    toolbar.getItems().add(this.previousSelectionToolbarButton);

    this.nextSelectionToolbarButton.setTooltip(new Tooltip(language.translate("toolbar.next_selection")));
    this.nextSelectionToolbarButton.setGraphic(theme.getIcon(Icon.PERSON_NEXT, Icon.Size.BIG));
    this.nextSelectionToolbarButton.setOnAction(event -> this.onNavigateSelectionHistory(1));
    toolbar.getItems().add(this.nextSelectionToolbarButton);

    toolbar.getItems().add(new Separator(Orientation.VERTICAL));

    //

    this.setAsRootToolbarButton.setTooltip(new Tooltip(language.translate("toolbar.set_as_root")));
    this.setAsRootToolbarButton.setGraphic(theme.getIcon(Icon.SET_AS_ROOT, Icon.Size.BIG));
    this.setAsRootToolbarButton.setOnAction(event -> this.onSetAsRootAction());
    toolbar.getItems().add(this.setAsRootToolbarButton);

    final Button addPersonToolbarButton = new Button();
    addPersonToolbarButton.setTooltip(new Tooltip(language.translate("toolbar.add_person")));
    addPersonToolbarButton.setGraphic(theme.getIcon(Icon.ADD_PERSON, Icon.Size.BIG));
    addPersonToolbarButton.setOnAction(event -> this.onAddPersonAction());
    toolbar.getItems().add(addPersonToolbarButton);

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

    this.editDocumentsToolbarButton.setTooltip(new Tooltip(language.translate("toolbar.edit_documents")));
    this.editDocumentsToolbarButton.setGraphic(theme.getIcon(Icon.EDIT_DOCUMENTS, Icon.Size.BIG));
    this.editDocumentsToolbarButton.setOnAction(event -> this.onEditObjectDocumentsAction());
    toolbar.getItems().add(this.editDocumentsToolbarButton);

    toolbar.getItems().add(new Separator(Orientation.VERTICAL));

    //

    final Button calculateRelationshipsToolbarButton = new Button();
    calculateRelationshipsToolbarButton.setTooltip(new Tooltip(language.translate("toolbar.calculate_relationships")));
    calculateRelationshipsToolbarButton.setGraphic(theme.getIcon(Icon.CALCULATE_RELATIONSHIPS, Icon.Size.BIG));
    calculateRelationshipsToolbarButton.setDisable(true); // TEMP disabled until implemented
    toolbar.getItems().add(calculateRelationshipsToolbarButton);

    final Button birthdaysToolbarButton = new Button();
    birthdaysToolbarButton.setTooltip(new Tooltip(language.translate("toolbar.birthdays")));
    birthdaysToolbarButton.setGraphic(theme.getIcon(Icon.BIRTHDAYS, Icon.Size.BIG));
    birthdaysToolbarButton.setOnAction(event -> this.onShowBirthdaysDialog());
    toolbar.getItems().add(birthdaysToolbarButton);

    final Button mapToolbarButton = new Button();
    mapToolbarButton.setTooltip(new Tooltip(language.translate("toolbar.map")));
    mapToolbarButton.setGraphic(theme.getIcon(Icon.MAP, Icon.Size.BIG));
    mapToolbarButton.setOnAction(event -> this.onShowMapDialog());
    toolbar.getItems().add(mapToolbarButton);

    toolbar.getItems().add(new Separator(Orientation.VERTICAL));

    final Button checkInconsistenciesToolbarButton = new Button();
    checkInconsistenciesToolbarButton.setTooltip(new Tooltip(language.translate("toolbar.check_inconsistencies")));
    checkInconsistenciesToolbarButton.setGraphic(theme.getIcon(Icon.CHECK_INCONSISTENCIES, Icon.Size.BIG));
    checkInconsistenciesToolbarButton.setDisable(true); // TEMP disabled until implemented
    toolbar.getItems().add(checkInconsistenciesToolbarButton);

    return toolbar;
  }

  private SplitPane createContent(final @NotNull Config config) {
    final Language language = config.language();
    final SplitPane splitPane = new SplitPane();

    this.familyMembersTreeView.personClickListeners()
        .add(event -> this.onPersonClick(event, this.familyMembersTreeView));
    splitPane.getItems().add(this.familyMembersTreeView);

    final TabPane tabPane = new TabPane();
    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    splitPane.getItems().add(tabPane);

    this.geneticFamilyTreePane.personClickListeners()
        .add(event -> this.onPersonClick(event, this.geneticFamilyTreePane));
    this.geneticFamilyTreePane.newParentClickListeners().add(this::onNewParentClick);
    this.geneticFamilyTreePane.setMaxHeight(this.config.maxTreeHeight());
    tabPane.getTabs().add(new Tab(language.translate("main_view.tab.genetic_tree"), this.geneticFamilyTreePane));

    this.familyMemberFullViewPane.personClickListeners()
        .add(event -> this.onPersonClick(event, this.familyMemberFullViewPane));
    this.familyMemberFullViewPane.newParentClickListeners().add(this::onNewParentClick);
    tabPane.getTabs().add(new Tab(language.translate("main_view.tab.person_relatives"), this.familyMemberFullViewPane));

    tabPane.getTabs().add(new Tab(language.translate("main_view.tab.statistics"), this.statisticsPanel));

    this.personDetailsView.personClickListeners()
        .add(event -> this.onPersonClick(event, null));
    this.personDetailsView.newParentClickListeners()
        .add(this::onNewParentClick);
    this.personDetailsView.documentEditedListeners()
        .add(document -> this.onDocumentEdited());
    splitPane.getItems().add(this.personDetailsView);

    splitPane.setDividerPositions(0.1, 0.9);
    return splitPane;
  }

  /**
   * Show the stage.
   * <p>
   * Hooks callbacks to the stage’s close event and load the specified tree or the default one.
   *
   * @param treeName The name of the tree to open. May be null.
   */
  public void show(String treeName) {
    this.stage.show();

    // Open the passed tree directory
    if (treeName != null && this.loadTree(treeName))
      return;

    final Map<String, TreeMetadata> treesMetadata = App.treesMetadataManager().treesMetadata();

    // Only one choice, open it
    if (treesMetadata.size() == 1 && this.loadTree(treesMetadata.keySet().iterator().next()))
      return;

    final Optional<TreeMetadata> lastOpened = treesMetadata
        .values()
        .stream()
        .filter(m -> m.lastOpenDate() != null)
        .sorted()
        .findFirst();
    // Re-open the most recently opened tree, if any
    if (lastOpened.isPresent() && this.loadTree(lastOpened.get().directoryName()))
      return;

    // No trees have a last-opened date, choose from a dialog
    if (!treesMetadata.isEmpty()) {
      this.treesManagerDialog.refresh(null);
      final Optional<String> name = this.treesManagerDialog.showAndWait();
      if (name.isPresent()) {
        this.loadTree(name.get());
        return;
      }
    }

    // No more options, create a new tree
    if (!this.onNewTreeAction())
      this.stage.hide();
  }

  public void onConfigUpdate() {
    this.geneticFamilyTreePane.setMaxHeight(this.config.maxTreeHeight());
    this.geneticFamilyTreePane.refresh();
    this.familyMemberFullViewPane.refresh();
    this.familyMembersTreeView.refresh();
    this.personDetailsView.refresh();
    this.statisticsPanel.refresh();
  }

  /**
   * Set the current family tree.
   *
   * @param tree      Family tree to use.
   * @param directory The directory the tree has been loaded from.
   */
  private void setFamilyTree(@NotNull FamilyTree tree, @NotNull Path directory) {
    this.familyTree = tree;
    this.familyMembersTreeView.setFamilyTree(this.familyTree);
    this.geneticFamilyTreePane.setFamilyTree(this.familyTree);
    this.familyMemberFullViewPane.setFamilyTree(this.familyTree);
    this.statisticsPanel.setFamilyTree(this.familyTree);
    this.personDetailsView.setPerson(null, this.familyTree);
    this.familyMembersTreeView.refresh();
    this.geneticFamilyTreePane.refresh();
    this.familyMemberFullViewPane.refresh();
    this.selectionHistory.clear();
    this.selectionIndex = -1;
    this.loadedFile = directory;
    this.unsavedChanges = false;
    App.treesMetadataManager().onTreeOpened(tree, directory.getFileName().toString(), this.config);
    this.updateUI();
  }

  /**
   * Open an alert dialog to rename the current tree.
   */
  private void onRenameTreeAction() {
    final Optional<String> name = Alerts.textInput(
        this.config,
        "alert.tree_name.header",
        "alert.tree_name.label",
        null,
        this.familyTree.name(),
        StringUtils.filePathTextFormatter()
    );
    if (name.isEmpty())
      return;
    this.familyTree.setName(name.get());
    this.unsavedChanges = true;
    this.updateUI();
  }

  /**
   * Open an alert dialog to create a new tree.
   * <p>
   * Checks for any unsaved changes.
   *
   * @return True if a new tree was created as a result of this call; false otherwise.
   */
  private boolean onNewTreeAction() {
    if (this.unsavedChanges && !this.canProceedAfterOptionalSave()) return false;

    final String defaultName = this.config.language().translate("app_title.undefined_tree_name");
    boolean proceed = true;
    do {
      final Optional<String> name = Alerts.textInput(
          this.config,
          "alert.tree_name.header",
          "alert.tree_name.label",
          null,
          defaultName,
          StringUtils.filePathTextFormatter()
      );
      if (name.isEmpty())
        return false;
      if (App.treesMetadataManager().treesMetadata().keySet().stream().noneMatch(Predicate.isEqual(name.get()))) {
        this.setFamilyTree(new FamilyTree(name.get()), App.USER_DATA_DIR.resolve(name.get()));
        this.onSaveAction();
      } else {
        Alerts.warning(this.config, "alert.tree_already_exists.header", null, null);
        proceed = false;
      }
    } while (!proceed);
    return true;
  }

  /**
   * Open a file chooser dialog to import a ZIPed family tree.
   * <p>
   * Checks for any unsaved changes.
   *
   * @param directoryName The name of the directory to open.
   */
  private void onOpenTreeAction(@NotNull String directoryName) {
    if (!this.unsavedChanges || this.canProceedAfterOptionalSave())
      this.loadTree(directoryName);
  }

  /**
   * Open the dialog to manage trees.
   * If the user has selected a tree, it will be opened.
   */
  private void onManageTreesAction() {
    this.treesManagerDialog.refresh(this.loadedFile.getFileName().toString());
    this.treesManagerDialog.showAndWait().ifPresent(this::onOpenTreeAction);
    this.updateUI();
  }

  /**
   * Open a file chooser dialog to import a zipped family tree.
   * <p>
   * Checks for any unsaved changes.
   */
  private void onImportTreeAction() {
    if (this.unsavedChanges && !this.canProceedAfterOptionalSave()) return;

    final Optional<Path> f = FileChoosers.showZippedTreeFileChooser(this.config, this.stage);
    if (f.isEmpty())
      return;
    final String targetDir;
    try {
      FileUtils.deleteRecursively(App.TEMP_DIR);
      targetDir = FileUtils.unzip(f.get(), App.TEMP_DIR);
      Files.move(App.TEMP_DIR.resolve(targetDir), App.USER_DATA_DIR.resolve(targetDir));
      FileUtils.deleteRecursively(App.TEMP_DIR);
    } catch (final IOException e) {
      App.LOGGER.exception(e);
      Alerts.error(
          this.config,
          "alert.load_error.header",
          "alert.load_error.content",
          "alert.load_error.title",
          new FormatArg("trace", e.getMessage())
      );
      return;
    }
    this.loadTree(targetDir);
  }

  /**
   * Open a file chooser dialog to export this tree as a ZIP file.
   */
  private void onExportTreeAction() {
    final Optional<Path> file = FileChoosers.showZippedTreeFileSaver(this.config, this.stage, this.familyTree.name());
    if (file.isEmpty())
      return;
    try {
      FileUtils.zip(this.loadedFile, file.get());
    } catch (final IOException e) {
      App.LOGGER.exception(e);
      Alerts.error(
          this.config,
          "alert.save_error.header",
          "alert.save_error.content",
          "alert.save_error.title",
          new FormatArg("trace", e.getMessage())
      );
    }
  }

  /**
   * Load a tree from a directory.
   *
   * @param directoryName The name of the directory to read.
   * @return The tree read from the file.
   */
  private boolean loadTree(@NotNull String directoryName) {
    App.LOGGER.info("Loading tree %s…".formatted(directoryName));
    final FamilyTree familyTree;
    final Path path = App.USER_DATA_DIR.resolve(directoryName);
    try {
      familyTree = new FamilyTreeReader().loadFromDirectory(path);
    } catch (final IOException e) {
      App.LOGGER.exception(e);
      Alerts.error(
          this.config,
          "alert.load_error.header",
          "alert.load_error.content",
          "alert.load_error.title",
          new FormatArg("trace", e.getMessage())
      );
      return false;
    }
    App.LOGGER.info("Done");

    this.setFamilyTree(familyTree, path);
    return true;
  }

  /**
   * Save the current tree.
   */
  private void onSaveAction() {
    if (this.saveFile()) {
      this.unsavedChanges = false;
      this.updateUI();
    }
  }

  /**
   * Save the current tree to a file.
   *
   * @return True if save succeeded, false otherwise.
   */
  private boolean saveFile() {
    App.LOGGER.info("Saving tree to %s…".formatted(this.loadedFile));
    try {
      this.familyTreeWriter.saveToDirectory(this.familyTree, this.loadedFile, this.config);
    } catch (final IOException e) {
      App.LOGGER.exception(e);
      Alerts.error(
          this.config,
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
    if (this.focusedComponent != null)
      return this.focusedComponent.getSelectedPerson();
    return Optional.empty();
  }

  /**
   * Navigate the selection history, selecting the neighbor item in the given direction.
   *
   * @param direction The navigation direction, either -1 or 1.
   */
  private void onNavigateSelectionHistory(int direction) {
    if (this.selectionIndex == -1
        || direction < 0 && this.selectionIndex == 0
        || direction > 0 && this.selectionIndex == this.selectionHistory.size() - 1)
      return;
    this.selectionIndex += direction;
    final Person selection = this.selectionHistory.get(this.selectionIndex);
    final var event = new PersonClickedEvent(selection, PersonClickedEvent.Action.SELECT);
    this.updateWidgetsSelection(event, this.geneticFamilyTreePane);
    this.updateWidgetsSelection(event, this.familyMembersTreeView);
    this.updateWidgetsSelection(event, this.familyMemberFullViewPane);
    this.personDetailsView.setPerson(selection, this.familyTree);
    this.updateUI();
  }

  /**
   * Set the selected person as the root of the current tree.
   */
  private void onSetAsRootAction() {
    this.getSelectedPerson().ifPresent(root -> {
      this.familyTree.setRoot(root);
      this.unsavedChanges = true;
      this.geneticFamilyTreePane.refresh();
      this.familyMemberFullViewPane.refresh();
      this.familyMembersTreeView.refresh();
      this.personDetailsView.refresh();
      this.updateUI();
    });
  }

  /**
   * Open person edit dialog to create a new person.
   */
  private void onAddPersonAction() {
    this.openEditPersonDialog(null, null, null, EditPersonDialog.TAB_PROFILE);
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
   * Open person edit dialog to edit the specified tab.
   */
  private void openEditPersonDialogOnTab(int tabIndex) {
    this.getSelectedPerson().ifPresent(
        person -> this.openEditPersonDialog(person, null, null, tabIndex));
  }

  /**
   * Open dialog to edit the documents of the selected person.
   */
  private void onEditObjectDocumentsAction() {
    final Optional<? extends GenealogyObject<?>> selectedObject;
    final Optional<LifeEvent> selectedLifeEvent = this.personDetailsView.getDisplayedLifeEvent();
    if (selectedLifeEvent.isPresent())
      selectedObject = selectedLifeEvent;
    else
      selectedObject = this.getSelectedPerson();
    selectedObject.ifPresent(o -> {
      this.editDocumentsDialog.setObject(o, this.familyTree);
      this.editDocumentsDialog.showAndWait().ifPresent(this::onDocumentsUpdate);
    });
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
            this.config,
            "alert.cannot_delete_root.header",
            "alert.cannot_delete_root.content",
            null,
            new FormatArg("person", person)
        );
        return;
      }

      final boolean delete = Alerts.confirmation(
          this.config, "alert.delete_person.header", null, "alert.delete_person.title");
      if (delete) {
        this.familyTree.removePerson(person);

        // Update UI
        this.geneticFamilyTreePane.refresh();
        this.familyMemberFullViewPane.refresh();
        this.familyMembersTreeView.refresh();
        this.statisticsPanel.refresh();
        if (this.personDetailsView.person() == person)
          this.personDetailsView.setPerson(null, this.familyTree);
        else
          this.personDetailsView.refresh();
        this.unsavedChanges = true;
        this.updateUI();
      }
    });
  }

  /**
   * Open person edit dialog to create a new sibling of the selected person.
   */
  private void onAddSiblingAction() {
    this.getSelectedPerson().ifPresent(
        person -> this.openEditPersonDialog(null, null, person.parents(), EditPersonDialog.TAB_PROFILE));
  }

  /**
   * Open person edit dialog to create a new child of the selected person.
   */
  private void onAddChildAction() {
    this.getSelectedPerson().ifPresent(
        person -> {
          final var parent = Map.of(ParentalRelationType.BIOLOGICAL_PARENT, Set.of(person));
          this.openEditPersonDialog(null, null, parent, EditPersonDialog.TAB_PROFILE);
        });
  }

  /**
   * Open person edit dialog to create a parent of the given child.
   *
   * @param childInfo Information about the children of the parent to create.
   */
  private void onNewParentClick(@NotNull List<ChildInfo> childInfo) {
    this.openEditPersonDialog(null, childInfo, null, EditPersonDialog.TAB_PROFILE);
  }

  /**
   * Called whenever a document is edited in the {@link #personDetailsView}.
   */
  private void onDocumentEdited() {
    this.unsavedChanges = true;
    this.updateUI();
  }

  /**
   * Update display when a person is clicked.
   *
   * @param event    The event that was fired.
   * @param fromNode The {@link FamilyTreeComponent} in which the click occurred. Null if another node fired it.
   */
  private void onPersonClick(
      final @NotNull PersonClickEvent event,
      FamilyTreeComponent fromNode
  ) {
    if (fromNode == null) {
      // Click occurred outside of any FamilyTreeComponent, select person in the genetic tree pane
      this.updateWidgetsSelection(event, this.geneticFamilyTreePane);
      fromNode = this.geneticFamilyTreePane;
    }
    this.focusedComponent = fromNode;
    if (this.config.shouldSyncTreeWithMainPane()) {
      if (fromNode == this.familyMembersTreeView) {
        this.updateWidgetsSelection(event, this.geneticFamilyTreePane);
        this.updateWidgetsSelection(event, this.familyMemberFullViewPane);
      } else if (fromNode == this.geneticFamilyTreePane) {
        this.updateWidgetsSelection(event, this.familyMemberFullViewPane);
        this.updateWidgetsSelection(event, this.familyMembersTreeView);
      } else {
        this.updateWidgetsSelection(event, this.geneticFamilyTreePane);
        this.updateWidgetsSelection(event, this.familyMembersTreeView);
      }
    }
    Person person = null;
    if (event instanceof PersonClickedEvent e) {
      person = e.person();
      if (this.selectionIndex == -1 || this.selectionHistory.get(this.selectionIndex) != person) {
        if (this.selectionIndex == -1 || this.selectionHistory.get(this.selectionHistory.size() - 1) != person)
          this.selectionHistory.add(person); // Only add if not already at the end
        this.selectionIndex = this.selectionHistory.size() - 1;
      }
    }
    this.personDetailsView.setPerson(person, this.familyTree);
    if (event instanceof PersonClickedEvent e && e.action() == PersonClickedEvent.Action.EDIT)
      this.openEditPersonDialog(person, null, null, EditPersonDialog.TAB_PROFILE);
    this.updateUI();
  }

  /**
   * Update the selection of a {@link FamilyTreeComponent} depending on the given {@link PersonClickEvent}.
   */
  private void updateWidgetsSelection(
      final @NotNull PersonClickEvent event,
      @NotNull FamilyTreeComponent component
  ) {
    if (event instanceof DeselectPersonsEvent)
      component.deselectAll();
    else if (event instanceof PersonClickedEvent e)
      component.select(e.person(), e.action().shouldUpdateTarget());
  }

  /**
   * Open the dialog to edit registries.
   */
  private void onEditRegistriesAction() {
    this.editRegistriesDialog.refresh(this.familyTree);
    final Optional<ButtonType> result = this.editRegistriesDialog.showAndWait();
    if (result.isPresent() && !result.get().getButtonData().isCancelButton()) {
      this.familyMembersTreeView.refresh();
      this.geneticFamilyTreePane.refresh();
      this.familyMemberFullViewPane.refresh();
      this.statisticsPanel.refresh();
      this.personDetailsView.refresh();
      this.unsavedChanges = true;
      this.updateUI();
    }
  }

  /**
   * Open the dialog to edit the current tree’s documents.
   */
  private void onEditTreeDocumentsAction() {
    this.editDocumentsDialog.setObject(null, this.familyTree);
    this.editDocumentsDialog.showAndWait().ifPresent(this::onDocumentsUpdate);
  }

  private void onDocumentsUpdate(@NotNull ManageDocumentsDialog.Result result) {
    if (result.targetUpdated() || result.anyDocumentUpdated()) {
      this.familyMembersTreeView.refresh();
      this.geneticFamilyTreePane.refresh();
      this.familyMemberFullViewPane.refresh();
      this.statisticsPanel.refresh();
      this.personDetailsView.refresh();
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
      Person person,
      final List<ChildInfo> childInfo,
      final Map<ParentalRelationType, Set<Person>> parents,
      int tabIndex
  ) {
    this.editPersonDialog.setPerson(person, childInfo, this.familyTree);
    if (parents != null && !parents.isEmpty())
      this.editPersonDialog.setParents(parents);
    this.editPersonDialog.selectTab(tabIndex);
    this.editPersonDialog.showAndWait().ifPresent(editedPerson -> {
      this.familyMembersTreeView.refresh();
      this.geneticFamilyTreePane.refresh();
      this.familyMemberFullViewPane.refresh();
      this.statisticsPanel.refresh();
      this.personDetailsView.refresh();
      if (person == null && childInfo != null && childInfo.isEmpty())
        this.onPersonClick(new PersonClickedEvent(editedPerson, PersonClickedEvent.Action.SET_AS_TARGET), null);
      this.unsavedChanges = true;
      this.updateUI();
    });
  }

  /**
   * Update the UI, i.e. menu items, toolbar buttons and stage’s title.
   */
  private void updateUI() {
    final String fileName = this.loadedFile.getFileName().toString();
    final String treeName = this.familyTree.name();
    String title = "%s%s – %s%s".formatted(
        App.NAME,
        this.config.isDebug() ? " [Debug]" : "",
        treeName,
        this.unsavedChanges ? "*" : ""
    );
    if (!treeName.equals(fileName))
      title += " (%s)".formatted(fileName);
    this.stage.setTitle(title);

    if (this.birthdaysDialog.isShowing())
      this.birthdaysDialog.refresh(this.familyTree);
    if (this.mapDialog.isShowing())
      this.mapDialog.refresh(this.familyTree);

    final Optional<Person> selectedPerson = this.getSelectedPerson();
    final boolean selection = selectedPerson.isPresent();
    final boolean hasAnyParents = selection && selectedPerson.get().hasAnyParents();
    final boolean selectedIsRoot = selection && selectedPerson.map(this.familyTree::isRoot).orElse(false);

    this.openTreeMenu.getItems().clear();
    final Map<String, TreeMetadata> treesMetadata = App.treesMetadataManager().treesMetadata();
    if (!treesMetadata.isEmpty()) {
      treesMetadata.values().stream()
          .filter(m -> !m.directoryName().equals(fileName))
          .sorted()
          .forEach(m -> {
            final MenuItem item = new MenuItem("%s (%s)".formatted(m.name(), App.USER_DATA_DIR.resolve(m.directoryName())));
            item.setOnAction(event -> this.onOpenTreeAction(m.directoryName()));
            this.openTreeMenu.getItems().add(item);
          });
      if (!this.openTreeMenu.getItems().isEmpty())
        this.openTreeMenu.getItems().add(new SeparatorMenuItem());
      this.openTreeMenu.getItems().add(this.manageTreesMenuItem);
    }

    this.saveMenuItem.setDisable(!this.unsavedChanges);
    this.setAsRootMenuItem.setDisable(!selection || selectedIsRoot);
    this.editPersonMenuItem.setDisable(!selection);
    this.removePersonMenuItem.setDisable(!selection || selectedIsRoot);
    this.addChildMenuItem.setDisable(!selection);
    this.addSiblingMenuItem.setDisable(!hasAnyParents);
    this.editParentsMenuItem.setDisable(!selection);
    this.editLifeEventsMenuItem.setDisable(!selection);
    this.editDocumentsMenuItem.setDisable(!selection);

    this.saveToolbarButton.setDisable(!this.unsavedChanges);
    this.previousSelectionToolbarButton.setDisable(this.selectionIndex <= 0);
    this.nextSelectionToolbarButton.setDisable(this.selectionIndex == -1 || this.selectionIndex == this.selectionHistory.size() - 1);
    this.setAsRootToolbarButton.setDisable(!selection || selectedIsRoot);
    this.addChildToolbarButton.setDisable(!selection);
    this.addSiblingToolbarButton.setDisable(!hasAnyParents);
    this.editParentsToolbarButton.setDisable(!selection);
    this.editLifeEventsToolbarButton.setDisable(!selection);
    this.editDocumentsToolbarButton.setDisable(!selection);
  }

  /**
   * Open birthdays dialog.
   */
  private void onShowBirthdaysDialog() {
    if (this.birthdaysDialog.isShowing())
      return;
    this.birthdaysDialog.refresh(this.familyTree);
    this.birthdaysDialog.show();
  }

  /**
   * Open map dialog.
   */
  private void onShowMapDialog() {
    if (this.mapDialog.isShowing())
      return;
    this.mapDialog.refresh(this.familyTree);
    this.mapDialog.show();
  }

  /**
   * Open settings dialog.
   */
  private void onSettingsAction() {
    this.settingsDialog.resetLocalConfig(this.config);
    this.settingsDialog.showAndWait();
  }

  /**
   * Open about dialog.
   */
  private void onAboutAction() {
    this.aboutDialog.showAndWait();
  }

  /**
   * Check if there are unsaved changes and, if so, warn the user.
   * The user can choose to discard or save changes, or cancel the dialog.
   *
   * @return True if there are no unsaved changes, or the user chose to discard them,
   * or they chose to save and the save was successful.
   */
  private boolean canProceedAfterOptionalSave() {
    final Optional<Boolean> open = Alerts.confirmationWithCancel(
        this.config,
        "alert.unsaved_changes.header",
        "alert.unsaved_changes.content",
        null
    );
    return open.isPresent() && (!open.get() || this.saveFile());
  }
}
