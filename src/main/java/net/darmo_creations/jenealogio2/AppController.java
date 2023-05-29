package net.darmo_creations.jenealogio2;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import net.darmo_creations.jenealogio2.themes.Icon;
import net.darmo_creations.jenealogio2.themes.Theme;
import net.darmo_creations.jenealogio2.ui.TreeWidget;
import net.darmo_creations.jenealogio2.ui.dialogs.AboutDialog;
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
  private MenuItem addParentMenuItem;
  @FXML
  private MenuItem addChildMenuItem;
  @FXML
  private MenuItem removeParentalRelationMenuItem;
  @FXML
  private MenuItem addRelationMenuItem;
  @FXML
  private MenuItem editRelationMenuItem;
  @FXML
  private MenuItem removeRelationMenuItem;
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
  private Button addParentToolbarButton;
  @FXML
  private Button addChildToolbarButton;
  @FXML
  private Button addRelationToolbarButton;
  @FXML
  private Button checkInconsistenciesToolbarButton;

  @FXML
  private AnchorPane mainPane;

  private final TreeWidget treeWidget = new TreeWidget();

  private final SettingsDialog settingsDialog = new SettingsDialog();
  private final AboutDialog aboutDialog = new AboutDialog();

  public void initialize() {
    Theme theme = App.config().theme();

    // Menu items
    this.newFileMenuItem.setGraphic(theme.getIcon(Icon.NEW_FILE, Icon.Size.SMALL));
    this.openFileMenuItem.setGraphic(theme.getIcon(Icon.OPEN_FILE, Icon.Size.SMALL));
    this.saveMenuItem.setGraphic(theme.getIcon(Icon.SAVE, Icon.Size.SMALL));
    this.saveAsMenuItem.setGraphic(theme.getIcon(Icon.SAVE_AS, Icon.Size.SMALL));
    this.settingsMenuItem.setGraphic(theme.getIcon(Icon.SETTINGS, Icon.Size.SMALL));
    this.quitMenuItem.setGraphic(theme.getIcon(Icon.QUIT, Icon.Size.SMALL));

    this.undoMenuItem.setGraphic(theme.getIcon(Icon.UNDO, Icon.Size.SMALL));
    this.redoMenuItem.setGraphic(theme.getIcon(Icon.REDO, Icon.Size.SMALL));
    this.addPersonMenuItem.setGraphic(theme.getIcon(Icon.ADD_PERSON, Icon.Size.SMALL));
    this.editPersonMenuItem.setGraphic(theme.getIcon(Icon.EDIT_PERSON, Icon.Size.SMALL));
    this.removePersonMenuItem.setGraphic(theme.getIcon(Icon.REMOVE_PERSON, Icon.Size.SMALL));
    this.addParentMenuItem.setGraphic(theme.getIcon(Icon.ADD_PARENT, Icon.Size.SMALL));
    this.addChildMenuItem.setGraphic(theme.getIcon(Icon.ADD_CHILD, Icon.Size.SMALL));
    this.removeParentalRelationMenuItem.setGraphic(theme.getIcon(Icon.REMOVE_PARENTAL_RELATION, Icon.Size.SMALL));
    this.addRelationMenuItem.setGraphic(theme.getIcon(Icon.ADD_RELATION, Icon.Size.SMALL));
    this.editRelationMenuItem.setGraphic(theme.getIcon(Icon.EDIT_RELATION, Icon.Size.SMALL));
    this.removeRelationMenuItem.setGraphic(theme.getIcon(Icon.REMOVE_RELATION, Icon.Size.SMALL));

    this.checkInconsistenciesMenuItem.setGraphic(theme.getIcon(Icon.CHECK_INCONSISTENCIES, Icon.Size.SMALL));

    this.aboutMenuItem.setGraphic(theme.getIcon(Icon.ABOUT, Icon.Size.SMALL));

    // Toolbar buttons
    this.newToolbarButton.setGraphic(theme.getIcon(Icon.NEW_FILE, Icon.Size.BIG));
    this.openToolbarButton.setGraphic(theme.getIcon(Icon.OPEN_FILE, Icon.Size.BIG));
    this.saveToolbarButton.setGraphic(theme.getIcon(Icon.SAVE, Icon.Size.BIG));
    this.saveAsToolbarButton.setGraphic(theme.getIcon(Icon.SAVE_AS, Icon.Size.BIG));

    this.undoToolbarButton.setGraphic(theme.getIcon(Icon.UNDO, Icon.Size.BIG));
    this.redoToolbarButton.setGraphic(theme.getIcon(Icon.REDO, Icon.Size.BIG));
    this.addPersonToolbarButton.setGraphic(theme.getIcon(Icon.ADD_PERSON, Icon.Size.BIG));
    this.addParentToolbarButton.setGraphic(theme.getIcon(Icon.ADD_PARENT, Icon.Size.BIG));
    this.addChildToolbarButton.setGraphic(theme.getIcon(Icon.ADD_CHILD, Icon.Size.BIG));
    this.addRelationToolbarButton.setGraphic(theme.getIcon(Icon.ADD_RELATION, Icon.Size.BIG));

    this.checkInconsistenciesToolbarButton.setGraphic(theme.getIcon(Icon.CHECK_INCONSISTENCIES, Icon.Size.BIG));

    AnchorPane.setTopAnchor(this.treeWidget, 0.0);
    AnchorPane.setBottomAnchor(this.treeWidget, 0.0);
    AnchorPane.setLeftAnchor(this.treeWidget, 0.0);
    AnchorPane.setRightAnchor(this.treeWidget, 0.0);
    this.mainPane.getChildren().add(this.treeWidget);
  }

  @FXML
  public void onSettings() {
    this.settingsDialog.setConfig(App.config());
    this.settingsDialog.show();
  }

  @FXML
  public void onAbout() {
    this.aboutDialog.show();
  }

  @FXML
  public void onQuit() {
    Platform.exit();
  }
}
