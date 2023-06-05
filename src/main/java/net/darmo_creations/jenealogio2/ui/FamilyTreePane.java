package net.darmo_creations.jenealogio2.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import net.darmo_creations.jenealogio2.AppController;
import net.darmo_creations.jenealogio2.model.FamilyTree;
import net.darmo_creations.jenealogio2.model.Person;
import net.darmo_creations.jenealogio2.ui.components.PersonWidget;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * JavaFX component displaying a part of a family tree’s graph.
 */
public class FamilyTreePane extends FamilyTreeComponent {
  private static final int MAX_LEVEL = 4;
  private static final int HGAP = 10;
  private static final int VGAP = 20;

  private final ObservableList<PersonWidget> personWidgets = FXCollections.observableList(new ArrayList<>());
  private final Pane pane = new Pane();

  private Person targettedPerson;

  /**
   * Create an empty family tree pane.
   */
  public FamilyTreePane() {
    this.setOnMouseClicked(this::onBackgroundClicked);
    AnchorPane.setTopAnchor(this.pane, 0.0);
    AnchorPane.setBottomAnchor(this.pane, 0.0);
    AnchorPane.setLeftAnchor(this.pane, 0.0);
    AnchorPane.setRightAnchor(this.pane, 0.0);
    this.getChildren().add(this.pane);
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

    List<List<PersonWidget>> levels = new ArrayList<>();
    PersonWidget root = this.createWidget(this.targettedPerson, null);
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
            PersonWidget parent1Widget = this.createWidget(parents.left().orElse(null),
                new ChildInfo(c, 0));
            PersonWidget parent2Widget = this.createWidget(parents.right().orElse(null),
                new ChildInfo(c, 1));
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

    double w = this.getWidth();
    // Widgets positioning
    for (int level = levels.size() - 1, row = 0; level >= 0; level--, row++) {
      List<PersonWidget> widgets = levels.get(level);
      double y = VGAP + (VGAP + PersonWidget.HEIGHT) * row;
      int cardsNb = (int) Math.pow(2, level);
      int betweenCardsGad = HGAP + ((int) Math.pow(2, row) - 1) * (PersonWidget.WIDTH + HGAP);
      double rowWidth = cardsNb * PersonWidget.WIDTH + (cardsNb - 1) * betweenCardsGad;
      double x = (w - rowWidth) / 2;
      for (PersonWidget widget : widgets) {
        if (widget != null) {
          widget.setLayoutX(x);
          widget.setLayoutY(y);
        }
        x += PersonWidget.WIDTH + betweenCardsGad;
      }
    }

    // Draw lines
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
        this.pane.getChildren().add(0, this.newLine(p1x, p1y, p2x, p2y)); // Line between parents
        this.pane.getChildren().add(0, this.newLine(cx, cy, (p1x + p2x) / 2, p1y)); // Child to parents line
      }
    });

    // TODO display direct children and all spouses
  }

  private Line newLine(double x1, double y1, double x2, double y2) {
    Line line = new Line(x1, y1, x2, y2);
    line.getStyleClass().add("tree-line");
    line.setStrokeWidth(2);
    return line;
  }

  private PersonWidget createWidget(final Person person, final ChildInfo childInfo) {
    PersonWidget w = new PersonWidget(person, childInfo);
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
  protected void deselectAll() {
    this.personWidgets.forEach(w -> w.setSelected(false));
  }

  @Override
  protected void select(@NotNull Person person, boolean updateTarget) {
    if (updateTarget) {
      this.targettedPerson = person;
    }
    this.refresh();
    this.personWidgets.forEach(w -> w.setSelected(w.person().isPresent() && person == w.person().get()));
  }

  private void onPersonWidgetClick(@NotNull PersonWidget personWidget, int clickCount, MouseButton button) {
    Optional<Person> person = personWidget.person();
    if (person.isPresent()) {
      this.select(person.get(), button == AppController.TARGET_UPDATE_BUTTON);
      // Prevent double-click when right-clicking to avoid double-click action to miss the widget
      this.firePersonClickEvent(person.get(), button == AppController.TARGET_UPDATE_BUTTON ? 1 : clickCount, button);
    } else {
      personWidget.childInfo().ifPresent(this::fireNewParentClickEvent);
    }
    this.pane.requestFocus();
  }

  private void onBackgroundClicked(MouseEvent event) {
    this.deselectAll();
    this.firePersonClickEvent(null, event.getClickCount(), MouseButton.PRIMARY);
    this.pane.requestFocus();
  }
}
