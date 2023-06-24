package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import net.darmo_creations.jenealogio2.App;
import net.darmo_creations.jenealogio2.config.Config;
import net.darmo_creations.jenealogio2.config.Language;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.themes.Icon;
import net.darmo_creations.jenealogio2.themes.Theme;
import net.darmo_creations.jenealogio2.ui.components.NotNullComboBoxItem;
import net.darmo_creations.jenealogio2.utils.FormatArg;
import net.darmo_creations.jenealogio2.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Dialog that allows editing registries.
 */
@SuppressWarnings("unused")
public class RegistriesDialog extends DialogBase<ButtonType> {
  @FXML
  private VBox lifeEventTypesVBox;
  @FXML
  private VBox gendersVBox;

  private final RegistryView<LifeEventType, LifeEventType.RegistryArgs, LifeEventTypeRegistryViewEntry> eventTypesView =
      new LifeEventTypeRegistryView(Registries.LIFE_EVENT_TYPES);
  private final RegistryView<Gender, String, GenderRegistryViewEntry> gendersView =
      new GenderRegistryView(Registries.GENDERS);

  /**
   * Create an about dialog.
   */
  public RegistriesDialog() {
    super("edit_registries", true, ButtonTypes.CANCEL, ButtonTypes.OK);

    VBox.setVgrow(this.eventTypesView, Priority.ALWAYS);
    this.lifeEventTypesVBox.getChildren().add(0, this.eventTypesView);
    VBox.setVgrow(this.gendersView, Priority.ALWAYS);
    this.gendersVBox.getChildren().add(0, this.gendersView);

    Stage stage = this.stage();
    stage.setMinWidth(800);
    stage.setMinHeight(500);

    this.setResultConverter(buttonType -> {
      if (buttonType == ButtonTypes.OK) {
        this.eventTypesView.applyChanges();
        this.gendersView.applyChanges();
      }
      return buttonType;
    });
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
   * @param <VE> Type of registry entries wrapper.
   */
  private abstract class RegistryView<RE extends RegistryEntry, A, VE extends RegistryViewEntry<RE>>
      extends VBox {
    private final Button removeButton;
    protected final ListView<VE> entriesList = new ListView<>();

    protected final Registry<RE, A> registry;
    private final Set<RE> entriesToDelete = new HashSet<>();

    /**
     * Show all entries of the given registry in a {@link ListView}.
     *
     * @param registry The registry to display the entries of.
     */
    protected RegistryView(@NotNull Registry<RE, A> registry) {
      super(4);
      this.registry = registry;
      Pane spacer = new Pane();
      HBox.setHgrow(spacer, Priority.ALWAYS);
      Config config = App.config();
      Language language = config.language();
      Theme theme = config.theme();
      Button addButton = new Button(language.translate("dialog.edit_registries.add_entry"),
          theme.getIcon(Icon.ADD_ENTRY, Icon.Size.SMALL));
      addButton.setOnAction(event -> this.onAddItem());
      this.removeButton = new Button(language.translate("dialog.edit_registries.delete_entry"),
          theme.getIcon(Icon.DELETE_ENTRY, Icon.Size.SMALL));
      this.removeButton.setOnAction(event -> this.onRemoveSelectedItems());
      HBox buttonsBox = new HBox(4, spacer, addButton, this.removeButton);
      VBox.setVgrow(this.entriesList, Priority.ALWAYS);
      this.entriesList.setPrefHeight(0);
      this.getChildren().addAll(buttonsBox, this.entriesList);

      this.entriesList.getSelectionModel().selectedItemProperty()
          .addListener((observable, oldValue, newValue) -> this.onSelectionChange());
      this.removeButton.setDisable(true);
    }

    /**
     * Refresh the entries list.
     */
    public void refresh(final @NotNull FamilyTree familyTree) {
      this.entriesList.getItems().clear();
      this.entriesToDelete.clear();
    }

    /**
     * Indicate whether all entries in this view are valid.
     */
    public boolean isValid() {
      return this.entriesList.getItems().stream().allMatch(RegistryViewEntry::isValid);
    }

    /**
     * Apply all changes to the wrapped registry.
     */
    public void applyChanges() {
      this.entriesToDelete.forEach(this.registry::removeEntry);
      for (VE item : this.entriesList.getItems()) {
        Optional<RE> entry = item.entry();
        if (entry.isEmpty()) {
          String label = item.label();
          boolean ok;
          Random rng = new Random();
          do {
            String keyName = String.valueOf(rng.nextInt(1_000_000));
            RegistryEntryKey key = new RegistryEntryKey(Registry.USER_NS, keyName);
            try {
              this.registry.registerEntry(key, label, this.getBuildArgs(item));
              ok = true;
            } catch (IllegalArgumentException e) {
              ok = false; // Key is already used, try another one
            }
          } while (!ok);
        } else {
          this.applyChange(item, entry.get());
        }
      }
    }

    protected abstract A getBuildArgs(final @NotNull VE item);

    protected void applyChange(final @NotNull VE item, @NotNull RE entry) {
      if (!entry.isBuiltin()) {
        entry.setUserDefinedName(item.label());
      }
    }

    /**
     * Add a new entry to {@link #entriesList}.
     */
    private void onAddItem() {
      VE item = this.newEntry().apply(null, 0);
      this.entriesList.getItems().add(item);
      this.entriesList.getSelectionModel().select(item);
      RegistriesDialog.this.updateButtons();
    }

    protected abstract BiFunction<RE, Integer, VE> newEntry();

    /**
     * Remove all selected items and stage them for deletion from the registry.
     */
    private void onRemoveSelectedItems() {
      Set<VE> toDelete = new HashSet<>();
      for (VE selectedItem : this.entriesList.getSelectionModel().getSelectedItems()) {
        selectedItem.entry().ifPresent(this.entriesToDelete::add);
        toDelete.add(selectedItem);
      }
      this.entriesList.getItems().removeAll(toDelete);
      RegistriesDialog.this.updateButtons();
    }

    /**
     * Called when the selection of {@link #entriesList} changes.
     */
    private void onSelectionChange() {
      // Prevent deleting builtin and used entries
      boolean anyInvalid = this.entriesList.getSelectionModel().getSelectedItems().stream()
          .anyMatch(i -> i.entry().map(RegistryEntry::isBuiltin).orElse(false) || i.usage() != 0);
      this.removeButton.setDisable(anyInvalid);
    }
  }

  /**
   * This class allows editing {@link Registries#LIFE_EVENT_TYPES}.
   */
  private class LifeEventTypeRegistryView
      extends RegistryView<LifeEventType, LifeEventType.RegistryArgs, LifeEventTypeRegistryViewEntry> {
    public LifeEventTypeRegistryView(@NotNull Registry<LifeEventType, LifeEventType.RegistryArgs> registry) {
      super(registry);
    }

    @Override
    public void refresh(final @NotNull FamilyTree familyTree) {
      super.refresh(familyTree);
      Map<LifeEventType, List<LifeEvent>> eventTypesUsage = familyTree.lifeEvents().stream()
          .collect(Collectors.groupingBy(LifeEvent::type));
      for (LifeEventType entry : this.registry.entries()) {
        if (!entry.isBuiltin()) {
          int usage = eventTypesUsage.getOrDefault(entry, List.of()).size();
          this.entriesList.getItems().add(new LifeEventTypeRegistryViewEntry(entry, usage));
        }
      }
      this.entriesList.getItems().sort(Comparator.comparing(RegistryViewEntry::label));
    }

    @Override
    protected BiFunction<LifeEventType, Integer, LifeEventTypeRegistryViewEntry> newEntry() {
      return LifeEventTypeRegistryViewEntry::new;
    }

    @Override
    protected LifeEventType.RegistryArgs getBuildArgs(final @NotNull LifeEventTypeRegistryViewEntry item) {
      int actorsNb = item.actorsNb();
      return new LifeEventType.RegistryArgs(
          item.group(),
          item.indicatesDeath(),
          item.indicatesUnion(),
          actorsNb,
          actorsNb,
          item.isUnique()
      );
    }

    @Override
    protected void applyChange(final @NotNull LifeEventTypeRegistryViewEntry item, @NotNull LifeEventType entry) {
      super.applyChange(item, entry);
      entry.setGroup(item.group());
      entry.setIndicatesDeath(item.indicatesDeath());
      entry.setIndicatesUnion(item.indicatesUnion());
      if (item.usage() == 0) {
        entry.setMaxActors(item.actorsNb());
        entry.setMinActors(item.actorsNb());
        entry.setUnique(item.isUnique());
      }
    }
  }

  /**
   * This class allows editing {@link Registries#GENDERS}.
   */
  private class GenderRegistryView
      extends RegistryView<Gender, String, GenderRegistryViewEntry> {
    public GenderRegistryView(@NotNull Registry<Gender, String> registry) {
      super(registry);
    }

    @Override
    public void refresh(final @NotNull FamilyTree familyTree) {
      super.refresh(familyTree);
      //noinspection OptionalGetWithoutIsPresent
      Map<Gender, List<Person>> gendersUsage = familyTree.persons().stream()
          .filter(person -> person.gender().isPresent())
          .collect(Collectors.groupingBy(person -> person.gender().get()));
      for (Gender entry : this.registry.entries()) {
        int usage = gendersUsage.getOrDefault(entry, List.of()).size();
        this.entriesList.getItems().add(new GenderRegistryViewEntry(entry, usage));
      }
      this.entriesList.getItems().sort(Comparator.comparing(RegistryViewEntry::label));
    }

    @Override
    protected BiFunction<Gender, Integer, GenderRegistryViewEntry> newEntry() {
      return GenderRegistryViewEntry::new;
    }

    @Override
    protected String getBuildArgs(final @NotNull GenderRegistryViewEntry item) {
      return item.color();
    }

    @Override
    protected void applyChange(final @NotNull GenderRegistryViewEntry item, @NotNull Gender entry) {
      super.applyChange(item, entry);
      entry.setColor(item.color());
    }
  }

  /**
   * Base class for entries of {@link RegistryView}. Wraps a single {@link RegistryEntry}.
   *
   * @param <E> Type of wrapped registry entry.
   */
  private abstract class RegistryViewEntry<E extends RegistryEntry> extends HBox {
    private final E entry;
    private final int usage;

    private final TextField nameField;

    /**
     * Create a {@link RegistryEntry} wrapper.
     *
     * @param entry          Entry to wrap. Null for new entries.
     * @param translationKey Translation key prefix for builtin entries.
     * @param usage          Number of times the entry is being used.
     */
    protected RegistryViewEntry(E entry, @NotNull String translationKey, int usage) {
      super(10);
      this.entry = entry;
      this.usage = usage;
      Language language = App.config().language();
      this.setAlignment(Pos.CENTER_LEFT);
      String label = null;
      boolean builtin = entry != null && entry.isBuiltin();
      if (builtin) {
        label = language.translate(translationKey + "." + entry.key().name());
      } else if (entry != null) {
        label = Objects.requireNonNull(entry.userDefinedName());
      }
      this.nameField = new TextField(label);
      this.nameField.setDisable(builtin);
      this.nameField.textProperty()
          .addListener((observable, oldValue, newValue) -> RegistriesDialog.this.updateButtons());
      String text = language.translate("dialog.edit_registries.entry_usage", new FormatArg("nb", usage));
      this.getChildren().addAll(
          new Label(language.translate("dialog.edit_registries.entry_name")),
          this.nameField,
          new Label(text)
      );
    }

    /**
     * The wrapped entry.
     */
    public Optional<E> entry() {
      return Optional.ofNullable(this.entry);
    }

    /**
     * Check whether the form is valid.
     */
    public boolean isValid() {
      return StringUtils.stripNullable(this.nameField.getText()).isPresent();
    }

    /**
     * The number of times the wrapped entry is used.
     */
    public int usage() {
      return this.usage;
    }

    /**
     * Entry’s label.
     */
    public String label() {
      return StringUtils.stripNullable(this.nameField.getText()).orElseThrow();
    }
  }

  /**
   * This class wraps a single entry of {@link Registries#LIFE_EVENT_TYPES}.
   */
  private class LifeEventTypeRegistryViewEntry extends RegistryViewEntry<LifeEventType> {
    private final ComboBox<NotNullComboBoxItem<LifeEventType.Group>> groupCombo = new ComboBox<>();
    private final CheckBox indicatesDeathCheckBox = new CheckBox();
    private final CheckBox indicatesUnionCheckBox = new CheckBox();
    private final ComboBox<Integer> actorsNbCombo = new ComboBox<>();
    private final CheckBox isUniqueCheckBox = new CheckBox();

    /**
     * Create a wrapper for a {@link LifeEventType}.
     *
     * @param entry Entry to wrap. Null for new entries.
     * @param usage Number of times the entry is used.
     */
    public LifeEventTypeRegistryViewEntry(LifeEventType entry, int usage) {
      super(entry, "life_event_type", usage);
      Language language = App.config().language();
      Node usageLabel = this.getChildren().remove(this.getChildren().size() - 1);
      this.getChildren().addAll(
          new Label(language.translate("dialog.edit_registries.tab.life_event_types.group")),
          this.groupCombo,
          this.indicatesDeathCheckBox,
          new Label(language.translate("dialog.edit_registries.tab.life_event_types.indication_type.death")),
          this.indicatesUnionCheckBox,
          new Label(language.translate("dialog.edit_registries.tab.life_event_types.indication_type.union")),
          new Label(language.translate("dialog.edit_registries.tab.life_event_types.actors_nb")),
          this.actorsNbCombo,
          this.isUniqueCheckBox,
          new Label(language.translate("dialog.edit_registries.tab.life_event_types.unique")),
          usageLabel
      );

      for (LifeEventType.Group group : LifeEventType.Group.values()) {
        String key = "life_event_type_group." + group.name().toLowerCase();
        this.groupCombo.getItems().add(new NotNullComboBoxItem<>(group, language.translate(key)));
      }
      if (entry != null) {
        this.groupCombo.getSelectionModel().select(new NotNullComboBoxItem<>(entry.group()));
      } else {
        this.groupCombo.getSelectionModel().select(0);
      }

      this.indicatesDeathCheckBox.setSelected(entry != null && entry.indicatesDeath());
      this.indicatesUnionCheckBox.setSelected(entry != null && entry.indicatesUnion());
      this.indicatesUnionCheckBox.setDisable(usage != 0);

      this.actorsNbCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
        boolean oneActor = newValue == 1;
        this.indicatesUnionCheckBox.setDisable(oneActor);
        if (oneActor) {
          this.indicatesUnionCheckBox.setSelected(false);
        }
      });
      this.actorsNbCombo.getItems().addAll(1, 2);
      // Explicit cast is necessary to select by value instead of index
      this.actorsNbCombo.getSelectionModel().select((Integer) (entry != null ? entry.minActors() : 1));
      this.actorsNbCombo.setDisable(usage != 0);
      this.isUniqueCheckBox.setSelected(entry != null && entry.isUnique());
      this.isUniqueCheckBox.setDisable(usage != 0);
    }

    /**
     * Entry’s group.
     */
    public LifeEventType.Group group() {
      return this.groupCombo.getSelectionModel().getSelectedItem().data();
    }

    /**
     * Whether the entry indicates death.
     */
    public boolean indicatesDeath() {
      return this.indicatesDeathCheckBox.isSelected();
    }

    /**
     * Whether the entry indicates a union.
     */
    public boolean indicatesUnion() {
      return this.indicatesUnionCheckBox.isSelected();
    }

    /**
     * Entry’s number of actors.
     */
    public int actorsNb() {
      return this.actorsNbCombo.getSelectionModel().getSelectedItem();
    }

    /**
     * Whether the entry should have a unicity constraint.
     */
    public boolean isUnique() {
      return this.isUniqueCheckBox.isSelected();
    }
  }

  /**
   * This class wraps a single entry of {@link Registries#GENDERS}.
   */
  private class GenderRegistryViewEntry extends RegistryViewEntry<Gender> {
    private final ColorPicker colorPicker = new ColorPicker();

    /**
     * Create a wrapper for a {@link Gender}.
     *
     * @param entry Entry to wrap. Null for new entries.
     * @param usage Number of times the entry is used.
     */
    public GenderRegistryViewEntry(Gender entry, int usage) {
      super(entry, "gender", usage);
      Node usageLabel = this.getChildren().remove(this.getChildren().size() - 1);
      this.getChildren().addAll(
          new Label(App.config().language().translate("dialog.edit_registries.tab.genders.color")),
          this.colorPicker,
          usageLabel
      );
      this.colorPicker.setValue(entry != null ? Color.valueOf(entry.color()) : Color.BLACK);
    }

    /**
     * Entry’s color in hexadecimal CSS format.
     */
    public String color() {
      Color color = this.colorPicker.getValue();
      int r = (int) Math.round(color.getRed() * 255);
      int g = (int) Math.round(color.getGreen() * 255);
      int b = (int) Math.round(color.getBlue() * 255);
      return String.format("#%02x%02x%02x", r, g, b);
    }
  }
}
