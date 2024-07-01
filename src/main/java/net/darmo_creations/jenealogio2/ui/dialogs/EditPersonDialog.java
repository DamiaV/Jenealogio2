package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.collections.*;
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
  private final ComboBox<ComboBoxItem<Gender>> genderCombo = new ComboBox<>();
  private final TextField mainOccupationField = new TextField();
  private final TextField disambiguationIDField = new TextField();
  private final TextArea notesField = new TextArea();
  private final TextArea sourcesField = new TextArea();

  private final Button addEventButton = new Button();
  private final ListView<LifeEventView> lifeEventsList = new ListView<>();

  private final Label parent1Label = new Label();
  private final Button removeParent1Button = new Button();
  private final Label parent2Label = new Label();
  private final Button removeParent2Button = new Button();
  private final Button swapParentsButton = new Button();

  private final Map<Person.RelativeType, RelativesListView> relativesLists = new HashMap<>();

  private final SelectPersonDialog selectPersonDialog;
  private final SelectCoordinatesDialog selectCoordinatesDialog;

  /**
   * The person object being edited.
   */
  private Person person;
  /**
   * The parents of the person.
   */
  private Person parent1, parent2;
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
    super(config, "edit_person", true, ButtonTypes.OK, ButtonTypes.CANCEL);
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
            info.child().setParent(info.parentIndex(), this.person);
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

    {
      this.lifeStatusCombo.getItems().addAll(Arrays.stream(LifeStatus.values())
          .map(lifeStatus -> new NotNullComboBoxItem<>(
              lifeStatus,
              language.translate("life_status." + lifeStatus.name().toLowerCase())
          ))
          .toList());
      this.lifeStatusCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
        if (!this.internalLifeStatusChange)
          this.lifeStatusCache = newValue.data();
      });
      this.addRow(gridPane, 0, "dialog.edit_person.profile.life_status", this.lifeStatusCombo);
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
      gridPane.addRow(1, hBox, this.legalLastNameField);
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
      gridPane.addRow(2, hBox, this.legalFirstNamesField);
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
      gridPane.addRow(3, hBox, this.publicLastNameField);
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
      gridPane.addRow(4, hBox, this.publicFirstNamesField);
      final RowConstraints rc = new RowConstraints();
      rc.setVgrow(Priority.SOMETIMES);
      gridPane.getRowConstraints().add(rc);
    }

    {
      this.addRow(gridPane, 5, "dialog.edit_person.profile.nicknames", this.nicknamesField);
      final RowConstraints rc = new RowConstraints();
      rc.setVgrow(Priority.SOMETIMES);
      gridPane.getRowConstraints().add(rc);
    }

    {
      this.addRow(gridPane, 6, "dialog.edit_person.profile.gender", this.genderCombo);
      final RowConstraints rc = new RowConstraints();
      rc.setVgrow(Priority.SOMETIMES);
      gridPane.getRowConstraints().add(rc);
    }

    {
      this.addRow(gridPane, 7, "dialog.edit_person.profile.main_occupation", this.mainOccupationField);
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
      this.disambiguationIDField.textProperty().addListener((observable, oldValue, newValue) -> this.updateButtons());
      gridPane.addRow(8, hBox, this.disambiguationIDField);
      final RowConstraints rc = new RowConstraints();
      rc.setVgrow(Priority.SOMETIMES);
      gridPane.getRowConstraints().add(rc);
    }

    {
      final Label notesLabel = new Label(language.translate("dialog.edit_person.profile.notes"));
      notesLabel.setPadding(new Insets(3, 0, 0, 0));
      GridPane.setValignment(notesLabel, VPos.TOP);
      gridPane.addRow(9, notesLabel, this.notesField);
      final RowConstraints rc = new RowConstraints();
      rc.setVgrow(Priority.ALWAYS);
      gridPane.getRowConstraints().add(rc);
    }

    {
      final Label sourcesLabel = new Label(language.translate("dialog.edit_person.profile.sources"));
      sourcesLabel.setPadding(new Insets(3, 0, 0, 0));
      GridPane.setValignment(sourcesLabel, VPos.TOP);
      gridPane.addRow(10, sourcesLabel, this.sourcesField);
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

    {
      final Button parent1Button = new Button();
      parent1Button.setGraphic(theme.getIcon(Icon.EDIT_PARENT, Icon.Size.SMALL));
      parent1Button.setTooltip(new Tooltip(language.translate("dialog.edit_person.parents.parents.edit_parent1")));
      parent1Button.setOnAction(e -> this.onParentSelect(1));
      this.removeParent1Button.setGraphic(theme.getIcon(Icon.REMOVE_PARENT, Icon.Size.SMALL));
      this.removeParent1Button.setTooltip(new Tooltip(language.translate("dialog.edit_person.parents.parents.remove_parent1")));
      this.removeParent1Button.setOnAction(e -> this.onRemoveParent(1));
      final Button parent2Button = new Button();
      parent2Button.setGraphic(theme.getIcon(Icon.EDIT_PARENT, Icon.Size.SMALL));
      parent2Button.setTooltip(new Tooltip(language.translate("dialog.edit_person.parents.parents.edit_parent2")));
      parent2Button.setOnAction(e -> this.onParentSelect(2));
      this.removeParent2Button.setGraphic(theme.getIcon(Icon.REMOVE_PARENT, Icon.Size.SMALL));
      this.removeParent2Button.setTooltip(new Tooltip(language.translate("dialog.edit_person.parents.parents.remove_parent2")));
      this.removeParent2Button.setOnAction(e -> this.onRemoveParent(2));
      this.swapParentsButton.setGraphic(theme.getIcon(Icon.SWAP_PARENTS, Icon.Size.SMALL));
      this.swapParentsButton.setTooltip(new Tooltip(language.translate("dialog.edit_person.parents.parents.swap")));
      this.swapParentsButton.setOnAction(e -> this.onSwapParents());
      final HBox parentsBox = new HBox(
          5,
          this.parent1Label,
          parent1Button,
          this.removeParent1Button,
          this.swapParentsButton,
          this.parent2Label,
          parent2Button,
          this.removeParent2Button
      );
      parentsBox.setAlignment(Pos.CENTER_LEFT);
      this.addRow(gridPane, 0, "dialog.edit_person.parents.parents", parentsBox);
      final RowConstraints rc = new RowConstraints();
      rc.setVgrow(Priority.SOMETIMES);
      gridPane.getRowConstraints().add(rc);
    }

    int i = 1;
    for (final Person.RelativeType type : Person.RelativeType.values()) {
      final RelativesListView component = new RelativesListView(this.config);
      component.setPersonRequestListener(this);
      this.relativesLists.put(type, component);
      final Label label = new Label(language.translate(
          "dialog.edit_person.parents.%s_parents".formatted(type.name().toLowerCase())));
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
   * Update the gender entries in the genders combobox.
   */
  public void updateGendersList() {
    final Language language = this.config.language();
    final Collator collator = Collator.getInstance(language.locale());
    this.genderCombo.getItems().clear();
    this.genderCombo.getItems().add(new ComboBoxItem<>(null, language.translate("genders.unknown")));
    this.genderCombo.getItems().addAll(this.familyTree.genderRegistry().entries().stream()
        .map(gender -> {
          final String text = gender.isBuiltin()
              ? language.translate("genders." + gender.key().name())
              : Objects.requireNonNull(gender.userDefinedName());
          return new ComboBoxItem<>(gender, text);
        })
        .sorted((i1, i2) -> collator.compare(i1.text(), i2.text())) // Perform locale-dependent comparison
        .toList());
  }

  /**
   * Set the person to edit.
   *
   * @param person     The person to edit. A null value indicates to create a new person.
   * @param childInfo  If not null, indicates the persons the one being created should be a parent of.
   * @param familyTree The family tree the person belongs or should belong to.
   */
  public void setPerson(Person person, final @NotNull List<ChildInfo> childInfo, @NotNull FamilyTree familyTree) {
    this.childInfo = new ArrayList<>(childInfo);
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

    this.updateGendersList();
    this.setPersonProfileFields();
    this.setPersonLifeEventsFields();
    this.setPersonRelativesFields();
    this.updateButtons();
  }

  private void setPersonProfileFields() {
    this.lifeStatusCombo.getSelectionModel().select(new NotNullComboBoxItem<>(this.person.lifeStatus()));
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

  private void setPersonRelativesFields() {
    final var parents = this.person.parents();
    this.setParents(parents.left().orElse(null), parents.right().orElse(null));
    for (final var type : Person.RelativeType.values())
      this.relativesLists.get(type)
          .setPersons(this.person.getRelatives(type));
  }

  /**
   * Set the values of the parents fields.
   *
   * @param parent1 Person to set as parent 1.
   * @param parent2 Person to set as parent 2.
   */
  public void setParents(Person parent1, Person parent2) {
    final Language language = this.config.language();
    final String cssClass = "unknown";
    final ObservableList<String> styleClass1 = this.parent1Label.getStyleClass();
    if (parent1 != null) {
      this.parent1 = parent1;
      this.parent1Label.setText(parent1.toString());
      styleClass1.remove(cssClass);
    } else {
      this.parent1 = null;
      this.parent1Label.setText(language.translate("dialog.edit_person.parents.parents.unknown"));
      if (!styleClass1.contains(cssClass))
        styleClass1.add(cssClass);
    }
    final ObservableList<String> styleClass2 = this.parent2Label.getStyleClass();
    if (parent2 != null) {
      this.parent2 = parent2;
      this.parent2Label.setText(parent2.toString());
      styleClass2.remove(cssClass);
    } else {
      this.parent2 = null;
      this.parent2Label.setText(language.translate("dialog.edit_person.parents.parents.unknown"));
      if (!styleClass2.contains(cssClass))
        styleClass2.add(cssClass);
    }
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
   * Open the parent selection dialog for the given parent index and
   * replace the corresponding parent with the chosen person.
   *
   * @param parentIndex Either 1 or 2.
   */
  private void onParentSelect(int parentIndex) {
    final List<Person> excl = new LinkedList<>();
    excl.add(this.person);
    if (this.parent1 != null)
      excl.add(this.parent1);
    if (this.parent2 != null)
      excl.add(this.parent2);
    this.selectPersonDialog.updatePersonList(this.familyTree, excl);
    this.selectPersonDialog.showAndWait().ifPresent(p -> {
      if (parentIndex == 1)
        this.setParents(p, this.parent2);
      else if (parentIndex == 2)
        this.setParents(this.parent1, p);
      else
        throw new IllegalArgumentException("Invalid parent index: " + parentIndex);
    });
    this.updateButtons();
  }

  /**
   * Remove the parent with the given index.
   *
   * @param parentIndex Index of the parent to remove.
   */
  private void onRemoveParent(int parentIndex) {
    if (parentIndex == 1)
      this.setParents(null, this.parent2);
    else if (parentIndex == 2)
      this.setParents(this.parent1, null);
    else
      throw new IllegalArgumentException("Invalid parent index: " + parentIndex);
    this.updateButtons();
  }

  private void onSwapParents() {
    this.setParents(this.parent2, this.parent1);
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
        this.config, prefix + "header", prefix + "content", prefix + "title");
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

    final boolean sameParents = this.parent1 != null && this.parent1 == this.parent2;
    this.parent1Label.pseudoClassStateChanged(PseudoClasses.INVALID, sameParents);
    this.parent2Label.pseudoClassStateChanged(PseudoClasses.INVALID, sameParents);
    invalid |= sameParents;

    this.removeParent1Button.setDisable(this.parent1 == null);
    this.removeParent2Button.setDisable(this.parent2 == null);
    this.swapParentsButton.setDisable(this.parent1 == null && this.parent2 == null);

    this.getDialogPane().lookupButton(ButtonTypes.OK).setDisable(invalid);
  }

  /**
   * Update the given person object with data from this dialog’s fields.
   *
   * @param person The person to update.
   */
  private void updatePerson(@NotNull Person person) {
    // Profile
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

    // Relatives
    person.setParent(0, this.parent1);
    person.setParent(1, this.parent2);
    for (final var type : Person.RelativeType.values()) {
      // Clear relatives of the current type
      for (final Person relative : this.person.getRelatives(type))
        this.person.removeRelative(relative, type);
      // Add back the selected relatives
      this.relativesLists.get(type).getPersons().forEach(p -> this.person.addRelative(p, type));
    }
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
