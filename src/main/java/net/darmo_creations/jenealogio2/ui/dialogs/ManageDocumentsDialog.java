package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.collections.*;
import javafx.collections.transformation.*;
import javafx.event.*;
import javafx.geometry.*;
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
import java.util.stream.*;

/**
 * This dialog manages the documents of {@link GenealogyObject}s.
 */
public class ManageDocumentsDialog extends DialogBase<ManageDocumentsDialog.Result> {
  private static final int MAX_IMAGE_SIZE = 100;

  private final SelectDocumentDialog selectDocumentDialog;
  private final EditDocumentDialog editDocumentDialog;

  private final Label buttonDescriptionLabel = new Label();

  private final VBox mainImagePanel;
  private final ImageView mainImageView = new ImageView();
  private final Button removeMainImageButton = new Button();
  private final Button setAsMainImageButton = new Button();
  private final Button addDocumentButton = new Button();
  private final Button removeDocumentButton = new Button();
  private final Button editDocumentDescButton = new Button();
  private final Button deleteDocumentButton = new Button();
  private final ErasableTextField filterTextInput;
  private final ListView<DocumentView> documentsList = new ListView<>();
  private final ObservableList<DocumentView> documentsList_ = FXCollections.observableArrayList();
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
  public ManageDocumentsDialog(final @NotNull Config config) {
    super(config, "manage_object_documents", true, ButtonTypes.OK, ButtonTypes.APPLY, ButtonTypes.CANCEL);

    final Language language = config.language();
    final Theme theme = config.theme();

    this.selectDocumentDialog = new SelectDocumentDialog(config);
    this.editDocumentDialog = new EditDocumentDialog(config);

    final VBox content = new VBox(5);

    final HBox hBox = new HBox(5,
        new Label(language.translate("dialog.manage_object_documents.main_image")),
        this.removeMainImageButton
    );
    hBox.setAlignment(Pos.CENTER_LEFT);
    this.mainImageView.setPreserveRatio(true);

    this.mainImagePanel = new VBox(5, hBox, this.mainImageView);
    this.mainImagePanel.managedProperty().bind(this.mainImagePanel.visibleProperty());
    content.getChildren().add(this.mainImagePanel);

    final Pane spacer = new Pane();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    this.removeMainImageButton.setText(language.translate("dialog.manage_object_documents.remove_main_image"));
    this.removeMainImageButton.setGraphic(theme.getIcon(Icon.REMOVE_MAIN_IMAGE, Icon.Size.SMALL));
    this.removeMainImageButton.setOnAction(e -> this.onRemoveMainImage());
    this.removeMainImageButton.hoverProperty().addListener((observable, oldValue, newValue)
        -> this.showButtonDescription(newValue, "dialog.manage_object_documents.remove_main_image"));
    this.removeMainImageButton.managedProperty().bind(this.removeMainImageButton.visibleProperty());

    this.addDocumentButton.setGraphic(theme.getIcon(Icon.ADD_DOCUMENT, Icon.Size.SMALL));
    this.addDocumentButton.setOnAction(e -> this.onAddDocument());
    this.addDocumentButton.hoverProperty().addListener((observable, oldValue, newValue)
        -> this.showButtonDescription(newValue, "dialog.manage_%s_documents.add_document".formatted(this.genealogyObject != null ? "object" : "tree")));

    this.setAsMainImageButton.setText(language.translate("dialog.manage_object_documents.set_as_main_image"));
    this.setAsMainImageButton.setGraphic(theme.getIcon(Icon.SET_AS_MAIN_IMAGE, Icon.Size.SMALL));
    this.setAsMainImageButton.setOnAction(e -> this.onSetAsMainImage());
    this.setAsMainImageButton.hoverProperty().addListener((observable, oldValue, newValue)
        -> this.showButtonDescription(newValue, "dialog.manage_object_documents.set_as_main_image"));
    this.setAsMainImageButton.managedProperty().bind(this.setAsMainImageButton.visibleProperty());

    this.removeDocumentButton.setText(language.translate("dialog.manage_object_documents.remove_document"));
    this.removeDocumentButton.setGraphic(theme.getIcon(Icon.REMOVE_DOCUMENT, Icon.Size.SMALL));
    this.removeDocumentButton.setOnAction(e -> this.onRemoveDocuments());
    this.stage().getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.DELETE), this::onRemoveDocuments);
    this.removeDocumentButton.hoverProperty().addListener((observable, oldValue, newValue)
        -> this.showButtonDescription(newValue, "dialog.manage_object_documents.remove_document"));
    this.removeDocumentButton.managedProperty().bind(this.removeDocumentButton.visibleProperty());

    this.editDocumentDescButton.setGraphic(theme.getIcon(Icon.EDIT_DOCUMENT_DESC, Icon.Size.SMALL));
    this.editDocumentDescButton.setOnAction(e -> this.onEditDocumentDesc());
    this.editDocumentDescButton.hoverProperty().addListener((observable, oldValue, newValue)
        -> this.showButtonDescription(newValue, "dialog.manage_%s_documents.edit_document_desc".formatted(this.genealogyObject != null ? "object" : "tree")));

    this.deleteDocumentButton.setGraphic(theme.getIcon(Icon.DELETE_DOCUMENT, Icon.Size.SMALL));
    this.deleteDocumentButton.setOnAction(e -> this.onDeleteDocuments());
    this.stage().getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.DELETE, KeyCombination.SHIFT_DOWN), this::onDeleteDocuments);
    this.deleteDocumentButton.hoverProperty().addListener((observable, oldValue, newValue)
        -> this.showButtonDescription(newValue, "dialog.manage_%s_documents.delete_document".formatted(this.genealogyObject != null ? "object" : "tree")));

    final HBox title = new HBox(
        5,
        new Label(language.translate("dialog.manage_object_documents.list")),
        spacer,
        this.setAsMainImageButton,
        this.addDocumentButton,
        this.removeDocumentButton,
        this.editDocumentDescButton,
        this.deleteDocumentButton
    );
    title.setAlignment(Pos.CENTER_LEFT);
    content.getChildren().add(title);

    this.filterTextInput = new ErasableTextField(config);
    HBox.setHgrow(this.filterTextInput, Priority.ALWAYS);
    this.filterTextInput.textField().setPromptText(language.translate("dialog.select_documents.filter"));
    final FilteredList<DocumentView> filteredList = new FilteredList<>(this.documentsList_, data -> true);
    this.documentsList.setItems(filteredList);
    this.filterTextInput.textField().textProperty().addListener(((observable, oldValue, newValue) ->
        filteredList.setPredicate(documentView -> {
          if (newValue == null || newValue.isEmpty())
            return true;
          final String filter = newValue.toLowerCase();
          final AttachedDocument document = documentView.document();
          return document.fileName().toLowerCase().contains(filter)
                 || document.description().map(d -> d.toLowerCase().contains(filter)).orElse(false);
        })
    ));
    content.getChildren().add(this.filterTextInput);

    this.documentsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    this.documentsList.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> this.updateButtons());
    this.documentsList.setOnMouseClicked(this::onListClicked);
    VBox.setVgrow(this.documentsList, Priority.ALWAYS);
    content.getChildren().add(this.documentsList);

    this.buttonDescriptionLabel.getStyleClass().add("help-text");
    content.getChildren().add(this.buttonDescriptionLabel);

    content.setPrefWidth(900);
    this.getDialogPane().setContent(content);

    final Stage stage = this.stage();
    stage.setMinWidth(900);
    stage.setMinHeight(700);

    this.applyButton = (Button) this.getDialogPane().lookupButton(ButtonTypes.APPLY);
    this.applyButton.addEventFilter(ActionEvent.ACTION, event -> {
      this.updateObject();
      event.consume();
    });

    this.setResultConverter(buttonType -> {
      final boolean updated = !buttonType.getButtonData().isCancelButton();
      if (updated)
        this.updateObject();
      return new Result(updated, this.anyDocumentUpdated);
    });
  }

  public void setObject(GenealogyObject<?> object, @NotNull FamilyTree familyTree) {
    this.genealogyObject = object;
    this.familyTree = familyTree;
    this.documentsToDelete.clear();
    this.documentsToRemove.clear();
    this.documentsToAdd.clear();

    final Language language = this.config.language();

    final boolean hasObject = object != null;
    this.mainImagePanel.setVisible(hasObject);
    this.setAsMainImageButton.setVisible(hasObject);
    this.removeDocumentButton.setVisible(hasObject);
    final Collection<AttachedDocument> documents;
    if (hasObject) {
      this.setTitle(language.translate("dialog.manage_object_documents.title",
          new FormatArg("name", object.name(language))));
      final Optional<Picture> picture = this.genealogyObject.mainPicture();
      this.mainPicture = picture.orElse(null);
      final Image defaultImage = object instanceof Person ? PersonWidget.DEFAULT_IMAGE : PersonDetailsView.DEFAULT_EVENT_IMAGE;
      final Optional<Image> image = picture.map(p -> p.image().orElse(this.config.theme().getIconImage(Icon.NO_IMAGE, Icon.Size.BIG)));
      this.mainImageView.setImage(image.orElse(defaultImage));
      this.mainImageView.setFitHeight(Math.min(MAX_IMAGE_SIZE, image.map(Image::getHeight).orElse(Double.MAX_VALUE)));
      this.mainImageView.setFitWidth(Math.min(MAX_IMAGE_SIZE, image.map(Image::getWidth).orElse(Double.MAX_VALUE)));
      this.removeMainImageButton.setDisable(picture.isEmpty());
      this.addDocumentButton.setText(language.translate("dialog.manage_object_documents.add_document"));
      this.editDocumentDescButton.setText(language.translate("dialog.manage_object_documents.edit_document_desc"));
      this.deleteDocumentButton.setText(language.translate("dialog.manage_object_documents.delete_document"));
      documents = object.documents();
    } else {
      this.setTitle(language.translate("dialog.manage_tree_documents.title"));
      documents = familyTree.documents();
      this.addDocumentButton.setText(language.translate("dialog.manage_tree_documents.add_document"));
      this.editDocumentDescButton.setText(language.translate("dialog.manage_tree_documents.edit_document_desc"));
      this.deleteDocumentButton.setText(language.translate("dialog.manage_tree_documents.delete_document"));
    }

    this.filterTextInput.textField().setText(null);
    this.documentsList_.clear();
    for (final var document : documents)
      this.documentsList_.add(new DocumentView(document, true, this.config));
    this.documentsList_.sort(null);

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
    final List<DocumentView> selection = this.getSelectedDocuments();
    if (selection.size() != 1)
      return;
    final DocumentView dv = selection.get(0);
    if (!(dv.document() instanceof Picture pic))
      return;
    this.mainPicture = pic;
    final Image image = this.mainPicture.image().orElse(this.config.theme().getIconImage(Icon.NO_IMAGE, Icon.Size.BIG));
    this.mainImageView.setImage(image);
    //noinspection DataFlowIssue
    this.mainImageView.setFitHeight(Math.min(MAX_IMAGE_SIZE, image.getHeight()));
    this.mainImageView.setFitWidth(Math.min(MAX_IMAGE_SIZE, image.getWidth()));
    this.pendingUpdates = true;
    this.updateButtons();
  }

  private void onAddDocument() {
    final List<AttachedDocument> exclusionList = this.documentsList_.stream()
        .map(DocumentView::document)
        .collect(Collectors.toCollection(LinkedList::new));
    exclusionList.addAll(this.documentsToDelete);
    this.selectDocumentDialog.updateDocumentsList(this.familyTree, exclusionList);
    this.selectDocumentDialog.showAndWait().ifPresent(this::addDocumentsToList);
  }

  private void addDocumentsToList(final @NotNull Collection<AttachedDocument> documents) {
    documents.forEach(d -> {
      final DocumentView dv = new DocumentView(d, true, this.config);
      this.documentsList_.add(dv);
      this.documentsList.scrollTo(dv);
      this.documentsToAdd.add(d);
      this.documentsToRemove.remove(d);
      this.documentsToDelete.remove(d);
    });
    if (!documents.isEmpty()) {
      this.documentsList_.sort(null);
      this.pendingUpdates = true;
      this.updateButtons();
    }
  }

  private void onRemoveDocuments() {
    final List<DocumentView> selection = this.getSelectedDocuments();
    if (selection.isEmpty())
      return;
    selection.forEach(dv -> {
      final AttachedDocument document = dv.document();
      if (document instanceof Picture pic && pic.equals(this.mainPicture))
        this.removeMainImage();
      this.documentsToRemove.add(document);
      this.documentsToAdd.remove(document);
      this.documentsList_.remove(dv);
    });
    this.pendingUpdates = true;
    this.updateButtons();
  }

  private void onDeleteDocuments() {
    final List<DocumentView> selection = this.getSelectedDocuments();
    if (selection.isEmpty())
      return;
    if (!Alerts.confirmation(
        this.config, "alert.delete_documents.header", null, "alert.delete_documents.title"))
      return;
    selection.forEach(dv -> {
      final AttachedDocument document = dv.document();
      if (document instanceof Picture pic && pic.equals(this.mainPicture))
        this.removeMainImage();
      this.documentsToDelete.add(document);
      this.documentsToAdd.remove(document);
      this.documentsList_.remove(dv);
    });
    this.pendingUpdates = true;
    this.updateButtons();
  }

  private void onEditDocumentDesc() {
    final List<DocumentView> selection = this.getSelectedDocuments();
    if (selection.size() == 1)
      this.openDocumentEditDialog(selection.get(0));
  }

  private void onListClicked(final @NotNull MouseEvent event) {
    if (event.getClickCount() > 1)
      this.onEditDocumentDesc();
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
    final var selectionModel = this.documentsList.getSelectionModel();
    final boolean noSelection = selectionModel.isEmpty();
    this.removeDocumentButton.setDisable(noSelection);
    this.deleteDocumentButton.setDisable(noSelection);
    final var selectedItems = selectionModel.getSelectedItems();
    final boolean not1Selected = selectedItems.size() != 1;
    this.setAsMainImageButton.setDisable(
        not1Selected ||
        selectedItems.get(0) != null // Selection list sometimes contains null
        && (selectedItems.get(0).document().equals(this.mainPicture)
            || !(selectedItems.get(0).document() instanceof Picture))
    );
    this.editDocumentDescButton.setDisable(not1Selected);
    this.applyButton.setDisable(!this.pendingUpdates);
  }

  private void updateObject() {
    if (this.genealogyObject != null) {
      this.documentsToRemove
          .forEach(document -> this.familyTree.removeDocumentFromObject(document.fileName(), this.genealogyObject));
      this.documentsToRemove.clear();
    }

    this.documentsToDelete
        .forEach(document -> this.familyTree.removeDocument(document.fileName()));
    this.documentsToDelete.clear();

    this.documentsToAdd.forEach(p -> {
      if (this.familyTree.getDocument(p.fileName()).isEmpty())
        this.familyTree.addDocument(p);
      if (this.genealogyObject != null)
        this.familyTree.addDocumentToObject(p.fileName(), this.genealogyObject);
    });
    this.documentsToAdd.clear();

    if (this.genealogyObject != null)
      if (this.mainPicture != null)
        this.familyTree.setMainPictureOfObject(this.mainPicture.fileName(), this.genealogyObject);
      else
        this.familyTree.setMainPictureOfObject(null, this.genealogyObject);

    this.pendingUpdates = false;
    this.updateButtons();
  }

  public record Result(boolean targetUpdated, boolean anyDocumentUpdated) {
  }
}
