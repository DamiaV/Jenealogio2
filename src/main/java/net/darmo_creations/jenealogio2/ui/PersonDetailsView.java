package net.darmo_creations.jenealogio2.ui;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import net.darmo_creations.jenealogio2.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.themes.*;
import net.darmo_creations.jenealogio2.ui.components.*;
import net.darmo_creations.jenealogio2.ui.dialogs.*;
import net.darmo_creations.jenealogio2.ui.events.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.*;

/**
 * This view shows all available information about a specific {@link Person} object.
 */
public class PersonDetailsView extends TabPane implements PersonClickObservable {
  @SuppressWarnings("DataFlowIssue")
  public static final Image DEFAULT_EVENT_IMAGE =
      new Image(PersonWidget.class.getResourceAsStream(App.IMAGES_PATH + "default_event_image.png"));

  private final Config config;

  private Person person;
  private FamilyTree familyTree;

  private final Tab profileTab = new Tab();
  private final Tab eventsTab = new Tab();
  private final Tab familyTab = new Tab();
  private final Tab fosterParentsTab = new Tab();

  private final ClickableImageView imageView;
  private final Label fullNameLabel = new Label();
  private final Label genderLabel = new Label();
  private final Label occupationLabel = new Label();
  private final Label publicLastNameLabel = new Label();
  private final Label publicFirstNamesLabel = new Label();
  private final Label nicknamesLabel = new Label();
  private final TextFlow notesTextFlow = new TextFlow();
  private final TextFlow sourcesTextFlow = new TextFlow();

  private final SplitPane eventsTabPane = new SplitPane();
  private final ListView<LifeEventItem> lifeEventsList = new ListView<>();
  private final ListView<WitnessedEventItem> witnessedEventsList = new ListView<>();

  private final SplitPane eventPane = new SplitPane();
  private final ClickableImageView eventImageView;
  private final Label eventTypeLabel = new Label();
  private final DateLabel eventDateLabel;
  private final VBox eventActorsPane = new VBox(5);
  private final Label eventPlaceLabel = new Label();
  private final TextFlow eventNotesTextFlow = new TextFlow();
  private final TextFlow eventSourcesTextFlow = new TextFlow();
  private final ListView<PersonCard> eventWitnessesList = new ListView<>();
  private final ListView<DocumentView> eventDocumentsList = new ListView<>();

  private final PersonCard parent1Card;
  private final PersonCard parent2Card;
  private final ListView<ChildrenItem> siblingsList = new ListView<>();
  private final ListView<ChildrenItem> childrenList = new ListView<>();

  private final ListView<PersonCard> adoptiveParentsList = new ListView<>();
  private final ListView<PersonCard> godparentsList = new ListView<>();
  private final ListView<PersonCard> fosterParentsList = new ListView<>();

  private final List<PersonClickListener> personClickListeners = new LinkedList<>();
  private final List<NewParentClickListener> newParentClickListeners = new LinkedList<>();
  private final List<Consumer<AttachedDocument>> documentEditedListeners = new LinkedList<>();

  private final ListView<DocumentView> documentsList = new ListView<>();

  private final EditDocumentDialog editDocumentDialog;

  private LifeEvent displayedLifeEvent = null;

  /**
   * Create a new person view.
   *
   * @param config The app’s config.
   */
  public PersonDetailsView(final @NotNull Config config) {
    super();
    this.config = config;
    final Language language = config.language();
    final Theme theme = config.theme();

    this.editDocumentDialog = new EditDocumentDialog(config);
    this.imageView = new ClickableImageView(PersonWidget.DEFAULT_IMAGE);
    this.eventImageView = new ClickableImageView(DEFAULT_EVENT_IMAGE);
    this.eventDateLabel = new DateLabel(null, config);
    this.parent1Card = new PersonCard(null);
    this.parent2Card = new PersonCard(null);

    this.profileTab.setText(language.translate("person_details_view.profile_tab.title"));
    this.profileTab.setGraphic(theme.getIcon(Icon.PROFILE_TAB, Icon.Size.SMALL));
    this.eventsTab.setText(language.translate("person_details_view.events_tab.title"));
    this.eventsTab.setGraphic(theme.getIcon(Icon.LIFE_EVENTS_TAB, Icon.Size.SMALL));
    this.familyTab.setText(language.translate("person_details_view.family_tab.title"));
    this.familyTab.setGraphic(theme.getIcon(Icon.FAMILY_TAB, Icon.Size.SMALL));
    this.fosterParentsTab.setText(language.translate("person_details_view.foster_parents_tab.title"));
    this.fosterParentsTab.setGraphic(theme.getIcon(Icon.FOSTER_PARENTS_TAB, Icon.Size.SMALL));
    this.getTabs().addAll(
        this.profileTab,
        this.eventsTab,
        this.familyTab,
        this.fosterParentsTab
    );
    this.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

    this.setMinWidth(200);

    this.setupProfileTab();
    this.setupEventsTab();
    this.setupFamilyTab();
    this.setupFosterParentsTab();
  }

  private void setupProfileTab() {
    final Language language = this.config.language();

    final SplitPane tabPane = new SplitPane();
    tabPane.setOrientation(Orientation.VERTICAL);
    this.profileTab.setContent(tabPane);

    final VBox vHeader = new VBox(5);
    vHeader.getStyleClass().add("person-details-header");
    final HBox header = new HBox(8);
    this.imageView.setPreserveRatio(true);
    this.imageView.setFitWidth(100);
    this.imageView.setFitHeight(100);
    this.imageView.setOnMouseClicked(e -> this.onMainImageClicked(this.person, this.documentsList));
    this.fullNameLabel.getStyleClass().add("person-details-title");
    this.fullNameLabel.setWrapText(true);
    this.genderLabel.setWrapText(true);
    this.occupationLabel.getStyleClass().add("person-details-occupation");
    this.occupationLabel.setWrapText(true);
    final VBox headerTexts = new VBox(5, this.fullNameLabel, this.genderLabel, this.occupationLabel);
    header.getChildren().addAll(this.imageView, headerTexts);
    vHeader.getChildren().add(header);

    final VBox publicNamesBox = new VBox(5);
    final HBox plnBox = new HBox(5);
    plnBox.getChildren().add(new Label(language.translate("person_details_view.public_last_name")));
    this.publicLastNameLabel.setWrapText(true);
    plnBox.getChildren().add(this.publicLastNameLabel);
    final HBox pfnBox = new HBox(5);
    pfnBox.getChildren().add(new Label(language.translate("person_details_view.public_first_names")));
    this.publicFirstNamesLabel.setWrapText(true);
    pfnBox.getChildren().add(this.publicFirstNamesLabel);
    final HBox nnBox = new HBox(5);
    nnBox.getChildren().add(new Label(language.translate("person_details_view.nicknames")));
    this.nicknamesLabel.setWrapText(true);
    nnBox.getChildren().add(this.nicknamesLabel);
    publicNamesBox.getChildren().addAll(plnBox, pfnBox, nnBox);

    final ScrollPane notesScroll = new ScrollPane(this.notesTextFlow);
    VBox.setVgrow(notesScroll, Priority.ALWAYS);

    final VBox topBox = new VBox(
        vHeader,
        publicNamesBox,
        new SectionLabel("notes"),
        notesScroll
    );
    topBox.getStyleClass().add("person-details");
    final ScrollPane sourcesScroll = new ScrollPane(this.sourcesTextFlow);
    VBox.setVgrow(sourcesScroll, Priority.ALWAYS);
    final VBox sourcesBox = new VBox(new SectionLabel("sources"), sourcesScroll);
    sourcesBox.getStyleClass().add("person-details");
    this.documentsList.setOnMouseClicked(this::onDocumentListClicked);
    VBox.setVgrow(this.documentsList, Priority.ALWAYS);
    final VBox documentsBox = new VBox(new SectionLabel("documents"), this.documentsList);
    documentsBox.getStyleClass().add("person-details");
    tabPane.getItems().addAll(
        topBox,
        sourcesBox,
        documentsBox
    );
    tabPane.setDividerPositions(0.2, 0.4);
  }

  private void setupEventsTab() {
    this.eventsTabPane.setOrientation(Orientation.VERTICAL);
    this.eventsTab.contentProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue == this.eventsTabPane)
        this.displayedLifeEvent = null; // Reset whenever a life event’s detailled view is closed
    });
    this.eventsTab.setContent(this.eventsTabPane);

    this.lifeEventsList.setOnMouseClicked(event -> {
      final var selectedItem = this.lifeEventsList.getSelectionModel().getSelectedItem();
      if (selectedItem != null)
        this.showEvent(selectedItem.lifeEvent());
    });
    this.lifeEventsList.getStyleClass().add("life-event-list");

    this.witnessedEventsList.setOnMouseClicked(event -> {
      final var selectedItem = this.witnessedEventsList.getSelectionModel().getSelectedItem();
      if (selectedItem != null)
        this.showEvent(selectedItem.lifeEvent());
    });
    this.witnessedEventsList.getStyleClass().add("life-event-list");

    VBox.setVgrow(this.lifeEventsList, Priority.ALWAYS);
    final VBox vBox = new VBox(new SectionLabel("lived_events"), this.lifeEventsList);
    vBox.getStyleClass().add("person-details");
    VBox.setVgrow(this.witnessedEventsList, Priority.ALWAYS);
    final VBox vBox2 = new VBox(new SectionLabel("witnessed_events"), this.witnessedEventsList);
    vBox2.getStyleClass().add("person-details");

    this.eventsTabPane.getItems().addAll(vBox, vBox2);
    this.eventsTabPane.setDividerPositions(0.5);

    // Event pane, hidden by default

    this.eventImageView.setPreserveRatio(true);
    this.eventImageView.setFitWidth(100);
    this.eventImageView.setFitHeight(100);
    this.eventImageView.setOnMouseClicked(e -> this.onMainImageClicked(this.displayedLifeEvent, this.eventDocumentsList));

    this.eventTypeLabel.getStyleClass().add("person-details-title");
    this.eventDateLabel.getStyleClass().add("person-details-title");
    final Pane spacer = new Pane();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    final Button closeButton = new Button(null, this.config.theme().getIcon(Icon.CLOSE_LIFE_EVENT, Icon.Size.SMALL));
    closeButton.setTooltip(new Tooltip(this.config.language().translate("person_details_view.close_life_event")));
    closeButton.setOnAction(event -> this.eventsTab.setContent(this.eventsTabPane));
    final HBox hBox = new HBox(
        5,
        this.eventImageView,
        this.eventTypeLabel,
        spacer,
        this.eventDateLabel,
        closeButton
    );
    hBox.getStyleClass().add("person-details-header");

    final ScrollPane notesScroll = new ScrollPane(this.eventNotesTextFlow);
    VBox.setVgrow(notesScroll, Priority.ALWAYS);

    final VBox topBox = new VBox(
        hBox,
        this.eventActorsPane,
        this.eventPlaceLabel,
        new SectionLabel("notes"),
        notesScroll
    );
    topBox.getStyleClass().add("person-details");
    final ScrollPane sourcesScroll = new ScrollPane(this.eventSourcesTextFlow);
    VBox.setVgrow(sourcesScroll, Priority.ALWAYS);
    final VBox sourcesBox = new VBox(new SectionLabel("sources"), sourcesScroll);
    sourcesBox.getStyleClass().add("person-details");
    final VBox witnessesBox = new VBox(new SectionLabel("witnesses"), this.eventWitnessesList);
    witnessesBox.getStyleClass().add("person-details");
    this.eventDocumentsList.setOnMouseClicked(this::onDocumentListClicked);
    VBox.setVgrow(this.eventDocumentsList, Priority.ALWAYS);
    final VBox documentsBox = new VBox(new SectionLabel("documents"), this.eventDocumentsList);
    documentsBox.getStyleClass().add("person-details");
    this.eventPane.getItems().addAll(
        topBox,
        witnessesBox,
        sourcesBox,
        documentsBox
    );
    this.eventPane.setOrientation(Orientation.VERTICAL);
    this.eventPane.setDividerPositions(0.25, 0.5, 0.75);
  }

  private void setupFamilyTab() {
    final SplitPane tabPane = new SplitPane();
    tabPane.setOrientation(Orientation.VERTICAL);
    this.familyTab.setContent(tabPane);

    final VBox parentsVBox = new VBox(5, this.parent1Card, this.parent2Card);
    parentsVBox.setMinHeight(0);
    final VBox topBox = new VBox(new SectionLabel("parents"), parentsVBox);
    topBox.getStyleClass().add("person-details");

    this.siblingsList.setSelectionModel(new NoSelectionModel<>());
    VBox.setVgrow(this.siblingsList, Priority.ALWAYS);
    final VBox middleBox = new VBox(new SectionLabel("siblings"), this.siblingsList);
    middleBox.getStyleClass().add("person-details");

    this.childrenList.setSelectionModel(new NoSelectionModel<>());
    VBox.setVgrow(this.childrenList, Priority.ALWAYS);
    final VBox bottomBox = new VBox(new SectionLabel("children"), this.childrenList);
    bottomBox.getStyleClass().add("person-details");

    tabPane.getItems().addAll(topBox, middleBox, bottomBox);
    tabPane.setDividerPositions(0.33, 0.67);
  }

  private void setupFosterParentsTab() {
    final SplitPane tabPane = new SplitPane();
    tabPane.setOrientation(Orientation.VERTICAL);
    this.fosterParentsTab.setContent(tabPane);

    this.adoptiveParentsList.setSelectionModel(new NoSelectionModel<>());
    VBox.setVgrow(this.adoptiveParentsList, Priority.ALWAYS);
    final VBox topBox = new VBox(new SectionLabel("adoptive_parents"), this.adoptiveParentsList);
    topBox.getStyleClass().add("person-details");

    this.godparentsList.setSelectionModel(new NoSelectionModel<>());
    VBox.setVgrow(this.godparentsList, Priority.ALWAYS);
    final VBox middleBox = new VBox(new SectionLabel("godparents"), this.godparentsList);
    middleBox.getStyleClass().add("person-details");

    this.fosterParentsList.setSelectionModel(new NoSelectionModel<>());
    VBox.setVgrow(this.fosterParentsList, Priority.ALWAYS);
    final VBox bottomBox = new VBox(new SectionLabel("foster_parents"), this.fosterParentsList);
    bottomBox.getStyleClass().add("person-details");

    tabPane.getItems().addAll(topBox, middleBox, bottomBox);
    tabPane.setDividerPositions(0.33, 0.67);
  }

  /**
   * Select the main picture of the given {@link GenealogyObject} in the given document list.
   *
   * @param object        The object whose main picture was clicked. May be null.
   * @param documentsList The document list in which to select the picture.
   */
  private void onMainImageClicked(
      final GenealogyObject<?> object,
      @NotNull ListView<DocumentView> documentsList
  ) {
    if (object != null) {
      object.mainPicture()
          .flatMap(mainPicture -> documentsList.getItems().stream()
              .filter(pv -> pv.document() instanceof Picture pic && pic == mainPicture)
              .findFirst()
          ).ifPresent(pv -> {
            documentsList.scrollTo(pv);
            documentsList.getSelectionModel().select(pv);
          });
    }
  }

  private void onDocumentListClicked(final @NotNull MouseEvent event) {
    if (event.getClickCount() > 1)
      //noinspection unchecked
      this.onEditDocument((ListView<DocumentView>) event.getSource());
  }

  private void onEditDocument(@NotNull ListView<DocumentView> list) {
    final List<DocumentView> selection = list.getSelectionModel().getSelectedItems();
    if (selection.size() == 1) {
      final DocumentView documentView = selection.get(0);
      final AttachedDocument document = documentView.document();
      this.editDocumentDialog.setDocument(document, this.familyTree);
      this.editDocumentDialog.showAndWait().ifPresent(b -> {
        if (b.getButtonData().isCancelButton())
          return;
        documentView.refresh();
        this.documentEditedListeners.forEach(l -> l.accept(document));
        list.getItems().sort(null);
      });
    }
  }

  public void setPerson(final Person person, @NotNull final FamilyTree familyTree) {
    if (person != null && this.person == person)
      return;
    this.person = person;
    this.familyTree = Objects.requireNonNull(familyTree);

    this.resetLists();

    if (this.person != null)
      this.populateFields();
    else
      this.resetFields();
  }

  /**
   * The currently visible life event.
   */
  public Optional<LifeEvent> getDisplayedLifeEvent() {
    return Optional.ofNullable(this.displayedLifeEvent);
  }

  private void resetLists() {
    this.notesTextFlow.getChildren().clear();
    this.sourcesTextFlow.getChildren().clear();

    this.lifeEventsList.getItems().clear();
    this.witnessedEventsList.getItems().clear();

    this.siblingsList.getItems().clear();
    this.childrenList.getItems().clear();

    this.adoptiveParentsList.getItems().clear();
    this.godparentsList.getItems().clear();
    this.fosterParentsList.getItems().clear();

    this.documentsList.getItems().clear();

    this.eventDocumentsList.getItems().clear();
    this.eventsTab.setContent(this.eventsTabPane);
  }

  private void populateFields() {
    this.imageView.setImage(this.person.mainPicture().flatMap(Picture::image).orElse(null));
    this.fullNameLabel.setText(this.person.toString());
    this.fullNameLabel.setTooltip(new Tooltip(this.person.toString()));
    final Optional<Gender> gender = this.person.gender();
    String g = null;
    if (gender.isPresent()) {
      final RegistryEntryKey key = gender.get().key();
      if (key.isBuiltin())
        g = this.config.language().translate("genders." + key.name());
      else
        g = gender.get().userDefinedName();
    }
    this.genderLabel.setText(g);
    this.genderLabel.setGraphic(gender.map(g_ -> new ImageView(g_.icon())).orElse(null));
    this.genderLabel.setTooltip(g != null ? new Tooltip(g) : null);
    this.occupationLabel.setText(this.person.mainOccupation().orElse(null));
    this.occupationLabel.setTooltip(this.person.mainOccupation().map(Tooltip::new).orElse(null));
    this.publicLastNameLabel.setText(this.person.publicLastName().orElse("-"));
    this.publicLastNameLabel.setTooltip(this.person.publicLastName().map(Tooltip::new).orElse(null));
    this.publicFirstNamesLabel.setText(this.person.getJoinedPublicFirstNames().orElse("-"));
    this.publicFirstNamesLabel.setTooltip(this.person.getJoinedPublicFirstNames().map(Tooltip::new).orElse(null));
    this.nicknamesLabel.setText(this.person.getJoinedNicknames().orElse("-"));
    this.nicknamesLabel.setTooltip(this.person.getJoinedNicknames().map(Tooltip::new).orElse(null));
    this.person.notes().ifPresent(s -> this.notesTextFlow.getChildren().addAll(StringUtils.parseText(s, App::openURL)));
    this.person.sources().ifPresent(s -> this.sourcesTextFlow.getChildren().addAll(StringUtils.parseText(s, App::openURL)));

    this.person.getLifeEventsAsActor().stream()
        .sorted()
        .forEach(lifeEvent -> this.lifeEventsList.getItems().add(new LifeEventItem(lifeEvent, this.person)));
    this.person.getLifeEventsAsWitness().stream()
        .sorted()
        .forEach(lifeEvent -> this.witnessedEventsList.getItems().add(new WitnessedEventItem(lifeEvent)));

    this.populateParentCards();

    this.person.getAllSiblings().entrySet().stream()
        // Sort according to first parent then second
        .sorted((e1, e2) -> {
          final var key1 = e1.getKey();
          final var key2 = e2.getKey();
          final Optional<Person> left1 = key1.parent1();
          final Optional<Person> right1 = key1.parent2();
          final Optional<Person> left2 = key2.parent1();
          final Optional<Person> right2 = key2.parent2();
          final Supplier<Integer> testRight = () -> {
            if (right1.isEmpty())
              return right2.isPresent() ? 1 : 0;
            //noinspection OptionalIsPresent
            if (right2.isEmpty())
              return -1;
            return Person.birthDateThenNameComparator(false).compare(right1.get(), right2.get());
          };
          if (left1.isEmpty()) {
            if (left2.isPresent())
              return 1;
            return testRight.get();
          }
          if (left2.isEmpty())
            return -1;
          final int c = Person.birthDateThenNameComparator(false).compare(left1.get(), left2.get());
          if (c != 0)
            return c;
          return testRight.get();
        })
        .forEach(e -> {
          // Sort children
          final List<Person> sortedChildren = e.getValue().stream()
              .sorted(Person.birthDateThenNameComparator(false))
              .toList();
          final var parents = e.getKey();
          this.siblingsList.getItems().add(new ChildrenItem(
              parents.parent1().orElse(null),
              parents.parent2().orElse(null),
              sortedChildren
          ));
        });

    this.person.getPartnersAndChildren().entrySet().stream()
        .sorted((e1, e2) -> Person.optionalBirthDateThenNameComparator().compare(e1.getKey(), e2.getKey()))
        .forEach(e -> this.childrenList.getItems().add(new ChildrenItem(e.getKey().orElse(null), null, new LinkedList<>(e.getValue()))));

    this.person.getRelatives(Person.RelativeType.ADOPTIVE).stream()
        .sorted(Person.birthDateThenNameComparator(false))
        .forEach(parent -> this.adoptiveParentsList.getItems().add(new PersonCard(parent)));
    this.person.getRelatives(Person.RelativeType.GOD).stream()
        .sorted(Person.birthDateThenNameComparator(false))
        .forEach(parent -> this.godparentsList.getItems().add(new PersonCard(parent)));
    this.person.getRelatives(Person.RelativeType.FOSTER).stream()
        .sorted(Person.birthDateThenNameComparator(false))
        .forEach(parent -> this.fosterParentsList.getItems().add(new PersonCard(parent)));

    this.person.documents().forEach(p -> this.documentsList.getItems().add(new DocumentView(p, false, this.config)));
    this.documentsList.getItems().sort(null);
  }

  private void populateParentCards() {
    final var parents = this.person.parents();
    final Person parent1 = parents.parent1().orElse(null);
    final Person parent2 = parents.parent2().orElse(null);
    final Set<Person> sameParentsSiblings = this.person.getSameParentsSiblings();
    sameParentsSiblings.add(this.person);
    final List<ChildInfo> childInfo = new LinkedList<>();
    for (final Person child : sameParentsSiblings)
      childInfo.add(new ChildInfo(child, 0));
    this.parent1Card.setPerson(parent1, childInfo);
    this.parent1Card.setVisible(true);
    childInfo.clear();
    for (final Person child : sameParentsSiblings)
      childInfo.add(new ChildInfo(child, 1));
    this.parent2Card.setPerson(parent2, childInfo);
    this.parent2Card.setVisible(true);
  }

  private void resetFields() {
    this.imageView.setImage(null);
    this.fullNameLabel.setText(null);
    this.fullNameLabel.setTooltip(null);
    this.genderLabel.setText(null);
    this.genderLabel.setGraphic(null);
    this.genderLabel.setTooltip(null);
    this.occupationLabel.setText(null);
    this.occupationLabel.setTooltip(null);
    this.publicLastNameLabel.setText("-");
    this.publicLastNameLabel.setTooltip(null);
    this.publicFirstNamesLabel.setText("-");
    this.publicFirstNamesLabel.setTooltip(null);
    this.nicknamesLabel.setText("-");
    this.nicknamesLabel.setTooltip(null);

    this.parent1Card.setPerson(null, List.of());
    this.parent1Card.setVisible(false);
    this.parent2Card.setPerson(null, List.of());
    this.parent2Card.setVisible(false);
  }

  private void showEvent(final @NotNull LifeEvent lifeEvent) {
    this.displayedLifeEvent = lifeEvent;

    final Language language = this.config.language();

    final String text;
    if (lifeEvent.type().isBuiltin())
      text = language.translate("life_event_types." + lifeEvent.type().key().name());
    else
      text = Objects.requireNonNull(lifeEvent.type().userDefinedName());
    this.eventTypeLabel.setText(text);

    this.eventActorsPane.getChildren().clear();

    final List<Person> actors = lifeEvent.actors().stream()
        .sorted(Person.birthDateThenNameComparator(false)).toList();
    boolean first = true;
    for (final Person actor : actors) {
      final Label label = new Label(language.translate("person_details_view.life_events." + (first ? "of" : "and")));
      final Button b = new Button(actor.toString(), this.config.theme().getIcon(Icon.GO_TO, Icon.Size.SMALL));
      b.setOnAction(event -> PersonDetailsView.this.firePersonClickEvent(b));
      b.setUserData(actor);
      final HBox hBox = new HBox(5, label, b);
      hBox.setAlignment(Pos.CENTER_LEFT);
      this.eventActorsPane.getChildren().add(hBox);
      first = false;
    }

    this.eventDateLabel.setDateTime(lifeEvent.date());
    this.eventPlaceLabel.setText(lifeEvent.place().map(Place::address).orElse("-"));

    this.eventImageView.setImage(lifeEvent.mainPicture().flatMap(Picture::image).orElse(null));

    this.eventNotesTextFlow.getChildren().clear();
    lifeEvent.notes().ifPresent(s -> this.eventNotesTextFlow.getChildren().addAll(StringUtils.parseText(s, App::openURL)));
    this.eventSourcesTextFlow.getChildren().clear();
    lifeEvent.sources().ifPresent(s -> this.eventSourcesTextFlow.getChildren().addAll(StringUtils.parseText(s, App::openURL)));

    this.eventWitnessesList.getItems().clear();
    lifeEvent.witnesses().stream()
        .sorted(Person.birthDateThenNameComparator(false))
        .forEach(w -> this.eventWitnessesList.getItems().add(new PersonCard(w)));

    this.eventDocumentsList.getItems().clear();
    lifeEvent.documents().forEach(p -> this.eventDocumentsList.getItems().add(new DocumentView(p, false, this.config)));
    this.eventDocumentsList.getItems().sort(null);

    this.eventsTab.setContent(this.eventPane);
  }

  public final List<PersonClickListener> personClickListeners() {
    return this.personClickListeners;
  }

  public final List<NewParentClickListener> newParentClickListeners() {
    return this.newParentClickListeners;
  }

  public List<Consumer<AttachedDocument>> documentEditedListeners() {
    return this.documentEditedListeners;
  }

  /**
   * Fire a person button click event.
   *
   * @param button The clicked button.
   */
  private void firePersonClickEvent(final @NotNull Button button) {
    this.firePersonClickEvent(new PersonClickedEvent((Person) button.getUserData(),
        PersonClickedEvent.Action.SET_AS_TARGET));
  }

  /**
   * Fire a person click event.
   *
   * @param person The person.
   */
  private void firePersonClickEvent(final @NotNull Person person, @NotNull MouseButton mouseButton) {
    this.firePersonClickEvent(new PersonClickedEvent(
        person,
        PersonClickedEvent.getClickType(1, mouseButton)
    ));
  }

  /**
   * Label that displays some text in bold and bigger font on a darker background.
   */
  private class SectionLabel extends AnchorPane {
    /**
     * Create a label.
     *
     * @param key Final part of the translation key.
     */
    public SectionLabel(@NotNull String key) {
      this.getStyleClass().add("person-details-header");
      final Label label = new Label(PersonDetailsView.this.config.language().translate("person_details_view." + key));
      label.getStyleClass().add("person-details-title");
      AnchorPane.setTopAnchor(label, 0.0);
      AnchorPane.setBottomAnchor(label, 0.0);
      AnchorPane.setLeftAnchor(label, 0.0);
      AnchorPane.setRightAnchor(label, 0.0);
      this.getChildren().add(label);
    }
  }

  /**
   * Small component that displays a person’s image, name, birth and death dates.
   */
  private class PersonCard extends HBox {
    private final ImageView imageView = new ImageView();
    private final Label rootLabel = new Label();
    private final Label genderLabel = new Label();
    private final Label nameLabel = new Label();
    private final DateLabel birthDateLabel = new DateLabel("?", PersonDetailsView.this.config);
    private final DateLabel deathDateLabel = new DateLabel("?", PersonDetailsView.this.config);
    private final HBox nameBox = new HBox(5);
    private final HBox birthBox = new HBox(5);
    private final HBox deathBox = new HBox(5);

    /**
     * Create a person card.
     *
     * @param person The person to show information of.
     */
    public PersonCard(final Person person) {
      this(person, List.of());
    }

    /**
     * Create a person card.
     *
     * @param person    The person to show information of.
     * @param childInfo Information about the visible children of this person.
     */
    public PersonCard(final Person person, final @NotNull List<ChildInfo> childInfo) {
      super(5);

      this.getStyleClass().add("person-widget");

      final Language language = PersonDetailsView.this.config.language();
      final Theme theme = PersonDetailsView.this.config.theme();

      final int imageSize = 50;
      this.imageView.setPreserveRatio(true);
      this.imageView.setFitHeight(imageSize);
      this.imageView.setFitWidth(imageSize);
      final VBox imageBox = new VBox(this.imageView);
      imageBox.setAlignment(Pos.CENTER);
      imageBox.setMinHeight(imageSize);
      imageBox.setMinWidth(imageSize);

      this.rootLabel.setGraphic(theme.getIcon(Icon.TREE_ROOT, Icon.Size.SMALL));
      this.rootLabel.setTooltip(new Tooltip(language.translate("person_widget.root.tooltip")));
      this.rootLabel.managedProperty().bind(this.rootLabel.visibleProperty());
      this.genderLabel.managedProperty().bind(this.genderLabel.visibleProperty());
      this.nameBox.getChildren().addAll(this.rootLabel, this.genderLabel, this.nameLabel);

      this.birthBox.getChildren().addAll(new Label(null, theme.getIcon(Icon.BIRTH, Icon.Size.SMALL)), this.birthDateLabel);
      this.deathBox.getChildren().addAll(new Label(null, theme.getIcon(Icon.DEATH, Icon.Size.SMALL)), this.deathDateLabel);

      this.getChildren().addAll(
          imageBox,
          new VBox(5, this.nameBox, this.birthBox, this.deathBox)
      );

      this.setPerson(person, childInfo);
    }

    /**
     * Set the person to display info of.
     *
     * @param person Person to display.
     */
    public void setPerson(final Person person, final @NotNull List<ChildInfo> childInfo) {
      final boolean isNull = person == null;
      this.nameBox.setVisible(!isNull);
      this.birthBox.setVisible(!isNull);
      this.deathBox.setVisible(!isNull);

      if (isNull) {
        this.imageView.setImage(PersonWidget.ADD_IMAGE);
        this.setOnMouseClicked(event -> {
          PersonDetailsView.this.fireNewParentClickEvent(childInfo);
          event.consume();
        });
        return;
      }

      this.setOnMouseClicked(event -> {
        PersonDetailsView.this.firePersonClickEvent(person, event.getButton());
        event.consume();
      });

      this.imageView.setImage(person.mainPicture().flatMap(Picture::image).orElse(PersonWidget.DEFAULT_IMAGE));

      final String name = person.toString();
      this.nameLabel.setText(name);
      this.nameLabel.setTooltip(new Tooltip(name));
      this.rootLabel.setVisible(PersonDetailsView.this.familyTree.isRoot(person));
      final Optional<Gender> gender = person.gender();
      final boolean present = gender.isPresent();
      this.genderLabel.setVisible(present);
      if (present) {
        final String label;
        if (gender.get().isBuiltin())
          label = PersonDetailsView.this.config.language().translate("genders." + gender.get().key().name());
        else
          label = Objects.requireNonNull(gender.get().userDefinedName());
        this.genderLabel.setGraphic(new ImageView(gender.get().icon()));
        this.genderLabel.setTooltip(new Tooltip(label));
      } else {
        this.genderLabel.setGraphic(null);
        this.genderLabel.setTooltip(null);
      }

      this.birthDateLabel.setDateTime(person.getBirthDate().orElse(null));

      final boolean consideredDeceased = person.lifeStatus().isConsideredDeceased();
      this.deathBox.setVisible(consideredDeceased);
      if (consideredDeceased)
        this.deathDateLabel.setDateTime(person.getDeathDate().orElse(null));
    }
  }

  private abstract class EventItem extends VBox {
    protected final LifeEvent lifeEvent;

    private EventItem(final @NotNull LifeEvent lifeEvent) {
      super(5);
      this.lifeEvent = Objects.requireNonNull(lifeEvent);
      this.getStyleClass().add("life-events-list-item");
      final Config config = PersonDetailsView.this.config;
      final Language language = config.language();
      final HBox header = new HBox(5);
      header.getStyleClass().add("life-events-list-item-header");
      final RegistryEntryKey typeKey = lifeEvent.type().key();
      final String type;
      if (typeKey.isBuiltin())
        type = language.translate("life_event_types." + typeKey.name());
      else
        type = Objects.requireNonNull(lifeEvent.type().userDefinedName());
      final Label typeLabel = new Label(type);
      final Pane spacer = new Pane();
      HBox.setHgrow(spacer, Priority.ALWAYS);
      final DateLabel dateLabel = new DateLabel(lifeEvent.date(), null, config);
      header.getChildren().addAll(typeLabel, spacer, dateLabel);
      this.getChildren().add(header);
    }

    /**
     * The life event wrapped by this node.
     */
    public LifeEvent lifeEvent() {
      return this.lifeEvent;
    }
  }

  /**
   * Item for showing a {@link LifeEvent} lived by a {@link Person}.
   */
  private class LifeEventItem extends EventItem {
    private LifeEventItem(final @NotNull LifeEvent lifeEvent, final @NotNull Person mainActor) {
      super(lifeEvent);
      final Config config = PersonDetailsView.this.config;
      if (lifeEvent.type().maxActors() > 1) {
        final Optional<Person> partner = lifeEvent.actors().stream().filter(p -> p != mainActor).findFirst();
        if (partner.isPresent()) {
          final Label partnerLabel = new Label(config.language().translate("person_details_view.life_events.with"));
          final Button b = new Button(partner.get().toString(), config.theme().getIcon(Icon.GO_TO, Icon.Size.SMALL));
          b.setUserData(partner.get());
          b.setOnAction(event -> PersonDetailsView.this.firePersonClickEvent(b));
          final HBox hBox = new HBox(5, partnerLabel, b);
          hBox.setAlignment(Pos.CENTER_LEFT);
          this.getChildren().add(hBox);
        }
      }

      final Label placeLabel = new Label(lifeEvent.place().map(Place::address).orElse(""));
      placeLabel.setWrapText(true);
      this.getChildren().add(placeLabel);
    }
  }

  /**
   * Item for showing a {@link LifeEvent} that the current person witnessed.
   */
  private class WitnessedEventItem extends EventItem {
    public WitnessedEventItem(final @NotNull LifeEvent lifeEvent) {
      super(lifeEvent);
      final Config config = PersonDetailsView.this.config;
      final VBox actorsBox = new VBox(5);
      final List<Person> actors = lifeEvent.actors().stream()
          .sorted(Person.birthDateThenNameComparator(false)).toList();
      boolean first = true;
      for (final Person actor : actors) {
        final Label label = new Label(config.language().translate("person_details_view.life_events." + (first ? "of" : "and")));
        final Button b = new Button(actor.toString(), config.theme().getIcon(Icon.GO_TO, Icon.Size.SMALL));
        b.setOnAction(event -> PersonDetailsView.this.firePersonClickEvent(b));
        b.setUserData(actor);
        final HBox hBox = new HBox(5, label, b);
        hBox.setAlignment(Pos.CENTER_LEFT);
        actorsBox.getChildren().add(hBox);
        first = false;
      }
      this.getChildren().add(actorsBox);
    }
  }

  /**
   * Item for showing the children of a person in a {@link ListView}.
   */
  private class ChildrenItem extends VBox {
    public ChildrenItem(final Person parent1, final Person parent2, final @NotNull List<Person> children) {
      super(5);
      final List<ChildInfo> childInfo = new LinkedList<>();
      for (final Person child : children)
        //noinspection OptionalGetWithoutIsPresent
        childInfo.add(new ChildInfo(child, child.getParentIndex(parent1).get())); // Will always exist in this context
      final Theme theme = PersonDetailsView.this.config.theme();
      final HBox parentsBox = new HBox(5);
      final PersonCard parent1Card = new PersonCard(parent1, childInfo);
      HBox.setHgrow(parent1Card, Priority.ALWAYS);
      parentsBox.getChildren().add(parent1Card);
      if (parent2 != null) {
        final PersonCard parent2Card = new PersonCard(parent2);
        HBox.setHgrow(parent2Card, Priority.ALWAYS);
        final Label plus = new Label("", theme.getIcon(Icon.PLUS, Icon.Size.BIG));
        parentsBox.setAlignment(Pos.CENTER);
        parentsBox.getChildren().addAll(
            plus,
            parent2Card
        );
      }
      this.getChildren().add(parentsBox);
      children.stream()
          .sorted(Person.birthDateThenNameComparator(false))
          .forEach(child -> {
            final Label arrow = new Label("", theme.getIcon(Icon.GO_TO, Icon.Size.BIG));
            final PersonCard childCard = new PersonCard(child);
            HBox.setHgrow(childCard, Priority.ALWAYS);
            final HBox hBox = new HBox(8, arrow, childCard);
            hBox.setAlignment(Pos.CENTER_LEFT);
            this.getChildren().add(hBox);
          });
    }
  }

  private class ClickableImageView extends ImageView {
    private boolean internalUpdate = false;

    public ClickableImageView(final @NotNull Image defaultImage) {
      final Tooltip tooltip = new Tooltip(PersonDetailsView.this.config.language().translate("person_details_view.main_image.tooltip"));
      this.setImage(defaultImage);
      this.imageProperty().addListener((observable, oldValue, newValue) -> {
        if (this.internalUpdate)
          return;
        this.internalUpdate = true;
        if (newValue != null) {
          this.getStyleClass().add("clickable-image");
          Tooltip.install(this, tooltip);
          this.setImage(newValue);
        } else {
          this.getStyleClass().remove("clickable-image");
          Tooltip.uninstall(this, tooltip);
          this.setImage(defaultImage);
        }
        this.internalUpdate = false;
      });
    }
  }
}
