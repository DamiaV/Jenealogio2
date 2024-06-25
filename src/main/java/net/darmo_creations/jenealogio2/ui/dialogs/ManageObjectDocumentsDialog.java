package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.event.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.themes.*;
import net.darmo_creations.jenealogio2.ui.*;
import net.darmo_creations.jenealogio2.ui.components.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * This dialog manages the documents of {@link GenealogyObject}s.
 */
public class ManageObjectDocumentsDialog extends DialogBase<ManageObjectDocumentsDialog.Result> {
  private final SelectDocumentDialog selectDocumentDialog;
  private final EditDocumentDialog editDocumentDialog;

  private final Label buttonDescriptionLabel = new Label();

  private final ImageView mainImageView = new ImageView();
  private final Button removeMainImageButton = new Button();
  private final Button setAsMainImageButton = new Button();
  private final Button removeDocumentButton = new Button();
  private final Button editDocumentDescButton = new Button();
  private final Button deleteDocumentButton = new Button();
  private final ListView<DocumentView> documentsList = new ListView<>();
  private final Button applyButton;

  /**
   * The object’s current main document.
   */
  private Picture mainPicture;
  /**
   * Set of documents to remove from the current tree.
   */
  private final Set<AttachedDocument> documentsToDelete = new HashSet<>();
  /**
   * Set of documents to remove from the current object.
   */
  private final Set<AttachedDocument> documentsToRemove = new HashSet<>();
  /**
   * Set of documents to add to the current object.
   */
  private final Set<AttachedDocument> documentsToAdd = new HashSet<>();

  private boolean pendingUpdates = false;
  private boolean anyDocumentUpdated = false;

  /**
   * The object to manage the documents of.
   */
  private GenealogyObject<?> genealogyObject;
  /**
   * The family tree the object belongs to.
   */
  private FamilyTree familyTree;

  /**
   * Create a new dialog to manage documents.
   *
   * @param config The app’s config.
   */
  public ManageObjectDocumentsDialog(final @NotNull Config config) {
    super(config, "manage_object_documents", true, ButtonTypes.OK, ButtonTypes.APPLY, ButtonTypes.CANCEL);

    Language language = config.language();
    Theme theme = config.theme();

    this.selectDocumentDialog = new SelectDocumentDialog(config);
    this.editDocumentDialog = new EditDocumentDialog(config);

    VBox content = new VBox(5);
    content.getChildren().add(new HBox(5,
        new Label(language.translate("dialog.manage_object_documents.main_image")),
        this.removeMainImageButton
    ));
    this.mainImageView.setPreserveRatio(true);
    this.mainImageView.setFitWidth(100);
    this.mainImageView.setFitHeight(100);
    content.getChildren().add(this.mainImageView);

    Pane spacer = new Pane();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    this.removeMainImageButton.setText(language.translate("dialog.manage_object_documents.remove_main_image"));
    this.removeMainImageButton.setGraphic(theme.getIcon(Icon.REMOVE_MAIN_IMAGE, Icon.Size.SMALL));
    this.removeMainImageButton.setOnAction(e -> this.onRemoveMainImage());
    this.removeMainImageButton.hoverProperty().addListener((observable, oldValue, newValue)
        -> this.showButtonDescription(newValue, "dialog.manage_object_documents.remove_main_image"));

    Button addDocumentButton = new Button(
        language.translate("dialog.manage_object_documents.add_document"),
        theme.getIcon(Icon.ADD_DOCUMENT, Icon.Size.SMALL)
    );
    addDocumentButton.setOnAction(e -> this.onAddDocument());
    addDocumentButton.hoverProperty().addListener((observable, oldValue, newValue)
        -> this.showButtonDescription(newValue, "dialog.manage_object_documents.add_document"));

    this.setAsMainImageButton.setText(language.translate("dialog.manage_object_documents.set_as_main_image"));
    this.setAsMainImageButton.setGraphic(theme.getIcon(Icon.SET_AS_MAIN_IMAGE, Icon.Size.SMALL));
    this.setAsMainImageButton.setOnAction(e -> this.onSetAsMainImage());
    this.setAsMainImageButton.hoverProperty().addListener((observable, oldValue, newValue)
        -> this.showButtonDescription(newValue, "dialog.manage_object_documents.set_as_main_image"));

    this.removeDocumentButton.setText(language.translate("dialog.manage_object_documents.remove_document"));
    this.removeDocumentButton.setGraphic(theme.getIcon(Icon.REMOVE_DOCUMENT, Icon.Size.SMALL));
    this.removeDocumentButton.setOnAction(e -> this.onRemoveDocuments());
    this.removeDocumentButton.hoverProperty().addListener((observable, oldValue, newValue)
        -> this.showButtonDescription(newValue, "dialog.manage_object_documents.remove_document"));

    this.editDocumentDescButton.setText(language.translate("dialog.manage_object_documents.edit_document_desc"));
    this.editDocumentDescButton.setGraphic(theme.getIcon(Icon.EDIT_DOCUMENT_DESC, Icon.Size.SMALL));
    this.editDocumentDescButton.setOnAction(e -> this.onEditDocumentDesc());
    this.editDocumentDescButton.hoverProperty().addListener((observable, oldValue, newValue)
        -> this.showButtonDescription(newValue, "dialog.manage_object_documents.edit_document_desc"));

    this.deleteDocumentButton.setText(language.translate("dialog.manage_object_documents.delete_document"));
    this.deleteDocumentButton.setGraphic(theme.getIcon(Icon.DELETE_DOCUMENT, Icon.Size.SMALL));
    this.deleteDocumentButton.setOnAction(e -> this.onDeleteDocuments());
    this.deleteDocumentButton.hoverProperty().addListener((observable, oldValue, newValue)
        -> this.showButtonDescription(newValue, "dialog.manage_object_documents.delete_document"));

    HBox title = new HBox(
        5,
        new Label(language.translate("dialog.manage_object_documents.list")),
        spacer,
        this.setAsMainImageButton,
        addDocumentButton,
        this.removeDocumentButton,
        this.editDocumentDescButton,
        this.deleteDocumentButton
    );
    content.getChildren().add(title);

    this.documentsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    this.documentsList.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> this.updateButtons());
    this.documentsList.setOnMouseClicked(this::onListClicked);
    VBox.setVgrow(this.documentsList, Priority.ALWAYS);
    content.getChildren().add(this.documentsList);

    this.buttonDescriptionLabel.getStyleClass().add("help-text");
    content.getChildren().add(this.buttonDescriptionLabel);

    content.setPrefWidth(800);
    content.setPrefHeight(600);
    this.getDialogPane().setContent(content);

    Stage stage = this.stage();
    stage.setMinWidth(400);
    stage.setMinHeight(400);

    this.applyButton = (Button) this.getDialogPane().lookupButton(ButtonTypes.APPLY);
    this.applyButton.addEventFilter(ActionEvent.ACTION, event -> {
      this.updateObject();
      event.consume();
    });

    this.setResultConverter(buttonType -> {
      boolean updated = !buttonType.getButtonData().isCancelButton();
      if (updated)
        this.updateObject();
      return new Result(updated, this.anyDocumentUpdated);
    });
  }

  public void setObject(@NotNull GenealogyObject<?> object, @NotNull FamilyTree familyTree) {
    this.genealogyObject = object;
    this.familyTree = familyTree;
    this.documentsToDelete.clear();
    this.documentsToRemove.clear();
    this.documentsToAdd.clear();
    Language language = this.config.language();
    this.setTitle(language.translate("dialog.manage_object_documents.title",
        new FormatArg("name", object.name(language))));
    Optional<Picture> image = this.genealogyObject.mainPicture();
    this.mainPicture = image.orElse(null);
    Image defaultImage = object instanceof Person ? PersonWidget.DEFAULT_IMAGE : PersonDetailsView.DEFAULT_EVENT_IMAGE;
    this.mainImageView.setImage(image.flatMap(Picture::image).orElse(defaultImage));
    this.removeMainImageButton.setDisable(image.isEmpty());
    this.documentsList.getItems().clear();
    for (var document : this.genealogyObject.documents()) {
      this.documentsList.getItems().add(new DocumentView(document, true, this.config));
    }
    this.documentsList.getItems().sort(null);
    this.pendingUpdates = false;
    this.anyDocumentUpdated = false;
    this.updateButtons();
  }

  private void removeMainImage() {
    this.mainPicture = null;
    this.mainImageView.setImage(PersonWidget.DEFAULT_IMAGE);
    this.pendingUpdates = true;
  }

  private void onRemoveMainImage() {
    if (this.mainPicture == null)
      return;
    this.removeMainImage();
    this.updateButtons();
  }

  private void onSetAsMainImage() {
    List<DocumentView> selection = this.getSelectedDocuments();
    if (selection.size() != 1)
      return;
    DocumentView dv = selection.get(0);
    if (!(dv.document() instanceof Picture pic))
      return;
    this.mainPicture = pic;
    this.mainImageView.setImage(this.mainPicture.image().orElse(null));
    this.pendingUpdates = true;
    this.updateButtons();
  }

  private void onAddDocument() {
    var exclusionList = new LinkedList<>(this.documentsList.getItems().stream().map(DocumentView::document).toList());
    exclusionList.addAll(this.documentsToDelete);
    this.selectDocumentDialog.updateDocumentsList(this.familyTree, exclusionList);
    this.selectDocumentDialog.showAndWait().ifPresent(documents -> {
      documents.forEach(p -> {
        DocumentView dv = new DocumentView(p, true, this.config);
        this.documentsList.getItems().add(dv);
        this.documentsList.scrollTo(dv);
        this.documentsToAdd.add(p);
        this.documentsToRemove.remove(p);
        this.documentsToDelete.remove(p);
      });
      this.documentsList.getItems().sort(null);
      this.pendingUpdates = true;
      this.updateButtons();
    });
  }

  private void onRemoveDocuments() {
    List<DocumentView> selection = this.getSelectedDocuments();
    if (selection.isEmpty())
      return;
    selection.forEach(dv -> {
      AttachedDocument document = dv.document();
      if (document instanceof Picture pic && pic.equals(this.mainPicture))
        this.removeMainImage();
      this.documentsToRemove.add(document);
      this.documentsToAdd.remove(document);
      this.documentsList.getItems().remove(dv);
    });
    this.pendingUpdates = true;
    this.updateButtons();
  }

  private void onDeleteDocuments() {
    List<DocumentView> selection = this.getSelectedDocuments();
    if (selection.isEmpty())
      return;
    if (!Alerts.confirmation(
        this.config, "alert.delete_documents.header", null, "alert.delete_documents.title"))
      return;
    selection.forEach(dv -> {
      AttachedDocument document = dv.document();
      if (document instanceof Picture pic && pic.equals(this.mainPicture))
        this.removeMainImage();
      this.documentsToDelete.add(document);
      this.documentsToAdd.remove(document);
      this.documentsList.getItems().remove(dv);
    });
    this.pendingUpdates = true;
    this.updateButtons();
  }

  private void onEditDocumentDesc() {
    List<DocumentView> selection = this.getSelectedDocuments();
    if (selection.size() == 1) {
      this.openDocumentEditDialog(selection.get(0));
    }
  }

  private void onListClicked(final @NotNull MouseEvent event) {
    if (event.getClickCount() > 1) {
      this.onEditDocumentDesc();
    }
  }

  private void openDocumentEditDialog(@NotNull DocumentView documentView) {
    this.editDocumentDialog.setDocument(documentView.document(), this.familyTree);
    this.editDocumentDialog.showAndWait().ifPresent(b -> {
      if (b.getButtonData().isCancelButton())
        return;
      documentView.refresh();
      this.anyDocumentUpdated = true;
      this.pendingUpdates = true;
    });
    this.updateButtons();
  }

  private void showButtonDescription(boolean show, String i18nKey) {
    this.buttonDescriptionLabel.setText(show ? this.config.language().translate(i18nKey + ".tooltip") : null);
  }

  private List<DocumentView> getSelectedDocuments() {
    return new ArrayList<>(this.documentsList.getSelectionModel().getSelectedItems());
  }

  private void updateButtons() {
    this.removeMainImageButton.setDisable(this.mainPicture == null);
    var selectionModel = this.documentsList.getSelectionModel();
    boolean noSelection = selectionModel.isEmpty();
    this.removeDocumentButton.setDisable(noSelection);
    this.deleteDocumentButton.setDisable(noSelection);
    var selectedItems = selectionModel.getSelectedItems();
    boolean not1Selected = selectedItems.size() != 1;
    this.setAsMainImageButton.setDisable(
        not1Selected ||
        selectedItems.get(0) != null // Selection list sometimes contains null
        && (selectedItems.get(0).document().equals(this.mainPicture) || !(selectedItems.get(0).document() instanceof Picture))
    );
    this.editDocumentDescButton.setDisable(not1Selected);
    this.applyButton.setDisable(!this.pendingUpdates);
  }

  private void updateObject() {
    this.documentsToRemove
        .forEach(document -> this.familyTree.removeDocumentFromObject(document.fileName(), this.genealogyObject));
    this.documentsToRemove.clear();

    this.documentsToDelete
        .forEach(document -> this.familyTree.removeDocument(document.fileName()));
    this.documentsToDelete.clear();

    this.documentsToAdd.forEach(p -> {
      if (this.familyTree.getDocument(p.fileName()).isEmpty())
        this.familyTree.addDocument(p);
      this.familyTree.addDocumentToObject(p.fileName(), this.genealogyObject);
    });
    this.documentsToAdd.clear();

    if (this.mainPicture != null)
      this.familyTree.setMainPictureOfObject(this.mainPicture.fileName(), this.genealogyObject);
    else
      this.familyTree.setMainPictureOfObject(null, this.genealogyObject);

    this.pendingUpdates = false;
    this.updateButtons();
  }

  public record Result(boolean personUpdated, boolean anyDocumentUpdated) {
  }
}
