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
import net.darmo_creations.jenealogio2.themes.Icon;
import net.darmo_creations.jenealogio2.ui.components.ComboBoxItem;
import net.darmo_creations.jenealogio2.ui.components.NotNullComboBoxItem;
import net.darmo_creations.jenealogio2.utils.FormatArg;
import net.darmo_creations.jenealogio2.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
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
  private ListView<TitledPane> lifeEventsList;

  private Person person;

  public EditPersonDialog() {
    super("edit_person", true, ButtonTypes.OK, ButtonTypes.CANCEL);
    Config config = App.config();
    Language language = config.language();

    //noinspection DataFlowIssue
    this.legalLastNameHelpLabel.setGraphic(config.theme().getIcon(Icon.HELP, Icon.Size.SMALL));
    this.legalFirstNamesHelpLabel.setGraphic(config.theme().getIcon(Icon.HELP, Icon.Size.SMALL));
    this.publicLastNameHelpLabel.setGraphic(config.theme().getIcon(Icon.HELP, Icon.Size.SMALL));
    this.publicFirstNamesHelpLabel.setGraphic(config.theme().getIcon(Icon.HELP, Icon.Size.SMALL));
    this.disambiguationIDHelpLabel.setGraphic(config.theme().getIcon(Icon.HELP, Icon.Size.SMALL));

    this.lifeStatusCombo.getItems().addAll(Arrays.stream(LifeStatus.values())
        .map(lifeStatus -> {
          String text = language.translate("life_status." + lifeStatus.name().toLowerCase());
          return new NotNullComboBoxItem<>(lifeStatus, text);
        })
        .sorted(Comparator.comparing(NotNullComboBoxItem::text))
        .toList());
    this.updateGendersList();

    // Only allow digits and empty text
    this.disambiguationIDField.setTextFormatter(new TextFormatter<>(
        new IntegerStringConverter(),
        null,
        change -> change.getControlNewText().matches("^\\d*$") ? change : null
    ));

    this.addEventButton.setGraphic(config.theme().getIcon(Icon.ADD_EVENT, Icon.Size.SMALL));
    this.lifeEventsList.setSelectionModel(new NoSelectionModel());

    Stage stage = this.stage();
    stage.setMinWidth(700);
    stage.setMinHeight(600);

    this.setResultConverter(buttonType -> {
      if (!buttonType.getButtonData().isCancelButton()) {
        Person p = this.person == null ? new Person() : this.person;
        this.updatePerson(p);
        return Optional.of(new Result(p, this.person == null));
      }
      return Optional.empty();
    });
  }

  public void updateGendersList() {
    Language language = App.config().language();
    this.genderCombo.getItems().add(new ComboBoxItem<>(null, language.translate("gender.unknown")));
    this.genderCombo.getItems().addAll(Registries.GENDERS.entries().stream()
        .map(gender -> {
          RegistryEntryKey key = gender.key();
          String text = key.namespace().equals(Registry.BUILTIN_NS)
              ? language.translate("gender." + key.name())
              : key.name();
          return new ComboBoxItem<>(gender, text);
        })
        .sorted(Comparator.comparing(ComboBoxItem::text))
        .toList());
  }

  public void setPerson(final Person person) {
    this.person = person;
    if (person != null) {
      this.setTitle(StringUtils.format(this.getTitle(), new FormatArg("person_name", person.toString())));
      this.lifeStatusCombo.getSelectionModel().select(new NotNullComboBoxItem<>(person.lifeStatus()));
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
        TitledPane titledPane = new TitledPane();
        titledPane.setAnimated(false);
        titledPane.setContent(new GridPane());
        this.lifeEventsList.getItems().add(titledPane);
      });
      if (!this.lifeEventsList.getItems().isEmpty()) {
        this.lifeEventsList.getItems().get(0).setExpanded(true);
      }
    } else {
      this.setTitle(App.config().language().translate("dialog.edit_person.title.create"));
      this.lifeStatusCombo.getSelectionModel().select(new NotNullComboBoxItem<>(LifeStatus.LIVING));
      this.genderCombo.getSelectionModel().select(0);
      this.lifeEventsList.getItems().clear();
    }
  }

  private void updatePerson(@NotNull Person p) {
    // Profile
    p.setLifeStatus(this.lifeStatusCombo.getSelectionModel().getSelectedItem().data());
    p.setGender(this.genderCombo.getSelectionModel().getSelectedItem().data());
    p.setLegalLastName(this.getText(this.legalLastNameField));
    p.setPublicLastName(this.getText(this.publicLastNameField));
    p.setLegalFirstNames(this.splitText(this.legalFirstNamesField));
    p.setPublicFirstNames(this.splitText(this.publicFirstNamesField));
    p.setNicknames(this.splitText(this.nicknamesField));
    p.setDisambiguationID((Integer) this.disambiguationIDField.getTextFormatter().getValue());
    p.setNotes(this.getText(this.notesField));
    p.setSources(this.getText(this.sourcesField));

    // Life events
    // TODO
  }

  private String getText(final TextInputControl textInput) {
    return StringUtils.stripNullable(textInput.getText()).orElse(null);
  }

  private List<String> splitText(final TextInputControl textInput) {
    return Arrays.stream(textInput.getText().split("\\s+"))
        .map(String::strip)
        .filter(s -> !s.isEmpty())
        .toList();
  }

  /**
   * Custom selection model to prevent item selection.
   */
  private static class NoSelectionModel extends MultipleSelectionModel<TitledPane> {
    @Override
    public ObservableList<Integer> getSelectedIndices() {
      return FXCollections.emptyObservableList();
    }

    @Override
    public ObservableList<TitledPane> getSelectedItems() {
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
    public void select(TitledPane obj) {
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
