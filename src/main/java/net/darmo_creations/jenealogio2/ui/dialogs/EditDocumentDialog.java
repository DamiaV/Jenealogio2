package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.config.theme.*;
import net.darmo_creations.jenealogio2.io.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.ui.components.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * This dialog allows editing the description of an {@link AttachedDocument}.
 */
public class EditDocumentDialog extends DocumentViewDialogBase<ButtonType>
    implements PersonRequester.PersonRequestListener,
    LifeEventRequester.LifeEventRequestListener {
  private AttachedDocument document;
  private FamilyTree familyTree;
  private final TextField documentNameField = new TextField();
  private final Label fileExtensionLabel = new Label();
  private final Label fileExtensionGraphicsLabel = new Label();
  private final DateTimeSelector dateTimeSelector;
  private final TextArea documentDescTextInput = new TextArea();
  private final PersonListView authorsList;
  private final Map<AnnotationType, AnnotationsListView> annotationsLists = new EnumMap<>(AnnotationType.class);

  private final TabPane tabPane = new TabPane();
  private final Tab fileMetadataTab = new Tab();
  private final Tab authorsTab = new Tab();
  private final Tab annotationsTab = new Tab();

  private final SelectPersonDialog selectPersonDialog;
  private final SelectLifeEventDialog selectLifeEventDialog;

  /**
   * Create a new dialog to edit documents.
   *
   * @param config The app’s config.
   */
  public EditDocumentDialog(final @NotNull Config config) {
    super(
        config,
        "edit_document",
        true,
        true,
        ButtonTypes.OK,
        ButtonTypes.CANCEL
    );
    this.selectPersonDialog = new SelectPersonDialog(config);
    this.selectLifeEventDialog = new SelectLifeEventDialog(config);

    this.dateTimeSelector = new DateTimeSelector(config);

    this.setupFileMetadataTab();
    this.authorsList = new PersonListView(config, false);
    this.setupAuthorsTab();
    this.setupAnnotationsTab();

    this.tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    this.tabPane.getTabs().addAll(this.fileMetadataTab, this.authorsTab, this.annotationsTab);

    this.setContent(this.tabPane);

    this.setResultConverter(buttonType -> {
      if (!buttonType.getButtonData().isCancelButton()) {
        final String newName = StringUtils.stripNullable(this.documentNameField.getText())
            .orElseThrow(() -> new RuntimeException("Document name cannot be empty"));
        if (!this.document.name().equals(newName))
          this.familyTree.renameDocument(this.document.fileName(), newName);
        this.document.setDescription(
            StringUtils.stripNullable(this.documentDescTextInput.getText()).orElse(null));
        this.document.setDate(this.dateTimeSelector.getDateTime());

        this.document.clearAuthors();
        final var authors = this.authorsList.getPersons();
        for (int i = 0; i < authors.size(); i++)
          this.document.addAuthor(authors.get(i), i);

        this.document.clearObjectAnnotations();
        for (final var annotationType : AnnotationType.values())
          for (final var annotation : this.annotationsLists.get(annotationType).getAnnotations())
            this.document.annotateObject(
                annotationType, annotation.object(), annotation.note().orElse(null));
      }
      return buttonType;
    });

    this.updateUI();
  }

  private void setupFileMetadataTab() {
    final Language language = this.config.language();

    HBox.setHgrow(this.documentNameField, Priority.ALWAYS);
    this.documentNameField.textProperty().addListener(
        (observableValue, oldValue, newValue) -> this.updateUI());
    this.documentNameField.setTextFormatter(StringUtils.filePathTextFormatter());
    final HBox documentNameBox = new HBox(
        5,
        this.documentNameField,
        this.fileExtensionLabel,
        this.fileExtensionGraphicsLabel
    );
    documentNameBox.setAlignment(Pos.CENTER_LEFT);

    this.dateTimeSelector.dateTimeProperty().addListener(
        (observable, oldValue, newValue) -> this.updateUI());

    VBox.setVgrow(this.documentDescTextInput, Priority.ALWAYS);

    final VBox content = new VBox(
        5,
        new Label(language.translate("dialog.edit_document.name")),
        documentNameBox,
        new Label(language.translate("dialog.edit_document.date")),
        this.dateTimeSelector,
        new Label(language.translate("dialog.edit_document.description")),
        this.documentDescTextInput
    );
    content.setPadding(new Insets(5, 0, 0, 0));
    this.fileMetadataTab.setText(language.translate("dialog.edit_document.tab.file_metadata"));
    this.fileMetadataTab.setContent(content);
  }

  private void setupAuthorsTab() {
    final Language language = this.config.language();

    this.authorsList.setPersonRequestListener(this);

    this.authorsTab.setText(language.translate("dialog.edit_document.tab.authors"));
    this.authorsTab.setContent(this.authorsList);
  }

  private void setupAnnotationsTab() {
    final Language language = this.config.language();

    final GridPane content = new GridPane();
    content.setHgap(5);
    content.setVgap(5);

    int i = 0;
    for (final var annotationType : AnnotationType.values()) {
      final var list = new AnnotationsListView(this.config);
      list.setPersonRequestListener(this);
      list.setLifeEventRequestListener(this);
      this.annotationsLists.put(annotationType, list);
      final Label label = new Label(language.translate(
          "dialog.edit_document.annotations." + annotationType.name().toLowerCase()));
      label.setPadding(new Insets(3, 0, 0, 0));
      content.addRow(i++, label, list);
      final RowConstraints rc = new RowConstraints();
      rc.setValignment(VPos.TOP);
      rc.setVgrow(Priority.ALWAYS);
      content.getRowConstraints().add(rc);
    }
    final var cc1 = new ColumnConstraints();
    cc1.setHgrow(Priority.SOMETIMES);
    final var cc2 = new ColumnConstraints();
    cc2.setHgrow(Priority.ALWAYS);
    content.getColumnConstraints().addAll(cc1, cc2);
    content.setPadding(new Insets(5, 0, 0, 0));

    this.annotationsTab.setText(language.translate("dialog.edit_document.tab.annotations"));
    this.annotationsTab.setContent(content);
  }

  private void updateUI() {
    final var name = StringUtils.stripNullable(this.documentNameField.getText());
    this.getDialogPane().lookupButton(ButtonTypes.OK)
        .setDisable(name.isEmpty() || this.familyTree.getDocument(name.get()).isPresent());
  }

  @Override
  public Optional<Person> onPersonRequest(final @NotNull List<Person> exclusionList) {
    this.selectPersonDialog.updatePersonList(this.familyTree, exclusionList);
    return this.selectPersonDialog.showAndWait();
  }

  @Override
  public Optional<LifeEvent> onLifeEventRequest(final @NotNull List<LifeEvent> exclusionList) {
    this.selectLifeEventDialog.updatePersonList(this.familyTree, exclusionList);
    return this.selectLifeEventDialog.showAndWait();
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
    final Icon fileTypeIcon = Icon.forFile(document.fileName());
    final Image image;
    if (document instanceof Picture pic)
      image = pic.image().orElse(this.config.theme().getIconImage(Icon.NO_IMAGE, Icon.Size.BIG));
    else image = this.config.theme().getIconImage(fileTypeIcon, Icon.Size.BIG);
    this.imageView.setImage(image);
    this.documentNameField.setText(document.name());
    // Disable renaming if document is not yet registered in the tree
    this.documentNameField.setDisable(familyTree.getDocument(document.fileName()).isEmpty());
    final Optional<String> ext = FileUtils.splitExtension(document.fileName()).extension();
    this.fileExtensionLabel.setText(ext.orElse(null));
    this.fileExtensionGraphicsLabel.setGraphic(this.config.theme().getIcon(fileTypeIcon, Icon.Size.SMALL));
    this.documentDescTextInput.setText(document.description().orElse(""));
    this.dateTimeSelector.setDateTime(document.date().orElse(null));

    this.authorsList.setPersons(document.authors());

    for (final var annotationType : AnnotationType.values()) {
      final var annotatedObjects = document.annotatedObjects(annotationType);
      this.annotationsLists.get(annotationType).setAnnotations(annotatedObjects.entrySet()
          .stream()
          .map(e -> new ObjectAnnotation(e.getKey(), e.getValue().orElse(null)))
          .toList());
    }

    this.tabPane.getSelectionModel().select(this.fileMetadataTab);
  }
}
