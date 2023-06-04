package net.darmo_creations.jenealogio2.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import net.darmo_creations.jenealogio2.model.Person;
import net.darmo_creations.jenealogio2.ui.components.PersonWidget;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Optional;

/**
 * JavaFX component displaying a part of a family tree’s graph.
 */
public class FamilyTreePane extends FamilyTreeComponent {
  private final ObservableList<PersonWidget> personWidgets = FXCollections.observableList(new ArrayList<>());
  private final Pane pane = new Pane();

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

  public void refresh() {
    this.pane.getChildren().clear();
    this.personWidgets.clear();
    if (this.familyTree().isEmpty()) {
      return;
    }

    // TODO draw proper tree
    int y = 20;
    for (Person person : this.familyTree().get().persons()) {
      PersonWidget w = new PersonWidget(person);
      this.pane.getChildren().add(w);
      this.personWidgets.add(w);
      w.setLayoutX(20);
      w.setLayoutY(y);
      w.clickListeners().add(this::onPersonWidgetClick);
      y += 100;
    }
  }

  @Override
  public Optional<Person> getSelectedPerson() {
    return this.personWidgets.stream()
        .filter(PersonWidget::isSelected)
        .findFirst()
        .map(PersonWidget::person);
  }

  @Override
  protected void deselectAll() {
    this.personWidgets.forEach(w -> w.setSelected(false));
  }

  protected void select(@NotNull Person person) {
    this.personWidgets.forEach(w -> w.setSelected(person == w.person()));
  }

  private void onPersonWidgetClick(@NotNull PersonWidget personWidget, int clickCount) {
    this.select(personWidget.person());
    this.firePersonClickEvent(personWidget.person(), clickCount);
    this.pane.requestFocus();
  }

  private void onBackgroundClicked(MouseEvent event) {
    this.deselectAll();
    this.firePersonClickEvent(null, event.getClickCount());
    this.pane.requestFocus();
  }
}
