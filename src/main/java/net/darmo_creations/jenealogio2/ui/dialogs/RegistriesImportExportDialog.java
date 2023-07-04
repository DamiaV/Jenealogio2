package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.darmo_creations.jenealogio2.App;
import net.darmo_creations.jenealogio2.config.Language;
import net.darmo_creations.jenealogio2.model.Gender;
import net.darmo_creations.jenealogio2.model.LifeEventType;
import net.darmo_creations.jenealogio2.themes.Icon;
import net.darmo_creations.jenealogio2.utils.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

/**
 * Simple dialog that allows selecting which registry entries to import/export.
 */
public class RegistriesImportExportDialog extends DialogBase<ButtonType> {
  private final EntriesTab<LifeEventType> eventTypesTab;
  private final EntriesTab<Gender> gendersTab;

  public RegistriesImportExportDialog(boolean importing) {
    super(importing ? "registries_import" : "registries_export", true, ButtonTypes.OK, ButtonTypes.CANCEL);
    Language language = App.config().language();

    Label descLabel = new Label(language.translate("dialog.registries_%s.description".formatted(importing ? "import" : "export")),
        App.config().theme().getIcon(Icon.INFO, Icon.Size.SMALL));
    descLabel.setWrapText(true);
    this.eventTypesTab = new EntriesTab<>(language.translate("dialog.registries_import_export.tab.life_event_types"));
    this.gendersTab = new EntriesTab<>(language.translate("dialog.registries_import_export.tab.genders"));
    TabPane tabPane = new TabPane(this.eventTypesTab, this.gendersTab);
    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    this.getDialogPane().setContent(new VBox(4, descLabel, tabPane));

    Stage stage = this.stage();
    stage.setMinWidth(300);
    stage.setMinHeight(200);
  }

  public Pair<List<LifeEventType>, List<Gender>> getSelectedItems() {
    return new Pair<>(this.eventTypesTab.getSelectedItems(), this.gendersTab.getSelectedItems());
  }

  public void setItems(
      final @NotNull List<LifeEventType> lifeEventTypes,
      final @NotNull List<Gender> genders
  ) {
    this.eventTypesTab.setItems(lifeEventTypes);
    this.gendersTab.setItems(genders);
  }

  private void updateButtons() {
    boolean noSelection = !this.eventTypesTab.getSelectedItems().isEmpty()
        && !this.gendersTab.getSelectedItems().isEmpty();
    this.getDialogPane().lookupButton(ButtonTypes.OK).setDisable(noSelection);
  }

  private class EntriesTab<T> extends Tab {
    private final TreeView<T> treeView = new TreeView<>();

    public EntriesTab(String text) {
      super(text);
      Language language = App.config().language();
      Button selectAllButton = new Button(language.translate("dialog.registries_import_export.select_all"));
      selectAllButton.setOnAction(event -> this.select(SelectionMode.ALL));
      Button deselectAllButton = new Button(language.translate("dialog.registries_import_export.deselect_all"));
      deselectAllButton.setOnAction(event -> this.select(SelectionMode.NONE));
      Button invertSelectionButton = new Button(language.translate("dialog.registries_import_export.invert_selection"));
      invertSelectionButton.setOnAction(event -> this.select(SelectionMode.INVERT));
      HBox buttonsBox = new HBox(4, selectAllButton, deselectAllButton, invertSelectionButton);
      this.treeView.setShowRoot(false);
      this.treeView.setRoot(new TreeItem<>());
      this.treeView.setCellFactory(e -> new CheckBoxTreeCell<>());
      this.setContent(new VBox(4, this.treeView, buttonsBox));
    }

    private void select(@NotNull SelectionMode mode) {
      for (TreeItem<T> child : this.treeView.getRoot().getChildren()) {
        if (child instanceof CheckBoxTreeItem<T> i) {
          i.setSelected(mode.apply(i.isSelected()));
        }
      }
      RegistriesImportExportDialog.this.updateButtons();
    }

    public List<T> getSelectedItems() {
      return this.treeView.getRoot().getChildren().stream()
          .filter(item -> item instanceof CheckBoxTreeItem<?> i && i.isSelected())
          .map(TreeItem::getValue)
          .toList();
    }

    public void setItems(final @NotNull List<T> items) {
      this.treeView.getRoot().getChildren().clear();
      for (T eventType : items) {
        this.treeView.getRoot().getChildren().add(new CheckBoxTreeItem<>(eventType, null, true));
      }
    }

    private enum SelectionMode {
      ALL(b -> true),
      NONE(b -> false),
      INVERT(b -> !b);

      private final Function<Boolean, Boolean> f;

      SelectionMode(@NotNull Function<Boolean, Boolean> f) {
        this.f = f;
      }

      public boolean apply(boolean b) {
        return this.f.apply(b);
      }
    }
  }
}
