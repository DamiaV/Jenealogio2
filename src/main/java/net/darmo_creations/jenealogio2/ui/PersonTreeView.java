package net.darmo_creations.jenealogio2.ui;

import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
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

public abstract class PersonTreeView extends FamilyTreeComponent {
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

  private Person targettedPerson;
  private boolean internalClick;

  public PersonTreeView(final @NotNull Config config) {
    this.config = Objects.requireNonNull(config);
    this.setOnMouseClicked(this::onBackgroundClicked);
    this.scrollPane.setPannable(true);
    this.scrollPane.getStyleClass().add("no-focus-scroll-pane");
    AnchorPane.setTopAnchor(this.scrollPane, 0.0);
    AnchorPane.setBottomAnchor(this.scrollPane, 0.0);
    AnchorPane.setLeftAnchor(this.scrollPane, 0.0);
    AnchorPane.setRightAnchor(this.scrollPane, 0.0);
    this.getChildren().add(this.scrollPane);
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
    if (this.familyTree().isEmpty())
      return;
    final FamilyTree familyTree = this.familyTree().get();
    if (!familyTree.persons().contains(this.targettedPerson)) {
      final Optional<Person> root = familyTree.root();
      if (root.isEmpty())
        return;
      this.targettedPerson = root.get();
    }
    final PersonWidget root = this.buildTree(familyTree, this.targettedPerson);
    this.adjustView();

    this.scrollPane.layout(); // Allows proper positioning when scrolling to a specific widget
    if (selectedPerson.isPresent() && familyTree.persons().contains(selectedPerson.get())) { // Keep current selection
      this.internalClick = true;
      this.select(selectedPerson.get(), false);
      this.internalClick = false;
    }

    this.centerNodeInScrollPane(root);
  }

  /**
   * Build the tree view.
   *
   * @return The targetted {@link PersonWidget}.
   */
  protected abstract PersonWidget buildTree(final @NotNull FamilyTree familyTree, final @NotNull Person targettedPerson);

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
        || this.personWidgets.stream().noneMatch(w -> w.person().map(p -> p.equals(person)).orElse(false))) {
      this.targettedPerson = person;
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
    final double gap = HGAP;
    children.forEach(w -> {
      w.setLayoutX(w.getLayoutX() - xOffset + gap);
      w.setLayoutY(w.getLayoutY() - yOffset + gap);
    });
  }
}
