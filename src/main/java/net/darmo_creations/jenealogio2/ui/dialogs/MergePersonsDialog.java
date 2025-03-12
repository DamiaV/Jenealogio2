package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.themes.*;
import net.darmo_creations.jenealogio2.ui.components.choice.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.stream.*;

public class MergePersonsDialog extends DialogBase<ButtonType> {
  private final GridPane selectionPanel = new GridPane();
  private final Label person1Label = new Label();
  private final Label person2Label = new Label();
  private final SelectPersonDialog selectPersonDialog;
  private final Button personSelectorButton = new Button();

  private final GridPane dataPanel = new GridPane();
  private final Label leftPersonLabel = new Label();
  private final Label rightPersonLabel = new Label();
  private final ObjectChoiceWidget selectAllChoice;
  private final ObjectChoiceWidget lifeStatusChoice;
  private final ObjectChoiceWidget legalLastNameChoice;
  private final StringListChoiceWidget legalFirstNamesChoice;
  private final ObjectChoiceWidget publicLastNameChoice;
  private final StringListChoiceWidget publicFirstNamesChoice;
  private final StringListChoiceWidget nicknamesChoice;
  private final GenderChoiceWidget agabChoice;
  private final GenderChoiceWidget genderChoice;
  private final ObjectChoiceWidget disambiguationIdChoice;
  private final ObjectChoiceWidget mainOccupationChoice;
  private final List<ChoiceContainer> choices;
  private boolean internalChoiceUpdate;

  private final GridPane parentsPanel = new GridPane();
  // TODO parents/children

  // TODO life events

  private final List<Pair<Pane, Runnable>> panels = List.of(
      new Pair<>(this.selectionPanel, this::updateSelectorPanel),
      new Pair<>(this.dataPanel, this::updateDataPanel),
      new Pair<>(this.parentsPanel, this::updateParentsPanel)
  );
  private final boolean[] filledPanels = new boolean[this.panels.size()];
  private int visiblePanelIndex;

  private final Button previousButton;
  private final Button nextButton;
  private final Button finishButton;

  private FamilyTree familyTree;
  private Person person1;
  private Person person2;

  public MergePersonsDialog(final @NotNull Config config) {
    super(
        config,
        "merge_persons",
        true,
        ButtonTypes.FINISH,
        ButtonTypes.PREVIOUS,
        ButtonTypes.NEXT,
        ButtonTypes.CANCEL
    );

    this.selectPersonDialog = new SelectPersonDialog(config);
    this.setupSelectionPanel();

    // region Select panel
    this.selectAllChoice = new ObjectChoiceWidget(config, "", true);
    // Reuse translations from edit dialog
    final Language language = config.language();
    this.lifeStatusChoice = new ObjectChoiceWidget(
        config,
        language.translate("dialog.edit_person.profile.life_status"),
        false
    );
    this.legalFirstNamesChoice = new StringListChoiceWidget(
        config,
        language.translate("dialog.edit_person.profile.legal_first_names")
    );
    this.legalLastNameChoice = new ObjectChoiceWidget(
        config,
        language.translate("dialog.edit_person.profile.legal_last_name"),
        false
    );
    this.publicFirstNamesChoice = new StringListChoiceWidget(
        config,
        language.translate("dialog.edit_person.profile.public_first_names")
    );
    this.publicLastNameChoice = new ObjectChoiceWidget(
        config,
        language.translate("dialog.edit_person.profile.public_last_name"),
        false
    );
    this.nicknamesChoice = new StringListChoiceWidget(
        config,
        language.translate("dialog.edit_person.profile.nicknames")
    );
    this.agabChoice = new GenderChoiceWidget(
        config,
        language.translate("dialog.edit_person.profile.agab")
    );
    this.genderChoice = new GenderChoiceWidget(
        config,
        language.translate("dialog.edit_person.profile.gender")
    );
    this.disambiguationIdChoice = new ObjectChoiceWidget(
        config,
        language.translate("dialog.edit_person.profile.disambiguation_id"),
        false
    );
    this.mainOccupationChoice = new ObjectChoiceWidget(
        config,
        language.translate("dialog.edit_person.profile.main_occupation"),
        true
    );
    this.choices = List.of(
        new ChoiceContainer(
            this.lifeStatusChoice,
            () -> this.lifeStatusChoice.setData(
                language.translate("life_status." + this.person1.lifeStatus().name().toLowerCase()),
                language.translate("life_status." + this.person2.lifeStatus().name().toLowerCase())
            )
        ),
        new ChoiceContainer(
            this.legalFirstNamesChoice,
            () -> this.legalFirstNamesChoice.setData(
                this.person1.legalFirstNames(),
                this.person2.legalFirstNames()
            )
        ),
        new ChoiceContainer(
            this.legalLastNameChoice,
            () -> this.legalLastNameChoice.setData(
                this.person1.legalLastName().orElse(null),
                this.person2.legalLastName().orElse(null)
            )
        ),
        new ChoiceContainer(
            this.publicFirstNamesChoice,
            () -> this.publicFirstNamesChoice.setData(
                this.person1.publicFirstNames(),
                this.person2.publicFirstNames()
            )
        ),
        new ChoiceContainer(
            this.publicLastNameChoice,
            () -> this.publicLastNameChoice.setData(
                this.person1.publicLastName().orElse(null),
                this.person2.publicLastName().orElse(null)
            )
        ),
        new ChoiceContainer(
            this.nicknamesChoice,
            () -> this.nicknamesChoice.setData(
                this.person1.nicknames(),
                this.person2.nicknames()
            )
        ),
        new ChoiceContainer(
            this.agabChoice,
            () -> this.agabChoice.setData(
                this.person1.assignedGenderAtBirth().orElse(null),
                this.person2.assignedGenderAtBirth().orElse(null)
            )
        ),
        new ChoiceContainer(
            this.genderChoice,
            () -> this.genderChoice.setData(
                this.person1.gender().orElse(null),
                this.person2.gender().orElse(null)
            )
        ),
        new ChoiceContainer(
            this.disambiguationIdChoice,
            () -> this.disambiguationIdChoice.setData(
                this.person1.disambiguationID().orElse(null),
                this.person2.disambiguationID().orElse(null)
            )
        ),
        new ChoiceContainer(
            this.mainOccupationChoice,
            () -> this.mainOccupationChoice.setData(
                this.person1.mainOccupation().orElse(null),
                this.person2.mainOccupation().orElse(null)
            )
        )
    );
    this.setupDataPanel();
    // endregion

    this.setupParentsPanel();

    // region Bottom buttons
    this.previousButton = (Button) this.getDialogPane().lookupButton(ButtonTypes.PREVIOUS);
    this.previousButton.addEventFilter(ActionEvent.ACTION, event -> {
      if (this.visiblePanelIndex > 0) {
        this.visiblePanelIndex--;
        this.updateView();
      }
      event.consume();
    });
    this.nextButton = (Button) this.getDialogPane().lookupButton(ButtonTypes.NEXT);
    this.nextButton.addEventFilter(ActionEvent.ACTION, event -> {
      if (this.visiblePanelIndex < this.panels.size() - 1) {
        this.visiblePanelIndex++;
        this.updateView();
      }
      event.consume();
    });
    this.finishButton = (Button) this.getDialogPane().lookupButton(ButtonTypes.FINISH);
    // endregion

    final Stage stage = this.stage();
    stage.setMinWidth(800);
    stage.setMinHeight(600);

    this.setResultConverter(buttonType -> {
      if (!buttonType.getButtonData().isCancelButton())
        this.applyChanges();
      return buttonType;
    });
  }

  public void setPerson(final @NotNull FamilyTree familyTree, final @NotNull Person person) {
    this.familyTree = Objects.requireNonNull(familyTree);
    this.person1 = Objects.requireNonNull(person);
    this.person2 = null;
    this.selectPersonDialog.updatePersonList(familyTree, List.of(person));
    this.visiblePanelIndex = 0;
    Arrays.fill(this.filledPanels, false);
    this.updateView();
  }

  // region Panels setup

  private void setupSelectionPanel() {
    final Language language = this.config.language();

    this.personSelectorButton.setGraphic(this.config.theme().getIcon(Icon.EDIT_PERSON, Icon.Size.SMALL));
    this.personSelectorButton.setTooltip(new Tooltip(
        language.translate("dialog.merge_persons.selection_panel.select_person.tooltip")));
    this.personSelectorButton.setOnAction(event -> {
      final var result = this.selectPersonDialog.showAndWait();
      result.ifPresent(person -> {
        this.person2 = person;
        Arrays.fill(this.filledPanels, 1, this.filledPanels.length, false);
        this.updateSelectorPanel();
      });
    });

    this.selectionPanel.setHgap(5);
    this.selectionPanel.setVgap(5);

    this.selectionPanel.addRow(
        0,
        new Label(language.translate("dialog.merge_persons.selection_panel.source_person")),
        new Pane(),
        this.person1Label
    );
    this.selectionPanel.addRow(
        1,
        new Label(language.translate("dialog.merge_persons.selection_panel.target_person")),
        this.personSelectorButton,
        this.person2Label
    );
    final var cc = new ColumnConstraints();
    cc.setHgrow(Priority.ALWAYS);
    this.selectionPanel.getColumnConstraints().addAll(
        new ColumnConstraints(),
        new ColumnConstraints(),
        cc
    );
  }

  private void setupDataPanel() {
    final Language language = this.config.language();

    this.selectAllChoice.setData(
        language.translate("dialog.merge_persons.data_panel.keep_all_left"),
        language.translate("dialog.merge_persons.data_panel.keep_all_right")
    );
    this.selectAllChoice.addSelectionListener(selection -> {
      if (this.internalChoiceUpdate) return;
      this.choices.forEach(choice -> {
        if (selection == PersonMergeInfo.Which.BOTH && !choice.choiceWidget().bothEnabled())
          return;
        choice.choiceWidget().setSelection(selection);
      });
    });

    this.lifeStatusChoice.addSelectionListener(selection -> this.onRadioButtonUpdate());
    this.legalFirstNamesChoice.addSelectionListener(selection -> this.onRadioButtonUpdate());
    this.legalLastNameChoice.addSelectionListener(selection -> this.onRadioButtonUpdate());
    this.publicFirstNamesChoice.addSelectionListener(selection -> this.onRadioButtonUpdate());
    this.publicLastNameChoice.addSelectionListener(selection -> this.onRadioButtonUpdate());
    this.nicknamesChoice.addSelectionListener(selection -> this.onRadioButtonUpdate());
    this.agabChoice.addSelectionListener(selection -> this.onRadioButtonUpdate());
    this.genderChoice.addSelectionListener(selection -> this.onRadioButtonUpdate());
    this.disambiguationIdChoice.addSelectionListener(selection -> this.onRadioButtonUpdate());
    this.mainOccupationChoice.addSelectionListener(selection -> this.onRadioButtonUpdate());

    this.dataPanel.setHgap(5);
    this.dataPanel.setVgap(5);
    int j = 0;
    this.dataPanel.addRow(
        j++,
        new Pane(),
        new Pane(),
        this.leftPersonLabel,
        new Pane(),
        this.rightPersonLabel
    );
    this.dataPanel.addRow(
        j++,
        this.selectAllChoice.titleLabel(),
        this.selectAllChoice.leftRadio(),
        this.selectAllChoice.leftNode(),
        this.selectAllChoice.rightRadio(),
        this.selectAllChoice.rightNode(),
        this.selectAllChoice.bothRadio()
    );
    this.dataPanel.addRow(
        j++,
        new Separator(Orientation.HORIZONTAL),
        new Separator(Orientation.HORIZONTAL),
        new Separator(Orientation.HORIZONTAL),
        new Separator(Orientation.HORIZONTAL),
        new Separator(Orientation.HORIZONTAL),
        new Separator(Orientation.HORIZONTAL)
    );
    for (int i = 0; i < this.choices.size(); i++) {
      final var choice = this.choices.get(i).choiceWidget();
      this.dataPanel.addRow(
          i + j,
          choice.titleLabel(),
          choice.leftRadio(),
          choice.leftNode(),
          choice.rightRadio(),
          choice.rightNode(),
          choice.bothRadio()
      );
    }

    final var cc1 = new ColumnConstraints();
    cc1.setHgrow(Priority.ALWAYS);
    final var cc2 = new ColumnConstraints();
    cc2.setHgrow(Priority.ALWAYS);
    this.dataPanel.getColumnConstraints().addAll(
        new ColumnConstraints(),
        new ColumnConstraints(),
        cc1,
        new ColumnConstraints(),
        cc2
    );
  }

  private void setupParentsPanel() {
    // TODO
  }

  // endregion
  // region Panels filling

  private void updateView() {
    this.previousButton.setDisable(this.visiblePanelIndex == 0);
    this.nextButton.setDisable(this.visiblePanelIndex == this.panels.size() - 1);
    this.finishButton.setDisable(this.visiblePanelIndex != this.panels.size() - 1);

    // TODO title and help text
    final Pair<Pane, Runnable> step = this.panels.get(this.visiblePanelIndex);
    this.getDialogPane().setContent(step.left());
    if (!this.filledPanels[this.visiblePanelIndex]) {
      step.right().run();
      this.filledPanels[this.visiblePanelIndex] = true;
    }
  }

  private void updateSelectorPanel() {
    this.person1Label.setText(this.person1.toString());
    this.person2Label.setText(
        this.person2 != null
            ? this.person2.toString()
            : this.config.language().translate("dialog.merge_persons.selector_panel.no_selection")
    );
    this.nextButton.setDisable(this.person2 == null);
  }

  private void updateDataPanel() {
    this.leftPersonLabel.setText(this.person1.toString());
    this.rightPersonLabel.setText(this.person2.toString());
    this.internalChoiceUpdate = true;
    boolean enableBoth = false;
    for (final ChoiceContainer choiceContainer : this.choices) {
      choiceContainer.setChoice().run();
      enableBoth |= choiceContainer.choiceWidget().bothEnabled();
    }
    this.selectAllChoice.bothRadio().setDisable(!enableBoth);
    this.internalChoiceUpdate = false;
    this.onRadioButtonUpdate(); // Force update
  }

  private void updateParentsPanel() {
    // TODO
  }

  // endregion
  // region Actions

  private void onRadioButtonUpdate() {
    final Set<PersonMergeInfo.Which> selections = this.choices.stream()
        .map(ChoiceContainer::choiceWidget)
        .filter(ChoiceWidget::isEnabled)
        .map(ChoiceWidget::selection)
        .collect(Collectors.toSet());
    this.internalChoiceUpdate = true;
    if (selections.size() > 1) this.selectAllChoice.setSelection(PersonMergeInfo.Which.NONE);
    else this.selectAllChoice.setSelection(selections.iterator().next());
    this.internalChoiceUpdate = false;
  }

  private void applyChanges() {
    final var dataSelection = new PersonMergeInfo(
        this.lifeStatusChoice.selection(),
        this.legalLastNameChoice.selection(),
        this.legalFirstNamesChoice.selection(),
        this.publicLastNameChoice.selection(),
        this.publicFirstNamesChoice.selection(),
        this.nicknamesChoice.selection(),
        this.agabChoice.selection(),
        this.genderChoice.selection(),
        this.disambiguationIdChoice.selection(),
        this.mainOccupationChoice.selection(),
        Map.of(), // TODO gather parents selection
        Map.of() // TODO gather children selection
    );

    // TODO gather life events selection

    this.familyTree.mergePersons(this.person1, this.person2, dataSelection);
  }

  // endregion

  private record ChoiceContainer(
      @NotNull ChoiceWidget<?, ?> choiceWidget,
      @NotNull Runnable setChoice
  ) {
    private ChoiceContainer {
      Objects.requireNonNull(choiceWidget);
      Objects.requireNonNull(setChoice);
    }
  }
}
