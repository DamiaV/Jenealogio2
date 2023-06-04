package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
import net.darmo_creations.jenealogio2.ui.components.NotNullComboBoxItem;
import net.darmo_creations.jenealogio2.utils.FormatArg;
import net.darmo_creations.jenealogio2.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.Collator;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("unused")
public class EditPersonDialog extends DialogBase<Optional<EditPersonDialog.Result>> {
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

  private Person person;
  private FamilyTree familyTree;
  private boolean creating;
  /**
   * Stores the life status to be restored if the user selects an event type
   * that indicates death but reverts it later on.
   */
  private LifeStatus lifeStatusCache;

  public EditPersonDialog() {
    super("edit_person", true, ButtonTypes.OK, ButtonTypes.CANCEL);
    Config config = App.config();
    Language language = config.language();
    Theme theme = config.theme();

    //noinspection DataFlowIssue
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
      this.addEvent(new LifeEvent(this.person, date, birth));
    });
    this.lifeEventsList.setSelectionModel(new NoSelectionModel<>());

    Stage stage = this.stage();
    stage.setMinWidth(700);
    stage.setMinHeight(600);

    this.setResultConverter(buttonType -> {
      if (!buttonType.getButtonData().isCancelButton()) {
        this.updatePerson(this.person);
        return Optional.of(new Result(this.person, this.creating));
      }
      return Optional.empty();
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
    if (person != null) {
      this.person = person;
      this.creating = false;
      this.setTitle(StringUtils.format(this.getTitle(), new FormatArg("person_name", person.toString())));
      this.lifeStatusCache = person.lifeStatus();
      this.lifeStatusCombo.getSelectionModel().select(new NotNullComboBoxItem<>(this.lifeStatusCache));
      this.genderCombo.getSelectionModel().select(new ComboBoxItem<>(person.gender().orElse(null)));
      this.legalLastNameField.setText(person.legalLastName().orElse(""));
      this.publicLastNameField.setText(person.publicLastName().orElse(""));
      this.legalFirstNamesField.setText(person.getJoinedLegalFirstNames().orElse(""));
      this.publicFirstNamesField.setText(person.getJoinedPublicFirstNames().orElse(""));
      this.nicknamesField.setText(person.getJoinedNicknames().orElse(""));
      this.disambiguationIDField.setText(person.disambiguationID().map(String::valueOf).orElse(""));
      this.notesField.setText(person.notes().orElse(""));
      this.sourcesField.setText(person.sources().orElse(""));

      this.lifeEventsList.getItems().clear();
      person.getLifeEventsAsActor().forEach(lifeEvent -> {
        this.addEvent(lifeEvent);
        if (lifeEvent.type().indicatesDeath()) {
          this.lifeStatusCombo.getSelectionModel().select(new NotNullComboBoxItem<>(LifeStatus.DECEASED));
          this.lifeStatusCombo.setDisable(true);
        }
      });
      if (!this.lifeEventsList.getItems().isEmpty()) {
        this.lifeEventsList.getItems().get(0).setExpanded(true);
      }
    } else {
      this.person = new Person();
      this.creating = true;
      this.setTitle(App.config().language().translate("dialog.edit_person.title.create"));
      this.lifeStatusCache = LifeStatus.LIVING;
      this.lifeStatusCombo.getSelectionModel().select(new NotNullComboBoxItem<>(this.lifeStatusCache));
      this.lifeStatusCombo.setDisable(false);
      this.genderCombo.getSelectionModel().select(0);
      this.lifeEventsList.getItems().clear();
    }
  }

  private void addEvent(@NotNull LifeEvent lifeEvent) {
    LifeEventView lifeEventView = new LifeEventView(lifeEvent, this.person, this.familyTree.persons());
    lifeEventView.getUpdateListeners().add(this::updateButtons);
    lifeEventView.getTypeListeners().add(t -> {
      boolean anyDeath = this.lifeEventsList.getItems().stream()
          .anyMatch(i -> i.selectedLifeEventType().indicatesDeath());
      if (anyDeath) {
        this.lifeStatusCombo.getSelectionModel().select(new NotNullComboBoxItem<>(LifeStatus.DECEASED));
      } else {
        this.lifeStatusCombo.getSelectionModel().select(new NotNullComboBoxItem<>(this.lifeStatusCache));
      }
      this.lifeStatusCombo.setDisable(anyDeath);
    });
    this.lifeEventsList.getItems().add(lifeEventView);
  }

  private void updateButtons() {
    Integer disambiguationID = this.getDisambiguationID();
    boolean invalid = disambiguationID != null && disambiguationID == 0;
    this.disambiguationIDField.pseudoClassStateChanged(PseudoClasses.INVALID, invalid);

    for (LifeEventView item : this.lifeEventsList.getItems()) {
      if (!item.checkValidity()) {
        invalid = true;
      }
    }

    this.getDialogPane().lookupButton(ButtonTypes.OK).setDisable(invalid);
  }

  private void updatePerson(@NotNull Person p) {
    // Profile
    p.setGender(this.genderCombo.getSelectionModel().getSelectedItem().data());
    p.setLegalLastName(this.getText(this.legalLastNameField));
    p.setPublicLastName(this.getText(this.publicLastNameField));
    p.setLegalFirstNames(this.splitText(this.legalFirstNamesField));
    p.setPublicFirstNames(this.splitText(this.publicFirstNamesField));
    p.setNicknames(this.splitText(this.nicknamesField));
    p.setDisambiguationID(this.getDisambiguationID());
    p.setNotes(this.getText(this.notesField));
    p.setSources(this.getText(this.sourcesField));
    // Life events
    this.lifeEventsList.getItems().forEach(LifeEventView::applyChanges);
    // Update life status after events to avoid assertion error
    p.setLifeStatus(this.lifeStatusCombo.getSelectionModel().getSelectedItem().data());
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
