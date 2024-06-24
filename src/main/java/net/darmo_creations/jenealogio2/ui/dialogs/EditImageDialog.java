package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.model.datetime.*;
import net.darmo_creations.jenealogio2.ui.components.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * This dialog allows editing the description of a {@link Picture}.
 */
public class EditImageDialog extends DialogBase<ButtonType> {
  private Picture picture;
  private FamilyTree familyTree;
  private final ImageView imageView = new ImageView();
  private final TextField imageNameField = new TextField();
  private final ComboBox<NotNullComboBoxItem<CalendarDateField.DateType>> datePrecisionCombo = new ComboBox<>();
  private final CalendarDateField dateField;
  private final TextArea imageDescTextInput = new TextArea();

  /**
   * Create a new dialog to edit images.
   *
   * @param config The app’s config.
   */
  public EditImageDialog(final @NotNull Config config) {
    super(config, "edit_image", true, ButtonTypes.OK, ButtonTypes.CANCEL);

    Language language = config.language();

    HBox imageViewBox = new HBox(this.imageView);
    imageViewBox.setAlignment(Pos.CENTER);
    this.imageView.setPreserveRatio(true);

    HBox.setHgrow(this.imageNameField, Priority.ALWAYS);
    this.imageNameField.textProperty().addListener((observableValue, oldValue, newValue) -> this.updateUI());
    this.imageNameField.setTextFormatter(StringUtils.filePathTextFormatter());
    HBox imageNameBox = new HBox(
        4,
        new Label(language.translate("dialog.edit_image.name")),
        this.imageNameField
    );

    this.populateDatePrecisionCombo();
    this.dateField = new CalendarDateField(config);
    this.dateField.getUpdateListeners().add(this::updateUI);
    this.datePrecisionCombo.getSelectionModel().selectedItemProperty().addListener(
        (observable, oldValue, newValue) -> this.dateField.setDateType(newValue.data()));
    HBox.setHgrow(this.dateField, Priority.ALWAYS);
    HBox dateBox = new HBox(5, this.datePrecisionCombo, this.dateField);

    VBox.setVgrow(this.imageDescTextInput, Priority.ALWAYS);

    VBox content = new VBox(
        5,
        imageViewBox,
        imageNameBox,
        new Label(language.translate("dialog.edit_image.date")),
        dateBox,
        new Label(language.translate("dialog.edit_image.description")),
        this.imageDescTextInput
    );
    content.setPrefWidth(850);
    content.setPrefHeight(600);
    this.getDialogPane().setContent(content);

    this.imageView.fitWidthProperty().bind(this.stage().widthProperty().subtract(20));
    this.imageView.fitHeightProperty().bind(this.stage().heightProperty().subtract(300));

    Stage stage = this.stage();
    stage.setMinWidth(850);
    stage.setMinHeight(650);

    this.setResultConverter(buttonType -> {
      if (!buttonType.getButtonData().isCancelButton()) {
        String newName = StringUtils.stripNullable(this.imageNameField.getText())
            .orElseThrow(() -> new RuntimeException("Image name cannot be empty"));
        if (!this.picture.name().equals(newName))
          this.familyTree.renameDocument(this.picture.fileName(), newName);
        this.picture.setDescription(StringUtils.stripNullable(this.imageDescTextInput.getText()).orElse(null));
        this.picture.setDate(this.dateField.getDate().orElse(null));
      }
      return buttonType;
    });

    this.updateUI();
  }

  private void updateUI() {
    var name = StringUtils.stripNullable(this.imageNameField.getText());
    this.getDialogPane().lookupButton(ButtonTypes.OK)
        .setDisable(name.isEmpty() || this.familyTree.getPicture(name.get()).isPresent());
  }

  /**
   * Populate the date precision combobox.
   */
  private void populateDatePrecisionCombo() {
    for (var dateType : CalendarDateField.DateType.values()) {
      this.datePrecisionCombo.getItems()
          .add(new NotNullComboBoxItem<>(dateType, this.config.language().translate(dateType.key())));
    }
  }

  /**
   * Set the picture to edit.
   *
   * @param picture    A picture.
   * @param familyTree The family tree the picture belongs to.
   */
  public void setPicture(@NotNull Picture picture, @NotNull FamilyTree familyTree) {
    this.picture = Objects.requireNonNull(picture);
    this.familyTree = Objects.requireNonNull(familyTree);
    this.imageView.setImage(picture.image().orElse(null));
    this.imageNameField.setText(picture.name());
    // Disable renaming if image is not yet registered in the tree
    this.imageNameField.setDisable(familyTree.getPicture(picture.fileName()).isEmpty());
    this.imageDescTextInput.setText(picture.description().orElse(""));
    var date = picture.date();
    if (date.isPresent()) {
      DateTime d = date.get();
      this.datePrecisionCombo.getSelectionModel()
          .select(new NotNullComboBoxItem<>(CalendarDateField.DateType.fromDate(d)));
      this.dateField.setDate(d);
    } else {
      this.datePrecisionCombo.getSelectionModel().select(0);
      this.dateField.setDate(null);
    }
  }
}
