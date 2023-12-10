package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.beans.property.*;
import javafx.collections.*;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.util.converter.*;
import net.darmo_creations.jenealogio2.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.io.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.themes.*;
import net.darmo_creations.jenealogio2.ui.*;
import net.darmo_creations.jenealogio2.ui.components.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.util.*;
import java.util.stream.*;

/**
 * Dialog that allows editing registries.
 */
public class RegistriesDialog extends DialogBase<ButtonType> {
  private final LifeEventTypeRegistryView eventTypesView = new LifeEventTypeRegistryView();
  private final GenderRegistryView gendersView = new GenderRegistryView();

  private final Button exportButton;
  private final Button applyButton;

  private final RegistriesImportExportDialog importDialog = new RegistriesImportExportDialog(true);
  private final RegistriesImportExportDialog exportDialog = new RegistriesImportExportDialog(false);

  private FamilyTree familyTree;
  private boolean changes;

  // File managers
  private final TreeFileReader treeFileReader = new TreeFileReader();
  private final TreeXMLReader treeXMLReader = new TreeXMLReader();
  private final TreeXMLWriter treeXMLWriter = new TreeXMLWriter();

  /**
   * Create an about dialog.
   */
  public RegistriesDialog() {
    super("edit_registries", true, ButtonTypes.CANCEL, ButtonTypes.OK, ButtonTypes.APPLY);

    Language language = App.config().language();
    Theme theme = App.config().theme();

    Label helpLabel = new Label(language.translate("dialog.edit_registries.help"),
        theme.getIcon(Icon.INFO, Icon.Size.SMALL));
    helpLabel.setWrapText(true);

    TabPane tabPane = new TabPane(
        this.createTab("life_event_types", this.eventTypesView),
        this.createTab("genders", this.gendersView)
    );
    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    VBox.setVgrow(tabPane, Priority.ALWAYS);

    Button importButton = new Button(language.translate("dialog.edit_registries.import"),
        theme.getIcon(Icon.IMPORT_REGISTRIES, Icon.Size.SMALL));
    importButton.setOnAction(event -> this.onImport());
    Button importFromTreeButton = new Button(language.translate("dialog.edit_registries.import_from_tree"),
        theme.getIcon(Icon.IMPORT_REGISTRIES_FROM_TREE, Icon.Size.SMALL));
    importFromTreeButton.setOnAction(event -> this.onImportFromTree());
    this.exportButton = new Button(language.translate("dialog.edit_registries.export"),
        theme.getIcon(Icon.EXPORT_REGISTRIES, Icon.Size.SMALL));
    this.exportButton.setOnAction(event -> this.onExport());
    HBox buttonsBox = new HBox(4, importButton, importFromTreeButton, this.exportButton);

    VBox content = new VBox(4, helpLabel, tabPane, new Separator(), buttonsBox);
    content.setPrefWidth(600);
    content.setPrefHeight(400);
    this.getDialogPane().setContent(content);

    this.applyButton = (Button) this.getDialogPane().lookupButton(ButtonTypes.APPLY);
    this.applyButton.addEventFilter(ActionEvent.ACTION, event -> {
      this.applyChanges();
      event.consume();
    });

    Stage stage = this.stage();
    stage.setMinWidth(600);
    stage.setMinHeight(500);

    this.setResultConverter(buttonType -> {
      if (buttonType == ButtonTypes.OK) {
        this.applyChanges();
      }
      return buttonType;
    });

    this.updateButtons();
  }

  private void applyChanges() {
    this.eventTypesView.applyChanges();
    this.gendersView.applyChanges();
    this.changes = false;
    this.updateButtons();
  }

  private void onImport() {
    Optional<File> file = FileChoosers.showRegistriesFileChooser(this.getOwner(), null);
    if (file.isEmpty()) {
      return;
    }
    TreeXMLManager.RegistriesWrapper registries;
    try {
      registries = this.treeXMLReader.loadRegistriesFile(file.get());
    } catch (IOException e) {
      App.LOGGER.exception(e);
      Alerts.error(
          "alert.load_error.header",
          "alert.load_error.content",
          "alert.load_error.title",
          new FormatArg("trace", e.getMessage())
      );
      return;
    }
    List<LifeEventType> eventTypes = registries.lifeEventTypes();
    List<Gender> genders = registries.genders();
    if (!this.checkNotEmpty(eventTypes, genders, false)) {
      return;
    }
    this.importDialog.setItems(eventTypes, genders);
    Optional<ButtonType> buttonType = this.importDialog.showAndWait();
    if (buttonType.isEmpty() || buttonType.get().getButtonData() != ButtonBar.ButtonData.OK_DONE) {
      return;
    }
    this.addItems(this.importDialog.getSelectedItems());
  }

  private void onImportFromTree() {
    Optional<File> file = FileChoosers.showTreeFileChooser(this.getOwner(), null);
    if (file.isEmpty()) {
      return;
    }
    FamilyTree familyTree;
    try {
      familyTree = this.treeFileReader.loadFile(file.get());
    } catch (IOException e) {
      App.LOGGER.exception(e);
      Alerts.error(
          "alert.load_error.header",
          "alert.load_error.content",
          "alert.load_error.title",
          new FormatArg("trace", e.getMessage())
      );
      return;
    }
    List<LifeEventType> eventTypes = familyTree.lifeEventTypeRegistry().serializableEntries();
    List<Gender> genders = familyTree.genderRegistry().serializableEntries();
    if (!this.checkNotEmpty(eventTypes, genders, false)) {
      return;
    }
    this.importDialog.setItems(eventTypes, genders);
    Optional<ButtonType> buttonType = this.importDialog.showAndWait();
    if (buttonType.isEmpty() || buttonType.get().getButtonData() != ButtonBar.ButtonData.OK_DONE) {
      return;
    }
    this.addItems(this.importDialog.getSelectedItems());
  }

  private void addItems(@NotNull Pair<List<LifeEventType>, List<Gender>> selectedItems) {
    for (LifeEventType lifeEventType : selectedItems.left()) {
      this.eventTypesView.importEntry(lifeEventType);
    }
    for (Gender gender : selectedItems.right()) {
      this.gendersView.importEntry(gender);
    }
  }

  private void onExport() {
    List<LifeEventType> eventTypes = this.eventTypesView.getExportableEntries();
    List<Gender> genders = this.gendersView.getExportableEntries();
    if (!this.checkNotEmpty(eventTypes, genders, true)) {
      return;
    }
    this.exportDialog.setItems(eventTypes, genders);
    Optional<ButtonType> buttonType = this.exportDialog.showAndWait();
    if (buttonType.isEmpty() || buttonType.get().getButtonData() != ButtonBar.ButtonData.OK_DONE) {
      return;
    }
    Optional<File> file = FileChoosers.showRegistriesFileSaver(this.getOwner(), "registries");
    if (file.isEmpty()) {
      return;
    }
    var selectedItems = this.exportDialog.getSelectedItems();
    var selectedKeys = new Pair<>(
        selectedItems.left().stream().map(RegistryEntry::key).toList(),
        selectedItems.right().stream().map(RegistryEntry::key).toList()
    );
    try {
      this.treeXMLWriter.saveRegistriesToFile(file.get(), this.familyTree, selectedKeys);
    } catch (IOException e) {
      App.LOGGER.exception(e);
      Alerts.error(
          "alert.save_error.header",
          "alert.save_error.content",
          "alert.save_error.title",
          new FormatArg("trace", e.getMessage())
      );
    }
  }

  private boolean checkNotEmpty(@NotNull List<LifeEventType> eventTypes, @NotNull List<Gender> genders, boolean exporting) {
    if (eventTypes.isEmpty() && genders.isEmpty()) {
      String key = exporting ? "export" : "import";
      Alerts.warning("alert.nothing_to_%s.header".formatted(key), null, "alert.nothing_to_%s.title".formatted(key));
      return false;
    }
    return true;
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
    this.familyTree = familyTree;
    this.eventTypesView.refresh(familyTree);
    this.gendersView.refresh(familyTree);
    this.changes = false;
    this.updateButtons();
  }

  private void updateButtons() {
    boolean invalid = !this.eventTypesView.isValid() || !this.gendersView.isValid();
    this.exportButton.setDisable(invalid || this.changes);
    this.applyButton.setDisable(!this.changes);
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

      // Buttons
      Button addButton = new Button(language.translate("dialog.edit_registries.add_entry"),
          theme.getIcon(Icon.ADD_ENTRY, Icon.Size.SMALL));
      addButton.setOnAction(event -> this.onCreateEntry());
      this.removeButton = new Button(language.translate("dialog.edit_registries.delete_entry"),
          theme.getIcon(Icon.DELETE_ENTRY, Icon.Size.SMALL));
      this.removeButton.setOnAction(event -> this.onRemoveSelectedItems());
      this.removeButton.setDisable(true);
      Pane spacer = new Pane();
      HBox.setHgrow(spacer, Priority.ALWAYS);
      this.buttonsBox = new HBox(4, spacer, addButton, this.removeButton);

      // Table
      VBox.setVgrow(this.entriesTable, Priority.ALWAYS);
      this.entriesTable.setPlaceholder(new Text(language.translate("dialog.edit_registries.entries_table.empty")));
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

      this.getChildren().addAll(this.buttonsBox, this.entriesTable);
    }

    /**
     * Refresh the entries list.
     */
    public void refresh(final @NotNull FamilyTree familyTree) {
      this.entriesTable.getItems().clear();
      this.entriesToDelete.clear();
    }

    /**
     * Add an entry to the table with a usage of 0.
     * The actual entry object is copied and will have a new key.
     *
     * @param entry Entry to add.
     */
    public void importEntry(@NotNull RE entry) {
      if (!entry.isBuiltin()) {
        this.entriesTable.getItems().add(this.newEntry(entry, 0));
      }
    }

    /**
     * The list of exportable entries.
     */
    public List<RE> getExportableEntries() {
      return this.registry.serializableEntries();
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
          this.registry.registerEntry(this.generateKey(), label, item.getBuildArgs());
        } else {
          item.updateEntry();
        }
      }
    }

    public RegistryEntryKey generateKey() {
      Random rng = new Random();
      while (true) {
        String keyName = String.valueOf(rng.nextInt(1_000_000));
        RegistryEntryKey key = new RegistryEntryKey(Registry.USER_NS, keyName);
        if (!this.registry.containsKey(key)) {
          return key;
        }
      }
    }

    /**
     * Add a new entry to {@link #entriesTable}.
     */
    private void onCreateEntry() {
      this.entriesTable.getItems().add(this.newEntry(null, 0));
      this.entriesTable.getSelectionModel().select(this.entriesTable.getItems().size() - 1);
      RegistriesDialog.this.changes = true;
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
      RegistriesDialog.this.changes = true;
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
          this.entriesTable.getItems().add(new LifeEventTypeTableItem(entry, usage, false));
        }
      }
    }

    @Override
    protected LifeEventTypeTableItem newEntry(LifeEventType entry, int usage) {
      return new LifeEventTypeTableItem(entry, usage, true);
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
        this.entriesTable.getItems().add(new GenderTableItem(entry, usage, false));
      }
    }

    @Override
    protected GenderTableItem newEntry(Gender entry, int usage) {
      return new GenderTableItem(entry, usage, true);
    }

    @Override
    public void importEntry(@NotNull Gender entry) {
      if (entry.isBuiltin()) {
        for (int i = 0; i < this.entriesTable.getItems().size(); i++) {
          GenderTableItem item = this.entriesTable.getItems().get(i);
          if (item.entry().key().equals(entry.key())) {
            item.colorProperty().set(Color.valueOf(entry.color()));
            break;
          }
        }
      } else {
        super.importEntry(entry);
      }
    }
  }

  private abstract class TableItem<E extends RegistryEntry, A> {
    private final E entry;
    private final int usage;
    private final StringProperty nameProperty = new SimpleStringProperty();

    private TableItem(E entry, String registryName, int usage, boolean isNew) {
      this.usage = usage;
      this.entry = isNew ? null : entry;
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
      this.nameProperty.addListener((observable, oldValue, newValue) -> {
        RegistriesDialog.this.changes = true;
        RegistriesDialog.this.updateButtons();
      });
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

    public LifeEventTypeTableItem(LifeEventType entry, int usage, boolean isNew) {
      super(entry, "life_event_types", usage, isNew);
      this.groupProperty.set(entry != null ? entry.group() : LifeEventType.Group.LIFESPAN);
      this.groupProperty.addListener((observable, oldValue, newValue) -> {
        RegistriesDialog.this.changes = true;
        RegistriesDialog.this.updateButtons();
      });
      this.indicatesDeathProperty.set(entry != null && entry.indicatesDeath());
      this.indicatesDeathProperty.addListener((observable, oldValue, newValue) -> {
        RegistriesDialog.this.changes = true;
        RegistriesDialog.this.updateButtons();
      });
      this.indicatesUnionProperty.set(entry != null && entry.indicatesUnion());
      this.indicatesUnionProperty.addListener((observable, oldValue, newValue) -> {
        RegistriesDialog.this.changes = true;
        RegistriesDialog.this.updateButtons();
      });
      this.actorsNbProperty.set(entry != null ? entry.minActors() : 1);
      this.actorsNbProperty.addListener((observable, oldValue, newValue) -> {
        RegistriesDialog.this.changes = true;
        RegistriesDialog.this.updateButtons();
      });
      this.uniqueProperty.set(entry != null && entry.isUnique());
      this.uniqueProperty.addListener((observable, oldValue, newValue) -> {
        RegistriesDialog.this.changes = true;
        RegistriesDialog.this.updateButtons();
      });
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

    public GenderTableItem(Gender entry, int usage, boolean isNew) {
      super(entry, "genders", usage, isNew);
      this.colorProperty.set(entry != null ? Color.valueOf(entry.color()) : Color.BLACK);
      this.colorProperty.addListener((observable, oldValue, newValue) -> {
        RegistriesDialog.this.changes = true;
        RegistriesDialog.this.updateButtons();
      });
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
