package net.darmo_creations.jenealogio2.ui;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import net.darmo_creations.jenealogio2.App;
import net.darmo_creations.jenealogio2.config.Config;
import net.darmo_creations.jenealogio2.config.Language;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.themes.Icon;
import net.darmo_creations.jenealogio2.themes.Theme;
import net.darmo_creations.jenealogio2.ui.components.PersonWidget;
import net.darmo_creations.jenealogio2.ui.dialogs.NoSelectionModel;
import net.darmo_creations.jenealogio2.utils.DateTimeUtils;
import net.darmo_creations.jenealogio2.utils.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class PersonDetailsView extends TabPane {
  private Person person;

  private final Tab profileTab = new Tab();
  private final Tab eventsTab = new Tab();
  private final Tab familyTab = new Tab();
  private final Tab fosterParentsTab = new Tab();

  private final ImageView imageView = new ImageView();
  private final Label fullNameLabel = new Label();
  private final Label genderLabel = new Label();
  private final Label occupationLabel = new Label();
  private final Label publicLastNameLabel = new Label();
  private final Label publicFirstNamesLabel = new Label();
  private final Label nicknamesLabel = new Label();
  private final Label notesLabel = new Label();
  private final Label sourcesLabel = new Label();

  private final ListView<LifeEventItem> lifeEventsList = new ListView<>();
  private final ListView<WitnessedEventItem> witnessedEventsList = new ListView<>();

  private final PersonCard parent1Card = new PersonCard(null);
  private final PersonCard parent2Card = new PersonCard(null);
  private final ListView<ChildrenItem> siblingsList = new ListView<>();
  private final ListView<ChildrenItem> childrenList = new ListView<>();

  private final ListView<PersonCard> adoptiveParentsList = new ListView<>();
  private final ListView<PersonCard> godparentsList = new ListView<>();
  private final ListView<PersonCard> fosterParentsList = new ListView<>();

  private final List<PersonClickListener> personClickListeners = new LinkedList<>();

  public PersonDetailsView() {
    super();
    Language language = App.config().language();

    this.profileTab.setText(language.translate("person_details_view.profile_tab.title"));
    this.eventsTab.setText(language.translate("person_details_view.events_tab.title"));
    this.familyTab.setText(language.translate("person_details_view.family_tab.title"));
    this.fosterParentsTab.setText(language.translate("person_details_view.foster_parents_tab.title"));
    this.getTabs().addAll(this.profileTab, this.eventsTab, this.familyTab, this.fosterParentsTab);
    this.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

    this.setMinWidth(200);

    this.setupProfileTab();
    this.setupEventsTab();
    this.setupFamilyTab();
    this.setupFosterParentsTab();
  }

  private void setupProfileTab() {
    Language language = App.config().language();

    VBox tabPane = new VBox();
    tabPane.getStyleClass().add("person-details");
    this.profileTab.setContent(tabPane);

    VBox vHeader = new VBox(4);
    vHeader.getStyleClass().add("person-details-header");
    HBox header = new HBox(8);
    this.imageView.setPreserveRatio(true);
    this.imageView.setFitWidth(100);
    this.imageView.setFitHeight(100);
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

    this.notesLabel.setWrapText(true);

    this.sourcesLabel.setWrapText(true);

    tabPane.getChildren().addAll(
        vHeader,
        publicNamesBox,
        new SectionLabel("notes"),
        this.notesLabel,
        new SectionLabel("sources"),
        this.sourcesLabel
    );
  }

  private void setupEventsTab() {
    SplitPane tabPane = new SplitPane();
    tabPane.setOrientation(Orientation.VERTICAL);
    this.eventsTab.setContent(tabPane);

    this.lifeEventsList.getStyleClass().add("life-events-list");
    // TODO show details when clicked

    VBox.setVgrow(this.lifeEventsList, Priority.ALWAYS);
    VBox vBox = new VBox(this.lifeEventsList);
    vBox.getStyleClass().add("person-details");
    VBox.setVgrow(this.witnessedEventsList, Priority.ALWAYS);
    VBox vBox2 = new VBox(new SectionLabel("witnessed_events"), this.witnessedEventsList);
    vBox2.getStyleClass().add("person-details");
    // TODO show details when clicked

    tabPane.getItems().addAll(vBox, vBox2);
    tabPane.setDividerPositions(0.5);
  }

  private void setupFamilyTab() {
    SplitPane tabPane = new SplitPane();
    tabPane.setOrientation(Orientation.VERTICAL);
    this.familyTab.setContent(tabPane);

    VBox parentsVBox = new VBox(4, this.parent1Card, this.parent2Card);
    this.siblingsList.setSelectionModel(new NoSelectionModel<>());
    VBox.setVgrow(this.siblingsList, Priority.ALWAYS);
    VBox topBox = new VBox(
        new SectionLabel("parents"),
        parentsVBox,
        new SectionLabel("siblings"),
        this.siblingsList
    );
    topBox.getStyleClass().add("person-details");

    this.childrenList.setSelectionModel(new NoSelectionModel<>());
    VBox.setVgrow(this.childrenList, Priority.ALWAYS);
    VBox bottomBox = new VBox(new SectionLabel("children"), this.childrenList);
    bottomBox.getStyleClass().add("person-details");

    tabPane.getItems().addAll(topBox, bottomBox);
    tabPane.setDividerPositions(0.6);
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

  public void setPerson(final Person person) {
    this.person = person;
    this.populateFields();
  }

  private void populateFields() {
    this.lifeEventsList.getItems().clear();
    this.witnessedEventsList.getItems().clear();
    this.siblingsList.getItems().clear();
    this.childrenList.getItems().clear();
    this.adoptiveParentsList.getItems().clear();
    this.godparentsList.getItems().clear();
    this.fosterParentsList.getItems().clear();

    if (this.person != null) {
      this.imageView.setImage(this.person.getImage().orElse(PersonWidget.DEFAULT_IMAGE));
      this.fullNameLabel.setText(this.person.toString());
      this.fullNameLabel.setTooltip(new Tooltip(this.person.toString()));
      Optional<Gender> gender = this.person.gender();
      String g = null;
      if (gender.isPresent()) {
        RegistryEntryKey key = gender.get().key();
        String name = key.name();
        g = key.namespace().equals(Registry.BUILTIN_NS) ? App.config().language().translate("gender." + name) : name;
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
      this.notesLabel.setText(this.person.notes().orElse(null));
      this.sourcesLabel.setText(this.person.sources().orElse(null)); // TODO make links clickable

      this.person.getLifeEventsAsActor()
          .forEach(lifeEvent -> this.lifeEventsList.getItems().add(new LifeEventItem(lifeEvent, this.person)));
      this.person.getLifeEventsAsWitness()
          .forEach(lifeEvent -> this.witnessedEventsList.getItems().add(new WitnessedEventItem(lifeEvent)));

      {
        var parents = this.person.parents();
        this.parent1Card.setPerson(parents.left().orElse(null));
        this.parent2Card.setPerson(parents.right().orElse(null));
      }

      this.person.getAllSiblings().entrySet().stream()
          // Sort according to first parent then second
          .sorted((e1, e2) -> {
            Pair<Person, Person> key1 = e1.getKey();
            Pair<Person, Person> key2 = e2.getKey();
            int c = Person.birthDateThenNameComparator(false).compare(key1.left(), key2.left());
            if (c != 0) {
              return c;
            }
            return Person.birthDateThenNameComparator(false).compare(key1.right(), key2.right());
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
          .sorted((e1, e2) -> Person.birthDateThenNameComparator(false).compare(e1.getKey(), e2.getKey()))
          .forEach(e -> this.childrenList.getItems().add(new ChildrenItem(e.getKey(), null, e.getValue())));

      this.person.getRelatives(Person.RelativeType.ADOPTIVE).stream()
          .sorted(Person.birthDateThenNameComparator(false))
          .forEach(parent -> this.adoptiveParentsList.getItems().add(new PersonCard(parent)));
      this.person.getRelatives(Person.RelativeType.GOD).stream()
          .sorted(Person.birthDateThenNameComparator(false))
          .forEach(parent -> this.godparentsList.getItems().add(new PersonCard(parent)));
      this.person.getRelatives(Person.RelativeType.FOSTER).stream()
          .sorted(Person.birthDateThenNameComparator(false))
          .forEach(parent -> this.fosterParentsList.getItems().add(new PersonCard(parent)));
    } else {
      this.imageView.setImage(PersonWidget.DEFAULT_IMAGE);
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
      this.notesLabel.setText(null);
      this.sourcesLabel.setText(null);

      this.parent1Card.setPerson(null);
      this.parent2Card.setPerson(null);
    }
  }

  /**
   * The list of all listeners to person button clicks.
   */
  public final List<PersonClickListener> personClickListeners() {
    return this.personClickListeners;
  }

  /**
   * Fire a person button click event.
   *
   * @param button The clicked button.
   */
  protected final void firePersonClickEvent(final @NotNull Button button) {
    this.firePersonClickEvent((Person) button.getUserData(), MouseButton.PRIMARY);
  }

  /**
   * Fire a person click event.
   *
   * @param person The person.
   */
  protected final void firePersonClickEvent(final @NotNull Person person, @NotNull MouseButton mouseButton) {
    this.personClickListeners.forEach(listener -> listener.onClick(person, 1, mouseButton));
  }

  /**
   * Label that displays some text in bold and bigger font on a darker background.
   */
  private static class SectionLabel extends AnchorPane {
    /**
     * Create a label.
     *
     * @param key Final part of the translation key.
     */
    public SectionLabel(@NotNull String key) {
      this.getStyleClass().add("person-details-header");
      Label label = new Label(App.config().language().translate("person_details_view." + key));
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
    private final AnchorPane imagePane = new AnchorPane();
    private final ImageView imageView = new ImageView();
    private final Label nameLabel = new Label();
    private final Label birthDateLabel = new Label();
    private final Label deathDateLabel = new Label();

    /**
     * Create a person card.
     *
     * @param person The person to show information of.
     */
    public PersonCard(final Person person) {
      super(4);

      this.getStyleClass().add("person-widget");

      int size = 54;
      this.imagePane.setPrefSize(size, size);
      this.imagePane.setMinSize(size, size);
      this.imagePane.setMaxSize(size, size);
      this.imagePane.setPadding(new Insets(2));
      HBox imageBox = new HBox(this.imagePane);
      imageBox.setAlignment(Pos.CENTER);
      AnchorPane.setTopAnchor(this.imageView, 0.0);
      AnchorPane.setBottomAnchor(this.imageView, 0.0);
      AnchorPane.setLeftAnchor(this.imageView, 0.0);
      AnchorPane.setRightAnchor(this.imageView, 0.0);
      this.imageView.setPreserveRatio(true);
      this.imageView.setFitHeight(50);
      this.imageView.setFitWidth(50);
      this.imagePane.getChildren().add(this.imageView);

      Theme theme = App.config().theme();
      this.birthDateLabel.setGraphic(theme.getIcon(Icon.BIRTH, Icon.Size.SMALL));
      this.deathDateLabel.setGraphic(theme.getIcon(Icon.DEATH, Icon.Size.SMALL));

      this.getChildren().addAll(
          imageBox,
          new VBox(4, this.nameLabel, this.birthDateLabel, this.deathDateLabel)
      );

      this.setPerson(person);
    }

    /**
     * Set the person to display info of.
     *
     * @param person Person to display.
     */
    public void setPerson(final Person person) {
      this.setVisible(person != null);
      if (person == null) {
        return;
      }

      this.setOnMouseClicked(event -> {
        PersonDetailsView.this.firePersonClickEvent(person, event.getButton());
        event.consume();
      });

      this.imageView.setImage(person.getImage().orElse(PersonWidget.DEFAULT_IMAGE));
      String genderColor = person.gender().map(Gender::color).orElse(Gender.MISSING_COLOR);
      this.imagePane.setStyle("-fx-background-color: " + genderColor);

      String name = person.toString();
      this.nameLabel.setText(name);
      this.nameLabel.setTooltip(new Tooltip(name));

      String birthDate = person.getBirthDate().map(DateTimeUtils::formatCalendarDate).orElse("?");
      this.birthDateLabel.setText(birthDate);
      this.birthDateLabel.setTooltip(new Tooltip(birthDate));

      boolean consideredDeceased = person.lifeStatus().isConsideredDeceased();
      this.deathDateLabel.setVisible(consideredDeceased);
      if (consideredDeceased) {
        String deathDate = person.getDeathDate().map(DateTimeUtils::formatCalendarDate).orElse("?");
        this.deathDateLabel.setText(deathDate);
        this.deathDateLabel.setTooltip(new Tooltip(deathDate));
      }
    }
  }

  /**
   * Item for showing a {@link LifeEvent} in a {@link ListView}.
   */
  private class LifeEventItem extends VBox {
    private LifeEventItem(final @NotNull LifeEvent lifeEvent, final @NotNull Person mainActor) {
      super(4);
      this.getStyleClass().add("life-events-list-item");
      Config config = App.config();
      Language language = config.language();
      HBox header = new HBox(4);
      header.getStyleClass().add("life-events-list-item-header");
      RegistryEntryKey typeKey = lifeEvent.type().key();
      String type;
      if (typeKey.namespace().equals(Registry.BUILTIN_NS)) {
        type = language.translate("life_event_type." + typeKey.name());
      } else {
        type = typeKey.name();
      }
      Label typeLabel = new Label(type);
      Label dateLabel = new Label(DateTimeUtils.formatCalendarDate(lifeEvent.date()));
      Pane spacer = new Pane();
      HBox.setHgrow(spacer, Priority.ALWAYS);
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

      Label placeLabel = new Label(lifeEvent.place().orElse(""));
      placeLabel.setWrapText(true);
      this.getChildren().add(placeLabel);
    }
  }

  /**
   * Item for showing a {@link LifeEvent} that the current person witnessed, in a {@link ListView}.
   */
  private class WitnessedEventItem extends VBox {
    public WitnessedEventItem(final @NotNull LifeEvent lifeEvent) {
      super(4);
      this.getStyleClass().add("life-events-list-item");
      Language language = App.config().language();
      HBox header = new HBox(4);
      header.getStyleClass().add("life-events-list-item-header");
      RegistryEntryKey typeKey = lifeEvent.type().key();
      String type;
      if (typeKey.namespace().equals(Registry.BUILTIN_NS)) {
        type = language.translate("life_event_type." + typeKey.name());
      } else {
        type = typeKey.name();
      }
      Label typeLabel = new Label(type);
      Label dateLabel = new Label(DateTimeUtils.formatCalendarDate(lifeEvent.date()));
      Pane spacer = new Pane();
      HBox.setHgrow(spacer, Priority.ALWAYS);
      header.getChildren().addAll(typeLabel, spacer, dateLabel);
      this.getChildren().add(header);

      VBox actorsBox = new VBox(4);
      List<Person> actors = lifeEvent.actors().stream()
          .sorted(Person.birthDateThenNameComparator(false)).toList();
      boolean first = true;
      for (Person actor : actors) {
        Label label = new Label(language.translate("person_details_view.life_events." + (first ? "of" : "and")));
        Button b = new Button(actor.toString(), App.config().theme().getIcon(Icon.GO_TO, Icon.Size.SMALL));
        b.setOnAction(event -> PersonDetailsView.this.firePersonClickEvent(b));
        b.setUserData(actor);
        HBox hBox = new HBox(4, label, b);
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
    public ChildrenItem(final @NotNull Person parent1, final Person parent2, final @NotNull List<Person> children) {
      super(4);
      this.getChildren().add(new PersonCard(parent1));
      if (parent2 != null) {
        this.getChildren().add(new PersonCard(parent2));
      }
      children.stream()
          .sorted(Person.birthDateThenNameComparator(false))
          .forEach(child -> {
            Label arrow = new Label("", App.config().theme().getIcon(Icon.GO_TO, Icon.Size.BIG));
            PersonCard childCard = new PersonCard(child);
            HBox.setHgrow(childCard, Priority.ALWAYS);
            HBox hBox = new HBox(8, arrow, childCard);
            hBox.setAlignment(Pos.CENTER_LEFT);
            this.getChildren().add(hBox);
          });
    }
  }

  /**
   * Interface representing a listener to person button clicks.
   */
  public interface PersonClickListener {
    /**
     * Called when a person button is clicked.
     *
     * @param person      The person object wrapped by the clicked component.
     * @param clickCount  Number of mouse clicks.
     * @param mouseButton Mouse button that was clicked.
     */
    void onClick(@NotNull Person person, int clickCount, @NotNull MouseButton mouseButton);
  }
}
