package net.darmo_creations.jenealogio2.ui;

import javafx.collections.*;
import javafx.scene.Node;
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
import java.util.stream.*;

/**
 * JavaFX component displaying a part of a family tree’s graph.
 */
public class FamilyTreePane extends FamilyTreeComponent {
  public static final int MIN_ALLOWED_HEIGHT = 1;
  public static final int MAX_ALLOWED_HEIGHT = 7;
  public static final int DEFAULT_MAX_HEIGHT = 4;

  private static final int HGAP = 10;
  private static final int VGAP = 20;

  private final Config config;
  private final ObservableList<PersonWidget> personWidgets = FXCollections.observableList(new ArrayList<>());
  private final Pane pane = new Pane();
  private final ScrollPane scrollPane = new ScrollPane(this.pane);

  private Person targettedPerson;
  private boolean internalClick;
  private int maxHeight;

  /**
   * Create an empty family tree pane.
   *
   * @param config The app’s config.
   */
  public FamilyTreePane(final @NotNull Config config) {
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

  @Override
  public void setFamilyTree(FamilyTree familyTree) {
    if (familyTree != null)
      this.targettedPerson = familyTree.root().orElse(null);
    super.setFamilyTree(familyTree);
  }

  /**
   * Set the maximum number of levels to display above the center widget.
   *
   * @param height The new maximum height.
   */
  public void setMaxHeight(int height) {
    if (height < MIN_ALLOWED_HEIGHT || height > MAX_ALLOWED_HEIGHT)
      throw new IllegalArgumentException("invalid max height");
    this.maxHeight = height;
  }

  @Override
  public void refresh() {
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

    final PersonWidget root = this.buildParentsTree(familyTree);
    this.drawChildToParentsLines();
    final double xOffset = this.buildChildrenAndSiblingsAndPartnersTree(root, familyTree);
    if (xOffset <= 0)
      this.pane.getChildren().stream()
          .filter(w -> !w.layoutXProperty().isBound())
          .forEach(w -> w.setLayoutX(w.getLayoutX() - xOffset + HGAP));

    this.scrollPane.layout(); // Allows proper positioning when scrolling to a specific widget

    this.centerNodeInScrollPane(root);
  }

  /**
   * Create and position widgets for each ascendant of the current root.
   *
   * @param familyTree Tree to draw.
   * @return The widget for the tree’s root.
   */
  private PersonWidget buildParentsTree(@NotNull FamilyTree familyTree) {
    final PersonWidget root = this.createWidget(this.targettedPerson, List.of(), false, true, familyTree);

    final List<List<PersonWidget>> levels = new ArrayList<>();
    levels.add(List.of(root));
    // Build levels breadth-first
    for (int i = 1; i <= this.maxHeight; i++) {
      final List<PersonWidget> widgets = new ArrayList<>();
      levels.add(widgets);
      for (final PersonWidget childWidget : levels.get(i - 1)) {
        if (childWidget != null) {
          final Optional<Person> child = childWidget.person();
          if (child.isPresent()) {
            final Person c = child.get();
            final var parents = c.parents();
            final PersonWidget parent1Widget = this.createWidget(
                parents.left().orElse(null),
                List.of(new ChildInfo(c, 0)),
                this.hasHiddenRelatives(parents.left(), i, c, 1),
                false,
                familyTree
            );
            final PersonWidget parent2Widget = this.createWidget(
                parents.right().orElse(null),
                List.of(new ChildInfo(c, 1)),
                this.hasHiddenRelatives(parents.right(), i, c, 1),
                false,
                familyTree
            );
            widgets.add(parent1Widget);
            widgets.add(parent2Widget);
            childWidget.setParentWidget1(parent1Widget);
            childWidget.setParentWidget2(parent2Widget);
            continue;
          }
        }
        widgets.add(null);
        widgets.add(null);
      }
      if (widgets.stream().allMatch(Objects::isNull)) {
        levels.remove(i);
        break;
      }
    }

    // Widgets positioning
    for (int level = levels.size() - 1, row = 0; level >= 0; level--, row++) {
      final List<PersonWidget> widgets = levels.get(level);
      final double y = VGAP + (VGAP + PersonWidget.HEIGHT) * row;
      final double gap = (Math.pow(2, row) - 1) * (PersonWidget.WIDTH + HGAP);
      final double betweenCardsGap = HGAP + gap;
      double x = HGAP + 0.5 * gap;
      for (final PersonWidget widget : widgets) {
        if (widget != null) {
          widget.setLayoutX(x);
          widget.setLayoutY(y);
        }
        x += PersonWidget.WIDTH + betweenCardsGap;
      }
    }

    return root;
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  private boolean hasHiddenRelatives(
      @NotNull Optional<Person> parent,
      int treeLevel,
      final Person child,
      int minPartners
  ) {
    final boolean hasHiddenParents = treeLevel == this.maxHeight && parent.map(Person::hasAnyParents).orElse(false);
    final boolean hasHiddenChildren = treeLevel > 1 && parent.map(p -> p.children().stream().anyMatch(p_ -> p_ != child)).orElse(false);
    final boolean hasHiddenPartners = parent.map(p -> p.getPartnersAndChildren().size() > minPartners).orElse(false);
    return hasHiddenParents || hasHiddenChildren || hasHiddenPartners;
  }

  /**
   * Draw lines from each person to their respective parents.
   */
  private void drawChildToParentsLines() {
    this.personWidgets.forEach(personWidget -> {
      final Optional<PersonWidget> parent1 = personWidget.parentWidget1();
      final Optional<PersonWidget> parent2 = personWidget.parentWidget2();
      if (parent1.isPresent() && parent2.isPresent()) {
        final double cx = personWidget.getLayoutX() + PersonWidget.WIDTH / 2.0;
        final double cy = personWidget.getLayoutY();
        final double p1x = parent1.get().getLayoutX() + PersonWidget.WIDTH;
        final double p1y = parent1.get().getLayoutY() + PersonWidget.HEIGHT / 2.0;
        final double p2x = parent2.get().getLayoutX();
        final double p2y = parent2.get().getLayoutY() + PersonWidget.HEIGHT / 2.0;
        // Insert at the start to render them underneath the cards
        this.drawLine(p1x, p1y, p2x, p2y); // Line between parents
        this.drawLine(cx, cy, (p1x + p2x) / 2, p1y); // Child to parents line
      }
    });
  }

  /**
   * Build the tree of partners, siblings and direct children of the tree’s root.
   *
   * @param root       Tree’s root.
   * @param familyTree Tree being drawn.
   * @return X amount to move every widgets by.
   */
  private double buildChildrenAndSiblingsAndPartnersTree(@NotNull PersonWidget root, @NotNull FamilyTree familyTree) {
    // Get root’s partners sorted by birth date and name
    //noinspection OptionalGetWithoutIsPresent
    final Person rootPerson = root.person().get(); // Always present

    final double personW = PersonWidget.WIDTH;
    final int personH = PersonWidget.HEIGHT;
    final double rootX = root.getLayoutX();
    final double rootY = root.getLayoutY();

    final Set<Person> siblings = rootPerson.getSameParentsSiblings();
    final List<Person> olderSiblings = siblings.stream()
        .filter(p -> Person.birthDateThenNameComparator(false).compare(p, rootPerson) < 0)
        .sorted(Person.birthDateThenNameComparator(false))
        .toList();
    final List<Person> youngerSiblings = siblings.stream()
        .filter(p -> Person.birthDateThenNameComparator(false).compare(p, rootPerson) >= 0)
        .sorted(Person.birthDateThenNameComparator(true))
        .toList();

    final Map<Optional<Person>, List<Person>> childrenMap = rootPerson.getPartnersAndChildren()
        .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> {
          final List<Person> list = new ArrayList<>(e.getValue());
          list.sort(Person.birthDateThenNameComparator(false));
          return list;
        }));

    final List<Optional<Person>> partners = childrenMap.keySet().stream()
        .sorted(Person.optionalBirthDateThenNameComparator())
        .toList();

    // Used to detect whether any widget have a negative x coordinate
    double minX = rootX;

    // Render older siblings to the left of root
    double x = rootX;
    for (int i = olderSiblings.size() - 1; i >= 0; i--) {
      x -= personW + HGAP;
      this.drawSibling(familyTree, root, olderSiblings.get(i), x, rootX, rootY);
      if (x < minX)
        minX = x;
    }

    x = rootX;
    // Render partners with enough space for direct children
    final int partnersNb = partners.size();
    for (int i = 0; i < partnersNb; i++) {
      final Optional<Person> partnerOpt = partners.get(i);
      x += HGAP + personW;

      final List<Person> children = childrenMap.get(partnerOpt);
      final boolean hasHiddenParents = partnerOpt.map(Person::hasAnyParents).orElse(false);
      final List<ChildInfo> childInfo = new LinkedList<>();
      final Person partner = partnerOpt.orElse(null);
      for (final Person child : children)
        //noinspection OptionalGetWithoutIsPresent
        childInfo.add(new ChildInfo(child, child.getParentIndex(partner).get())); // Will always exist in this context
      final PersonWidget partnerWidget = this.createWidget(partner, childInfo, hasHiddenParents, false, familyTree);
      partnerWidget.setLayoutX(x);
      partnerWidget.setLayoutY(rootY);

      // Draw line from root to partner
      final double lineY = rootY + personH / 2.0 + (partnersNb - i * 8);
      this.drawLine(rootX + personW, lineY, x, lineY);

      final int childrenNb = children.size();
      final double halfChildrenWidth = (childrenNb * personW + (childrenNb - 1) * HGAP) / 2;
      // Compute gap to fit children of this partner and the next
      double nextXOffset = Math.max(0, halfChildrenWidth - personW);
      if (i < partnersNb - 1) {
        final int nextChildrenNb = childrenMap.get(partners.get(i + 1)).size();
        nextXOffset += (nextChildrenNb * personW + (nextChildrenNb - 1) * HGAP) / 2;
      }

      // Render children
      final double rightParentX = partnerWidget.getLayoutX() - HGAP / 2.0;
      double childX = x - halfChildrenWidth - HGAP / 2.0;
      final double childY = rootY + VGAP + personH;
      if (childX < minX)
        minX = childX;
      for (final Person child : children) {
        final boolean hasHiddenChildren = this.hasHiddenRelatives(Optional.ofNullable(child), 0, null, 0);
        final PersonWidget childWidget = this.createWidget(child, List.of(), hasHiddenChildren, false, familyTree);
        childWidget.setLayoutX(childX);
        childWidget.setLayoutY(childY);
        childWidget.setParentWidget1(root);
        childWidget.setParentWidget2(partnerWidget);
        final double lineX = childX + personW / 2;
        this.drawLine(lineX, childY, lineX, childY - VGAP / 2.0);
        this.drawLine(lineX, childY - VGAP / 2.0, rightParentX, childY - VGAP / 2.0);
        childX += personW + HGAP;
      }
      if (!children.isEmpty())
        this.drawLine(rightParentX, childY - VGAP / 2.0, rightParentX, lineY);
      x += nextXOffset;
    }

    // Render younger sibling to the right of root’s partners
    for (int i = youngerSiblings.size() - 1; i >= 0; i--) {
      x += HGAP + personW;
      this.drawSibling(familyTree, root, youngerSiblings.get(i), x, rootX, rootY);
    }

    return minX;
  }

  private void drawSibling(
      @NotNull FamilyTree familyTree,
      @NotNull PersonWidget root,
      @NotNull Person sibling,
      double x,
      double rootX,
      double rootY
  ) {
    final double personW = PersonWidget.WIDTH;
    final boolean hasHiddenChildren = this.hasHiddenRelatives(Optional.of(sibling), 0, null, 0);
    final PersonWidget widget = this.createWidget(sibling, List.of(), hasHiddenChildren, false, familyTree);
    widget.setLayoutX(x);
    widget.setLayoutY(rootY);
    widget.setParentWidget1(root.parentWidget1().orElse(null));
    widget.setParentWidget2(root.parentWidget2().orElse(null));
    final double lineX = x + personW / 2;
    final double lineY = rootY - VGAP / 2.0;
    this.drawLine(lineX, rootY, lineX, lineY);
    this.drawLine(lineX, lineY, rootX + personW / 2.0, lineY);
  }

  /**
   * Create a {@link Line} object with the given coordinates, with the {@code .tree-line} CSS class.
   */
  private void drawLine(double x1, double y1, double x2, double y2) {
    final Line line = new Line(x1, y1, x2, y2);
    line.getStyleClass().add("tree-line");
    line.setStrokeWidth(2);
    this.pane.getChildren().add(0, line);
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
  private PersonWidget createWidget(
      final Person person,
      final @NotNull List<ChildInfo> childInfo,
      boolean showMoreIcon,
      boolean isTarget,
      final @NotNull FamilyTree familyTree
  ) {
    final PersonWidget w = new PersonWidget(person, childInfo, showMoreIcon, isTarget, familyTree.isRoot(person), this.config);
    this.pane.getChildren().add(w);
    this.personWidgets.add(w);
    w.clickListeners().add(this::onPersonWidgetClick);
    return w;
  }

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
   * Called when a {@link PersonWidget} is clicked.
   * <p>
   * Calls upon {@link #firePersonClickEvent(PersonClickEvent)} if the widget contains a person object,
   * calls upon {@link #fireNewParentClickEvent(List)} otherwise.
   *
   * @param personWidget The clicked widget.
   * @param clickCount   Number of clicks.
   * @param button       Clicked mouse button.
   */
  private void onPersonWidgetClick(@NotNull PersonWidget personWidget, int clickCount, MouseButton button) {
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

  /**
   * Called when the pane’s background is clicked.
   *
   * @param event The mouse event.
   */
  private void onBackgroundClicked(MouseEvent event) {
    this.pane.requestFocus();
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
}
