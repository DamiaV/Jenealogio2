package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.collections.*;
import javafx.collections.transformation.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.themes.*;
import net.darmo_creations.jenealogio2.ui.components.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * A dialog used to select a person from the given {@link FamilyTree} object.
 */
public class SelectPersonDialog extends DialogBase<Person> {
  private final ErasableTextField filterTextInput;
  private final ListView<PersonView> personListView = new ListView<>();
  private final ObservableList<PersonView> personList = FXCollections.observableArrayList();

  /**
   * Create a new dialog to select a person.
   *
   * @param config The app’s config.
   */
  public SelectPersonDialog(final @NotNull Config config) {
    super(
        config,
        "select_person",
        true,
        ButtonTypes.OK,
        ButtonTypes.CANCEL
    );
    final Language language = config.language();

    this.filterTextInput = new ErasableTextField(config);
    HBox.setHgrow(this.filterTextInput, Priority.ALWAYS);
    this.filterTextInput.textField().setPromptText(language.translate("dialog.select_person.filter"));
    final FilteredList<PersonView> filteredList = new FilteredList<>(this.personList, data -> true);
    this.personListView.setItems(filteredList);
    this.filterTextInput.textField().textProperty().addListener(
        (observable, oldValue, newValue) ->
            filteredList.setPredicate(pictureView -> {
              if (newValue == null || newValue.isEmpty())
                return true;
              return pictureView.person().matchesName(newValue, language);
            })
    );

    this.personListView.getSelectionModel().selectedItemProperty().addListener(
        (observable, oldValue, newValue) -> this.updateButtons());
    this.personListView.setOnMouseClicked(this::onListClicked);
    VBox.setVgrow(this.personListView, Priority.ALWAYS);

    final VBox content = new VBox(
        5,
        new HBox(
            5,
            new Label(language.translate("dialog.select_person.persons_list")),
            this.filterTextInput
        ),
        this.personListView
    );
    content.setPrefWidth(400);
    content.setPrefHeight(400);
    this.getDialogPane().setContent(content);

    this.getDialogPane().addEventHandler(KeyEvent.KEY_PRESSED, event -> {
      if (!this.filterTextInput.textField().isFocused())
        this.filterTextInput.textField().requestFocus();
    });

    final Stage stage = this.stage();
    stage.setMinWidth(400);
    stage.setMinHeight(400);

    this.setResultConverter(buttonType -> {
      if (!buttonType.getButtonData().isCancelButton())
        return this.personListView
            .getSelectionModel()
            .getSelectedItems()
            .stream()
            .map(PersonView::person)
            .findFirst()
            .orElse(null);
      return null;
    });
  }

  /**
   * Update the list of persons with the ones from the given tree, ignoring any that appear in the exclusion list.
   *
   * @param tree          Tree to pull persons from.
   * @param exclusionList List of persons to NOT add to the list view.
   */
  public void updatePersonList(final @NotNull FamilyTree tree, final @NotNull Collection<Person> exclusionList) {
    this.filterTextInput.textField().setText(null);
    this.personList.clear();
    tree.persons().stream()
        .filter(p -> !exclusionList.contains(p))
        .forEach(p -> this.personList.add(new PersonView(p, this.config)));
    this.personList.sort(
        (p1, p2) -> Person.lastThenFirstNamesComparator().compare(p1.person(), p2.person()));
  }

  /**
   * Enable double-click on a person to select it.
   */
  private void onListClicked(final @NotNull MouseEvent event) {
    if (event.getClickCount() > 1)
      ((Button) this.getDialogPane().lookupButton(ButtonTypes.OK)).fire();
  }

  private void updateButtons() {
    final boolean noSelection = this.personListView.getSelectionModel().getSelectedItems().isEmpty();
    this.getDialogPane().lookupButton(ButtonTypes.OK).setDisable(noSelection);
  }

  /**
   * Simple widget that shows the name, birth/death dates, and main picture of a given person.
   */
  public static class PersonView extends HBox {
    private static final int IMAGE_SIZE = 100;

    private final Person person;

    public PersonView(final @NotNull Person person, final @NotNull Config config) {
      super(5);
      this.person = person;
      final ImageView imageView = new ImageView(
          person.mainPicture().flatMap(Picture::image).orElse(PersonWidget.DEFAULT_IMAGE));
      imageView.setPreserveRatio(true);
      imageView.setFitWidth(IMAGE_SIZE);
      imageView.setFitHeight(IMAGE_SIZE);
      final Label nameLabel = new Label(person.toString());
      final DateLabel birthLabel = new DateLabel(person.getBirthDate().orElse(null), "?", config);
      birthLabel.setGraphic(config.theme().getIcon(Icon.BIRTH, Icon.Size.SMALL));
      final DateLabel deathLabel = new DateLabel(person.getDeathDate().orElse(null), "?", config);
      deathLabel.setGraphic(config.theme().getIcon(Icon.DEATH, Icon.Size.SMALL));
      this.getChildren().addAll(imageView, new VBox(5, nameLabel, birthLabel, deathLabel));
    }

    public Person person() {
      return this.person;
    }
  }
}
