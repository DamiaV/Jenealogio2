package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;
import net.darmo_creations.jenealogio2.App;
import net.darmo_creations.jenealogio2.config.Config;
import net.darmo_creations.jenealogio2.config.Language;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.themes.Icon;
import net.darmo_creations.jenealogio2.themes.Theme;
import net.darmo_creations.jenealogio2.ui.PseudoClasses;
import net.darmo_creations.jenealogio2.ui.components.ColorPickerTableCell;
import net.darmo_creations.jenealogio2.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Dialog that allows editing registries.
 */
public class RegistriesDialog extends DialogBase<ButtonType> {
  private final LifeEventTypeRegistryView eventTypesView = new LifeEventTypeRegistryView();
  private final GenderRegistryView gendersView = new GenderRegistryView();

  /**
   * Create an about dialog.
   */
  public RegistriesDialog() {
    super("edit_registries", true, ButtonTypes.CANCEL, ButtonTypes.OK);

    Language language = App.config().language();

    TabPane tabPane = new TabPane(
        this.createTab("life_event_types", this.eventTypesView),
        this.createTab("genders", this.gendersView)
    );
    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    VBox.setVgrow(tabPane, Priority.ALWAYS);

    Button importButton = new Button();
    importButton.setText(language.translate("dialog.edit_registries.import"));
    Button importFromTreeButton = new Button();
    importFromTreeButton.setText(language.translate("dialog.edit_registries.import_from_tree"));
    Button exportButton = new Button();
    exportButton.setText(language.translate("dialog.edit_registries.export"));
    HBox buttonsBox = new HBox(4, importButton, importFromTreeButton, exportButton);

    VBox content = new VBox(4, tabPane, buttonsBox);
    content.setPrefWidth(600);
    content.setPrefHeight(400);
    this.getDialogPane().setContent(content);

    Stage stage = this.stage();
    stage.setMinWidth(600);
    stage.setMinHeight(500);

    this.setResultConverter(buttonType -> {
      if (buttonType == ButtonTypes.OK) {
        this.eventTypesView.applyChanges();
        this.gendersView.applyChanges();
      }
      return buttonType;
    });
  }

  private Tab createTab(@NotNull String name, final @NotNull RegistryView<?, ?, ?> registryView) {
    Language language = App.config().language();
    Tab tab = new Tab(language.translate("dialog.edit_registries.tab.%s.title".formatted(name)));

    Label description = new Label(language.translate("dialog.edit_registries.tab.%s.description".formatted(name)));
    description.setWrapText(true);

    VBox.setVgrow(registryView, Priority.ALWAYS);

    VBox vBox = new VBox(4, registryView, description);
    vBox.setPadding(new Insets(4));
    tab.setContent(vBox);

    return tab;
  }

  /**
   * Refresh the registry entries lists.
   */
  public void refresh(final @NotNull FamilyTree familyTree) {
    this.eventTypesView.refresh(familyTree);
    this.gendersView.refresh(familyTree);
  }

  private void updateButtons() {
    boolean invalid = !this.eventTypesView.isValid() || !this.gendersView.isValid();
    this.getDialogPane().lookupButton(ButtonTypes.OK).setDisable(invalid);
  }

  /**
   * This allows editing the entries of a {@link Registry}.
   *
   * @param <RE> Type of wrapped registry entries.
   * @param <A>  Type of registry’s building arguments.
   */
  private abstract class RegistryView<TI extends TableItem<RE, A>, RE extends RegistryEntry, A>
      extends VBox {
    private final Button removeButton;
    protected final TableView<TI> entriesTable = new TableView<>();
    protected final HBox buttonsBox;

    protected Registry<RE, A> registry;
    private final Set<RE> entriesToDelete = new HashSet<>();

    /**
     * Show all entries of the given registry in a {@link TableView}.
     */
    protected RegistryView() {
      super(4);
      Config config = App.config();
      Language language = config.language();
      Theme theme = config.theme();

      Label helpLabel = new Label(language.translate("dialog.edit_registries.help"));
      helpLabel.setWrapText(true);
      helpLabel.setStyle("-fx-font-weight: bold");

      // Buttons
      Button addButton = new Button(language.translate("dialog.edit_registries.add_entry"),
          theme.getIcon(Icon.ADD_ENTRY, Icon.Size.SMALL));
      addButton.setOnAction(event -> this.onAddItem());
      this.removeButton = new Button(language.translate("dialog.edit_registries.delete_entry"),
          theme.getIcon(Icon.DELETE_ENTRY, Icon.Size.SMALL));
      this.removeButton.setOnAction(event -> this.onRemoveSelectedItems());
      this.removeButton.setDisable(true);
      Pane spacer = new Pane();
      HBox.setHgrow(spacer, Priority.ALWAYS);
      this.buttonsBox = new HBox(4, spacer, addButton, this.removeButton);

      // Table
      VBox.setVgrow(this.entriesTable, Priority.ALWAYS);
      this.entriesTable.setPrefHeight(0);
      this.entriesTable.setEditable(true);

      TableColumn<TI, Integer> usageCol = new NonSortableTableColumn<>(
          language.translate("dialog.edit_registries.entry_usage"));
      usageCol.setCellFactory(param -> new TableCell<>() {
        @Override
        protected void updateItem(Integer item, boolean empty) {
          super.updateItem(item, empty);
          super.setGraphic(null);
          super.setText(item != null ? item.toString() : null);
          this.pseudoClassStateChanged(PseudoClasses.DISABLED, true);
        }
      });
      usageCol.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().usage()));

      TableColumn<TI, String> nameCol = new NonSortableTableColumn<>(
          language.translate("dialog.edit_registries.entry_name"));
      nameCol.setEditable(true);
      nameCol.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()) {
        @Override
        public void updateItem(String item, boolean empty) {
          super.updateItem(item, empty);
          var row = this.getTableRow();
          if (row == null) {
            return;
          }
          var rowItem = row.getItem();
          if (rowItem == null) {
            return;
          }
          var value = rowItem.entry();
          boolean editable = value == null || !value.isBuiltin();
          this.setEditable(editable);
          this.pseudoClassStateChanged(PseudoClasses.DISABLED, !editable);
        }
      });
      nameCol.setCellValueFactory(param -> param.getValue().nameProperty());

      //noinspection unchecked
      this.entriesTable.getColumns().addAll(usageCol, nameCol);
      this.entriesTable.getSelectionModel().selectedItemProperty()
          .addListener((observable, oldValue, newValue) -> this.onSelectionChange());

      this.getChildren().addAll(
          helpLabel,
          this.buttonsBox,
          this.entriesTable
      );
    }

    /**
     * Refresh the entries list.
     */
    public void refresh(final @NotNull FamilyTree familyTree) {
      this.entriesTable.getItems().clear();
      this.entriesToDelete.clear();
    }

    /**
     * Indicate whether all entries in this view are valid.
     */
    public boolean isValid() {
      return this.entriesTable.getItems().stream().allMatch(TableItem::isValid);
    }

    /**
     * Apply all changes to the wrapped registry.
     */
    public void applyChanges() {
      this.entriesToDelete.forEach(this.registry::removeEntry);
      for (var item : this.entriesTable.getItems()) {
        RE entry = item.entry();
        if (entry == null) {
          String label = item.getName();
          boolean ok;
          Random rng = new Random();
          do {
            String keyName = String.valueOf(rng.nextInt(1_000_000));
            RegistryEntryKey key = new RegistryEntryKey(Registry.USER_NS, keyName);
            try {
              this.registry.registerEntry(key, label, item.getBuildArgs());
              ok = true;
            } catch (IllegalArgumentException e) {
              ok = false; // Key is already used, try another one
            }
          } while (!ok);
        } else {
          item.updateEntry();
        }
      }
    }

    /**
     * Add a new entry to {@link #entriesTable}.
     */
    private void onAddItem() {
      this.entriesTable.getItems().add(this.newEntry(null, 0));
      this.entriesTable.getSelectionModel().select(this.entriesTable.getItems().size() - 1);
      RegistriesDialog.this.updateButtons();
    }

    @SuppressWarnings("SameParameterValue")
    protected abstract TI newEntry(RE entry, int usage);

    /**
     * Remove all selected items and stage them for deletion from the registry.
     */
    private void onRemoveSelectedItems() {
      Set<TI> toDelete = new HashSet<>();
      for (var selectedItem : this.entriesTable.getSelectionModel().getSelectedItems()) {
        RE entry = selectedItem.entry();
        if (entry != null) {
          this.entriesToDelete.add(entry);
        }
        toDelete.add(selectedItem);
      }
      this.entriesTable.getItems().removeAll(toDelete);
      RegistriesDialog.this.updateButtons();
    }

    /**
     * Called when the selection of {@link #entriesTable} changes.
     */
    protected void onSelectionChange() {
      // Prevent deleting builtin and used entries
      ObservableList<TI> selectedItems = this.entriesTable.getSelectionModel().getSelectedItems();
      boolean disable = selectedItems.isEmpty() || selectedItems.stream()
          .anyMatch(i -> i.entry() != null && i.entry().isBuiltin() || i.usage() != 0);
      this.removeButton.setDisable(disable);
    }
  }

  /**
   * This class allows editing {@link FamilyTree#lifeEventTypeRegistry()}.
   */
  private class LifeEventTypeRegistryView
      extends RegistryView<LifeEventTypeTableItem, LifeEventType, LifeEventTypeRegistry.RegistryArgs> {
    public LifeEventTypeRegistryView() {
      Language language = App.config().language();

      TableColumn<LifeEventTypeTableItem, LifeEventType.Group> groupCol = new NonSortableTableColumn<>(
          language.translate("dialog.edit_registries.tab.life_event_types.group"));
      groupCol.setEditable(true);
      groupCol.setCellFactory(param -> new ComboBoxTableCell<>(LifeEventType.Group.values()) {
        @Override
        public void updateItem(LifeEventType.Group item, boolean empty) {
          super.updateItem(item, empty);
          var row = this.getTableRow();
          if (row == null) {
            return;
          }
          var rowItem = row.getItem();
          if (rowItem == null) {
            return;
          }
          var value = rowItem.entry();
          boolean editable = value == null || !value.isBuiltin();
          this.setEditable(editable);
          this.pseudoClassStateChanged(PseudoClasses.DISABLED, !editable);
        }
      });
      groupCol.setCellValueFactory(param -> param.getValue().groupProperty());

      TableColumn<LifeEventTypeTableItem, Boolean> deathCol = new NonSortableTableColumn<>(
          language.translate("dialog.edit_registries.tab.life_event_types.indication_type.death"));
      deathCol.setEditable(true);
      deathCol.setCellFactory(param -> new CheckBoxTableCell<>() {
        @Override
        public void updateItem(Boolean item, boolean empty) {
          super.updateItem(item, empty);
          this.pseudoClassStateChanged(PseudoClasses.DISABLED, !this.isEditable());
        }
      });
      deathCol.setCellValueFactory(param -> param.getValue().indicatesDeathProperty());

      TableColumn<LifeEventTypeTableItem, Boolean> unionCol = new NonSortableTableColumn<>(
          language.translate("dialog.edit_registries.tab.life_event_types.indication_type.union"));
      unionCol.setEditable(true);
      unionCol.setCellFactory(param -> new CheckBoxCell<>() {
        @Override
        public void updateItem(Boolean item, boolean empty) {
          super.updateItem(item, empty);
          if (this.isEditable()) {
            var row = this.getTableRow();
            if (row == null) {
              return;
            }
            var rowItem = row.getItem();
            if (rowItem == null) {
              return;
            }
            boolean editable = rowItem.actorsNbProperty().get() >= 2;
            this.setEditable(editable);
            if (!editable) {
              rowItem.indicatesUnionProperty().set(false);
            }
          }
        }
      });
      unionCol.setCellValueFactory(param -> param.getValue().indicatesUnionProperty());

      TableColumn<LifeEventTypeTableItem, Integer> actorsNbCol = new NonSortableTableColumn<>(
          language.translate("dialog.edit_registries.tab.life_event_types.actors_nb"));
      actorsNbCol.setEditable(true);
      actorsNbCol.setCellFactory(param -> new ComboBoxTableCell<>(1, 2) {
        @Override
        public void updateItem(Integer item, boolean empty) {
          super.updateItem(item, empty);
          var row = this.getTableRow();
          if (row == null) {
            return;
          }
          var rowItem = row.getItem();
          if (rowItem == null) {
            return;
          }
          var value = rowItem.entry();
          boolean editable = rowItem.usage() == 0 && (value == null || !value.isBuiltin());
          this.setEditable(editable);
          this.pseudoClassStateChanged(PseudoClasses.DISABLED, !editable);
        }

        @Override
        public void commitEdit(Integer newValue) {
          super.commitEdit(newValue);
          this.getTableView().refresh(); // Force table refresh to update "editable" state of union checkbox
        }
      });
      actorsNbCol.setCellValueFactory(param -> param.getValue().actorsNbProperty());

      TableColumn<LifeEventTypeTableItem, Boolean> uniqueCol = new NonSortableTableColumn<>(
          language.translate("dialog.edit_registries.tab.life_event_types.unique"));
      uniqueCol.setEditable(true);
      uniqueCol.setCellFactory(param -> new CheckBoxCell<>());
      uniqueCol.setCellValueFactory(param -> param.getValue().uniqueProperty());

      //noinspection unchecked
      this.entriesTable.getColumns().addAll(groupCol, deathCol, unionCol, actorsNbCol, uniqueCol);
    }

    @Override
    public void refresh(final @NotNull FamilyTree familyTree) {
      super.refresh(familyTree);
      this.registry = familyTree.lifeEventTypeRegistry();
      Map<LifeEventType, List<LifeEvent>> eventTypesUsage = familyTree.lifeEvents().stream()
          .collect(Collectors.groupingBy(LifeEvent::type));
      for (LifeEventType entry : this.registry.entries()) {
        if (!entry.isBuiltin()) {
          int usage = eventTypesUsage.getOrDefault(entry, List.of()).size();
          this.entriesTable.getItems().add(new LifeEventTypeTableItem(entry, usage));
        }
      }
    }

    @Override
    protected LifeEventTypeTableItem newEntry(LifeEventType entry, int usage) {
      return new LifeEventTypeTableItem(entry, usage);
    }
  }

  /**
   * This class allows editing {@link FamilyTree#genderRegistry()}.
   */
  private class GenderRegistryView
      extends RegistryView<GenderTableItem, Gender, GenderRegistry.RegistryArgs> {

    private final Button resetButton;

    public GenderRegistryView() {
      Language language = App.config().language();

      this.resetButton = new Button(language.translate("dialog.edit_registries.reset_entry"),
          App.config().theme().getIcon(Icon.RESET_ENTRY, Icon.Size.SMALL));
      this.resetButton.setOnAction(event -> {
        ObservableList<GenderTableItem> selectedCells = this.entriesTable.getSelectionModel().getSelectedItems();
        for (GenderTableItem selectedCell : selectedCells) {
          Gender entry = selectedCell.entry();
          if (entry.isBuiltin()) {
            //noinspection DataFlowIssue
            selectedCell.colorProperty().set(Color.valueOf(entry.defaultColor()));
          }
        }
      });
      this.resetButton.setDisable(true);
      this.buttonsBox.getChildren().add(1, this.resetButton);

      TableColumn<GenderTableItem, Color> colorCol = new NonSortableTableColumn<>(
          language.translate("dialog.edit_registries.tab.genders.color"));
      colorCol.setEditable(true);
      colorCol.setCellFactory(param -> new ColorPickerTableCell<>());
      colorCol.setCellValueFactory(param -> param.getValue().colorProperty());

      this.entriesTable.getColumns().add(colorCol);
    }

    @Override
    protected void onSelectionChange() {
      super.onSelectionChange();
      ObservableList<GenderTableItem> selectedItems = this.entriesTable.getSelectionModel().getSelectedItems();
      boolean disable = selectedItems.isEmpty() || selectedItems.stream()
          .allMatch(i -> {
            Gender entry = i.entry();
            return entry == null || !entry.isBuiltin() || i.convertColor().equals(entry.defaultColor());
          });
      this.resetButton.setDisable(disable);
    }

    @Override
    public void refresh(final @NotNull FamilyTree familyTree) {
      super.refresh(familyTree);
      this.registry = familyTree.genderRegistry();
      //noinspection OptionalGetWithoutIsPresent
      Map<Gender, List<Person>> gendersUsage = familyTree.persons().stream()
          .filter(person -> person.gender().isPresent())
          .collect(Collectors.groupingBy(person -> person.gender().get()));
      for (Gender entry : this.registry.entries()) {
        int usage = gendersUsage.getOrDefault(entry, List.of()).size();
        this.entriesTable.getItems().add(new GenderTableItem(entry, usage));
      }
    }

    @Override
    protected GenderTableItem newEntry(Gender entry, int usage) {
      return new GenderTableItem(entry, usage);
    }
  }

  private abstract class TableItem<E extends RegistryEntry, A> {
    private final E entry;
    private final int usage;
    private final StringProperty nameProperty = new SimpleStringProperty();

    private TableItem(E entry, String registryName, int usage) {
      this.usage = usage;
      this.entry = entry;
      this.nameProperty.addListener((observable, oldValue, newValue) -> RegistriesDialog.this.updateButtons());
      String label;
      boolean builtin = entry != null && entry.isBuiltin();
      if (builtin) {
        label = App.config().language().translate(registryName + "." + entry.key().name());
      } else if (entry != null) {
        label = Objects.requireNonNull(entry.userDefinedName());
      } else {
        label = "";
      }
      this.nameProperty.set(label);
    }

    public void updateEntry() {
      if (!this.entry.isBuiltin()) {
        this.entry.setUserDefinedName(this.getName());
      }
    }

    public boolean isValid() {
      return this.getName() != null;
    }

    public E entry() {
      return this.entry;
    }

    public int usage() {
      return this.usage;
    }

    public StringProperty nameProperty() {
      return this.nameProperty;
    }

    public String getName() {
      return StringUtils.stripNullable(this.nameProperty.get()).orElse(null);
    }

    public abstract A getBuildArgs();
  }

  private class LifeEventTypeTableItem extends TableItem<LifeEventType, LifeEventTypeRegistry.RegistryArgs> {
    private final ObjectProperty<LifeEventType.Group> groupProperty = new SimpleObjectProperty<>();
    private final BooleanProperty indicatesDeathProperty = new SimpleBooleanProperty();
    private final BooleanProperty indicatesUnionProperty = new SimpleBooleanProperty();
    private final ObjectProperty<Integer> actorsNbProperty = new SimpleObjectProperty<>();
    private final BooleanProperty uniqueProperty = new SimpleBooleanProperty();

    public LifeEventTypeTableItem(LifeEventType entry, int usage) {
      super(entry, "life_event_types", usage);
      this.groupProperty.set(entry != null ? entry.group() : LifeEventType.Group.LIFESPAN);
      this.indicatesDeathProperty.set(entry != null && entry.indicatesDeath());
      this.indicatesUnionProperty.set(entry != null && entry.indicatesUnion());
      this.actorsNbProperty.set(entry != null ? entry.minActors() : 1);
      this.uniqueProperty.set(entry != null && entry.isUnique());
    }

    @Override
    public void updateEntry() {
      super.updateEntry();
      LifeEventType entry = this.entry();
      if (entry.isBuiltin()) {
        return;
      }
      entry.setGroup(this.groupProperty.get());
      entry.setIndicatesDeath(this.indicatesDeathProperty.get());
      if (this.usage() == 0) {
        int actorsNb = this.actorsNbProperty.get();
        entry.setActorsNumber(actorsNb, actorsNb, this.indicatesUnionProperty.get());
        entry.setUnique(this.uniqueProperty.get());
      }
    }

    @Override
    public LifeEventTypeRegistry.RegistryArgs getBuildArgs() {
      return new LifeEventTypeRegistry.RegistryArgs(
          this.groupProperty.get(),
          this.indicatesDeathProperty.get(),
          this.indicatesUnionProperty.get(),
          this.actorsNbProperty.get(),
          this.actorsNbProperty.get(),
          this.uniqueProperty.get()
      );
    }

    public ObjectProperty<LifeEventType.Group> groupProperty() {
      return this.groupProperty;
    }

    public BooleanProperty indicatesDeathProperty() {
      return this.indicatesDeathProperty;
    }

    public BooleanProperty indicatesUnionProperty() {
      return this.indicatesUnionProperty;
    }

    public ObjectProperty<Integer> actorsNbProperty() {
      return this.actorsNbProperty;
    }

    public BooleanProperty uniqueProperty() {
      return this.uniqueProperty;
    }
  }

  private class GenderTableItem extends TableItem<Gender, GenderRegistry.RegistryArgs> {
    private final ObjectProperty<Color> colorProperty = new SimpleObjectProperty<>();

    public GenderTableItem(Gender entry, int usage) {
      super(entry, "genders", usage);
      this.colorProperty.set(entry != null ? Color.valueOf(entry.color()) : Color.BLACK);
    }

    @Override
    public void updateEntry() {
      super.updateEntry();
      this.entry().setColor(this.convertColor());
    }

    @Override
    public GenderRegistry.RegistryArgs getBuildArgs() {
      return new GenderRegistry.RegistryArgs(this.convertColor());
    }

    public ObjectProperty<Color> colorProperty() {
      return this.colorProperty;
    }

    private String convertColor() {
      return StringUtils.colorToCSSHex(this.colorProperty.get());
    }
  }

  private static class NonSortableTableColumn<S, T> extends TableColumn<S, T> {
    public NonSortableTableColumn(String text) {
      super(text);
      this.setSortable(false);
      this.setReorderable(false);
    }
  }

  private static class CheckBoxCell<S extends TableItem<?, ?>, T> extends CheckBoxTableCell<S, T> {
    @Override
    public void updateItem(T item, boolean empty) {
      super.updateItem(item, empty);
      var row = this.getTableRow();
      if (row == null) {
        return;
      }
      var rowItem = row.getItem();
      if (rowItem == null) {
        return;
      }
      var value = rowItem.entry();
      boolean editable = rowItem.usage() == 0 && (value == null || !value.isBuiltin());
      this.setEditable(editable);
      this.pseudoClassStateChanged(PseudoClasses.DISABLED, !editable);
    }
  }
}
