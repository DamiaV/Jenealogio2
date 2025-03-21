package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.beans.property.*;
import javafx.collections.*;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.util.*;
import javafx.util.converter.*;
import net.darmo_creations.jenealogio2.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.config.theme.*;
import net.darmo_creations.jenealogio2.io.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.ui.*;
import net.darmo_creations.jenealogio2.ui.components.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Dialog that allows editing registries.
 */
public class RegistriesDialog extends DialogBase<ButtonType> {
  private final LifeEventTypeRegistryView eventTypesView = new LifeEventTypeRegistryView();
  private final GenderRegistryView gendersView = new GenderRegistryView();

  private final Button exportButton;
  private final Button applyButton;

  private final RegistriesImportExportDialog importDialog;
  private final RegistriesImportExportDialog exportDialog;

  private FamilyTree familyTree;
  private boolean changes;

  // File managers
  private final TreeXMLReader treeXMLReader = new TreeXMLReader();
  private final TreeXMLWriter treeXMLWriter = new TreeXMLWriter();

  /**
   * Create an about dialog.
   *
   * @param config The app’s config.
   */
  public RegistriesDialog(final @NotNull Config config) {
    super(
        config,
        "edit_registries",
        true,
        ButtonTypes.CANCEL,
        ButtonTypes.OK,
        ButtonTypes.APPLY
    );
    final Language language = config.language();
    final Theme theme = config.theme();

    this.importDialog = new RegistriesImportExportDialog(config, true);
    this.exportDialog = new RegistriesImportExportDialog(config, false);

    final Label helpLabel = new Label(language.translate("dialog.edit_registries.help"),
        theme.getIcon(Icon.INFO, Icon.Size.SMALL));
    helpLabel.setWrapText(true);

    final TabPane tabPane = new TabPane(
        this.createTab("life_event_types", this.eventTypesView),
        this.createTab("genders", this.gendersView)
    );
    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    VBox.setVgrow(tabPane, Priority.ALWAYS);

    final Button importButton = new Button(language.translate("dialog.edit_registries.import"),
        theme.getIcon(Icon.IMPORT_REGISTRIES, Icon.Size.SMALL));
    importButton.setOnAction(event -> this.onImport());
    this.exportButton = new Button(language.translate("dialog.edit_registries.export"),
        theme.getIcon(Icon.EXPORT_REGISTRIES, Icon.Size.SMALL));
    this.exportButton.setOnAction(event -> this.onExport());
    final HBox buttonsBox = new HBox(5, importButton, this.exportButton);

    final VBox content = new VBox(5, helpLabel, tabPane, new Separator(), buttonsBox);
    content.setPrefWidth(600);
    content.setPrefHeight(400);
    this.getDialogPane().setContent(content);

    this.applyButton = (Button) this.getDialogPane().lookupButton(ButtonTypes.APPLY);
    this.applyButton.addEventFilter(ActionEvent.ACTION, event -> {
      this.applyChanges();
      event.consume();
    });

    final Stage stage = this.stage();
    stage.setMinWidth(600);
    stage.setMinHeight(500);

    this.setResultConverter(buttonType -> {
      if (buttonType == ButtonTypes.OK)
        this.applyChanges();
      return buttonType;
    });

    this.updateButtons();
  }

  private void applyChanges() {
    this.eventTypesView.applyChanges();
    this.gendersView.applyChanges();
    this.changes = false;
    this.refresh(this.familyTree);
  }

  private void onImport() {
    final var file = FileChoosers.showRegistriesFileChooser(this.config, this.getOwner(), null);
    if (file.isEmpty())
      return;
    final RegistriesValues registries;
    try {
      registries = this.treeXMLReader.loadRegistriesFile(file.get());
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
    this.load(registries.lifeEventTypes(), registries.genders());
  }

  private void load(@NotNull List<LifeEventType> eventTypes, @NotNull List<Gender> genders) {
    if (!this.checkNotEmpty(eventTypes, genders, false))
      return;
    this.importDialog.setItems(eventTypes, genders);
    final Optional<ButtonType> buttonType = this.importDialog.showAndWait();
    if (buttonType.isEmpty() || buttonType.get().getButtonData() != ButtonBar.ButtonData.OK_DONE)
      return;
    final var selectedItems = this.importDialog.getSelectedItems();
    for (final LifeEventType lifeEventType : selectedItems.lifeEventTypes())
      this.eventTypesView.importEntry(lifeEventType);
    for (final Gender gender : selectedItems.genders())
      this.gendersView.importEntry(gender);
  }

  private void onExport() {
    final List<LifeEventType> eventTypes = this.eventTypesView.getExportableEntries();
    final List<Gender> genders = this.gendersView.getExportableEntries();
    if (!this.checkNotEmpty(eventTypes, genders, true))
      return;
    this.exportDialog.setItems(eventTypes, genders);
    final var buttonType = this.exportDialog.showAndWait();
    if (buttonType.isEmpty() || buttonType.get().getButtonData() != ButtonBar.ButtonData.OK_DONE)
      return;
    final var file = FileChoosers.showRegistriesFileSaver(this.config, this.getOwner(), "registries");
    if (file.isEmpty())
      return;
    try {
      this.treeXMLWriter.saveRegistriesToFile(
          file.get(),
          this.familyTree,
          this.exportDialog.getSelectedItems(),
          this.config
      );
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

  private boolean checkNotEmpty(
      @NotNull List<LifeEventType> eventTypes,
      @NotNull List<Gender> genders,
      boolean exporting
  ) {
    if (eventTypes.isEmpty() && genders.isEmpty()) {
      final String key = exporting ? "export" : "import";
      Alerts.warning(
          this.config,
          "alert.nothing_to_%s.header".formatted(key),
          null,
          "alert.nothing_to_%s.title".formatted(key)
      );
      return false;
    }
    return true;
  }

  private Tab createTab(@NotNull String name, final @NotNull RegistryView<?, ?, ?> registryView) {
    final Language language = this.config.language();
    final Tab tab = new Tab(language.translate("dialog.edit_registries.tab.%s.title".formatted(name)));

    final Label description = new Label(
        language.translate("dialog.edit_registries.tab.%s.description".formatted(name)));
    description.setWrapText(true);

    VBox.setVgrow(registryView, Priority.ALWAYS);

    final VBox vBox = new VBox(5, registryView, description);
    vBox.setPadding(new Insets(5));
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
    final boolean invalid = !this.eventTypesView.isValid() || !this.gendersView.isValid();
    this.exportButton.setDisable(invalid || this.changes);
    this.applyButton.setDisable(invalid || !this.changes);
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
      super(5);
      final Language language = RegistriesDialog.this.config.language();
      final Theme theme = RegistriesDialog.this.config.theme();

      // Buttons
      final Button addButton = new Button(language.translate("dialog.edit_registries.add_entry"),
          theme.getIcon(Icon.ADD_ENTRY, Icon.Size.SMALL));
      addButton.setOnAction(event -> this.onCreateEntry());
      this.removeButton = new Button(language.translate("dialog.edit_registries.delete_entry"),
          theme.getIcon(Icon.DELETE_ENTRY, Icon.Size.SMALL));
      this.removeButton.setOnAction(event -> this.onRemoveSelectedItems());
      this.removeButton.setDisable(true);
      this.buttonsBox = new HBox(
          5,
          new Spacer(Orientation.HORIZONTAL),
          addButton,
          this.removeButton
      );

      // Table
      VBox.setVgrow(this.entriesTable, Priority.ALWAYS);
      this.entriesTable.setPlaceholder(new Text(language.translate("table.empty")));
      this.entriesTable.setPrefHeight(0);
      this.entriesTable.setEditable(true);

      final TableColumn<TI, Integer> usageCol = new NonSortableTableColumn<>(
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

      final TableColumn<TI, String> nameCol = new NonSortableTableColumn<>(
          language.translate("dialog.edit_registries.entry_name"));
      nameCol.setEditable(true);
      nameCol.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()) {
        @Override
        public void updateItem(String item, boolean empty) {
          super.updateItem(item, empty);
          updateTableCell(this, false);
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
      if (!entry.isBuiltin())
        this.entriesTable.getItems().add(this.newEntry(entry, 0));
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
      for (final var item : this.entriesTable.getItems()) {
        final RE entry = item.entry();
        if (entry == null) {
          final String label = item.getName();
          this.registry.registerEntry(this.generateKey(), label, item.getBuildArgs());
        } else
          item.updateEntry();
      }
    }

    public RegistryEntryKey generateKey() {
      final Random rng = new Random();
      while (true) {
        final String keyName = String.valueOf(rng.nextInt(1_000_000));
        final RegistryEntryKey key = new RegistryEntryKey(Registry.USER_NS, keyName);
        if (!this.registry.containsKey(key))
          return key;
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
      final Set<TI> toDelete = new HashSet<>();
      for (final var selectedItem : this.entriesTable.getSelectionModel().getSelectedItems()) {
        final RE entry = selectedItem.entry();
        if (entry != null)
          this.entriesToDelete.add(entry);
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
      final ObservableList<TI> selectedItems = this.entriesTable.getSelectionModel().getSelectedItems();
      final boolean disable = selectedItems.isEmpty() || selectedItems.stream()
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
      final Language language = RegistriesDialog.this.config.language();

      final TableColumn<LifeEventTypeTableItem, LifeEventType.Group> groupCol = new NonSortableTableColumn<>(
          language.translate("dialog.edit_registries.tab.life_event_types.group"));
      groupCol.setEditable(true);
      groupCol.setCellFactory(param -> {
        final ComboBoxTableCell<LifeEventTypeTableItem, LifeEventType.Group> comboBoxTableCell =
            new ComboBoxTableCell<>(LifeEventType.Group.values()) {
              @Override
              public void updateItem(LifeEventType.Group item, boolean empty) {
                super.updateItem(item, empty);
                this.setText(empty ? "" : language.translate("life_event_type_group." + item.name().toLowerCase()));
                updateTableCell(this, false);
              }
            };
        comboBoxTableCell.setConverter(new StringConverter<>() {
          @Override
          public String toString(LifeEventType.Group group) {
            return language.translate("life_event_type_group." + group.name().toLowerCase());
          }

          @Override
          public LifeEventType.Group fromString(String s) {
            return null; // No need to bother, combobox is not editable
          }
        });
        return comboBoxTableCell;
      });
      groupCol.setCellValueFactory(param -> param.getValue().groupProperty());

      final TableColumn<LifeEventTypeTableItem, Boolean> deathCol = new NonSortableTableColumn<>(
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

      final TableColumn<LifeEventTypeTableItem, Boolean> unionCol = new NonSortableTableColumn<>(
          language.translate("dialog.edit_registries.tab.life_event_types.indication_type.union"));
      unionCol.setEditable(true);
      unionCol.setCellFactory(param -> new CheckBoxCell<>() {
        @Override
        public void updateItem(Boolean item, boolean empty) {
          super.updateItem(item, empty);
          if (this.isEditable()) {
            final var row = this.getTableRow();
            if (row == null)
              return;
            final var rowItem = row.getItem();
            if (rowItem == null)
              return;
            final boolean editable = rowItem.actorsNbProperty().get() >= 2;
            this.setEditable(editable);
            if (!editable)
              rowItem.indicatesUnionProperty().set(false);
          }
        }
      });
      unionCol.setCellValueFactory(param -> param.getValue().indicatesUnionProperty());

      final TableColumn<LifeEventTypeTableItem, Integer> actorsNbCol = new NonSortableTableColumn<>(
          language.translate("dialog.edit_registries.tab.life_event_types.actors_nb"));
      actorsNbCol.setEditable(true);
      actorsNbCol.setCellFactory(param -> new ComboBoxTableCell<>(1, 2) {
        @Override
        public void updateItem(Integer item, boolean empty) {
          super.updateItem(item, empty);
          updateTableCell(this, true);
        }

        @Override
        public void commitEdit(Integer newValue) {
          super.commitEdit(newValue);
          this.getTableView().refresh(); // Force table refresh to update "editable" state of union checkbox
        }
      });
      actorsNbCol.setCellValueFactory(param -> param.getValue().actorsNbProperty());

      final TableColumn<LifeEventTypeTableItem, Boolean> uniqueCol = new NonSortableTableColumn<>(
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
      final Map<LifeEventType, List<LifeEvent>> eventTypesUsage = familyTree.lifeEvents().stream()
          .collect(Collectors.groupingBy(LifeEvent::type));
      for (final LifeEventType entry : this.registry.serializableEntries()) {
        final int usage = eventTypesUsage.getOrDefault(entry, List.of()).size();
        this.entriesTable.getItems().add(new LifeEventTypeTableItem(entry, usage, false));
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
    public GenderRegistryView() {
      final Config config = RegistriesDialog.this.config;
      final Language language = config.language();

      final TableColumn<GenderTableItem, Image> iconCol = new NonSortableTableColumn<>(
          language.translate("dialog.edit_registries.tab.genders.icon"));
      iconCol.setEditable(true);
      iconCol.setCellFactory(param -> new ImagePickerTableCell<>(() -> {
        final var path = FileChoosers.showFileChooser(
            config,
            RegistriesDialog.this.stage(),
            "image_file",
            ".png",
            ".jpg",
            ".jpeg"
        );
        if (path.isPresent()) {
          final Image image = new Image("file://" + path.get());
          if (image.getWidth() > 16 || image.getHeight() > 16) {
            Alerts.warning(
                config,
                "alert.invalid_icon_image.header",
                null,
                null
            );
            return Optional.empty();
          }
          return Optional.of(image);
        }
        return Optional.empty();
      }));
      iconCol.setCellValueFactory(param -> param.getValue().iconProperty());

      this.entriesTable.getColumns().add(iconCol);
    }

    @Override
    public void refresh(final @NotNull FamilyTree familyTree) {
      super.refresh(familyTree);
      this.registry = familyTree.genderRegistry();
      final Map<Gender, Set<Person>> gendersUsage = new HashMap<>();
      final BiConsumer<Person, Optional<Gender>> updateMap = (person, gender) ->
          gender.ifPresent(g -> {
            if (!gendersUsage.containsKey(g))
              gendersUsage.put(g, new HashSet<>());
            gendersUsage.get(g).add(person);
          });
      for (final Person person : familyTree.persons()) {
        updateMap.accept(person, person.assignedGenderAtBirth());
        updateMap.accept(person, person.gender());
      }
      for (final Gender entry : this.registry.serializableEntries()) {
        final int usage = gendersUsage.getOrDefault(entry, Set.of()).size();
        this.entriesTable.getItems().add(new GenderTableItem(entry, usage, false));
      }
    }

    @Override
    protected GenderTableItem newEntry(Gender entry, int usage) {
      return new GenderTableItem(entry, usage, true);
    }

    @Override
    public void importEntry(@NotNull Gender entry) {
      if (entry.isBuiltin())
        for (int i = 0; i < this.entriesTable.getItems().size(); i++) {
          final GenderTableItem item = this.entriesTable.getItems().get(i);
          if (item.entry().key().equals(entry.key())) {
            item.iconProperty().set(entry.icon());
            break;
          }
        }
      else super.importEntry(entry);
    }
  }

  private abstract class TableItem<E extends RegistryEntry, A> {
    private final E entry;
    private final int usage;
    private final StringProperty nameProperty = new SimpleStringProperty();

    private TableItem(E entry, String registryName, int usage, boolean isNew) {
      this.usage = usage;
      this.entry = isNew ? null : entry;
      final String label;
      if (entry != null && entry.isBuiltin())
        label = RegistriesDialog.this.config.language().translate(registryName + "." + entry.key().name());
      else if (entry != null)
        label = Objects.requireNonNull(entry.userDefinedName());
      else
        label = "";
      this.nameProperty.set(label);
      this.nameProperty.addListener((observable, oldValue, newValue) -> {
        RegistriesDialog.this.changes = true;
        RegistriesDialog.this.updateButtons();
      });
    }

    public void updateEntry() {
      if (!this.entry.isBuiltin())
        this.entry.setUserDefinedName(this.getName());
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
      final LifeEventType entry = this.entry();
      if (entry.isBuiltin())
        return;
      entry.setGroup(this.groupProperty.get());
      entry.setIndicatesDeath(this.indicatesDeathProperty.get());
      if (this.usage() == 0) {
        final int actorsNb = this.actorsNbProperty.get();
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
    private final ObjectProperty<Image> iconProperty = new SimpleObjectProperty<>();

    public GenderTableItem(Gender entry, int usage, boolean isNew) {
      super(entry, "genders", usage, isNew);
      this.iconProperty.set(entry != null ? entry.icon() : null);
      this.iconProperty.addListener((observable, oldValue, newValue) -> {
        RegistriesDialog.this.changes = true;
        RegistriesDialog.this.updateButtons();
      });
    }

    @Override
    public void updateEntry() {
      super.updateEntry();
      this.entry().setIcon(this.iconProperty().get());
    }

    @Override
    public boolean isValid() {
      return super.isValid() && this.iconProperty().get() != null;
    }

    @Override
    public GenderRegistry.RegistryArgs getBuildArgs() {
      return new GenderRegistry.RegistryArgs(this.iconProperty().get());
    }

    public ObjectProperty<Image> iconProperty() {
      return this.iconProperty;
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
      updateTableCell(this, true);
    }
  }

  private static <S extends TableItem<?, ?>> void updateTableCell(
      @NotNull TableCell<S, ?> cell,
      boolean disableIfInUse
  ) {
    final TableRow<S> row = cell.getTableRow();
    if (row == null)
      return;
    final S rowItem = row.getItem();
    if (rowItem == null)
      return;
    final RegistryEntry value = rowItem.entry();
    final boolean editable = (!disableIfInUse || rowItem.usage() == 0) && (value == null || !value.isBuiltin());
    cell.setEditable(editable);
    cell.pseudoClassStateChanged(PseudoClasses.DISABLED, !editable);
  }
}
