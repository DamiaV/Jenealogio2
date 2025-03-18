package net.darmo_creations.jenealogio2.ui;

import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.shape.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.ui.components.*;
import net.darmo_creations.jenealogio2.ui.events.*;
import org.jetbrains.annotations.*;

import java.util.*;

import static net.darmo_creations.jenealogio2.model.ParentalRelationType.*;

/**
 * Base class for components that show a partial view of a {@link FamilyTree}
 * relative to a specific {@link Person}.
 */
public abstract class PersonTreeView extends FamilyTreeComponent
    implements TreeImageProvider {
  protected static final Map<ParentalRelationType, String> CSS_CLASSES = new EnumMap<>(ParentalRelationType.class);

  static {
    for (final var value : values())
      CSS_CLASSES.put(value, value.name().toLowerCase().replace("_", "-"));
  }

  protected static final int HGAP = 20;
  protected static final int VGAP = 20;
  protected static final int LINE_GAP = 8;

  private final Config config;

  private final ObservableList<PersonWidget> personWidgets = FXCollections.observableList(new ArrayList<>());
  private final Pane pane = new Pane();
  private final ScrollPane scrollPane = new ScrollPane(this.pane);
  private final Legend legend;

  private Person targettedPerson;
  private boolean targetHasChanged;
  private boolean internalClick;

  /**
   * Create a new tree view.
   *
   * @param config                The current config.
   * @param possibleRelationTypes The {@link ParentalRelationType}s that may be shown in this view.
   *                              This is displayed in the legend.
   */
  protected PersonTreeView(final @NotNull Config config, @NotNull ParentalRelationType... possibleRelationTypes) {
    this.config = Objects.requireNonNull(config);
    this.setOnMouseClicked(this::onBackgroundClicked);
    this.setMinSize(300, 300);

    final StackPane stackPane = new StackPane();
    AnchorPane.setTopAnchor(stackPane, 0.0);
    AnchorPane.setBottomAnchor(stackPane, 0.0);
    AnchorPane.setLeftAnchor(stackPane, 0.0);
    AnchorPane.setRightAnchor(stackPane, 0.0);

    this.scrollPane.setPannable(true);
    this.scrollPane.getStyleClass().add("no-focus-scroll-pane");
    stackPane.getChildren().add(this.scrollPane);

    this.pane.getStyleClass().add("tree-pane");

    this.legend = new Legend(possibleRelationTypes);
    StackPane.setAlignment(this.legend, Pos.BOTTOM_RIGHT);
    // Leave some space for scroll bars
    StackPane.setMargin(this.legend, new Insets(20));
    stackPane.getChildren().add(this.legend);

    this.getChildren().add(stackPane);
  }

  protected ObservableList<PersonWidget> personWidgets() {
    return this.personWidgets;
  }

  protected Pane pane() {
    return this.pane;
  }

  @Override
  public void setFamilyTree(FamilyTree familyTree) {
    if (familyTree != null)
      this.targettedPerson = familyTree.root().orElse(null);
    super.setFamilyTree(familyTree);
  }

  @Override
  public void refresh() {
    final Optional<Person> selectedPerson = this.getSelectedPerson();
    this.pane.getChildren().clear();
    this.personWidgets.clear();
    if (this.familyTree().isEmpty()) return;
    final FamilyTree familyTree = this.familyTree().get();
    if (!familyTree.persons().contains(this.targettedPerson)) {
      final Optional<Person> root = familyTree.root();
      if (root.isEmpty()) return;
      this.targettedPerson = root.get();
      this.targetHasChanged = true;
    }
    final PersonWidget root = this.buildTree(familyTree, this.targettedPerson);
    this.adjustView();

    this.scrollPane.layout(); // Allows proper positioning when scrolling to a specific widget
    // Keep current selection if it the targetted person hasn’t changed
    if (!this.targetHasChanged &&
        selectedPerson.isPresent() &&
        familyTree.persons().contains(selectedPerson.get())) {
      this.internalClick = true;
      this.select(selectedPerson.get(), false);
      this.internalClick = false;
    }
    this.targetHasChanged = false;

    this.centerNodeInScrollPane(root);
  }

  /**
   * Set the visibility of the legend.
   *
   * @param visible True to set visible, false to hide.
   */
  public void setLegendVisible(boolean visible) {
    this.legend.setVisible(visible);
  }

  /**
   * Build the tree view.
   *
   * @return The targetted {@link PersonWidget}.
   */
  protected abstract PersonWidget buildTree(
      final @NotNull FamilyTree familyTree,
      final @NotNull Person targettedPerson
  );

  @Override
  public Optional<Person> getSelectedPerson() {
    return this.personWidgets.stream()
        .filter(PersonWidget::isSelected)
        .findFirst()
        .flatMap(PersonWidget::person);
  }

  @Override
  public void deselectAll() {
    this.personWidgets.forEach(w -> w.setSelected(false));
  }

  @Override
  public void select(@NotNull Person person, boolean updateTarget) {
    Objects.requireNonNull(person);
    if (updateTarget
        // Update target if the person is not currently visible
        || this.personWidgets.stream().noneMatch(w -> w.person().map(p -> p.equals(person))
        .orElse(false))) {
      this.targettedPerson = person;
      this.targetHasChanged = true;
      this.refresh();
    }
    this.personWidgets.forEach(w -> {
      final Optional<Person> p = w.person();
      w.setSelected(p.isPresent() && person == p.get());
    });
    if (!this.internalClick)
      this.personWidgets.stream()
          .filter(w -> w.person().orElse(null) == person)
          .findFirst()
          .ifPresent(this::centerNodeInScrollPane);
  }

  /**
   * Export this view as an image.
   */
  @Override
  public Image exportAsImage() {
    return this.pane.snapshot(new SnapshotParameters(), null);
  }

  /**
   * The currently targetted {@link Person}.
   */
  @Override
  public Optional<Person> targettedPerson() {
    return Optional.ofNullable(this.targettedPerson);
  }

  /**
   * Create a {@link Line} object with the given coordinates, with the {@code .tree-line} CSS class.
   *
   * @param x1 X coordinate of the first point.
   * @param y1 Y coordinate of the first point.
   * @param x2 X coordinate of the second point.
   * @param y2 Y coordinate of the second point.
   */
  protected void drawLine(double x1, double y1, double x2, double y2) {
    this.drawLine(x1, y1, x2, y2, null);
  }

  /**
   * Create a {@link Line} object with the given coordinates, with the {@code .tree-line} CSS class.
   *
   * @param x1       X coordinate of the first point.
   * @param y1       Y coordinate of the first point.
   * @param x2       X coordinate of the second point.
   * @param y2       Y coordinate of the second point.
   * @param cssClass Additional CSS class to apply to the {@link Line} object.
   */
  protected void drawLine(double x1, double y1, double x2, double y2, String cssClass) {
    final Line line = new Line(x1, y1, x2, y2);
    line.getStyleClass().add("tree-line");
    if (cssClass != null) line.getStyleClass().add(cssClass);
    line.setStrokeWidth(2);
    this.pane.getChildren().add(0, line);
  }

  /**
   * Scroll the scrollpane to make the given node visible.
   *
   * @param node Node to make visible.
   */
  private void centerNodeInScrollPane(@NotNull Node node) {
    final double w = this.pane.getWidth();
    final double h = this.pane.getHeight();
    final double x = node.getBoundsInParent().getMaxX();
    final double y = node.getBoundsInParent().getMaxY();
    this.scrollPane.setHvalue(x / w);
    this.scrollPane.setVvalue(y / h);
  }

  /**
   * Called when the pane’s background is clicked.
   *
   * @param event The mouse event.
   */
  private void onBackgroundClicked(MouseEvent event) {
    this.pane.requestFocus();
  }

  /**
   * Create a new {@link PersonWidget}.
   *
   * @param person       Person to create a widget for.
   * @param childInfo    Information about the relation to its visible child.
   * @param showMoreIcon Whether to show the “plus” icon.
   * @param isTarget     Whether the widget is targetted.
   * @param familyTree   Tree the person belongs to.
   * @return The new component.
   */
  protected PersonWidget createPersonWidget(
      final Person person,
      final List<ChildInfo> childInfo,
      boolean showMoreIcon,
      boolean isTarget,
      final @NotNull FamilyTree familyTree
  ) {
    final PersonWidget w = new PersonWidget(
        person,
        childInfo,
        showMoreIcon,
        isTarget,
        familyTree.isRoot(person),
        this.config
    );
    this.pane.getChildren().add(w);
    this.personWidgets.add(w);
    w.clickListeners().add(this::onPersonWidgetClick);
    return w;
  }

  /**
   * Called when a {@link PersonWidget} is clicked.
   * <p>
   * Calls upon {@link #firePersonClickEvent(PersonClickEvent)} if the widget contains a person object,
   * calls upon {@link #fireNewParentClickEvent(List)} otherwise.
   *
   * @param personWidget The clicked widget.
   * @param clickCount   Number of clicks.
   * @param button       Clicked mouse button.
   */
  private void onPersonWidgetClick(
      final @NotNull PersonWidget personWidget,
      int clickCount,
      MouseButton button
  ) {
    final Optional<Person> person = personWidget.person();
    if (person.isPresent()) {
      this.internalClick = true;
      final var clickType = PersonClickedEvent.getClickType(clickCount, button);
      final Person p = person.get();
      this.select(p, clickType.shouldUpdateTarget());
      this.firePersonClickEvent(new PersonClickedEvent(p, clickType));
      this.internalClick = false;
    } else if (!personWidget.childInfo().isEmpty())
      this.fireNewParentClickEvent(personWidget.childInfo());
    this.pane.requestFocus();
  }

  protected void adjustView() {
    double minX = Integer.MAX_VALUE;
    double minY = Integer.MAX_VALUE;

    final List<Node> children = this.pane()
        .getChildren()
        .stream()
        .filter(w -> !w.layoutXProperty().isBound())
        .toList();

    for (final Node child : children) {
      if (child instanceof PersonWidget pw) {
        if (pw.getLayoutX() < minX) minX = pw.getLayoutX();
        if (pw.getLayoutY() < minY) minY = pw.getLayoutY();
      } else if (child instanceof Line line) {
        final Bounds layoutBounds = line.getLayoutBounds();
        if (line.getLayoutX() < minX) minX = layoutBounds.getMinX();
        if (line.getLayoutY() < minY) minY = layoutBounds.getMinY();
      }
    }

    final double xOffset = minX;
    final double yOffset = minY;
    final double gap = 20;
    children.forEach(w -> {
      w.setLayoutX(w.getLayoutX() - xOffset + gap);
      w.setLayoutY(w.getLayoutY() - yOffset + gap);
    });
  }

  private class Legend extends VBox {
    /**
     * Create a new legend for the given {@link ParentalRelationType}s.
     *
     * @param relationTypes The relation types to show, in that order.
     */
    public Legend(final @NotNull ParentalRelationType[] relationTypes) {
      super(2);
      this.setMaxWidth(Region.USE_PREF_SIZE);
      this.setMaxHeight(Region.USE_PREF_SIZE);
      this.setPadding(new Insets(5));
      this.getStyleClass().add("tree-legend");
      this.setMouseTransparent(true);

      final Language language = PersonTreeView.this.config.language();

      final Label title = new Label(language.translate("person_tree_view.legend.title"));
      title.getStyleClass().add("legend-title");
      final HBox titleBox = new HBox(title);
      titleBox.setPadding(new Insets(0, 0, 3, 0));
      titleBox.setAlignment(Pos.CENTER);

      this.getChildren().add(titleBox);
      for (final var relationType : relationTypes) {
        final Label colorPatch = new Label("    ");
        colorPatch.getStyleClass().add("item-color-patch");
        final Label text = new Label(
            language.translate("person_tree_view.legend.item." + relationType.name().toLowerCase()));
        text.getStyleClass().add("item-text");
        final HBox itemBox = new HBox(5, colorPatch, text);
        itemBox.getStyleClass().addAll("legend-item", CSS_CLASSES.get(relationType));
        this.getChildren().add(itemBox);
      }
    }
  }
}
