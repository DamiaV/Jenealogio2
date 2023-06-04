package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import net.darmo_creations.jenealogio2.App;
import net.darmo_creations.jenealogio2.config.Config;
import net.darmo_creations.jenealogio2.config.Language;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.model.calendar.DatePrecision;
import net.darmo_creations.jenealogio2.model.calendar.DateWithPrecision;
import net.darmo_creations.jenealogio2.themes.Icon;
import net.darmo_creations.jenealogio2.themes.Theme;
import net.darmo_creations.jenealogio2.ui.PseudoClasses;
import net.darmo_creations.jenealogio2.ui.components.ComboBoxItem;
import net.darmo_creations.jenealogio2.ui.components.LifeEventView;
import net.darmo_creations.jenealogio2.ui.components.NotNullComboBoxItem;
import net.darmo_creations.jenealogio2.ui.components.RelativesListView;
import net.darmo_creations.jenealogio2.utils.FormatArg;
import net.darmo_creations.jenealogio2.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.Collator;
import java.time.LocalDateTime;
import java.util.*;

@SuppressWarnings("unused")
public class EditPersonDialog extends DialogBase<Boolean> {
  public static final int TAB_PROFILE = 0;
  public static final int TAB_EVENTS = 1;
  public static final int TAB_PARENTS = 2;

  @FXML
  private TabPane tabPane;

  @FXML
  private Label legalLastNameHelpLabel;
  @FXML
  private Label legalFirstNamesHelpLabel;
  @FXML
  private Label publicLastNameHelpLabel;
  @FXML
  private Label publicFirstNamesHelpLabel;
  @FXML
  private Label disambiguationIDHelpLabel;

  @FXML
  private ComboBox<NotNullComboBoxItem<LifeStatus>> lifeStatusCombo;
  @FXML
  private TextField legalLastNameField;
  @FXML
  private TextField publicLastNameField;
  @FXML
  private TextField legalFirstNamesField;
  @FXML
  private TextField publicFirstNamesField;
  @FXML
  private TextField nicknamesField;
  @FXML
  private ComboBox<ComboBoxItem<Gender>> genderCombo;
  @FXML
  private TextField disambiguationIDField;
  @FXML
  private TextArea notesField;
  @FXML
  private TextArea sourcesField;

  @FXML
  private Button addEventButton;
  @FXML
  private ListView<LifeEventView> lifeEventsList;

  @FXML
  private GridPane parentsPane;
  @FXML
  private ComboBox<ComboBoxItem<Person>> parent1Combo;
  @FXML
  private ComboBox<ComboBoxItem<Person>> parent2Combo;

  private final Map<Person.RelativeType, RelativesListView> relativesLists = new HashMap<>();

  private Person person;
  private FamilyTree familyTree;
  private boolean creating;
  /**
   * Stores the life status to be restored if the user selects an event type
   * that indicates death but reverts it later on.
   */
  private LifeStatus lifeStatusCache;
  private boolean internalTypeChange;
  private final Set<LifeEventView> eventsToDelete = new HashSet<>();

  public EditPersonDialog() {
    super("edit_person", true, ButtonTypes.OK, ButtonTypes.CANCEL);
    Config config = App.config();
    Language language = config.language();
    Theme theme = config.theme();

    this.legalLastNameHelpLabel.setGraphic(theme.getIcon(Icon.HELP, Icon.Size.SMALL));
    this.legalFirstNamesHelpLabel.setGraphic(theme.getIcon(Icon.HELP, Icon.Size.SMALL));
    this.publicLastNameHelpLabel.setGraphic(theme.getIcon(Icon.HELP, Icon.Size.SMALL));
    this.publicFirstNamesHelpLabel.setGraphic(theme.getIcon(Icon.HELP, Icon.Size.SMALL));
    this.disambiguationIDHelpLabel.setGraphic(theme.getIcon(Icon.HELP, Icon.Size.SMALL));

    Collator collator = Collator.getInstance(language.locale());
    this.lifeStatusCombo.getItems().addAll(Arrays.stream(LifeStatus.values())
        .map(lifeStatus -> {
          String text = language.translate("life_status." + lifeStatus.name().toLowerCase());
          return new NotNullComboBoxItem<>(lifeStatus, text);
        })
        .sorted((i1, i2) -> collator.compare(i1.text(), i2.text())) // Perform locale-dependent comparison
        .toList());
    this.lifeStatusCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      if (!this.internalTypeChange) {
        this.lifeStatusCache = newValue.data();
      }
    });
    this.updateGendersList();

    // Only allow digits and empty text
    this.disambiguationIDField.setTextFormatter(new TextFormatter<>(
        new IntegerStringConverter(),
        null,
        change -> change.getControlNewText().matches("^\\d*$") ? change : null
    ));
    this.disambiguationIDField.textProperty().addListener((observable, oldValue, newValue) -> this.updateButtons());

    this.addEventButton.setGraphic(theme.getIcon(Icon.ADD_EVENT, Icon.Size.SMALL));
    this.addEventButton.setOnAction(event -> {
      DateWithPrecision date = new DateWithPrecision(LocalDateTime.now(), DatePrecision.EXACT);
      LifeEventType birth = Registries.LIFE_EVENT_TYPES.getEntry(new RegistryEntryKey(Registry.BUILTIN_NS, "birth"));
      this.addEvent(new LifeEvent(this.person, date, birth), true);
    });
    this.lifeEventsList.setSelectionModel(new NoSelectionModel<>());

    this.parent1Combo.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> this.updateButtons());
    this.parent2Combo.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> this.updateButtons());
    int i = 1;
    for (Person.RelativeType type : Person.RelativeType.values()) {
      RelativesListView component = new RelativesListView();
      this.relativesLists.put(type, component);
      this.parentsPane.add(component, 1, i++);
    }

    Stage stage = this.stage();
    stage.setMinWidth(700);
    stage.setMinHeight(600);

    this.setResultConverter(buttonType -> {
      if (!buttonType.getButtonData().isCancelButton()) {
        this.updatePerson(this.person);
        if (this.creating) {
          this.familyTree.persons().add(this.person);
        }
        return true;
      }
      return false;
    });
  }

  public void updateGendersList() {
    Language language = App.config().language();
    Collator collator = Collator.getInstance(language.locale());
    this.genderCombo.getItems().add(new ComboBoxItem<>(null, language.translate("gender.unknown")));
    this.genderCombo.getItems().addAll(Registries.GENDERS.entries().stream()
        .map(gender -> {
          RegistryEntryKey key = gender.key();
          String text = key.namespace().equals(Registry.BUILTIN_NS)
              ? language.translate("gender." + key.name())
              : key.name();
          return new ComboBoxItem<>(gender, text);
        })
        .sorted((i1, i2) -> collator.compare(i1.text(), i2.text())) // Perform locale-dependent comparison
        .toList());
  }

  public void setPerson(Person person, @NotNull FamilyTree familyTree) {
    this.familyTree = Objects.requireNonNull(familyTree);
    Language language = App.config().language();

    this.creating = person == null;
    if (!this.creating) {
      this.person = person;
      this.setTitle(language.translate("dialog.edit_person.title",
          new FormatArg("person_name", this.person.toString())));
    } else {
      this.person = new Person();
      this.setTitle(language.translate("dialog.edit_person.title.create"));
    }

    // Profile
    this.lifeStatusCombo.getSelectionModel().select(new NotNullComboBoxItem<>(this.person.lifeStatus()));
    this.genderCombo.getSelectionModel().select(new ComboBoxItem<>(this.person.gender().orElse(null)));
    this.legalLastNameField.setText(this.person.legalLastName().orElse(""));
    this.publicLastNameField.setText(this.person.publicLastName().orElse(""));
    this.legalFirstNamesField.setText(this.person.getJoinedLegalFirstNames().orElse(""));
    this.publicFirstNamesField.setText(this.person.getJoinedPublicFirstNames().orElse(""));
    this.nicknamesField.setText(this.person.getJoinedNicknames().orElse(""));
    this.disambiguationIDField.setText(this.person.disambiguationID().map(String::valueOf).orElse(""));
    this.notesField.setText(this.person.notes().orElse(""));
    this.sourcesField.setText(this.person.sources().orElse(""));

    // Life events
    this.lifeEventsList.getItems().clear();
    this.person.getLifeEventsAsActor().stream().sorted().forEach(lifeEvent -> {
      this.addEvent(lifeEvent, false);
      if (lifeEvent.type().indicatesDeath()) {
        this.lifeStatusCombo.getSelectionModel().select(new NotNullComboBoxItem<>(LifeStatus.DECEASED));
        this.lifeStatusCombo.setDisable(true);
      }
    });
    if (!this.lifeEventsList.getItems().isEmpty()) {
      this.lifeEventsList.getItems().get(0).setExpanded(true);
    }

    // Relatives
    List<Person> potentialRelatives = this.familyTree.persons().stream()
        .filter(p -> p != this.person)
        .sorted(Person.lastThenFirstNamesComparator())
        .toList();
    List<ComboBoxItem<Person>> relatives = potentialRelatives.stream()
        .map(p -> new ComboBoxItem<>(p, p.toString()))
        .toList();
    this.parent1Combo.getItems().clear();
    this.parent1Combo.getItems().add(new ComboBoxItem<>(null,
        language.translate("dialog.edit_person.parents.parents.unknown")));
    this.parent1Combo.getItems().addAll(relatives);
    this.parent2Combo.getItems().clear();
    this.parent2Combo.getItems().add(new ComboBoxItem<>(null,
        language.translate("dialog.edit_person.parents.parents.unknown")));
    this.parent2Combo.getItems().addAll(relatives);
    var parents = this.person.parents();
    Optional<Person> leftParent = parents.left();
    if (leftParent.isPresent()) {
      this.parent1Combo.getSelectionModel().select(new ComboBoxItem<>(leftParent.get()));
    } else {
      this.parent1Combo.getSelectionModel().select(0);
    }
    Optional<Person> rightParent = parents.right();
    if (rightParent.isPresent()) {
      this.parent2Combo.getSelectionModel().select(new ComboBoxItem<>(rightParent.get()));
    } else {
      this.parent2Combo.getSelectionModel().select(0);
    }
    for (Person.RelativeType type : Person.RelativeType.values()) {
      RelativesListView list = this.relativesLists.get(type);
      list.setPersons(this.person.getRelatives(type));
      list.setPotentialRelatives(potentialRelatives);
    }
  }

  public void selectTab(int index) {
    this.tabPane.getSelectionModel().select(index);
  }

  private void addEvent(@NotNull LifeEvent lifeEvent, boolean expanded) {
    LifeEventView lifeEventView = new LifeEventView(lifeEvent, this.person, this.familyTree.persons(), expanded, this.lifeEventsList);
    lifeEventView.getDeletionListeners().add(this::onEventDelete);
    lifeEventView.getUpdateListeners().add(this::updateButtons);
    lifeEventView.getTypeListeners().add(t -> {
      boolean anyDeath = this.lifeEventsList.getItems().stream()
          .anyMatch(i -> i.selectedLifeEventType().indicatesDeath());
      this.internalTypeChange = true;
      if (anyDeath) {
        this.lifeStatusCombo.getSelectionModel().select(new NotNullComboBoxItem<>(LifeStatus.DECEASED));
      } else {
        this.lifeStatusCombo.getSelectionModel().select(new NotNullComboBoxItem<>(this.lifeStatusCache));
      }
      this.internalTypeChange = false;
      this.lifeStatusCombo.setDisable(anyDeath);
    });
    this.lifeEventsList.getItems().add(lifeEventView);
  }

  private void onEventDelete(@NotNull LifeEventView lifeEventView) {
    String prefix = "alert.delete_life_event.";
    boolean delete = Alerts.confirmation(prefix + "header", prefix + "content", prefix + "title");
    if (delete) {
      this.eventsToDelete.add(lifeEventView);
      this.lifeEventsList.getItems().remove(lifeEventView);
      this.updateButtons();
    }
  }

  private void updateButtons() {
    Integer disambiguationID = this.getDisambiguationID();
    boolean invalid = disambiguationID != null && disambiguationID == 0;
    this.disambiguationIDField.pseudoClassStateChanged(PseudoClasses.INVALID, invalid);

    Map<LifeEventType, List<LifeEventView>> uniqueTypes = new HashMap<>();
    for (LifeEventView item : this.lifeEventsList.getItems()) {
      item.pseudoClassStateChanged(PseudoClasses.INVALID, false);
      if (!item.checkValidity()) {
        invalid = true;
      }
      LifeEventType type = item.selectedLifeEventType();
      if (type.isUnique()) {
        if (!uniqueTypes.containsKey(type)) {
          uniqueTypes.put(type, new LinkedList<>());
        } else {
          item.pseudoClassStateChanged(PseudoClasses.INVALID, true);
          invalid = true;
        }
        uniqueTypes.get(type).add(item);
      }
    }

    ComboBoxItem<Person> selectedParent1 = this.parent1Combo.getSelectionModel().getSelectedItem();
    ComboBoxItem<Person> selectedParent2 = this.parent2Combo.getSelectionModel().getSelectedItem();
    if (selectedParent1 != null && selectedParent2 != null) {
      Person parent1 = selectedParent1.data();
      Person parent2 = selectedParent2.data();
      boolean sameParents = parent1 != null && parent1 == parent2;
      this.parent1Combo.pseudoClassStateChanged(PseudoClasses.INVALID, sameParents);
      this.parent2Combo.pseudoClassStateChanged(PseudoClasses.INVALID, sameParents);
      invalid |= sameParents;
    }

    this.getDialogPane().lookupButton(ButtonTypes.OK).setDisable(invalid);
  }

  private void updatePerson(@NotNull Person person) {
    // Profile
    person.setGender(this.genderCombo.getSelectionModel().getSelectedItem().data());
    person.setLegalLastName(this.getText(this.legalLastNameField));
    person.setPublicLastName(this.getText(this.publicLastNameField));
    person.setLegalFirstNames(this.splitText(this.legalFirstNamesField));
    person.setPublicFirstNames(this.splitText(this.publicFirstNamesField));
    person.setNicknames(this.splitText(this.nicknamesField));
    person.setDisambiguationID(this.getDisambiguationID());
    person.setNotes(this.getText(this.notesField));
    person.setSources(this.getText(this.sourcesField));

    // Life events
    this.lifeEventsList.getItems().forEach(w -> {
      w.applyChanges();
      this.familyTree.lifeEvents().add(w.lifeEvent());
    });
    for (LifeEventView lifeEventView : this.eventsToDelete) {
      LifeEvent event = lifeEventView.lifeEvent();
      if (event.actors().size() <= event.type().minActors()) {
        event.actors().forEach(a -> a.removeLifeEvent(event));
        event.witnesses().forEach(w -> w.removeLifeEvent(event));
        this.familyTree.lifeEvents().remove(event);
      } else {
        person.removeLifeEvent(event);
      }
    }
    // Update life status after events to avoid assertion error
    person.setLifeStatus(this.lifeStatusCombo.getSelectionModel().getSelectedItem().data());

    // Relatives
    person.setParent(0, this.parent1Combo.getSelectionModel().getSelectedItem().data());
    person.setParent(1, this.parent2Combo.getSelectionModel().getSelectedItem().data());
    for (Person.RelativeType type : Person.RelativeType.values()) {
      // Clear relatives of the current type
      for (Person relative : this.person.getRelatives(type)) {
        this.person.removeRelative(relative, type);
      }
      // Add back the selected relatives
      this.relativesLists.get(type).getPersons()
          .forEach(p -> this.person.addRelative(p, type));
    }
  }

  private @Nullable Integer getDisambiguationID() {
    String text = this.disambiguationIDField.getText();
    return text.isEmpty() ? null : Integer.parseInt(text);
  }

  private @Nullable String getText(final @NotNull TextInputControl textInput) {
    return StringUtils.stripNullable(textInput.getText()).orElse(null);
  }

  private List<String> splitText(final @NotNull TextInputControl textInput) {
    return Arrays.stream(textInput.getText().split("\\s+"))
        .map(String::strip)
        .filter(s -> !s.isEmpty())
        .toList();
  }

  /**
   * Custom selection model to prevent item selection.
   */
  private static class NoSelectionModel<T> extends MultipleSelectionModel<T> {
    @Override
    public ObservableList<Integer> getSelectedIndices() {
      return FXCollections.emptyObservableList();
    }

    @Override
    public ObservableList<T> getSelectedItems() {
      return FXCollections.emptyObservableList();
    }

    @Override
    public void selectIndices(int index, int... indices) {
    }

    @Override
    public void selectAll() {
    }

    @Override
    public void selectFirst() {
    }

    @Override
    public void selectLast() {
    }

    @Override
    public void clearAndSelect(int index) {
    }

    @Override
    public void select(int index) {
    }

    @Override
    public void select(T obj) {
    }

    @Override
    public void clearSelection(int index) {
    }

    @Override
    public void clearSelection() {
    }

    @Override
    public boolean isSelected(int index) {
      return false;
    }

    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public void selectPrevious() {
    }

    @Override
    public void selectNext() {
    }
  }

  public static final class Result {
    private final Person person;
    private final boolean created;

    private Result(@NotNull Person person, boolean created) {
      this.person = person;
      this.created = created;
    }

    public Person person() {
      return this.person;
    }

    public boolean isPersonCreated() {
      return this.created;
    }
  }
}
