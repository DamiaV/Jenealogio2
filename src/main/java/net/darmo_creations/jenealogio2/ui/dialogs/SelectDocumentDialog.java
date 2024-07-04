package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.collections.*;
import javafx.collections.transformation.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.io.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.themes.*;
import net.darmo_creations.jenealogio2.ui.components.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * A dialog used select documents from the given {@link FamilyTree} object.
 * Offers a button to import documents directly from the filesystem.
 */
public class SelectDocumentDialog extends DialogBase<Collection<AttachedDocument>> {
  private final ErasableTextField filterTextInput;
  private final ListView<DocumentView> documentsList = new ListView<>();
  private final ObservableList<DocumentView> documentsList_ = FXCollections.observableArrayList();

  private FamilyTree tree;

  /**
   * Create a new dialog to select documents.
   *
   * @param config The app’s config.
   */
  public SelectDocumentDialog(final @NotNull Config config) {
    super(config, "select_documents", true, ButtonTypes.OK, ButtonTypes.CANCEL);

    final Language language = config.language();
    final Theme theme = config.theme();

    final Label label = new Label(
        language.translate("dialog.select_documents.description"),
        theme.getIcon(Icon.INFO, Icon.Size.SMALL)
    );
    label.setWrapText(true);

    final Button addDocumentButton = new Button(
        language.translate("dialog.select_documents.open_file"),
        theme.getIcon(Icon.IMPORT_FILE, Icon.Size.SMALL)
    );
    addDocumentButton.setOnAction(e -> this.onAddDocument());
    final HBox hBox = new HBox(addDocumentButton);
    hBox.setAlignment(Pos.CENTER);

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

    this.documentsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    this.documentsList.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> this.updateButtons());
    this.documentsList.setOnMouseClicked(this::onListClicked);
    VBox.setVgrow(this.documentsList, Priority.ALWAYS);

    final HBox filterBox = new HBox(
        5,
        new Label(language.translate("dialog.select_documents.documents_list")),
        this.filterTextInput
    );
    filterBox.setAlignment(Pos.CENTER_LEFT);
    final VBox content = new VBox(
        5,
        label,
        hBox,
        filterBox,
        this.documentsList
    );
    content.setPrefWidth(400);
    this.getDialogPane().setContent(content);

    final Stage stage = this.stage();
    stage.setMinWidth(600);
    stage.setMinHeight(700);

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
      if (!buttonType.getButtonData().isCancelButton())
        return this.documentsList
            .getSelectionModel()
            .getSelectedItems()
            .stream()
            .map(DocumentView::document)
            .toList();
      return List.of();
    });
  }

  private boolean isDragAndDropValid(final @NotNull Dragboard dragboard) {
    return dragboard.hasFiles(); // Accept all file types
  }

  /**
   * Update the list of documents with the ones from the given tree, ignoring any that appear in the exclusion list.
   *
   * @param tree          Tree to pull documents from.
   * @param exclusionList List of documents to NOT add to the list view.
   */
  public void updateDocumentsList(@NotNull FamilyTree tree, final @NotNull Collection<AttachedDocument> exclusionList) {
    this.tree = Objects.requireNonNull(tree);
    this.filterTextInput.textField().setText(null);
    this.documentsList_.clear();
    tree.documents().stream()
        .filter(p -> !exclusionList.contains(p))
        .forEach(p -> this.documentsList_.add(new DocumentView(p, true, this.config)));
    this.documentsList_.sort(null);
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
   * Check whether a file name is already present in the document list.
   *
   * @param name Name of the file.
   * @return True if a file with the given name is in the list, false otherwise.
   */
  private boolean isFileImported(String name) {
    return this.tree.getDocument(name).isPresent();
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
      Alerts.warning(this.config, "alert.documents_already_imported.header", null, null);
  }

  /**
   * Import the given file from the filesystem into the current tree and document list.
   *
   * @param file The file to import.
   */
  private void importFile(final @NotNull Path file) throws IOException {
    final AttachedDocument document;
    final Optional<String> ext = FileUtils.splitExtension(file.getFileName().toString()).extension();
    if (ext.isPresent() && Picture.FILE_EXTENSIONS.contains(ext.get()))
      try (final var in = new FileInputStream(file.toFile())) {
        document = new Picture(new Image(in), file, null, null);
      }
    else
      document = new AttachedDocument(file, null, null);
    final DocumentView dv = new DocumentView(document, true, this.config);
    this.documentsList_.add(dv);
    this.documentsList_.sort(null);
    this.documentsList.getSelectionModel().select(dv);
    this.documentsList.scrollTo(dv);
    this.documentsList.requestFocus();
  }

  /**
   * Enable double-click on an document to select it.
   */
  private void onListClicked(final @NotNull MouseEvent event) {
    if (event.getClickCount() > 1)
      ((Button) this.getDialogPane().lookupButton(ButtonTypes.OK)).fire();
  }

  private void updateButtons() {
    final boolean noSelection = this.documentsList.getSelectionModel().getSelectedItems().isEmpty();
    this.getDialogPane().lookupButton(ButtonTypes.OK).setDisable(noSelection);
  }
}
