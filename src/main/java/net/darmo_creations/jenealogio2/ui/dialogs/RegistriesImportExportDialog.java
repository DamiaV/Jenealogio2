package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.util.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.themes.*;
import net.darmo_creations.jenealogio2.utils.Pair;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.*;

/**
 * Simple dialog that allows selecting which registry entries to import/export.
 */
public class RegistriesImportExportDialog extends DialogBase<ButtonType> {
  private final EntriesTab<LifeEventType> eventTypesTab;
  private final EntriesTab<Gender> gendersTab;

  /**
   * Create a new dialog to import or export registries.
   *
   * @param config    The app’s config.
   * @param importing If true, setup as an import dialog, otherwise setup as an export dialog.
   */
  public RegistriesImportExportDialog(final @NotNull Config config, boolean importing) {
    super(config, importing ? "registries_import" : "registries_export", true, ButtonTypes.OK, ButtonTypes.CANCEL);
    final Language language = config.language();

    final Label descLabel = new Label(
        language.translate("dialog.registries_%s.description".formatted(importing ? "import" : "export")),
        config.theme().getIcon(Icon.INFO, Icon.Size.SMALL)
    );
    descLabel.setWrapText(true);
    this.eventTypesTab = new EntriesTab<>("life_event_types");
    this.gendersTab = new EntriesTab<>("genders");
    final TabPane tabPane = new TabPane(this.eventTypesTab, this.gendersTab);
    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    this.getDialogPane().setContent(new VBox(5, descLabel, tabPane));

    final Stage stage = this.stage();
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
    final boolean noSelection = !this.eventTypesTab.getSelectedItems().isEmpty()
                                && !this.gendersTab.getSelectedItems().isEmpty();
    this.getDialogPane().lookupButton(ButtonTypes.OK).setDisable(noSelection);
  }

  private class EntriesTab<T extends RegistryEntry> extends Tab {
    private final TreeView<T> treeView = new TreeView<>();

    public EntriesTab(String registryName) {
      super(RegistriesImportExportDialog.this.config.language().translate("dialog.registries_import_export.tab." + registryName));
      final Language language = RegistriesImportExportDialog.this.config.language();
      final Button selectAllButton = new Button(language.translate("dialog.registries_import_export.select_all"));
      selectAllButton.setOnAction(event -> this.select(SelectionMode.ALL));
      final Button deselectAllButton = new Button(language.translate("dialog.registries_import_export.deselect_all"));
      deselectAllButton.setOnAction(event -> this.select(SelectionMode.NONE));
      final Button invertSelectionButton = new Button(language.translate("dialog.registries_import_export.invert_selection"));
      invertSelectionButton.setOnAction(event -> this.select(SelectionMode.INVERT));
      final HBox buttonsBox = new HBox(5, selectAllButton, deselectAllButton, invertSelectionButton);
      this.treeView.setShowRoot(false);
      this.treeView.setRoot(new TreeItem<>());
      this.treeView.setCellFactory(e -> {
        final CheckBoxTreeCell<T> cell = new CheckBoxTreeCell<>();
        cell.setConverter(new StringConverter<>() {
          @Override
          public String toString(TreeItem<T> item) {
            if (item == null)
              return "";
            final T entry = item.getValue();
            if (entry.isBuiltin())
              return language.translate(registryName + "." + entry.key().name());
            else
              return entry.userDefinedName();
          }

          @Override
          public TreeItem<T> fromString(String string) {
            return null;
          }
        });
        return cell;
      });
      this.setContent(new VBox(5, this.treeView, buttonsBox));
    }

    private void select(@NotNull SelectionMode mode) {
      for (final TreeItem<T> child : this.treeView.getRoot().getChildren())
        if (child instanceof final CheckBoxTreeItem<T> i)
          i.setSelected(mode.apply(i.isSelected()));
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
      for (final T eventType : items)
        this.treeView.getRoot().getChildren().add(new CheckBoxTreeItem<>(eventType, null, true));
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
