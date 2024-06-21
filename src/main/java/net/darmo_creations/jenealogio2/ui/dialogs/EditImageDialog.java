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
public class EditImageDialog extends DialogBase<EditImageDialog.PictureData> {
  private final ImageView imageView = new ImageView();
  private final Label imageNameLabel = new Label();
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
    HBox imageNameBox = new HBox(this.imageNameLabel);
    imageNameBox.setAlignment(Pos.TOP_CENTER);

    this.populateDatePrecisionCombo();
    this.dateField = new CalendarDateField(config);
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
    this.imageView.fitHeightProperty().bind(this.stage().heightProperty().subtract(250));

    Stage stage = this.stage();
    stage.setMinWidth(850);
    stage.setMinHeight(650);
    this.setIcon(config.theme().getAppIcon());

    this.setResultConverter(buttonType -> {
      if (!buttonType.getButtonData().isCancelButton()) {
        String desc = StringUtils.stripNullable(this.imageDescTextInput.getText()).orElse(null);
        DateTime date = this.dateField.getDate().orElse(null);
        return new PictureData(desc, date);
      }
      return null;
    });
  }

  /**
   * Populate the date precision combobox.
   */
  private void populateDatePrecisionCombo() {
    for (CalendarDateField.DateType dateType : CalendarDateField.DateType.values()) {
      this.datePrecisionCombo.getItems()
          .add(new NotNullComboBoxItem<>(dateType, this.config.language().translate(dateType.key())));
    }
  }

  /**
   * Set the picture to edit.
   *
   * @param picture A picture.
   */
  public void setPicture(@NotNull Picture picture) {
    this.imageView.setImage(picture.image());
    this.imageNameLabel.setText(picture.name());
    this.imageDescTextInput.setText(picture.description().orElse(""));
    Optional<DateTime> date = picture.date();
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

  public record PictureData(String description, DateTime date) {
  }
}
