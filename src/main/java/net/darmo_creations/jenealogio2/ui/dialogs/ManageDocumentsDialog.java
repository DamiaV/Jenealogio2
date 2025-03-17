package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.collections.*;
import javafx.collections.transformation.*;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.config.theme.*;
import net.darmo_creations.jenealogio2.io.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.ui.components.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * This dialog manages the documents of a {@link FamilyTree}.
 * Features a button to import documents directly from the filesystem.
 */
public class ManageDocumentsDialog extends DialogBase<ManageDocumentsDialog.Result> {
  private final EditDocumentDialog editDocumentDialog;

  private final Label buttonDescriptionLabel = new Label();

  private final Button editDocumentDescButton = new Button();
  private final Button deleteDocumentButton = new Button();
  private final ErasableTextField filterTextInput;
  private final ListView<DocumentView> documentsListView = new ListView<>();
  private final ObservableList<DocumentView> documentsList = FXCollections.observableArrayList();
  private final Button applyButton;

  /**
   * Set of documents to remove from the current tree.
   */
  private final Set<AttachedDocument> documentsToDelete = new HashSet<>();
  /**
   * Set of documents to add to the current object.
   */
  private final Set<AttachedDocument> documentsToAdd = new HashSet<>();

  private boolean pendingUpdates = false;
  private boolean anyDocumentUpdated = false;

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
    super(
        config,
        "manage_tree_documents",
        true,
        ButtonTypes.OK,
        ButtonTypes.APPLY,
        ButtonTypes.CANCEL
    );

    final Language language = config.language();
    final Theme theme = config.theme();

    this.editDocumentDialog = new EditDocumentDialog(config);

    final VBox content = new VBox(5);

    final Label label = new Label(
        language.translate("dialog.manage_tree_documents.description"),
        theme.getIcon(Icon.INFO, Icon.Size.SMALL)
    );
    label.setWrapText(true);
    content.getChildren().add(label);

    final Button addDocumentButton = new Button(
        language.translate("dialog.manage_tree_documents.add_document"),
        theme.getIcon(Icon.IMPORT_FILE, Icon.Size.SMALL)
    );
    addDocumentButton.setOnAction(e -> this.onAddDocument());
    addDocumentButton.hoverProperty().addListener(
        (observable, oldValue, newValue) ->
            this.showButtonDescription(
                newValue,
                "dialog.manage_tree_documents.add_document"
            ));

    this.editDocumentDescButton.setText(language.translate("dialog.manage_tree_documents.edit_document_desc"));
    this.editDocumentDescButton.setGraphic(theme.getIcon(Icon.EDIT_DOCUMENT_DESC, Icon.Size.SMALL));
    this.editDocumentDescButton.setOnAction(e -> this.onEditDocumentDesc());
    this.editDocumentDescButton.hoverProperty().addListener(
        (observable, oldValue, newValue) ->
            this.showButtonDescription(
                newValue,
                "dialog.manage_tree_documents.edit_document_desc"
            ));

    this.deleteDocumentButton.setText(language.translate("dialog.manage_tree_documents.delete_document"));
    this.deleteDocumentButton.setGraphic(theme.getIcon(Icon.DELETE_DOCUMENT, Icon.Size.SMALL));
    this.deleteDocumentButton.setOnAction(e -> this.onDeleteDocuments());
    this.stage().getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.DELETE, KeyCombination.SHIFT_DOWN), this::onDeleteDocuments);
    this.deleteDocumentButton.hoverProperty().addListener(
        (observable, oldValue, newValue) ->
            this.showButtonDescription(
                newValue,
                "dialog.manage_tree_documents.delete_document"
            ));

    final HBox buttons = new HBox(
        5,
        addDocumentButton,
        this.editDocumentDescButton,
        this.deleteDocumentButton
    );
    buttons.setAlignment(Pos.CENTER);
    content.getChildren().add(buttons);

    this.filterTextInput = new ErasableTextField(config);
    HBox.setHgrow(this.filterTextInput, Priority.ALWAYS);
    this.filterTextInput.textField().setPromptText(language.translate("dialog.select_documents.filter"));
    final FilteredList<DocumentView> filteredList = new FilteredList<>(this.documentsList, data -> true);
    this.documentsListView.setItems(filteredList);
    this.filterTextInput.textField().textProperty().addListener(
        ((observable, oldValue, newValue) ->
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

    this.documentsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    this.documentsListView.getSelectionModel().selectedItemProperty().addListener(
        (observable, oldValue, newValue) ->
            this.updateButtons());
    this.documentsListView.setOnMouseClicked(this::onListClicked);
    VBox.setVgrow(this.documentsListView, Priority.ALWAYS);
    content.getChildren().add(this.documentsListView);

    this.buttonDescriptionLabel.getStyleClass().add("help-text");
    content.getChildren().add(this.buttonDescriptionLabel);

    content.setPrefWidth(900);
    this.getDialogPane().setContent(content);

    final Stage stage = this.stage();
    stage.setMinWidth(900);
    stage.setMinHeight(700);

    this.applyButton = (Button) this.getDialogPane().lookupButton(ButtonTypes.APPLY);
    this.applyButton.addEventFilter(ActionEvent.ACTION, event -> {
      this.updateTree();
      event.consume();
    });

    this.getDialogPane().addEventHandler(KeyEvent.KEY_PRESSED, event -> {
      if (!this.filterTextInput.textField().isFocused())
        this.filterTextInput.textField().requestFocus();
    });

    // Files drag-and-drop
    final Scene scene = stage.getScene();
    scene.setOnDragOver(event -> {
      if (event.getGestureSource() == null // From another application
          && this.isDragAndDropValid(event.getDragboard()))
        event.acceptTransferModes(TransferMode.COPY);
      event.consume();
    });
    scene.setOnDragDropped(event -> {
      final Dragboard db = event.getDragboard();
      final boolean success = this.isDragAndDropValid(db);
      if (success)
        this.importFiles(db.getFiles().stream().map(File::toPath).toList());
      event.setDropCompleted(success);
      event.consume();
    });

    this.setResultConverter(buttonType -> {
      final boolean updated = !buttonType.getButtonData().isCancelButton();
      if (updated)
        this.updateTree();
      return new Result(updated, this.anyDocumentUpdated);
    });
  }

  private boolean isDragAndDropValid(final @NotNull Dragboard dragboard) {
    return dragboard.hasFiles(); // Accept all file types
  }

  public void refresh(@NotNull FamilyTree familyTree) {
    this.familyTree = familyTree;
    this.documentsToDelete.clear();
    this.documentsToAdd.clear();

    final Language language = this.config.language();

    final Collection<AttachedDocument> documents;
    this.setTitle(language.translate("dialog.manage_tree_documents.title"));
    documents = familyTree.documents();

    this.filterTextInput.textField().setText(null);
    this.documentsList.clear();
    for (final var document : documents)
      this.documentsList.add(new DocumentView(document, true, this.config));
    this.documentsList.sort(null);

    this.pendingUpdates = false;
    this.anyDocumentUpdated = false;
    this.updateButtons();
  }

  private void onAddDocument() {
    final Optional<Path> file = FileChoosers.showFileChooser(this.config, this.stage(), null);
    if (file.isPresent()) {
      final String name = file.get().getFileName().toString();
      if (this.isFileImported(name))
        Alerts.warning(
            this.config,
            "alert.document_already_imported.header",
            null,
            null,
            new FormatArg("file_name", name)
        );
      else {
        try {
          this.importFile(file.get());
        } catch (final IOException e) {
          Alerts.error(
              this.config,
              "alert.load_error.header",
              "alert.load_error.content",
              "alert.load_error.title",
              new FormatArg("trace", e.getMessage())
          );
        }
      }
    }
  }

  /**
   * Import the given files from the filesystem into the current tree and document list.
   *
   * @param files List of files to import.
   */
  private void importFiles(final @NotNull List<Path> files) {
    boolean someAlreadyImported = false;
    int errorsNb = 0;
    for (final Path file : files) {
      if (this.isFileImported(file.getFileName().toString())) {
        someAlreadyImported = true;
        continue;
      }
      try {
        this.importFile(file);
      } catch (final IOException e) {
        errorsNb++;
      }
    }
    if (errorsNb != 0)
      Alerts.error(
          this.config,
          "alert.load_errors.header",
          "alert.load_errors.content",
          "alert.load_errors.title",
          new FormatArg("nb", errorsNb)
      );
    if (someAlreadyImported)
      Alerts.warning(
          this.config,
          "alert.documents_already_imported.header",
          null,
          null
      );
  }

  /**
   * Import the given file from the filesystem into the current tree and document list.
   *
   * @param file The file to import.
   */
  private void importFile(final @NotNull Path file) throws IOException {
    final AttachedDocument document;
    final Optional<String> ext = FileUtils.splitExtension(file.getFileName().toString()).extension();
    if (ext.isPresent() && Picture.FILE_EXTENSIONS.contains(ext.get().toLowerCase()))
      document = new Picture(FileUtils.loadImage(file), file, null, null);
    else
      document = new AttachedDocument(file, null, null);
    this.addDocumentsToList(Set.of(document));
  }

  private void addDocumentsToList(final @NotNull Collection<AttachedDocument> documents) {
    documents.forEach(d -> {
      final DocumentView dv = new DocumentView(d, true, this.config);
      this.documentsList.add(dv);
      this.documentsListView.scrollTo(dv);
      this.documentsToAdd.add(d);
      this.documentsToDelete.remove(d);
    });
    if (!documents.isEmpty()) {
      this.documentsList.sort(null);
      this.pendingUpdates = true;
      this.updateButtons();
    }
  }

  /**
   * Check whether a file name is already present in the document list.
   *
   * @param name Name of the file.
   * @return True if a file with the given name is in the list, false otherwise.
   */
  private boolean isFileImported(String name) {
    return this.familyTree.getDocument(name).isPresent();
  }

  private void onDeleteDocuments() {
    final List<DocumentView> selection = this.getSelectedDocuments();
    if (selection.isEmpty()) return;
    if (!Alerts.confirmation(
        this.config,
        "alert.delete_documents.header",
        null,
        "alert.delete_documents.title"
    )) return;
    selection.forEach(dv -> {
      final AttachedDocument document = dv.document();
      this.documentsToDelete.add(document);
      this.documentsToAdd.remove(document);
      this.documentsList.remove(dv);
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
    return new ArrayList<>(this.documentsListView.getSelectionModel().getSelectedItems());
  }

  private void updateButtons() {
    final var selectionModel = this.documentsListView.getSelectionModel();
    final boolean noSelection = selectionModel.isEmpty();
    this.deleteDocumentButton.setDisable(noSelection);
    final var selectedItems = selectionModel.getSelectedItems();
    final boolean not1Selected = selectedItems.size() != 1;
    this.editDocumentDescButton.setDisable(not1Selected);
    this.applyButton.setDisable(!this.pendingUpdates);
  }

  private void updateTree() {
    this.documentsToDelete.forEach(document ->
        this.familyTree.removeDocument(document.fileName()));
    this.documentsToDelete.clear();

    this.documentsToAdd.forEach(p -> {
      if (this.familyTree.getDocument(p.fileName()).isEmpty())
        this.familyTree.addDocument(p);
    });
    this.documentsToAdd.clear();

    this.pendingUpdates = false;
    this.updateButtons();
  }

  public record Result(boolean targetUpdated, boolean anyDocumentUpdated) {
  }
}
