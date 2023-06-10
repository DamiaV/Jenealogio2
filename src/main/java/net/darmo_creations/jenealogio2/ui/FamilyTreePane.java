package net.darmo_creations.jenealogio2.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import net.darmo_creations.jenealogio2.App;
import net.darmo_creations.jenealogio2.AppController;
import net.darmo_creations.jenealogio2.model.FamilyTree;
import net.darmo_creations.jenealogio2.model.Person;
import net.darmo_creations.jenealogio2.model.calendar.CalendarDate;
import net.darmo_creations.jenealogio2.themes.Icon;
import net.darmo_creations.jenealogio2.ui.components.PersonWidget;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * JavaFX component displaying a part of a family tree’s graph.
 */
public class FamilyTreePane extends FamilyTreeComponent {
  private static final int MAX_LEVEL = 4;
  private static final int HGAP = 10;
  private static final int VGAP = 20;

  private final ObservableList<PersonWidget> personWidgets = FXCollections.observableList(new ArrayList<>());
  private final Pane pane = new Pane();
  private final ScrollPane scrollPane = new ScrollPane(this.pane);

  private Person targettedPerson;
  private boolean internalClick;

  /**
   * Create an empty family tree pane.
   */
  public FamilyTreePane() {
    this.setOnMouseClicked(this::onBackgroundClicked);
    this.scrollPane.setPannable(true);
    this.scrollPane.getStyleClass().add("tree-scroll-pane");
    AnchorPane.setTopAnchor(this.scrollPane, 0.0);
    AnchorPane.setBottomAnchor(this.scrollPane, 0.0);
    AnchorPane.setLeftAnchor(this.scrollPane, 0.0);
    AnchorPane.setRightAnchor(this.scrollPane, 0.0);
    this.getChildren().add(this.scrollPane);
  }

  @Override
  public void setFamilyTree(FamilyTree familyTree) {
    if (familyTree != null) {
      this.targettedPerson = familyTree.root().orElse(null);
    }
    super.setFamilyTree(familyTree);
  }

  @Override
  public void refresh() {
    this.pane.getChildren().clear();
    this.personWidgets.clear();
    if (this.familyTree().isEmpty()) {
      return;
    }
    if (!this.familyTree().get().persons().contains(this.targettedPerson)) {
      Optional<Person> root = this.familyTree().get().root();
      if (root.isEmpty()) {
        return;
      }
      this.targettedPerson = root.get();
    }

    PersonWidget root = this.buildParentsTree();
    this.drawChildToParentsLines();
    double xOffset = this.buildChildrenAndSiblingsAndPartnersTree(root);
    if (xOffset <= 0) {
      this.pane.getChildren().forEach(w -> w.setLayoutX(w.getLayoutX() - xOffset + HGAP));
    }

    this.scrollPane.layout(); // Allows proper positioning when scrolling to a specific widget

    this.centerNodeInScrollPane(root);
  }

  /**
   * Create and position widgets for each ascendant of the current root.
   *
   * @return The widget for the tree’s root.
   */
  private PersonWidget buildParentsTree() {
    PersonWidget root = this.createWidget(this.targettedPerson, null, false, true);

    List<List<PersonWidget>> levels = new ArrayList<>();
    levels.add(List.of(root));
    // Build levels breadth-first
    for (int i = 1; i <= MAX_LEVEL; i++) {
      List<PersonWidget> widgets = new ArrayList<>();
      levels.add(widgets);
      for (PersonWidget childWidget : levels.get(i - 1)) {
        if (childWidget != null) {
          Optional<Person> child = childWidget.person();
          if (child.isPresent()) {
            Person c = child.get();
            var parents = c.parents();
            boolean hasHiddenParents = i == MAX_LEVEL && parents.left().map(Person::hasAnyParents).orElse(false);
            boolean hasHiddenChildren = i > 1 && parents.left().map(p -> p.children().stream().anyMatch(p_ -> p_ != c)).orElse(false);
            PersonWidget parent1Widget = this.createWidget(parents.left().orElse(null),
                new ChildInfo(c, 0), hasHiddenParents || hasHiddenChildren, false);
            hasHiddenParents = i == MAX_LEVEL && parents.right().map(Person::hasAnyParents).orElse(false);
            hasHiddenChildren = i > 1 && parents.right().map(p -> p.children().stream().anyMatch(p_ -> p_ != c)).orElse(false);
            PersonWidget parent2Widget = this.createWidget(parents.right().orElse(null),
                new ChildInfo(c, 1), hasHiddenParents || hasHiddenChildren, false);
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
      List<PersonWidget> widgets = levels.get(level);
      double y = VGAP + (VGAP + PersonWidget.HEIGHT) * row;
      double gap = (Math.pow(2, row) - 1) * (PersonWidget.WIDTH + HGAP);
      double betweenCardsGap = HGAP + gap;
      double x = HGAP + 0.5 * gap;
      for (PersonWidget widget : widgets) {
        if (widget != null) {
          widget.setLayoutX(x);
          widget.setLayoutY(y);
        }
        x += PersonWidget.WIDTH + betweenCardsGap;
      }
    }

    return root;
  }

  /**
   * Draw lines from each person to their respective parents.
   */
  private void drawChildToParentsLines() {
    this.personWidgets.forEach(personWidget -> {
      Optional<PersonWidget> parent1 = personWidget.parentWidget1();
      Optional<PersonWidget> parent2 = personWidget.parentWidget2();
      if (parent1.isPresent() && parent2.isPresent()) {
        double cx = personWidget.getLayoutX() + PersonWidget.WIDTH / 2.0;
        double cy = personWidget.getLayoutY();
        double p1x = parent1.get().getLayoutX() + PersonWidget.WIDTH;
        double p1y = parent1.get().getLayoutY() + PersonWidget.HEIGHT / 2.0;
        double p2x = parent2.get().getLayoutX();
        double p2y = parent2.get().getLayoutY() + PersonWidget.HEIGHT / 2.0;
        // Insert at the start to render them underneath the cards
        this.drawLine(p1x, p1y, p2x, p2y); // Line between parents
        this.drawLine(cx, cy, (p1x + p2x) / 2, p1y); // Child to parents line
      }
    });
  }

  /**
   * Build the tree of partners, siblings and direct children of the tree’s root.
   *
   * @param root Tree’s root.
   * @return X amount to move every widgets by.
   */
  private double buildChildrenAndSiblingsAndPartnersTree(@NotNull PersonWidget root) {
    // Get root’s partners sorted by birth date and name
    //noinspection OptionalGetWithoutIsPresent
    final Person rootPerson = root.person().get(); // Always present

    final double personW = PersonWidget.WIDTH;
    final int personH = PersonWidget.HEIGHT;
    final double rootX = root.getLayoutX();
    final double rootY = root.getLayoutY();

    // Compare birth dates if available, names otherwise
    Function<Boolean, Comparator<Person>> personComparator = inverse -> (p1, p2) -> {
      Optional<CalendarDate> birthDate1 = p1.getBirthDate();
      Optional<CalendarDate> birthDate2 = p2.getBirthDate();
      if (birthDate1.isPresent() && birthDate2.isPresent()) {
        int c = birthDate1.get().compareTo(birthDate2.get());
        if (inverse) {
          c = -c;
        }
        if (c != 0) {
          return c;
        }
      }
      int c = Person.lastThenFirstNamesComparator().compare(p1, p2);
      if (inverse) {
        c = -c;
      }
      return c;
    };

    List<Person> siblings = this.getSiblings(rootPerson);
    List<Person> olderSiblings = siblings.stream()
        .filter(p -> personComparator.apply(false).compare(p, rootPerson) < 0)
        .sorted(personComparator.apply(false))
        .toList();
    List<Person> youngerSiblings = siblings.stream()
        .filter(p -> personComparator.apply(false).compare(p, rootPerson) >= 0)
        .sorted(personComparator.apply(true))
        .toList();

    Set<Person> rootsChildren = rootPerson.children(); // Make only one copy
    //noinspection OptionalGetWithoutIsPresent
    Set<Person> partnersSet = rootPerson.getLifeEventsAsActor().stream()
        .filter(e -> e.type().indicatesUnion())
        // Partner always present in unions
        .map(e -> e.actors().stream().filter(p -> p != rootPerson).findFirst().get())
        .collect(Collectors.toSet());
    // Add persons that had a child with root but are not linked by a life event
    for (Person rootsChild : rootsChildren) {
      var parents = rootsChild.parents();
      parents.left().ifPresent(p -> {
        if (p != rootPerson) {
          partnersSet.add(p);
        }
      });
      parents.right().ifPresent(p -> {
        if (p != rootPerson) {
          partnersSet.add(p);
        }
      });
    }

    List<Person> partners = partnersSet.stream()
        .sorted(personComparator.apply(false))
        .toList();

    // Get children for each relation with root, sorted by birth date and name
    Map<Person, List<Person>> childrenMap = new HashMap<>();
    partners.forEach(partner -> childrenMap.put(partner, rootsChildren.stream()
        .filter(child -> child.hasParent(partner))
        .sorted(personComparator.apply(false))
        .toList()));

    // Used to detect whether any widget have a negative x coordinate
    double minX = rootX;

    // Render older siblings to the left of root
    double x = rootX;
    for (int i = olderSiblings.size() - 1; i >= 0; i--) {
      x -= personW + HGAP;
      Person sibling = olderSiblings.get(i);
      boolean hasHiddenChildren = !sibling.children().isEmpty();
      PersonWidget widget = this.createWidget(sibling, null, hasHiddenChildren, false);
      widget.setLayoutX(x);
      widget.setLayoutY(rootY);
      widget.setParentWidget1(root.parentWidget1().orElse(null));
      widget.setParentWidget2(root.parentWidget2().orElse(null));
      double lineX = x + personW / 2;
      double lineY = rootY - VGAP / 2.0;
      this.drawLine(lineX, rootY, lineX, lineY);
      this.drawLine(lineX, lineY, rootX + personW / 2.0, lineY);
      if (x < minX) {
        minX = x;
      }
    }

    x = rootX;
    // Render partners with enough space for direct children
    int partnersNb = partners.size();
    for (int i = 0; i < partnersNb; i++) {
      Person partner = partners.get(i);
      x += HGAP + personW;

      boolean hasHiddenParents = partner.hasAnyParents();
      PersonWidget partnerWidget = this.createWidget(partner, null, hasHiddenParents, false);
      partnerWidget.setLayoutX(x);
      partnerWidget.setLayoutY(rootY);

      // Draw line from root to partner
      double lineY = rootY + personH / 2.0 + (partnersNb - i * 8);
      this.drawLine(rootX + personW, lineY, x, lineY);

      List<Person> children = childrenMap.get(partner);
      int childrenNb = children.size();
      double halfChildrenWidth = (childrenNb * personW + (childrenNb - 1) * HGAP) / 2;
      // Compute gap to fit children of this partner and the next
      double nextXOffset = Math.max(0, halfChildrenWidth - personW);
      if (i < partnersNb - 1) {
        int nextChildrenNb = childrenMap.get(partners.get(i + 1)).size();
        nextXOffset += (nextChildrenNb * personW + (nextChildrenNb - 1) * HGAP) / 2;
      }

      // Render children
      double rightParentX = partnerWidget.getLayoutX() - HGAP / 2.0;
      double childX = x - halfChildrenWidth - HGAP / 2.0;
      final double childY = rootY + VGAP + personH;
      if (childX < minX) {
        minX = childX;
      }
      for (Person child : children) {
        boolean hasHiddenChildren = !child.children().isEmpty();
        PersonWidget childWidget = this.createWidget(child, null, hasHiddenChildren, false);
        childWidget.setLayoutX(childX);
        childWidget.setLayoutY(childY);
        childWidget.setParentWidget1(root);
        childWidget.setParentWidget2(partnerWidget);
        double lineX = childX + personW / 2;
        this.drawLine(lineX, childY, lineX, childY - VGAP / 2.0);
        this.drawLine(lineX, childY - VGAP / 2.0, rightParentX, childY - VGAP / 2.0);
        childX += personW + HGAP;
      }
      if (!children.isEmpty()) {
        this.drawLine(rightParentX, childY - VGAP / 2.0, rightParentX, lineY);
      }
      x += nextXOffset;
    }

    // Render younger sibling to the right of root’s partners
    for (int i = youngerSiblings.size() - 1; i >= 0; i--) {
      x += HGAP + personW;
      Person sibling = youngerSiblings.get(i);
      boolean hasHiddenChildren = !sibling.children().isEmpty();
      PersonWidget widget = this.createWidget(sibling, null, hasHiddenChildren, false);
      widget.setLayoutX(x);
      widget.setLayoutY(rootY);
      widget.setParentWidget1(root.parentWidget1().orElse(null));
      widget.setParentWidget2(root.parentWidget2().orElse(null));
      double lineX = x + personW / 2;
      double lineY = rootY - VGAP / 2.0;
      this.drawLine(lineX, rootY, lineX, lineY);
      this.drawLine(lineX, lineY, rootX + personW / 2.0, lineY);
    }

    return minX;
  }

  /**
   * Create a {@link Line} object with the given coordinates, with the {@code .tree-line} CSS class.
   */
  private void drawLine(double x1, double y1, double x2, double y2) {
    Line line = new Line(x1, y1, x2, y2);
    line.getStyleClass().add("tree-line");
    line.setStrokeWidth(2);
    this.pane.getChildren().add(0, line);
  }

  /**
   * Get the list of persons that have the same parents as the given person.
   *
   * @param person Person to get siblings of.
   * @return List of the person’s siblings.
   */
  private List<Person> getSiblings(final @NotNull Person person) {
    //noinspection OptionalGetWithoutIsPresent
    return this.familyTree().get().persons().stream()
        .filter(p -> p != person && p.hasSameParents(person))
        .toList();
  }

  /**
   * Create a new {@link PersonWidget}.
   *
   * @param person    Person to create a widget for.
   * @param childInfo Information about the relation to its visible child.
   * @return The new component.
   */
  private PersonWidget createWidget(final Person person, final ChildInfo childInfo, boolean showMoreIcon, boolean isCenter) {
    PersonWidget w = new PersonWidget(person, childInfo, isCenter);
    this.pane.getChildren().add(w);
    this.personWidgets.add(w);
    w.clickListeners().add(this::onPersonWidgetClick);
    if (showMoreIcon) {
      Label moreIcon = new Label("", App.config().theme().getIcon(Icon.MORE, Icon.Size.SMALL));
      this.pane.getChildren().add(moreIcon);
      moreIcon.layoutXProperty().bind(w.layoutXProperty());
      moreIcon.layoutYProperty().bind(w.layoutYProperty().subtract(moreIcon.getPrefHeight()));
    }
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
  protected void deselectAll() {
    this.personWidgets.forEach(w -> w.setSelected(false));
  }

  @Override
  protected void select(@NotNull Person person, boolean updateTarget) {
    if (updateTarget) {
      this.targettedPerson = person;
      this.refresh();
    }
    this.personWidgets.forEach(w -> w.setSelected(w.person().isPresent() && person == w.person().get()));
    if (!this.internalClick) {
      this.personWidgets.stream()
          .filter(w -> w.person().orElse(null) == person)
          .findFirst()
          .ifPresent(this::centerNodeInScrollPane);
    }
  }

  /**
   * Called when a {@link PersonWidget} is clicked.
   * <p>
   * Calls upon {@link #firePersonClickEvent(Person, int, MouseButton)} if the widget contains a person object,
   * calls upon {@link #fireNewParentClickEvent(ChildInfo)} otherwise.
   *
   * @param personWidget The clicked widget.
   * @param clickCount   Number of clicks.
   * @param button       Clicked mouse button.
   */
  private void onPersonWidgetClick(@NotNull PersonWidget personWidget, int clickCount, MouseButton button) {
    Optional<Person> person = personWidget.person();
    if (person.isPresent()) {
      this.internalClick = true;
      this.select(person.get(), button == AppController.TARGET_UPDATE_BUTTON);
      // Prevent double-click when right-clicking to avoid double-click action to miss the widget
      this.firePersonClickEvent(person.get(), button == AppController.TARGET_UPDATE_BUTTON ? 1 : clickCount, button);
      this.internalClick = false;
    } else {
      personWidget.childInfo().ifPresent(this::fireNewParentClickEvent);
    }
    this.pane.requestFocus();
  }

  /**
   * Called when the pane’s background is clicked.
   *
   * @param event The mouse event.
   */
  private void onBackgroundClicked(MouseEvent event) {
    this.deselectAll();
    this.firePersonClickEvent(null, event.getClickCount(), MouseButton.PRIMARY);
    this.pane.requestFocus();
  }

  /**
   * Scroll the scrollpane to make the given node visible.
   *
   * @param node Node to make visible.
   */
  private void centerNodeInScrollPane(@NotNull Node node) {
    double w = this.pane.getWidth();
    double h = this.pane.getHeight();
    double x = node.getBoundsInParent().getMaxX();
    double y = node.getBoundsInParent().getMaxY();
    this.scrollPane.setHvalue(x / w);
    this.scrollPane.setVvalue(y / h);
  }
}
