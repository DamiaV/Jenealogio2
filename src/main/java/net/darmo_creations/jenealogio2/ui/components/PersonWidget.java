package net.darmo_creations.jenealogio2.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import net.darmo_creations.jenealogio2.App;
import net.darmo_creations.jenealogio2.model.Gender;
import net.darmo_creations.jenealogio2.model.Person;
import net.darmo_creations.jenealogio2.model.datetime.DateTime;
import net.darmo_creations.jenealogio2.model.datetime.DateTimeAlternative;
import net.darmo_creations.jenealogio2.model.datetime.DateTimeRange;
import net.darmo_creations.jenealogio2.model.datetime.DateTimeWithPrecision;
import net.darmo_creations.jenealogio2.themes.Icon;
import net.darmo_creations.jenealogio2.ui.ChildInfo;
import net.darmo_creations.jenealogio2.ui.FamilyTreePane;
import net.darmo_creations.jenealogio2.ui.PseudoClasses;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * A JavaFX component representing a single person in the {@link FamilyTreePane}.
 */
public class PersonWidget extends AnchorPane {
  public static final int WIDTH = 120;
  public static final int HEIGHT = 164;

  private static final String EMPTY_LABEL_VALUE = "?";

  @SuppressWarnings("DataFlowIssue")
  public static final Image DEFAULT_IMAGE =
      new Image(PersonWidget.class.getResourceAsStream(App.IMAGES_PATH + "default_person_image.png"));
  @SuppressWarnings("DataFlowIssue")
  public static final Image ADD_IMAGE =
      new Image(PersonWidget.class.getResourceAsStream(App.IMAGES_PATH + "add_person_image.png"));

  private final List<ClickListener> clickListeners = new LinkedList<>();

  private final Person person;
  private final List<ChildInfo> childInfo = new ArrayList<>();
  private PersonWidget parentWidget1;
  private PersonWidget parentWidget2;

  private final AnchorPane imagePane = new AnchorPane();
  private final ImageView imageView = new ImageView();
  private final Label firstNameLabel = new Label();
  private final Label lastNameLabel = new Label();
  private final Label birthDateLabel = new Label();
  private final Label deathDateLabel = new Label();

  private boolean selected;

  /**
   * Create a component for the given person.
   *
   * @param person       A person object.
   * @param childInfo    Information about the displayed children this widget is a parent of.
   * @param showMoreIcon Whether to show the “plus” icon.
   * @param isTarget     Whether the widget is targetted.
   * @param isRoot       Whether the person is the tree’s root.
   */
  public PersonWidget(final Person person, final @NotNull List<ChildInfo> childInfo,
                      boolean showMoreIcon, boolean isTarget, boolean isRoot) {
    this.person = person;
    this.childInfo.addAll(childInfo);
    this.getStyleClass().add("person-widget");
    if (isTarget) {
      this.getStyleClass().add("center");
    }

    this.setPrefWidth(WIDTH);
    this.setPrefHeight(HEIGHT);
    this.setMinWidth(this.getPrefWidth());
    this.setMaxWidth(this.getPrefWidth());

    VBox pane = new VBox();
    AnchorPane.setTopAnchor(pane, 0.0);
    AnchorPane.setBottomAnchor(pane, 0.0);
    AnchorPane.setLeftAnchor(pane, 0.0);
    AnchorPane.setRightAnchor(pane, 0.0);
    this.getChildren().add(pane);

    BorderPane iconsBox = new BorderPane();
    if (person != null && person.disambiguationID().isPresent()) {
      int id = person.disambiguationID().get();
      Label idLabel = new Label("#" + id);
      iconsBox.setLeft(idLabel);
    } else {
      iconsBox.setLeft(new Label()); // Empty label for proper alignment
    }
    if (isRoot) {
      Label rootIcon = new Label("", App.config().theme().getIcon(Icon.TREE_ROOT, Icon.Size.SMALL));
      rootIcon.setTooltip(new Tooltip(App.config().language().translate("person_widget.root.tooltip")));
      iconsBox.setRight(rootIcon);
    }
    if (showMoreIcon) {
      Label moreIcon = new Label("", App.config().theme().getIcon(Icon.MORE, Icon.Size.SMALL));
      moreIcon.setTooltip(new Tooltip(App.config().language().translate("person_widget.more_icon.tooltip")));
      iconsBox.setRight(moreIcon);
    }
    pane.getChildren().add(iconsBox);

    int size = 54;
    this.imagePane.setPrefSize(size, size);
    this.imagePane.setMinSize(size, size);
    this.imagePane.setMaxSize(size, size);
    this.imagePane.setPadding(new Insets(2));
    HBox imageBox = new HBox(this.imagePane);
    imageBox.setAlignment(Pos.CENTER);
    pane.getChildren().add(imageBox);
    AnchorPane.setTopAnchor(this.imageView, 0.0);
    AnchorPane.setBottomAnchor(this.imageView, 0.0);
    AnchorPane.setLeftAnchor(this.imageView, 0.0);
    AnchorPane.setRightAnchor(this.imageView, 0.0);
    this.imageView.setPreserveRatio(true);
    this.imageView.setFitHeight(50);
    this.imageView.setFitWidth(50);
    this.imagePane.getChildren().add(this.imageView);

    VBox infoPane = new VBox(this.firstNameLabel, this.lastNameLabel, this.birthDateLabel, this.deathDateLabel);
    infoPane.getStyleClass().add("person-data");
    pane.getChildren().add(infoPane);

    this.setOnMouseClicked(this::onClick);

    this.populateFields();
  }

  /**
   * The person object wrapped by this component.
   */
  public Optional<Person> person() {
    return Optional.ofNullable(this.person);
  }

  /**
   * Information about the visible child this widget is a parent of.
   */
  public List<ChildInfo> childInfo() {
    return new ArrayList<>(this.childInfo);
  }

  /**
   * The widget representing the parent 1 of this wrapped person.
   */
  public Optional<PersonWidget> parentWidget1() {
    return Optional.ofNullable(this.parentWidget1);
  }

  /**
   * Set the widget representing the parent 1 of this wrapped person.
   *
   * @param parentWidget1 The widget.
   */
  public void setParentWidget1(PersonWidget parentWidget1) {
    this.parentWidget1 = parentWidget1;
  }

  /**
   * The widget representing the parent 1 of this wrapped person.
   */
  public Optional<PersonWidget> parentWidget2() {
    return Optional.ofNullable(this.parentWidget2);
  }

  /**
   * Set the widget representing the parent 2 of this wrapped person.
   *
   * @param parentWidget2 The widget.
   */
  public void setParentWidget2(PersonWidget parentWidget2) {
    this.parentWidget2 = parentWidget2;
  }

  /**
   * Indicate whether this component is selected.
   */
  public boolean isSelected() {
    return this.selected;
  }

  /**
   * Set the selection of this component.
   */
  public void setSelected(boolean selected) {
    this.selected = selected;
    this.pseudoClassStateChanged(PseudoClasses.SELECTED, selected);
  }

  /**
   * List of click listeners.
   */
  public List<ClickListener> clickListeners() {
    return this.clickListeners;
  }

  /**
   * Called when this widget is clicked. Notifies all {@link ClickListener}s.
   *
   * @param mouseEvent The mouse event.
   */
  private void onClick(@NotNull MouseEvent mouseEvent) {
    this.clickListeners.forEach(
        clickListener -> clickListener.onClick(this, mouseEvent.getClickCount(), mouseEvent.getButton()));
    mouseEvent.consume();
  }

  /**
   * Populate labels with data from the wrapped person object.
   */
  private void populateFields() {
    if (this.person == null) {
      this.imageView.setImage(ADD_IMAGE);
      this.getStyleClass().add("add-parent");
      return;
    }

    this.imageView.setImage(this.person.getImage().orElse(DEFAULT_IMAGE));
    String genderColor = this.person.gender().map(Gender::color).orElse(Gender.MISSING_COLOR);
    this.imagePane.setStyle("-fx-background-color: " + genderColor);

    String firstNames = this.person.getFirstNames().orElse(EMPTY_LABEL_VALUE);
    this.firstNameLabel.setText(firstNames);
    this.firstNameLabel.setTooltip(new Tooltip(firstNames));

    String lastName = this.person.getLastName().orElse(EMPTY_LABEL_VALUE);
    this.lastNameLabel.setText(lastName);
    this.lastNameLabel.setTooltip(new Tooltip(lastName));

    String birthDate = this.person.getBirthDate().map(this::formatDate).orElse(EMPTY_LABEL_VALUE);
    this.birthDateLabel.setText(birthDate);
    this.birthDateLabel.setGraphic(App.config().theme().getIcon(Icon.BIRTH, Icon.Size.SMALL));
    this.birthDateLabel.setTooltip(new Tooltip(birthDate));

    if (this.person.lifeStatus().isConsideredDeceased()) {
      String deathDate = this.person.getDeathDate().map(this::formatDate).orElse(EMPTY_LABEL_VALUE);
      this.deathDateLabel.setText(deathDate);
      this.deathDateLabel.setGraphic(App.config().theme().getIcon(Icon.DEATH, Icon.Size.SMALL));
      this.deathDateLabel.setTooltip(new Tooltip(deathDate));
    }
  }

  /**
   * Format a date’s year.
   */
  private String formatDate(@NotNull DateTime date) {
    if (date instanceof DateTimeWithPrecision d) {
      int year = d.date().iso8601Date().getYear();
      return switch (d.precision()) {
        case EXACT -> String.valueOf(year);
        case ABOUT -> "~ " + year;
        case POSSIBLY -> year + " ?";
        case BEFORE -> "< " + year;
        case AFTER -> "> " + year;
      };
    } else if (date instanceof DateTimeRange d) {
      int startYear = d.startDate().iso8601Date().getYear();
      int endYear = d.endDate().iso8601Date().getYear();
      return startYear != endYear ? "%s-%s".formatted(startYear, endYear) : String.valueOf(startYear);
    } else if (date instanceof DateTimeAlternative d) {
      int earliestYear = d.earliestDate().iso8601Date().getYear();
      int latestYear = d.latestDate().iso8601Date().getYear();
      return earliestYear != latestYear ? "%s/%s".formatted(earliestYear, latestYear) : String.valueOf(earliestYear);
    }
    throw new IllegalArgumentException("unexpected date type: " + date.getClass());
  }

  /**
   * Interface for click listeners.
   */
  public interface ClickListener {
    void onClick(@NotNull PersonWidget personWidget, int clickCount, MouseButton button);
  }
}
