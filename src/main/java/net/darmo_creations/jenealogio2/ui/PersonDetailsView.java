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
  private final VBox eventActorsPane = new VBox(4);
  private final Label eventPlaceLabel = new Label();
  private final TextFlow eventNotesTextFlow = new TextFlow();
  private final TextFlow eventSourcesTextFlow = new TextFlow();
  private final ListView<PersonCard> eventWitnessesList = new ListView<>();
  private final ListView<PictureView> eventImagesList = new ListView<>();

  private final PersonCard parent1Card;
  private final PersonCard parent2Card;
  private final ListView<ChildrenItem> siblingsList = new ListView<>();
  private final ListView<ChildrenItem> childrenList = new ListView<>();

  private final ListView<PersonCard> adoptiveParentsList = new ListView<>();
  private final ListView<PersonCard> godparentsList = new ListView<>();
  private final ListView<PersonCard> fosterParentsList = new ListView<>();

  private final List<PersonClickListener> personClickListeners = new LinkedList<>();
  private final List<NewParentClickListener> newParentClickListeners = new LinkedList<>();
  private final List<Consumer<Picture>> imageEditedListeners = new LinkedList<>();

  private final ListView<PictureView> imageList = new ListView<>();

  private final EditImageDialog editImageDialog;

  private LifeEvent displayedLifeEvent = null;

  /**
   * Create a new person view.
   *
   * @param config The app’s config.
   */
  public PersonDetailsView(final @NotNull Config config) {
    super();
    this.config = config;
    Language language = config.language();
    Theme theme = config.theme();

    this.editImageDialog = new EditImageDialog(config);
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
    Language language = this.config.language();

    SplitPane tabPane = new SplitPane();
    tabPane.setOrientation(Orientation.VERTICAL);
    this.profileTab.setContent(tabPane);

    VBox vHeader = new VBox(4);
    vHeader.getStyleClass().add("person-details-header");
    HBox header = new HBox(8);
    this.imageView.setPreserveRatio(true);
    this.imageView.setFitWidth(100);
    this.imageView.setFitHeight(100);
    this.imageView.setOnMouseClicked(e -> this.onMainImageClicked(this.person, this.imageList));
    this.fullNameLabel.getStyleClass().add("person-details-title");
    this.fullNameLabel.setWrapText(true);
    this.genderLabel.setWrapText(true);
    this.occupationLabel.getStyleClass().add("person-details-occupation");
    this.occupationLabel.setWrapText(true);
    VBox headerTexts = new VBox(4, this.fullNameLabel, this.genderLabel, this.occupationLabel);
    header.getChildren().addAll(this.imageView, headerTexts);
    vHeader.getChildren().add(header);

    VBox publicNamesBox = new VBox(4);
    HBox plnBox = new HBox(4);
    plnBox.getChildren().add(new Label(language.translate("person_details_view.public_last_name")));
    this.publicLastNameLabel.setWrapText(true);
    plnBox.getChildren().add(this.publicLastNameLabel);
    HBox pfnBox = new HBox(4);
    pfnBox.getChildren().add(new Label(language.translate("person_details_view.public_first_names")));
    this.publicFirstNamesLabel.setWrapText(true);
    pfnBox.getChildren().add(this.publicFirstNamesLabel);
    HBox nnBox = new HBox(4);
    nnBox.getChildren().add(new Label(language.translate("person_details_view.nicknames")));
    this.nicknamesLabel.setWrapText(true);
    nnBox.getChildren().add(this.nicknamesLabel);
    publicNamesBox.getChildren().addAll(plnBox, pfnBox, nnBox);

    ScrollPane notesScroll = new ScrollPane(this.notesTextFlow);
    VBox.setVgrow(notesScroll, Priority.ALWAYS);

    VBox topBox = new VBox(
        vHeader,
        publicNamesBox,
        new SectionLabel("notes"),
        notesScroll
    );
    topBox.getStyleClass().add("person-details");
    ScrollPane sourcesScroll = new ScrollPane(this.sourcesTextFlow);
    VBox.setVgrow(sourcesScroll, Priority.ALWAYS);
    VBox sourcesBox = new VBox(new SectionLabel("sources"), sourcesScroll);
    sourcesBox.getStyleClass().add("person-details");
    this.imageList.setOnMouseClicked(this::onImageListClicked);
    VBox.setVgrow(this.imageList, Priority.ALWAYS);
    VBox imagesBox = new VBox(new SectionLabel("images"), this.imageList);
    imagesBox.getStyleClass().add("person-details");
    tabPane.getItems().addAll(
        topBox,
        sourcesBox,
        imagesBox
    );
    tabPane.setDividerPositions(0.2, 0.4);
  }

  private void setupEventsTab() {
    this.eventsTabPane.setOrientation(Orientation.VERTICAL);
    this.eventsTab.contentProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue == this.eventsTabPane) {
        this.displayedLifeEvent = null; // Reset whenever a life event’s detailled view is closed
      }
    });
    this.eventsTab.setContent(this.eventsTabPane);

    this.lifeEventsList.setOnMouseClicked(event -> {
      var selectedItem = this.lifeEventsList.getSelectionModel().getSelectedItem();
      if (selectedItem != null) {
        this.showEvent(selectedItem.lifeEvent());
      }
    });
    this.lifeEventsList.getStyleClass().add("life-event-list");

    this.witnessedEventsList.setOnMouseClicked(event -> {
      var selectedItem = this.witnessedEventsList.getSelectionModel().getSelectedItem();
      if (selectedItem != null) {
        this.showEvent(selectedItem.lifeEvent());
      }
    });
    this.witnessedEventsList.getStyleClass().add("life-event-list");

    VBox.setVgrow(this.lifeEventsList, Priority.ALWAYS);
    VBox vBox = new VBox(new SectionLabel("lived_events"), this.lifeEventsList);
    vBox.getStyleClass().add("person-details");
    VBox.setVgrow(this.witnessedEventsList, Priority.ALWAYS);
    VBox vBox2 = new VBox(new SectionLabel("witnessed_events"), this.witnessedEventsList);
    vBox2.getStyleClass().add("person-details");

    this.eventsTabPane.getItems().addAll(vBox, vBox2);
    this.eventsTabPane.setDividerPositions(0.5);

    // Event pane, hidden by default

    this.eventImageView.setPreserveRatio(true);
    this.eventImageView.setFitWidth(100);
    this.eventImageView.setFitHeight(100);
    this.eventImageView.setOnMouseClicked(e -> this.onMainImageClicked(this.displayedLifeEvent, this.eventImagesList));

    this.eventTypeLabel.getStyleClass().add("person-details-title");
    this.eventDateLabel.getStyleClass().add("person-details-title");
    this.eventDateLabel.setGraphic(this.config.theme().getIcon(Icon.HELP, Icon.Size.SMALL));
    Pane spacer = new Pane();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    Button closeButton = new Button(null, this.config.theme().getIcon(Icon.CLOSE_LIFE_EVENT, Icon.Size.SMALL));
    closeButton.setTooltip(new Tooltip(this.config.language().translate("person_details_view.close_life_event")));
    closeButton.setOnAction(event -> this.eventsTab.setContent(this.eventsTabPane));
    HBox hBox = new HBox(
        4,
        this.eventImageView,
        this.eventTypeLabel,
        spacer,
        this.eventDateLabel,
        closeButton
    );
    hBox.getStyleClass().add("person-details-header");

    ScrollPane notesScroll = new ScrollPane(this.eventNotesTextFlow);
    VBox.setVgrow(notesScroll, Priority.ALWAYS);

    VBox topBox = new VBox(
        hBox,
        this.eventActorsPane,
        this.eventPlaceLabel,
        new SectionLabel("notes"),
        notesScroll
    );
    topBox.getStyleClass().add("person-details");
    ScrollPane sourcesScroll = new ScrollPane(this.eventSourcesTextFlow);
    VBox.setVgrow(sourcesScroll, Priority.ALWAYS);
    VBox sourcesBox = new VBox(new SectionLabel("sources"), sourcesScroll);
    sourcesBox.getStyleClass().add("person-details");
    VBox witnessesBox = new VBox(new SectionLabel("witnesses"), this.eventWitnessesList);
    witnessesBox.getStyleClass().add("person-details");
    this.eventImagesList.setOnMouseClicked(this::onImageListClicked);
    VBox.setVgrow(this.eventImagesList, Priority.ALWAYS);
    VBox imagesBox = new VBox(new SectionLabel("images"), this.eventImagesList);
    imagesBox.getStyleClass().add("person-details");
    this.eventPane.getItems().addAll(
        topBox,
        witnessesBox,
        sourcesBox,
        imagesBox
    );
    this.eventPane.setOrientation(Orientation.VERTICAL);
    this.eventPane.setDividerPositions(0.25, 0.5, 0.75);
  }

  private void setupFamilyTab() {
    SplitPane tabPane = new SplitPane();
    tabPane.setOrientation(Orientation.VERTICAL);
    this.familyTab.setContent(tabPane);

    VBox parentsVBox = new VBox(4, this.parent1Card, this.parent2Card);
    parentsVBox.setMinHeight(0);
    VBox topBox = new VBox(new SectionLabel("parents"), parentsVBox);
    topBox.getStyleClass().add("person-details");

    this.siblingsList.setSelectionModel(new NoSelectionModel<>());
    VBox.setVgrow(this.siblingsList, Priority.ALWAYS);
    VBox middleBox = new VBox(new SectionLabel("siblings"), this.siblingsList);
    middleBox.getStyleClass().add("person-details");

    this.childrenList.setSelectionModel(new NoSelectionModel<>());
    VBox.setVgrow(this.childrenList, Priority.ALWAYS);
    VBox bottomBox = new VBox(new SectionLabel("children"), this.childrenList);
    bottomBox.getStyleClass().add("person-details");

    tabPane.getItems().addAll(topBox, middleBox, bottomBox);
    tabPane.setDividerPositions(0.33, 0.67);
  }

  private void setupFosterParentsTab() {
    SplitPane tabPane = new SplitPane();
    tabPane.setOrientation(Orientation.VERTICAL);
    this.fosterParentsTab.setContent(tabPane);

    this.adoptiveParentsList.setSelectionModel(new NoSelectionModel<>());
    VBox.setVgrow(this.adoptiveParentsList, Priority.ALWAYS);
    VBox topBox = new VBox(new SectionLabel("adoptive_parents"), this.adoptiveParentsList);
    topBox.getStyleClass().add("person-details");

    this.godparentsList.setSelectionModel(new NoSelectionModel<>());
    VBox.setVgrow(this.godparentsList, Priority.ALWAYS);
    VBox middleBox = new VBox(new SectionLabel("godparents"), this.godparentsList);
    middleBox.getStyleClass().add("person-details");

    this.fosterParentsList.setSelectionModel(new NoSelectionModel<>());
    VBox.setVgrow(this.fosterParentsList, Priority.ALWAYS);
    VBox bottomBox = new VBox(new SectionLabel("foster_parents"), this.fosterParentsList);
    bottomBox.getStyleClass().add("person-details");

    tabPane.getItems().addAll(topBox, middleBox, bottomBox);
    tabPane.setDividerPositions(0.33, 0.67);
  }

  /**
   * Select the main picture of the given {@link GenealogyObject} in the given image list.
   *
   * @param object     The object whose main picture was clicked. May be null.
   * @param imagesList The image list in which to select the picture.
   */
  private void onMainImageClicked(final GenealogyObject<?> object, @NotNull ListView<PictureView> imagesList) {
    if (object != null) {
      object.mainPicture()
          .flatMap(mainPicture -> imagesList.getItems().stream()
              .filter(pv -> pv.picture() == mainPicture)
              .findFirst()
          ).ifPresent(pv -> {
            imagesList.scrollTo(pv);
            imagesList.getSelectionModel().select(pv);
          });
    }
  }

  private void onImageListClicked(final @NotNull MouseEvent event) {
    if (event.getClickCount() > 1) {
      //noinspection unchecked
      this.onEditImage((ListView<PictureView>) event.getSource());
    }
  }

  private void onEditImage(@NotNull ListView<PictureView> list) {
    List<PictureView> selection = list.getSelectionModel().getSelectedItems();
    if (selection.size() == 1) {
      PictureView pictureView = selection.get(0);
      Picture picture = pictureView.picture();
      this.editImageDialog.setPicture(picture, this.familyTree);
      this.editImageDialog.showAndWait().ifPresent(b -> {
        pictureView.refresh();
        this.imageEditedListeners.forEach(l -> l.accept(picture));
        list.getItems().sort(null);
      });
    }
  }

  public void setPerson(final Person person, @NotNull final FamilyTree familyTree) {
    if (person != null && this.person == person) {
      return;
    }
    this.person = person;
    this.familyTree = Objects.requireNonNull(familyTree);

    this.resetLists();

    if (this.person != null) {
      this.populateFields();
    } else {
      this.resetFields();
    }
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

    this.imageList.getItems().clear();

    this.eventImagesList.getItems().clear();
    this.eventsTab.setContent(this.eventsTabPane);
  }

  private void populateFields() {
    this.imageView.setImage(this.person.mainPicture().flatMap(Picture::image).orElse(null));
    this.fullNameLabel.setText(this.person.toString());
    this.fullNameLabel.setTooltip(new Tooltip(this.person.toString()));
    Optional<Gender> gender = this.person.gender();
    String g = null;
    if (gender.isPresent()) {
      RegistryEntryKey key = gender.get().key();
      String name = key.name();
      g = key.isBuiltin() ? this.config.language().translate("genders." + name) : gender.get().userDefinedName();
    }
    this.genderLabel.setText(g);
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
          var key1 = e1.getKey();
          var key2 = e2.getKey();
          Person left1 = key1.left(), right1 = key1.right();
          Person left2 = key2.left(), right2 = key2.right();
          Supplier<Integer> testRight = () -> {
            if (right1 == null) {
              return right2 != null ? 1 : 0;
            }
            if (right2 == null) {
              return -1;
            }
            return Person.birthDateThenNameComparator(false).compare(right1, right2);
          };
          if (left1 == null) {
            if (left2 != null) {
              return 1;
            }
            return testRight.get();
          }
          if (left2 == null) {
            return -1;
          }
          int c = Person.birthDateThenNameComparator(false).compare(left1, left2);
          if (c != 0) {
            return c;
          }
          return testRight.get();
        })
        .forEach(e -> {
          // Sort children
          List<Person> sortedChildren = e.getValue().stream()
              .sorted(Person.birthDateThenNameComparator(false))
              .toList();
          var parents = e.getKey();
          this.siblingsList.getItems().add(new ChildrenItem(parents.left(), parents.right(), sortedChildren));
        });

    this.person.getPartnersAndChildren().entrySet().stream()
        .sorted((e1, e2) -> {
          Optional<Person> p1 = e1.getKey();
          Optional<Person> p2 = e2.getKey();
          boolean p2Present = p2.isPresent();
          if (p1.isEmpty()) {
            return p2Present ? 1 : 0;
          }
          if (!p2Present) {
            return -1;
          }
          return Person.birthDateThenNameComparator(false).compare(p1.get(), p2.get());
        })
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

    this.person.pictures().forEach(p -> this.imageList.getItems().add(new PictureView(p, false, this.config)));
    this.imageList.getItems().sort(null);
  }

  private void populateParentCards() {
    var parents = this.person.parents();
    Person parent1 = parents.left().orElse(null);
    Person parent2 = parents.right().orElse(null);
    Set<Person> sameParentsSiblings = this.person.getSameParentsSiblings();
    sameParentsSiblings.add(this.person);
    List<ChildInfo> childInfo = new LinkedList<>();
    for (Person child : sameParentsSiblings) {
      childInfo.add(new ChildInfo(child, 0));
    }
    this.parent1Card.setPerson(parent1, childInfo);
    this.parent1Card.setVisible(true);
    childInfo.clear();
    for (Person child : sameParentsSiblings) {
      childInfo.add(new ChildInfo(child, 1));
    }
    this.parent2Card.setPerson(parent2, childInfo);
    this.parent2Card.setVisible(true);
  }

  private void resetFields() {
    this.imageView.setImage(null);
    this.fullNameLabel.setText(null);
    this.fullNameLabel.setTooltip(null);
    this.genderLabel.setText(null);
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

    Language language = this.config.language();

    String text;
    if (lifeEvent.type().isBuiltin()) {
      text = language.translate("life_event_types." + lifeEvent.type().key().name());
    } else {
      text = Objects.requireNonNull(lifeEvent.type().userDefinedName());
    }
    this.eventTypeLabel.setText(text);

    this.eventActorsPane.getChildren().clear();

    List<Person> actors = lifeEvent.actors().stream()
        .sorted(Person.birthDateThenNameComparator(false)).toList();
    boolean first = true;
    for (Person actor : actors) {
      Label label = new Label(language.translate("person_details_view.life_events." + (first ? "of" : "and")));
      Button b = new Button(actor.toString(), this.config.theme().getIcon(Icon.GO_TO, Icon.Size.SMALL));
      b.setOnAction(event -> PersonDetailsView.this.firePersonClickEvent(b));
      b.setUserData(actor);
      HBox hBox = new HBox(4, label, b);
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

    this.eventImagesList.getItems().clear();
    lifeEvent.pictures().forEach(p -> this.eventImagesList.getItems().add(new PictureView(p, false, this.config)));
    this.eventImagesList.getItems().sort(null);

    this.eventsTab.setContent(this.eventPane);
  }

  public final List<PersonClickListener> personClickListeners() {
    return this.personClickListeners;
  }

  public final List<NewParentClickListener> newParentClickListeners() {
    return this.newParentClickListeners;
  }

  public List<Consumer<Picture>> imageEditedListeners() {
    return this.imageEditedListeners;
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
    var clickType = PersonClickedEvent.getClickType(1, mouseButton);
    this.firePersonClickEvent(new PersonClickedEvent(person, clickType));
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
      Label label = new Label(PersonDetailsView.this.config.language().translate("person_details_view." + key));
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
    private final VBox imagePane = new VBox();
    private final ImageView imageView = new ImageView();
    private final Label nameLabel = new Label();
    private final DateLabel birthDateLabel = new DateLabel("?", PersonDetailsView.this.config);
    private final DateLabel deathDateLabel = new DateLabel("?", PersonDetailsView.this.config);

    private final List<ChildInfo> childInfo = new ArrayList<>();

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
      super(4);

      this.getStyleClass().add("person-widget");

      final int imageSize = 50;
      final int inset = 2;
      final int size = imageSize + 2 * inset;
      this.imagePane.setPadding(new Insets(inset));
      HBox imageBoxInner = new HBox(this.imagePane);
      imageBoxInner.setAlignment(Pos.CENTER);
      VBox imageBoxOuter = new VBox(imageBoxInner);
      imageBoxOuter.setAlignment(Pos.CENTER);
      imageBoxOuter.setMinWidth(size);
      imageBoxOuter.setMaxWidth(size);
      imageBoxOuter.setPrefWidth(size);
      this.imageView.setPreserveRatio(true);
      this.imageView.setFitHeight(imageSize);
      this.imageView.setFitWidth(imageSize);
      this.imagePane.getChildren().add(this.imageView);

      Theme theme = PersonDetailsView.this.config.theme();
      this.birthDateLabel.setGraphic(theme.getIcon(Icon.BIRTH, Icon.Size.SMALL));
      this.deathDateLabel.setGraphic(theme.getIcon(Icon.DEATH, Icon.Size.SMALL));

      this.getChildren().addAll(
          imageBoxOuter,
          new VBox(4, this.nameLabel, this.birthDateLabel, this.deathDateLabel)
      );

      this.setPerson(person, childInfo);
    }

    /**
     * Set the person to display info of.
     *
     * @param person Person to display.
     */
    public void setPerson(final Person person, final @NotNull List<ChildInfo> childInfo) {
      this.childInfo.clear();
      this.childInfo.addAll(childInfo);
      boolean isNull = person == null;
      this.nameLabel.setVisible(!isNull);
      this.birthDateLabel.setVisible(!isNull);
      this.deathDateLabel.setVisible(!isNull);

      if (isNull) {
        this.imageView.setImage(PersonWidget.ADD_IMAGE);
        this.imagePane.setStyle(null);
        this.setOnMouseClicked(event -> {
          PersonDetailsView.this.fireNewParentClickEvent(this.childInfo);
          event.consume();
        });
        return;
      }

      this.setOnMouseClicked(event -> {
        PersonDetailsView.this.firePersonClickEvent(person, event.getButton());
        event.consume();
      });

      this.imageView.setImage(person.mainPicture().flatMap(Picture::image).orElse(PersonWidget.DEFAULT_IMAGE));
      String genderColor = person.gender().map(Gender::color).orElse(Gender.MISSING_COLOR);
      this.imagePane.setStyle("-fx-background-color: " + genderColor);

      String name = person.toString();
      this.nameLabel.setText(name);
      this.nameLabel.setTooltip(new Tooltip(name));
      if (PersonDetailsView.this.familyTree.isRoot(person)) {
        this.nameLabel.setGraphic(PersonDetailsView.this.config.theme().getIcon(Icon.TREE_ROOT, Icon.Size.SMALL));
      } else {
        this.nameLabel.setGraphic(null);
      }

      this.birthDateLabel.setDateTime(person.getBirthDate().orElse(null));

      boolean consideredDeceased = person.lifeStatus().isConsideredDeceased();
      this.deathDateLabel.setVisible(consideredDeceased);
      if (consideredDeceased) {
        this.deathDateLabel.setDateTime(person.getDeathDate().orElse(null));
      }
    }
  }

  /**
   * Item for showing a {@link LifeEvent} in a {@link ListView}.
   */
  private class LifeEventItem extends VBox {
    private final LifeEvent lifeEvent;

    private LifeEventItem(final @NotNull LifeEvent lifeEvent, final @NotNull Person mainActor) {
      super(4);
      this.lifeEvent = lifeEvent;
      this.getStyleClass().add("life-events-list-item");
      Config config = PersonDetailsView.this.config;
      Language language = config.language();
      HBox header = new HBox(4);
      header.getStyleClass().add("life-events-list-item-header");
      RegistryEntryKey typeKey = lifeEvent.type().key();
      String type;
      if (typeKey.isBuiltin()) {
        type = language.translate("life_event_types." + typeKey.name());
      } else {
        type = Objects.requireNonNull(lifeEvent.type().userDefinedName());
      }
      Label typeLabel = new Label(type);
      Pane spacer = new Pane();
      HBox.setHgrow(spacer, Priority.ALWAYS);
      DateLabel dateLabel = new DateLabel(lifeEvent.date(), null, config);
      dateLabel.setGraphic(config.theme().getIcon(Icon.HELP, Icon.Size.SMALL));
      header.getChildren().addAll(typeLabel, spacer, dateLabel);
      this.getChildren().add(header);

      if (lifeEvent.type().maxActors() > 1) {
        Optional<Person> partner = lifeEvent.actors().stream().filter(p -> p != mainActor).findFirst();
        if (partner.isPresent()) {
          Label partnerLabel = new Label(language.translate("person_details_view.life_events.with"));
          Button b = new Button(partner.get().toString(), config.theme().getIcon(Icon.GO_TO, Icon.Size.SMALL));
          b.setUserData(partner.get());
          b.setOnAction(event -> PersonDetailsView.this.firePersonClickEvent(b));
          HBox hBox = new HBox(4, partnerLabel, b);
          hBox.setAlignment(Pos.CENTER_LEFT);
          this.getChildren().add(hBox);
        }
      }

      Label placeLabel = new Label(lifeEvent.place().map(Place::address).orElse(""));
      placeLabel.setWrapText(true);
      this.getChildren().add(placeLabel);
    }

    /**
     * The life event wrapped by this node.
     */
    public LifeEvent lifeEvent() {
      return this.lifeEvent;
    }
  }

  /**
   * Item for showing a {@link LifeEvent} that the current person witnessed, in a {@link ListView}.
   */
  private class WitnessedEventItem extends VBox {
    private final LifeEvent lifeEvent;

    public WitnessedEventItem(final @NotNull LifeEvent lifeEvent) {
      super(4);
      this.lifeEvent = lifeEvent;
      this.getStyleClass().add("life-events-list-item");
      Language language = PersonDetailsView.this.config.language();
      HBox header = new HBox(4);
      header.getStyleClass().add("life-events-list-item-header");
      RegistryEntryKey typeKey = lifeEvent.type().key();
      String type;
      if (typeKey.isBuiltin()) {
        type = language.translate("life_event_types." + typeKey.name());
      } else {
        type = Objects.requireNonNull(lifeEvent.type().userDefinedName());
      }
      Label typeLabel = new Label(type);
      Pane spacer = new Pane();
      HBox.setHgrow(spacer, Priority.ALWAYS);
      DateLabel dateLabel = new DateLabel(lifeEvent.date(), null, PersonDetailsView.this.config);
      dateLabel.setGraphic(PersonDetailsView.this.config.theme().getIcon(Icon.HELP, Icon.Size.SMALL));
      header.getChildren().addAll(typeLabel, spacer, dateLabel);
      this.getChildren().add(header);

      VBox actorsBox = new VBox(4);
      List<Person> actors = lifeEvent.actors().stream()
          .sorted(Person.birthDateThenNameComparator(false)).toList();
      boolean first = true;
      for (Person actor : actors) {
        Label label = new Label(language.translate("person_details_view.life_events." + (first ? "of" : "and")));
        Button b = new Button(actor.toString(), PersonDetailsView.this.config.theme().getIcon(Icon.GO_TO, Icon.Size.SMALL));
        b.setOnAction(event -> PersonDetailsView.this.firePersonClickEvent(b));
        b.setUserData(actor);
        HBox hBox = new HBox(4, label, b);
        hBox.setAlignment(Pos.CENTER_LEFT);
        actorsBox.getChildren().add(hBox);
        first = false;
      }
      this.getChildren().add(actorsBox);
    }

    /**
     * The life event wrapped by this node.
     */
    public LifeEvent lifeEvent() {
      return this.lifeEvent;
    }
  }

  /**
   * Item for showing the children of a person in a {@link ListView}.
   */
  private class ChildrenItem extends VBox {
    public ChildrenItem(final Person parent1, final Person parent2, final @NotNull List<Person> children) {
      super(4);
      List<ChildInfo> childInfo = new LinkedList<>();
      for (Person child : children) {
        //noinspection OptionalGetWithoutIsPresent
        int parentIndex = child.getParentIndex(parent1).get(); // Will always exist in this context
        childInfo.add(new ChildInfo(child, parentIndex));
      }
      Theme theme = PersonDetailsView.this.config.theme();
      HBox parentsBox = new HBox(4);
      PersonCard parent1Card = new PersonCard(parent1, childInfo);
      HBox.setHgrow(parent1Card, Priority.ALWAYS);
      parentsBox.getChildren().add(parent1Card);
      if (parent2 != null) {
        PersonCard parent2Card = new PersonCard(parent2);
        HBox.setHgrow(parent2Card, Priority.ALWAYS);
        Label plus = new Label("", theme.getIcon(Icon.PLUS, Icon.Size.BIG));
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
            Label arrow = new Label("", theme.getIcon(Icon.GO_TO, Icon.Size.BIG));
            PersonCard childCard = new PersonCard(child);
            HBox.setHgrow(childCard, Priority.ALWAYS);
            HBox hBox = new HBox(8, arrow, childCard);
            hBox.setAlignment(Pos.CENTER_LEFT);
            this.getChildren().add(hBox);
          });
    }
  }

  private class ClickableImageView extends ImageView {
    private boolean internalUpdate = false;

    public ClickableImageView(final @NotNull Image defaultImage) {
      Tooltip tooltip = new Tooltip(PersonDetailsView.this.config.language().translate("person_details_view.main_image.tooltip"));
      this.setImage(defaultImage);
      this.imageProperty().addListener((observable, oldValue, newValue) -> {
        if (this.internalUpdate) {
          return;
        }
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
