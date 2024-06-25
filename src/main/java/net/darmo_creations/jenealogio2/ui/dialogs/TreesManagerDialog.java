package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import net.darmo_creations.jenealogio2.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.io.*;
import net.darmo_creations.jenealogio2.themes.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.util.*;

/**
 * Dialog that manages the trees in the user_data directory.
 */
public class TreesManagerDialog extends DialogBase<String> {
  private final ListView<TreeListItem> treesList = new ListView<>();
  private final Button showCurrentTreeButton = new Button();

  private String currentTreeDirectory;

  public TreesManagerDialog(final @NotNull Config config) {
    super(config, "trees_manager", false, ButtonTypes.OPEN, ButtonTypes.CLOSE);

    this.showCurrentTreeButton.setText(config.language().translate("dialog.trees_manager.show_current_in_explorer"));
    this.showCurrentTreeButton.setGraphic(config.theme().getIcon(Icon.SHOW_TREE_IN_EXPLORER, Icon.Size.SMALL));
    this.showCurrentTreeButton.setOnAction(
        event -> FileUtils.openInFileExplorer(App.USER_DATA_DIR.resolve(this.currentTreeDirectory)));

    this.treesList.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> this.updateUI());

    HBox hBox = new HBox(this.showCurrentTreeButton);
    hBox.setAlignment(Pos.CENTER);
    VBox content = new VBox(5, hBox, this.treesList);
    content.setPrefWidth(400);
    content.setPrefHeight(300);
    this.getDialogPane().setContent(content);

    this.setResultConverter(b -> {
      if (!b.getButtonData().isCancelButton()) {
        TreeListItem selectedItem = this.treesList.getSelectionModel().getSelectedItem();
        return selectedItem != null ? selectedItem.treeMetadata().directoryName() : null;
      }
      return null;
    });

    this.updateUI();
  }

  /**
   * Refresh the list of available trees.
   *
   * @param currentTreeDirectory The directory name of the currently loaded tree.
   */
  public void refresh(String currentTreeDirectory) {
    this.currentTreeDirectory = currentTreeDirectory;
    this.showCurrentTreeButton.setDisable(currentTreeDirectory == null);
    this.treesList.getItems().clear();
    App.treesMetadataManager().treesMetadata().values()
        .stream()
        .filter(m -> !m.directoryName().equals(currentTreeDirectory))
        .sorted()
        .forEach(m -> this.treesList.getItems().add(new TreeListItem(m)));
    this.updateUI();
  }

  private void updateUI() {
    this.getDialogPane().lookupButton(ButtonTypes.OPEN)
        .setDisable(this.treesList.getSelectionModel().getSelectedItem() == null);
  }

  /**
   * List item showing displaying a {@link TreeMetadata} object and buttons to interact with it.
   */
  private class TreeListItem extends HBox {
    private final TreeMetadata treeMetadata;

    private TreeListItem(@NotNull TreeMetadata treeMetadata) {
      super(5);
      this.treeMetadata = Objects.requireNonNull(treeMetadata);
      Node spacer = new Pane();
      HBox.setHgrow(spacer, Priority.ALWAYS);
      Button showInExplorerButton = new Button();
      Config config = TreesManagerDialog.this.config;
      showInExplorerButton.setTooltip(new Tooltip(config.language().translate("dialog.trees_manager.show_in_explorer.tooltip")));
      showInExplorerButton.setGraphic(config.theme().getIcon(Icon.SHOW_TREE_IN_EXPLORER, Icon.Size.SMALL));
      showInExplorerButton.setOnAction(
          event -> FileUtils.openInFileExplorer(App.USER_DATA_DIR.resolve(this.treeMetadata.directoryName())));
      Button deleteButton = new Button();
      deleteButton.setTooltip(new Tooltip(config.language().translate("dialog.trees_manager.delete_tree.tooltip")));
      deleteButton.setGraphic(config.theme().getIcon(Icon.DELETE_TREE, Icon.Size.SMALL));
      deleteButton.setOnAction(e -> this.onDeleteAction());
      String label;
      if (treeMetadata.name().equals(treeMetadata.directoryName()))
        label = treeMetadata.name();
      else
        label = "%s (%s)".formatted(treeMetadata.name(), treeMetadata.directoryName());
      this.getChildren().addAll(
          new Label(label),
          spacer,
          showInExplorerButton,
          deleteButton
      );
    }

    /**
     * The {@link TreeMetadata} held by this item.
     */
    public TreeMetadata treeMetadata() {
      return this.treeMetadata;
    }

    private void onDeleteAction() {
      boolean ok = Alerts.confirmation(
          TreesManagerDialog.this.config,
          "alert.confirm_tree_deletion.header",
          "alert.confirm_tree_deletion.content",
          null,
          new FormatArg("name", this.treeMetadata.name()),
          new FormatArg("directory", this.treeMetadata.directoryName())
      );
      if (!ok)
        return;
      try {
        FileUtils.deleteRecursively(App.USER_DATA_DIR.resolve(this.treeMetadata.directoryName()));
      } catch (IOException e) {
        App.LOGGER.exception(e);
        Alerts.error(
            TreesManagerDialog.this.config,
            "alert.deletion_error.header",
            "alert.deletion_error.content",
            "alert.deletion_error.title",
            new FormatArg("trace", e.getMessage())
        );
      }
      App.treesMetadataManager().removeEntry(this.treeMetadata.directoryName());
      TreesManagerDialog.this.refresh(TreesManagerDialog.this.currentTreeDirectory);
    }
  }
}
