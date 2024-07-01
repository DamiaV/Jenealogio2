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
  private final DateTimeSelector dateTimeSelector;
  private final TextArea documentDescTextInput = new TextArea();

  /**
   * Create a new dialog to edit documents.
   *
   * @param config The app’s config.
   */
  public EditDocumentDialog(final @NotNull Config config) {
    super(config, "edit_document", true, ButtonTypes.OK, ButtonTypes.CANCEL);

    final Language language = config.language();

    final HBox imageViewBox = new HBox(this.imageView);
    imageViewBox.setAlignment(Pos.CENTER);
    this.imageView.setPreserveRatio(true);
    this.imageView.managedProperty().bind(this.imageView.visibleProperty());

    HBox.setHgrow(this.documentNameField, Priority.ALWAYS);
    this.documentNameField.textProperty().addListener((observableValue, oldValue, newValue) -> this.updateUI());
    this.documentNameField.setTextFormatter(StringUtils.filePathTextFormatter());
    final HBox documentNameBox = new HBox(
        5,
        this.documentNameField,
        this.fileExtensionLabel,
        this.fileExtensionGraphicsLabel
    );
    documentNameBox.setAlignment(Pos.CENTER_LEFT);

    this.dateTimeSelector = new DateTimeSelector(config);
    this.dateTimeSelector.dateTimeProperty().addListener((observable, oldValue, newValue) -> this.updateUI());

    VBox.setVgrow(this.documentDescTextInput, Priority.ALWAYS);

    final VBox content = new VBox(
        5,
        imageViewBox,
        new Label(language.translate("dialog.edit_document.name")),
        documentNameBox,
        new Label(language.translate("dialog.edit_document.date")),
        this.dateTimeSelector,
        new Label(language.translate("dialog.edit_document.description")),
        this.documentDescTextInput
    );
    content.setPrefWidth(850);
    content.setPrefHeight(600);
    this.getDialogPane().setContent(content);

    this.imageView.fitWidthProperty().bind(this.stage().widthProperty().subtract(20));
    this.imageView.fitHeightProperty().bind(this.stage().heightProperty().subtract(300));

    final Stage stage = this.stage();
    stage.setMinWidth(850);
    stage.setMinHeight(650);

    this.setResultConverter(buttonType -> {
      if (!buttonType.getButtonData().isCancelButton()) {
        final String newName = StringUtils.stripNullable(this.documentNameField.getText())
            .orElseThrow(() -> new RuntimeException("Document name cannot be empty"));
        if (!this.document.name().equals(newName))
          this.familyTree.renameDocument(this.document.fileName(), newName);
        this.document.setDescription(StringUtils.stripNullable(this.documentDescTextInput.getText()).orElse(null));
        this.document.setDate(this.dateTimeSelector.getDateTime());
      }
      return buttonType;
    });

    this.updateUI();
  }

  private void updateUI() {
    final var name = StringUtils.stripNullable(this.documentNameField.getText());
    this.getDialogPane().lookupButton(ButtonTypes.OK)
        .setDisable(name.isEmpty() || this.familyTree.getDocument(name.get()).isPresent());
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
    final Optional<String> ext = FileUtils.splitExtension(document.fileName()).extension();
    this.fileExtensionLabel.setText(ext.orElse(null));
    this.fileExtensionGraphicsLabel.setGraphic(this.config.theme().getIcon(Icon.forFile(document.fileName()), Icon.Size.SMALL));
    this.documentDescTextInput.setText(document.description().orElse(""));
    final Optional<DateTime> date = document.date();
    this.dateTimeSelector.setDateTime(date.orElse(null));
  }
}
