package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.util.converter.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.model.datetime.*;
import net.darmo_creations.jenealogio2.model.datetime.calendar.*;
import net.darmo_creations.jenealogio2.themes.*;
import net.darmo_creations.jenealogio2.ui.*;
import net.darmo_creations.jenealogio2.ui.components.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.text.*;
import java.time.*;
import java.util.*;

/**
 * Dialog to edit a {@link Person} object and its {@link LifeEvent}s.
 */
public class EditPersonDialog extends DialogBase<Person>
    implements PersonRequester.PersonRequestListener, CoordinatesRequester.CoordinatesRequestListener {
  /**
   * Index of the profile tab.
   */
  public static final int TAB_PROFILE = 0;
  /**
   * Index of the life events tab.
   */
  public static final int TAB_EVENTS = 1;
  /**
   * Index of the parents and relatives tab.
   */
  public static final int TAB_PARENTS = 2;

  private final TabPane tabPane = new TabPane();

  private final ComboBox<NotNullComboBoxItem<LifeStatus>> lifeStatusCombo = new ComboBox<>();
  private final TextField legalLastNameField = new TextField();
  private final TextField legalFirstNamesField = new TextField();
  private final TextField publicLastNameField = new TextField();
  private final TextField publicFirstNamesField = new TextField();
  private final TextField nicknamesField = new TextField();
  private final ComboBox<ComboBoxItem<Gender>> agabCombo = new ComboBox<>();
  private final ComboBox<ComboBoxItem<Gender>> genderCombo = new ComboBox<>();
  private final TextField mainOccupationField = new TextField();
  private final TextField disambiguationIDField = new TextField();
  private final TextArea notesField = new TextArea();
  private final TextArea sourcesField = new TextArea();

  private final Button addEventButton = new Button();
  private final ListView<LifeEventView> lifeEventsList = new ListView<>();
  private final Map<ParentalRelationType, ParentsListView> parentsLists = new HashMap<>();

  private final SelectPersonDialog selectPersonDialog;
  private final SelectCoordinatesDialog selectCoordinatesDialog;

  /**
   * The person object being edited.
   */
  private Person person;
  /**
   * If not null, indicates the person the one being edited should be a parent of.
   */
  private List<ChildInfo> childInfo;
  /**
   * The family tree the person belongs to..
   */
  private FamilyTree familyTree;
  /**
   * Whether the person is being created.
   */
  private boolean creating;
  /**
   * Stores the life status to be restored if the user selects an event type
   * that indicates death but reverts it later on.
   */
  private LifeStatus lifeStatusCache;
  /**
   * Indicates whether an update event from the {@link #lifeStatusCombo} is internal or from the user.
   */
  private boolean internalLifeStatusChange;
  /**
   * Set of life events to delete.
   */
  private final Set<LifeEventView> eventsToDelete = new HashSet<>();

  /**
   * Create a person edit dialog.
   *
   * @param config The app’s config.
   */
  public EditPersonDialog(final @NotNull Config config) {
    super(
        config,
        "edit_person",
        true,
        ButtonTypes.OK,
        ButtonTypes.CANCEL
    );
    this.selectPersonDialog = new SelectPersonDialog(config);
    this.selectCoordinatesDialog = new SelectCoordinatesDialog(config);

    VBox.setVgrow(this.tabPane, Priority.ALWAYS);
    this.tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    this.tabPane.getTabs().add(this.createProfileTab());
    this.tabPane.getTabs().add(this.createEventsTab());
    this.tabPane.getTabs().add(this.createParentsTab());

    this.getDialogPane().setContent(this.tabPane);

    final Stage stage = this.stage();
    stage.setMinWidth(1000);
    stage.setMinHeight(650);

    this.setResultConverter(buttonType -> {
      if (!buttonType.getButtonData().isCancelButton()) {
        this.updatePerson(this.person);
        if (this.creating) {
          this.familyTree.addPerson(this.person);
          for (final ChildInfo info : this.childInfo)
            info.child().addParent(this.person, info.parentType());
        }
        this.dispose();
        return this.person;
      }
      this.dispose();
      return null;
    });
  }

  private void dispose() {
    this.lifeEventsList.getItems().forEach(LifeEventView::dispose);
  }

  private Tab createProfileTab() {
    final Language language = this.config.language();
    final Theme theme = this.config.theme();

    final Tab tab = new Tab(language.translate("dialog.edit_person.profile.title"));
    tab.setGraphic(theme.getIcon(Icon.PROFILE_TAB, Icon.Size.SMALL));

    final GridPane gridPane = new GridPane();
    gridPane.setHgap(5);
    gridPane.setVgap(5);
    int row = 0;

    {
      this.lifeStatusCombo.getItems().addAll(Arrays.stream(LifeStatus.values())
          .map(lifeStatus -> new NotNullComboBoxItem<>(
              lifeStatus,
              language.translate("life_status." + lifeStatus.name().toLowerCase())
          ))
          .toList());
      this.lifeStatusCombo.getSelectionModel().selectedItemProperty().addListener(
          (observable, oldValue, newValue) -> {
            if (!this.internalLifeStatusChange)
              this.lifeStatusCache = newValue.data();
          });
      this.addRow(gridPane, row++, "dialog.edit_person.profile.life_status", this.lifeStatusCombo);
      final RowConstraints rc = new RowConstraints();
      rc.setVgrow(Priority.SOMETIMES);
      gridPane.getRowConstraints().add(rc);
    }

    {
      final Label label = new Label(language.translate("dialog.edit_person.profile.legal_last_name"));
      final Label helpLabel = new Label(null, theme.getIcon(Icon.HELP, Icon.Size.SMALL));
      helpLabel.setTooltip(new Tooltip(language.translate("dialog.edit_person.profile.legal_last_name.tooltip")));
      final HBox hBox = new HBox(5, label, helpLabel);
      hBox.setAlignment(Pos.CENTER_LEFT);
      gridPane.addRow(row++, hBox, this.legalLastNameField);
      final RowConstraints rc = new RowConstraints();
      rc.setVgrow(Priority.SOMETIMES);
      gridPane.getRowConstraints().add(rc);
    }

    {
      final Label label = new Label(language.translate("dialog.edit_person.profile.legal_first_names"));
      final Label helpLable = new Label(null, theme.getIcon(Icon.HELP, Icon.Size.SMALL));
      helpLable.setTooltip(new Tooltip(language.translate("dialog.edit_person.profile.legal_first_names.tooltip")));
      final HBox hBox = new HBox(5, label, helpLable);
      hBox.setAlignment(Pos.CENTER_LEFT);
      gridPane.addRow(row++, hBox, this.legalFirstNamesField);
      final RowConstraints rc = new RowConstraints();
      rc.setVgrow(Priority.SOMETIMES);
      gridPane.getRowConstraints().add(rc);
    }

    {
      final Label label = new Label(language.translate("dialog.edit_person.profile.public_last_name"));
      final Label helpLabel = new Label(null, theme.getIcon(Icon.HELP, Icon.Size.SMALL));
      helpLabel.setTooltip(new Tooltip(language.translate("dialog.edit_person.profile.public_last_name.tooltip")));
      final HBox hBox = new HBox(5, label, helpLabel);
      hBox.setAlignment(Pos.CENTER_LEFT);
      gridPane.addRow(row++, hBox, this.publicLastNameField);
      final RowConstraints rc = new RowConstraints();
      rc.setVgrow(Priority.SOMETIMES);
      gridPane.getRowConstraints().add(rc);
    }

    {
      final Label label = new Label(language.translate("dialog.edit_person.profile.public_first_names"));
      final Label helpLabel = new Label(null, theme.getIcon(Icon.HELP, Icon.Size.SMALL));
      helpLabel.setTooltip(new Tooltip(language.translate("dialog.edit_person.profile.public_first_names.tooltip")));
      final HBox hBox = new HBox(5, label, helpLabel);
      hBox.setAlignment(Pos.CENTER_LEFT);
      gridPane.addRow(row++, hBox, this.publicFirstNamesField);
      final RowConstraints rc = new RowConstraints();
      rc.setVgrow(Priority.SOMETIMES);
      gridPane.getRowConstraints().add(rc);
    }

    {
      this.addRow(gridPane, row++, "dialog.edit_person.profile.nicknames", this.nicknamesField);
      final RowConstraints rc = new RowConstraints();
      rc.setVgrow(Priority.SOMETIMES);
      gridPane.getRowConstraints().add(rc);
    }

    {
      this.addRow(gridPane, row++, "dialog.edit_person.profile.agab", this.agabCombo);
      // If AGAB and gender comboboxes were identical, report the AGAB change to the gender combobox
      this.agabCombo.getSelectionModel().selectedItemProperty().addListener(
          (observable, oldValue, newValue) -> {
            if (Objects.equals(oldValue, this.genderCombo.getSelectionModel().getSelectedItem()))
              this.genderCombo.getSelectionModel().select(newValue);
          });
      final RowConstraints rc = new RowConstraints();
      rc.setVgrow(Priority.SOMETIMES);
      gridPane.getRowConstraints().add(rc);
    }

    {
      this.addRow(gridPane, row++, "dialog.edit_person.profile.gender", this.genderCombo);
      final RowConstraints rc = new RowConstraints();
      rc.setVgrow(Priority.SOMETIMES);
      gridPane.getRowConstraints().add(rc);
    }

    {
      this.addRow(gridPane, row++, "dialog.edit_person.profile.main_occupation", this.mainOccupationField);
      final RowConstraints rc = new RowConstraints();
      rc.setVgrow(Priority.SOMETIMES);
      gridPane.getRowConstraints().add(rc);
    }

    {
      final Label label = new Label(language.translate("dialog.edit_person.profile.disambiguation_id"));
      final Label helpLabel = new Label(null, theme.getIcon(Icon.HELP, Icon.Size.SMALL));
      helpLabel.setTooltip(new Tooltip(language.translate("dialog.edit_person.profile.disambiguation_id.tooltip")));
      final HBox hBox = new HBox(5, label, helpLabel);
      hBox.setAlignment(Pos.CENTER_LEFT);
      // Only allow digits and empty text
      this.disambiguationIDField.setTextFormatter(new TextFormatter<>(
          new IntegerStringConverter(),
          null,
          change -> change.getControlNewText().matches("^\\d*$") ? change : null
      ));
      this.disambiguationIDField.textProperty().addListener(
          (observable, oldValue, newValue) -> this.updateButtons());
      gridPane.addRow(row++, hBox, this.disambiguationIDField);
      final RowConstraints rc = new RowConstraints();
      rc.setVgrow(Priority.SOMETIMES);
      gridPane.getRowConstraints().add(rc);
    }

    {
      final Label notesLabel = new Label(language.translate("dialog.edit_person.profile.notes"));
      notesLabel.setPadding(new Insets(3, 0, 0, 0));
      GridPane.setValignment(notesLabel, VPos.TOP);
      gridPane.addRow(row++, notesLabel, this.notesField);
      final RowConstraints rc = new RowConstraints();
      rc.setVgrow(Priority.ALWAYS);
      gridPane.getRowConstraints().add(rc);
    }

    {
      final Label sourcesLabel = new Label(language.translate("dialog.edit_person.profile.sources"));
      sourcesLabel.setPadding(new Insets(3, 0, 0, 0));
      GridPane.setValignment(sourcesLabel, VPos.TOP);
      gridPane.addRow(row, sourcesLabel, this.sourcesField);
      final RowConstraints rc = new RowConstraints();
      rc.setVgrow(Priority.ALWAYS);
      gridPane.getRowConstraints().add(rc);
    }

    this.setupTabGridConstraints(tab, gridPane);
    return tab;
  }

  private Tab createEventsTab() {
    final Language language = this.config.language();
    final Theme theme = this.config.theme();

    final Tab tab = new Tab(language.translate("dialog.edit_person.events.title"));
    tab.setGraphic(theme.getIcon(Icon.LIFE_EVENTS_TAB, Icon.Size.SMALL));

    final Pane spacer = new Pane();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    this.addEventButton.setText(language.translate("dialog.edit_person.add_event"));
    this.addEventButton.setGraphic(theme.getIcon(Icon.ADD_EVENT, Icon.Size.SMALL));
    this.addEventButton.setOnAction(event -> {
      final DateTimeWithPrecision date = new DateTimeWithPrecision(
          Calendars.GREGORIAN.convertDate(LocalDateTime.now(), true),
          DateTimePrecision.EXACT
      );
      final LifeEventType birth = this.familyTree.lifeEventTypeRegistry()
          .getEntry(new RegistryEntryKey(Registry.BUILTIN_NS, "birth"));
      this.addEvent(new LifeEvent(date, birth), true);
    });
    final HBox buttonsBox = new HBox(spacer, this.addEventButton);

    this.lifeEventsList.setSelectionModel(new NoSelectionModel<>());
    VBox.setVgrow(this.lifeEventsList, Priority.ALWAYS);

    final VBox vBox = new VBox(5, buttonsBox, this.lifeEventsList);
    vBox.setPadding(new Insets(5, 0, 0, 0));
    tab.setContent(vBox);
    return tab;
  }

  private Tab createParentsTab() {
    final Language language = this.config.language();
    final Theme theme = this.config.theme();

    final Tab tab = new Tab(language.translate("dialog.edit_person.parents.title"));
    tab.setGraphic(theme.getIcon(Icon.FAMILY_TAB, Icon.Size.SMALL));

    final GridPane gridPane = new GridPane();
    gridPane.setHgap(5);
    gridPane.setVgap(5);

    int i = 0;
    for (final var parentType : ParentalRelationType.values()) {
      final ParentsListView component = new ParentsListView(this.config, parentType);
      component.setPersonRequestListener(this);
      component.addUpdateListener(this::updateButtons);
      this.parentsLists.put(parentType, component);
      final String plural = parentType.maxParentsCount().orElse(Integer.MAX_VALUE) > 1 ? "s" : "";
      final Label label = new Label(language.translate(
          "dialog.edit_person.parents.%s".formatted(parentType.name().toLowerCase() + plural)));
      label.setPadding(new Insets(3, 0, 0, 0));
      gridPane.addRow(i++, label, component);
      final RowConstraints rc = new RowConstraints();
      rc.setValignment(VPos.TOP);
      rc.setVgrow(Priority.ALWAYS);
      gridPane.getRowConstraints().add(rc);
    }

    this.setupTabGridConstraints(tab, gridPane);
    return tab;
  }

  private void addRow(@NotNull GridPane gridPane, int index, @NotNull String text, @NotNull Node node) {
    gridPane.addRow(index, new Label(this.config.language().translate(text)), node);
  }

  private void setupTabGridConstraints(@NotNull Tab tab, @NotNull GridPane gridPane) {
    final ColumnConstraints cc1 = new ColumnConstraints();
    cc1.setHgrow(Priority.SOMETIMES);
    final ColumnConstraints cc2 = new ColumnConstraints();
    cc2.setHgrow(Priority.ALWAYS);
    gridPane.getColumnConstraints().addAll(cc1, cc2);

    gridPane.setPadding(new Insets(5, 0, 0, 0));
    tab.setContent(gridPane);
  }

  /**
   * Update the gender entries in the given genders combobox.
   */
  public void updateGendersList(@NotNull ComboBox<ComboBoxItem<Gender>> cb) {
    final Language language = this.config.language();
    final Collator collator = Collator.getInstance(language.locale());
    cb.getItems().clear();
    cb.getItems().add(new ComboBoxItem<>(null, language.translate("genders.unknown")));
    cb.getItems().addAll(this.familyTree.genderRegistry().entries().stream()
        .map(gender -> {
          final String text = gender.isBuiltin()
              ? language.translate("genders." + gender.key().name())
              : Objects.requireNonNull(gender.userDefinedName());
          return new ComboBoxItem<>(gender, text);
        })
        // Perform locale-dependent comparison
        .sorted((i1, i2) -> collator.compare(i1.text(), i2.text()))
        .toList());
  }

  /**
   * Set the person to edit.
   *
   * @param person     The person to edit. A null value indicates to create a new person.
   * @param childInfo  If not null, indicates the persons the one being created should be a parent of.
   * @param familyTree The family tree the person belongs or should belong to.
   */
  public void setPerson(Person person, final List<ChildInfo> childInfo, @NotNull FamilyTree familyTree) {
    this.childInfo = childInfo == null || childInfo.isEmpty() ? new ArrayList<>() : new ArrayList<>(childInfo);
    this.familyTree = Objects.requireNonNull(familyTree);
    final Language language = this.config.language();

    this.creating = person == null;
    if (!this.creating) {
      this.person = person;
      this.setTitle(language.translate("dialog.edit_person.title",
          new FormatArg("person_name", this.person.toString())));
    } else {
      this.person = new Person();
      this.setTitle(language.translate("dialog.edit_person.title.create"));
    }

    this.updateGendersList(this.agabCombo);
    this.updateGendersList(this.genderCombo);
    this.setPersonProfileFields();
    this.setPersonLifeEventsFields();
    this.setPersonParentsFields();
    this.updateButtons();
  }

  private void setPersonProfileFields() {
    this.lifeStatusCombo.getSelectionModel().select(new NotNullComboBoxItem<>(this.person.lifeStatus()));
    this.agabCombo.getSelectionModel().select(new ComboBoxItem<>(this.person.assignedGenderAtBirth().orElse(null)));
    this.genderCombo.getSelectionModel().select(new ComboBoxItem<>(this.person.gender().orElse(null)));
    this.legalLastNameField.setText(this.person.legalLastName().orElse(""));
    this.publicLastNameField.setText(this.person.publicLastName().orElse(""));
    this.legalFirstNamesField.setText(this.person.getJoinedLegalFirstNames().orElse(""));
    this.publicFirstNamesField.setText(this.person.getJoinedPublicFirstNames().orElse(""));
    this.nicknamesField.setText(this.person.getJoinedNicknames().orElse(""));
    this.mainOccupationField.setText(this.person.mainOccupation().orElse(""));
    this.disambiguationIDField.setText(this.person.disambiguationID().map(String::valueOf).orElse(""));
    this.notesField.setText(this.person.notes().orElse(""));
    this.sourcesField.setText(this.person.sources().orElse(""));
  }

  private void setPersonLifeEventsFields() {
    this.lifeEventsList.getItems().forEach(LifeEventView::dispose);
    this.lifeEventsList.getItems().clear();
    this.person.getLifeEventsAsActor().stream()
        .sorted()
        .forEach(lifeEvent -> {
          this.addEvent(lifeEvent, false);
          if (lifeEvent.type().indicatesDeath())
            this.lifeStatusCombo.getSelectionModel().select(new NotNullComboBoxItem<>(LifeStatus.DECEASED));
        });
  }

  private void setPersonParentsFields() {
    for (final var parentType : ParentalRelationType.values())
      this.parentsLists.get(parentType).setPersons(this.person.parents(parentType));
  }

  /**
   * Set the values of the parents fields.
   *
   * @param parents The parents.
   */
  public void setParents(final @NotNull Map<ParentalRelationType, Set<Person>> parents) {
    parents.forEach((parentType, parents_) ->
        this.parentsLists.get(parentType).setPersons(parents_));
  }

  @Override
  public Optional<Person> onPersonRequest(@NotNull List<Person> exclusionList) {
    exclusionList.add(this.person);
    this.selectPersonDialog.updatePersonList(this.familyTree, exclusionList);
    return this.selectPersonDialog.showAndWait();
  }

  @Override
  public Optional<LatLon> onCoordinatesRequest() {
    return this.selectCoordinatesDialog.showAndWait();
  }

  /**
   * Select the tab at the given index.
   *
   * @param index Tab’s index.
   */
  public void selectTab(int index) {
    this.tabPane.getSelectionModel().select(index);
  }

  /**
   * Add a {@link LifeEventView} form for the given {@link LifeEvent} object.
   *
   * @param lifeEvent The life event to create a form for.
   * @param expanded  Whether to expand the created form.
   */
  private void addEvent(@NotNull LifeEvent lifeEvent, boolean expanded) {
    final LifeEventView lifeEventView = new LifeEventView(
        this.familyTree, lifeEvent, this.person, expanded, this.lifeEventsList, this.config);
    lifeEventView.setPersonRequestListener(this);
    lifeEventView.setCoordinatesRequestListener(this);
    lifeEventView.getDeletionListeners().add(this::onEventDelete);
    lifeEventView.getUpdateListeners().add(this::updateButtons);
    lifeEventView.getTypeListeners().add(t -> {
      final boolean anyDeath = this.lifeEventsList.getItems().stream()
          .anyMatch(i -> i.selectedLifeEventType().indicatesDeath());
      this.internalLifeStatusChange = true;
      this.lifeStatusCombo.getSelectionModel().select(new NotNullComboBoxItem<>(
          anyDeath ? LifeStatus.DECEASED : this.lifeStatusCache
      ));
      this.internalLifeStatusChange = false;
      this.lifeStatusCombo.setDisable(anyDeath);
    });
    this.lifeEventsList.getItems().add(lifeEventView);
    this.lifeEventsList.scrollTo(lifeEventView);
    this.updateButtons();
  }

  /**
   * Called when a {@link LifeEventView} has been set for deletion.
   * Adds it to the {@link #eventsToDelete} set.
   *
   * @param lifeEventView Component to delete.
   */
  private void onEventDelete(@NotNull LifeEventView lifeEventView) {
    final String prefix = "alert.delete_life_event.";
    final boolean delete = Alerts.confirmation(
        this.config,
        prefix + "header",
        prefix + "content",
        prefix + "title"
    );
    if (delete) {
      this.eventsToDelete.add(lifeEventView);
      this.lifeEventsList.getItems().remove(lifeEventView);
      lifeEventView.dispose();
      this.updateButtons();
    }
  }

  /**
   * Updates this dialog’s buttons.
   */
  private void updateButtons() {
    final Integer disambiguationID = this.getDisambiguationID();
    boolean invalid = disambiguationID != null && disambiguationID == 0;
    this.disambiguationIDField.pseudoClassStateChanged(PseudoClasses.INVALID, invalid);

    final Map<LifeEventType, List<LifeEventView>> uniqueTypes = new HashMap<>();
    boolean anyDeath = false;
    for (final LifeEventView item : this.lifeEventsList.getItems()) {
      item.pseudoClassStateChanged(PseudoClasses.INVALID, false);
      if (item.lifeEvent().type().indicatesDeath())
        anyDeath = true;
      if (!item.checkValidity())
        invalid = true;
      final LifeEventType type = item.selectedLifeEventType();
      if (type.isUnique()) {
        if (!uniqueTypes.containsKey(type))
          uniqueTypes.put(type, new LinkedList<>());
        else {
          item.pseudoClassStateChanged(PseudoClasses.INVALID, true);
          invalid = true;
        }
        uniqueTypes.get(type).add(item);
      }
    }
    this.lifeStatusCombo.setDisable(anyDeath);

    final var entries = new ArrayList<>(this.parentsLists.values());
    entries.forEach(listView -> listView.pseudoClassStateChanged(PseudoClasses.INVALID, false));
    // Check that no person is present in two different lists
    loop:
    for (int i = 0; i < entries.size() - 1; i++) {
      final ParentsListView list1 = entries.get(i);
      for (final Person person : list1.getPersons()) {
        for (int j = i + 1; j < entries.size(); j++) {
          final ParentsListView list2 = entries.get(j);
          if (list2.getPersons().contains(person)) {
            list2.pseudoClassStateChanged(PseudoClasses.INVALID, true);
            invalid = true;
            break loop;
          }
        }
      }
    }

    // Check that there are no more than 2 genetic parents
    int count = 0;
    for (final var geneticRelation : ParentalRelationType.GENETIC_RELATIONS) {
      final ParentsListView listView = this.parentsLists.get(geneticRelation);
      count += listView.getPersons().size();
      if (count > 2) {
        listView.pseudoClassStateChanged(PseudoClasses.INVALID, true);
        invalid = true;
        break;
      }
    }

    this.getDialogPane().lookupButton(ButtonTypes.OK).setDisable(invalid);
  }

  /**
   * Update the given person object with data from this dialog’s fields.
   *
   * @param person The person to update.
   */
  private void updatePerson(@NotNull Person person) {
    // Profile
    person.setAssignedGenderAtBirth(this.agabCombo.getSelectionModel().getSelectedItem().data());
    person.setGender(this.genderCombo.getSelectionModel().getSelectedItem().data());
    person.setLegalLastName(this.getText(this.legalLastNameField));
    person.setPublicLastName(this.getText(this.publicLastNameField));
    person.setLegalFirstNames(this.splitText(this.legalFirstNamesField));
    person.setPublicFirstNames(this.splitText(this.publicFirstNamesField));
    person.setNicknames(this.splitText(this.nicknamesField));
    person.setMainOccupation(this.getText(this.mainOccupationField));
    person.setDisambiguationID(this.getDisambiguationID());
    person.setNotes(this.getText(this.notesField));
    person.setSources(this.getText(this.sourcesField));

    // Life events
    this.lifeEventsList.getItems().forEach(LifeEventView::applyChanges);
    for (final LifeEventView lifeEventView : this.eventsToDelete) {
      final LifeEvent event = lifeEventView.lifeEvent();
      this.familyTree.removeActorFromLifeEvent(event, person);
      this.familyTree.removeWitnessFromLifeEvent(event, person);
    }
    // Update life status after events to avoid assertion error
    person.setLifeStatus(this.lifeStatusCombo.getSelectionModel().getSelectedItem().data());

    // Clear parents of the current type
    for (final var parentType : ParentalRelationType.values())
      for (final Person parent : new HashSet<>(this.person.parents(parentType)))
        this.person.removeParent(parent);
    // Add back the selected parents
    for (final var parentType : ParentalRelationType.values())
      this.parentsLists.get(parentType).getPersons().forEach(p -> this.person.addParent(p, parentType));
  }

  /**
   * Get the disambiguation ID from the corresponding text field.
   */
  private @Nullable Integer getDisambiguationID() {
    return StringUtils.stripNullable(this.disambiguationIDField.getText())
        .map(Integer::parseInt)
        .orElse(null);
  }

  /**
   * Strip the text of the given text input.
   *
   * @param textInput Text input to get the text from.
   * @return The stripped text.
   */
  private @Nullable String getText(final @NotNull TextInputControl textInput) {
    return StringUtils.stripNullable(textInput.getText()).orElse(null);
  }

  /**
   * Split the text of the given text input according to whitespace.
   *
   * @param textInput Text input to get the text from.
   * @return A list of the split text’s tokens.
   */
  private List<String> splitText(final @NotNull TextInputControl textInput) {
    return Arrays.stream(textInput.getText().split("\\s+"))
        .map(String::strip)
        .filter(s -> !s.isEmpty())
        .toList();
  }
}
