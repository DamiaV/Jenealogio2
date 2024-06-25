package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.io.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.model.datetime.*;
import net.darmo_creations.jenealogio2.themes.*;
import net.darmo_creations.jenealogio2.ui.components.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * This dialog allows editing the description of an {@link AttachedDocument}.
 */
public class EditDocumentDialog extends DialogBase<ButtonType> {
  private AttachedDocument document;
  private FamilyTree familyTree;
  private final ImageView imageView = new ImageView();
  private final TextField documentNameField = new TextField();
  private final Label fileExtensionLabel = new Label();
  private final Label fileExtensionGraphicsLabel = new Label();
  private final ComboBox<NotNullComboBoxItem<CalendarDateField.DateType>> datePrecisionCombo = new ComboBox<>();
  private final CalendarDateField dateField;
  private final TextArea documentDescTextInput = new TextArea();

  /**
   * Create a new dialog to edit documents.
   *
   * @param config The app’s config.
   */
  public EditDocumentDialog(final @NotNull Config config) {
    super(config, "edit_document", true, ButtonTypes.OK, ButtonTypes.CANCEL);

    Language language = config.language();

    HBox imageViewBox = new HBox(this.imageView);
    imageViewBox.setAlignment(Pos.CENTER);
    this.imageView.setPreserveRatio(true);
    this.imageView.managedProperty().bind(this.imageView.visibleProperty());

    HBox.setHgrow(this.documentNameField, Priority.ALWAYS);
    this.documentNameField.textProperty().addListener((observableValue, oldValue, newValue) -> this.updateUI());
    this.documentNameField.setTextFormatter(StringUtils.filePathTextFormatter());
    HBox documentNameBox = new HBox(
        4,
        this.documentNameField,
        this.fileExtensionLabel,
        this.fileExtensionGraphicsLabel
    );
    documentNameBox.setAlignment(Pos.CENTER_LEFT);

    this.populateDatePrecisionCombo();
    this.dateField = new CalendarDateField(config);
    this.dateField.getUpdateListeners().add(this::updateUI);
    this.datePrecisionCombo.getSelectionModel().selectedItemProperty().addListener(
        (observable, oldValue, newValue) -> this.dateField.setDateType(newValue.data()));
    HBox.setHgrow(this.dateField, Priority.ALWAYS);
    HBox dateBox = new HBox(5, this.datePrecisionCombo, this.dateField);

    VBox.setVgrow(this.documentDescTextInput, Priority.ALWAYS);

    VBox content = new VBox(
        5,
        imageViewBox,
        new Label(language.translate("dialog.edit_document.name")),
        documentNameBox,
        new Label(language.translate("dialog.edit_document.date")),
        dateBox,
        new Label(language.translate("dialog.edit_document.description")),
        this.documentDescTextInput
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
        String newName = StringUtils.stripNullable(this.documentNameField.getText())
            .orElseThrow(() -> new RuntimeException("Document name cannot be empty"));
        if (!this.document.name().equals(newName))
          this.familyTree.renameDocument(this.document.fileName(), newName);
        this.document.setDescription(StringUtils.stripNullable(this.documentDescTextInput.getText()).orElse(null));
        this.document.setDate(this.dateField.getDate().orElse(null));
      }
      return buttonType;
    });

    this.updateUI();
  }

  private void updateUI() {
    var name = StringUtils.stripNullable(this.documentNameField.getText());
    this.getDialogPane().lookupButton(ButtonTypes.OK)
        .setDisable(name.isEmpty() || this.familyTree.getDocument(name.get()).isPresent());
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
   * Set the document to edit.
   *
   * @param document   A document.
   * @param familyTree The family tree the document belongs to.
   */
  public void setDocument(@NotNull AttachedDocument document, @NotNull FamilyTree familyTree) {
    this.document = Objects.requireNonNull(document);
    this.familyTree = Objects.requireNonNull(familyTree);
    this.setTitle(this.config.language().translate("dialog.edit_document.title",
        new FormatArg("name", document.fileName())));
    this.imageView.setImage(document instanceof Picture pic ? pic.image().orElse(null) : null);
    this.imageView.setVisible(document instanceof Picture);
    this.documentNameField.setText(document.name());
    // Disable renaming if document is not yet registered in the tree
    this.documentNameField.setDisable(familyTree.getDocument(document.fileName()).isEmpty());
    Optional<String> ext = FileUtils.splitExtension(document.fileName()).right();
    this.fileExtensionLabel.setText(ext.orElse(null));
    this.fileExtensionGraphicsLabel.setGraphic(this.config.theme().getIcon(Icon.forFile(document.fileName()), Icon.Size.SMALL));
    this.documentDescTextInput.setText(document.description().orElse(""));
    var date = document.date();
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
