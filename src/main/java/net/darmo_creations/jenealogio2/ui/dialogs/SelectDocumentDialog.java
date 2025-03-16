package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.collections.*;
import javafx.collections.transformation.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.ui.components.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * A dialog used select documents from the given {@link FamilyTree} object.
 */
public class SelectDocumentDialog extends DialogBase<Collection<AttachedDocument>> {
  private final ErasableTextField filterTextInput;
  private final ListView<DocumentView> documentsListView = new ListView<>();
  private final ObservableList<DocumentView> documentsList = FXCollections.observableArrayList();

  /**
   * Create a new dialog to select documents.
   *
   * @param config        The app’s config.
   * @param selectionMode The selection mode for the documents list.
   */
  public SelectDocumentDialog(final @NotNull Config config, @NotNull SelectionMode selectionMode) {
    super(config, "select_documents", true, ButtonTypes.OK, ButtonTypes.CANCEL);

    final Language language = config.language();

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

    this.documentsListView.getSelectionModel().setSelectionMode(Objects.requireNonNull(selectionMode));
    this.documentsListView.getSelectionModel().selectedItemProperty().addListener(
        (observable, oldValue, newValue) -> this.updateButtons());
    this.documentsListView.setOnMouseClicked(this::onListClicked);
    VBox.setVgrow(this.documentsListView, Priority.ALWAYS);

    final HBox filterBox = new HBox(
        5,
        new Label(language.translate("dialog.select_documents.documents_list")),
        this.filterTextInput
    );
    filterBox.setAlignment(Pos.CENTER_LEFT);
    final VBox content = new VBox(
        5,
        filterBox,
        this.documentsListView
    );
    content.setPrefWidth(400);
    this.getDialogPane().setContent(content);

    this.getDialogPane().addEventHandler(KeyEvent.KEY_PRESSED, event -> {
      if (!this.filterTextInput.textField().isFocused())
        this.filterTextInput.textField().requestFocus();
    });

    final Stage stage = this.stage();
    stage.setMinWidth(600);
    stage.setMinHeight(700);

    this.setResultConverter(buttonType -> {
      if (!buttonType.getButtonData().isCancelButton())
        return this.documentsListView
            .getSelectionModel()
            .getSelectedItems()
            .stream()
            .map(DocumentView::document)
            .toList();
      return List.of();
    });
  }

  /**
   * Update the list of documents with the ones from the given tree, ignoring any that appear in the exclusion list.
   *
   * @param tree          Tree to pull documents from.
   * @param exclusionList List of documents to NOT add to the list view.
   */
  public void updateDocumentsList(
      @NotNull FamilyTree tree,
      final @NotNull Collection<AttachedDocument> exclusionList
  ) {
    this.filterTextInput.textField().setText(null);
    this.documentsList.clear();
    tree.documents().stream()
        .filter(p -> !exclusionList.contains(p))
        .forEach(p -> this.documentsList.add(new DocumentView(p, true, this.config)));
    this.documentsList.sort(null);
  }

  /**
   * Enable double-click on an document to select it.
   */
  private void onListClicked(final @NotNull MouseEvent event) {
    if (event.getClickCount() > 1)
      ((Button) this.getDialogPane().lookupButton(ButtonTypes.OK)).fire();
  }

  private void updateButtons() {
    final boolean noSelection = this.documentsListView.getSelectionModel().getSelectedItems().isEmpty();
    this.getDialogPane().lookupButton(ButtonTypes.OK).setDisable(noSelection);
  }
}
