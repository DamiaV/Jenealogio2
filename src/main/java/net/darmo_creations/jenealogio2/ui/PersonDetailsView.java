package net.darmo_creations.jenealogio2.ui;

import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import net.darmo_creations.jenealogio2.App;
import net.darmo_creations.jenealogio2.config.Config;
import net.darmo_creations.jenealogio2.config.Language;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.themes.Icon;
import net.darmo_creations.jenealogio2.ui.components.PersonWidget;
import net.darmo_creations.jenealogio2.utils.DateTimeUtils;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class PersonDetailsView extends VBox {
  private Person person;

  private final ImageView imageView = new ImageView();
  private final Label fullNameLabel = new Label();
  private final Label genderLabel = new Label();
  private final Label occupationLabel = new Label();
  private final Label publicLastNameLabel = new Label();
  private final Label publicFirstNamesLabel = new Label();
  private final Label nicknamesLabel = new Label();

  private final ListView<LifeEventItem> lifeEventsList = new ListView<>();
  private final ListView<WitnessedEventItem> witnessedEventsList = new ListView<>();
  private final Label notesLabel = new Label();
  private final Label sourcesLabel = new Label();

  private final List<PersonClickListener> personClickListeners = new LinkedList<>();

  public PersonDetailsView() {
    super(4);
    this.setMinWidth(200);

    Language language = App.config().language();

    // TODO tab for family relations (parents, unions, children)

    SplitPane splitPane = new SplitPane();
    splitPane.setOrientation(Orientation.VERTICAL);

    VBox vHeader = new VBox(4);
    vHeader.getStyleClass().add("person-details-header");
    HBox header = new HBox(8);
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
    vHeader.getChildren().addAll(plnBox, pfnBox, nnBox);

    this.lifeEventsList.getStyleClass().add("life-events-list");
    // TODO show details when clicked

    Label witnessedLabel = new Label(language.translate("person_details_view.witnessed_events"));
    witnessedLabel.getStyleClass().add("person-details-title");
    AnchorPane witnessedLabelPane = new AnchorPane(witnessedLabel);
    witnessedLabelPane.getStyleClass().add("person-details-header");
    AnchorPane.setTopAnchor(witnessedLabel, 0.0);
    AnchorPane.setBottomAnchor(witnessedLabel, 0.0);
    AnchorPane.setLeftAnchor(witnessedLabel, 0.0);
    AnchorPane.setRightAnchor(witnessedLabel, 0.0);

    Label notesLabel = new Label(language.translate("person_details_view.notes"));
    notesLabel.getStyleClass().add("person-details-title");
    AnchorPane notesLabelPane = new AnchorPane(notesLabel);
    notesLabelPane.getStyleClass().add("person-details-header");
    AnchorPane.setTopAnchor(notesLabel, 0.0);
    AnchorPane.setBottomAnchor(notesLabel, 0.0);
    AnchorPane.setLeftAnchor(notesLabel, 0.0);
    AnchorPane.setRightAnchor(notesLabel, 0.0);
    this.notesLabel.setWrapText(true);

    Label sourcesLabel = new Label(language.translate("person_details_view.sources"));
    sourcesLabel.getStyleClass().add("person-details-title");
    AnchorPane sourcesLabelPane = new AnchorPane(sourcesLabel);
    sourcesLabelPane.getStyleClass().add("person-details-header");
    AnchorPane.setTopAnchor(sourcesLabel, 0.0);
    AnchorPane.setBottomAnchor(sourcesLabel, 0.0);
    AnchorPane.setLeftAnchor(sourcesLabel, 0.0);
    AnchorPane.setRightAnchor(sourcesLabel, 0.0);
    this.sourcesLabel.setWrapText(true);

    VBox top = new VBox(vHeader, publicNamesBox, this.lifeEventsList);
    top.getStyleClass().add("person-details");
    VBox middle = new VBox(witnessedLabelPane, this.witnessedEventsList);
    middle.getStyleClass().add("person-details");
    VBox bottom = new VBox(notesLabelPane, this.notesLabel, sourcesLabelPane, this.sourcesLabel);
    bottom.getStyleClass().add("person-details");

    splitPane.getItems().addAll(top, middle, bottom);
    splitPane.setDividerPositions(0.5, 0.8);
    this.getChildren().add(splitPane);
  }

  public void setPerson(final Person person) {
    this.person = person;
    this.populateFields();
  }

  private void populateFields() {
    this.lifeEventsList.getItems().clear();
    this.witnessedEventsList.getItems().clear();

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
    Person person = (Person) button.getUserData();
    this.personClickListeners.forEach(listener -> listener.onClick(person));
  }

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
      lifeEvent.actors().stream()
          .sorted(Person.lastThenFirstNamesComparator())
          .forEach(p -> {
            Button b = new Button(p.toString(), App.config().theme().getIcon(Icon.GO_TO, Icon.Size.SMALL));
            b.setOnAction(event -> PersonDetailsView.this.firePersonClickEvent(b));
            b.setUserData(p);
            actorsBox.getChildren().add(b);
          });
      this.getChildren().add(actorsBox);
    }
  }

  /**
   * Interface representing a listener to person button clicks.
   */
  public interface PersonClickListener {
    /**
     * Called when a person button is clicked.
     *
     * @param person The person object wrapped by the clicked component.
     */
    void onClick(@NotNull Person person);
  }
}
