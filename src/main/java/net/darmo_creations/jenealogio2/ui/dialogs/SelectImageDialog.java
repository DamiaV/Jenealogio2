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
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.themes.*;
import net.darmo_creations.jenealogio2.ui.components.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * A dialog used select images from the given {@link FamilyTree} object.
 * Offers a button to import images directly from the filesystem.
 */
public class SelectImageDialog extends DialogBase<Collection<Picture>> {
  private final TextField filterTextInput = new TextField();
  private final ListView<PictureView> imagesList = new ListView<>();
  private final ObservableList<PictureView> picturesList = FXCollections.observableArrayList();

  private FamilyTree tree;

  /**
   * Create a new dialog to select images.
   *
   * @param config The app’s config.
   */
  public SelectImageDialog(final @NotNull Config config) {
    super(config, "select_images", true, ButtonTypes.OK, ButtonTypes.CANCEL);

    Language language = config.language();
    Theme theme = config.theme();

    Label label = new Label(
        language.translate("dialog.select_images.description"),
        theme.getIcon(Icon.INFO, Icon.Size.SMALL)
    );
    label.setWrapText(true);
    label.setPrefHeight(70);
    label.setMinHeight(70);
    label.setAlignment(Pos.TOP_LEFT);

    Button addImageButton = new Button(
        language.translate("dialog.select_images.open_file"),
        theme.getIcon(Icon.OPEN_IMAGE_FILE, Icon.Size.SMALL)
    );
    addImageButton.setOnAction(e -> this.onAddImage());
    HBox hBox = new HBox(addImageButton);
    hBox.setAlignment(Pos.CENTER);

    HBox.setHgrow(this.filterTextInput, Priority.ALWAYS);
    this.filterTextInput.setPromptText(language.translate("dialog.select_images.filter"));
    FilteredList<PictureView> filteredList = new FilteredList<>(this.picturesList, data -> true);
    this.imagesList.setItems(filteredList);
    this.filterTextInput.textProperty().addListener(((observable, oldValue, newValue) ->
        filteredList.setPredicate(pictureView -> {
          if (newValue == null || newValue.isEmpty()) {
            return true;
          }
          String filter = newValue.toLowerCase();
          Picture picture = pictureView.picture();
          return picture.fileName().toLowerCase().contains(filter)
                 || picture.description().map(d -> d.toLowerCase().contains(filter)).orElse(false);
        })
    ));

    this.imagesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    this.imagesList.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> this.updateButtons());
    this.imagesList.setOnMouseClicked(this::onListClicked);
    VBox.setVgrow(this.imagesList, Priority.ALWAYS);

    VBox content = new VBox(
        5,
        label,
        hBox,
        new HBox(5, new Label(language.translate("dialog.select_images.images_list")), this.filterTextInput),
        this.imagesList
    );
    content.setPrefWidth(400);
    content.setPrefHeight(400);
    this.getDialogPane().setContent(content);

    Stage stage = this.stage();
    stage.setMinWidth(400);
    stage.setMinHeight(400);

    // Files drag-and-drop
    Scene scene = stage.getScene();
    scene.setOnDragOver(event -> {
      if (event.getGestureSource() == null // From another application
          && this.isDragAndDropValid(event.getDragboard())) {
        event.acceptTransferModes(TransferMode.COPY);
      }
      event.consume();
    });
    scene.setOnDragDropped(event -> {
      Dragboard db = event.getDragboard();
      boolean success = this.isDragAndDropValid(db);
      if (success) {
        this.importFiles(db.getFiles().stream().map(File::toPath).toList());
      }
      event.setDropCompleted(success);
      event.consume();
    });

    this.setResultConverter(buttonType -> {
      if (!buttonType.getButtonData().isCancelButton()) {
        return this.imagesList.getSelectionModel().getSelectedItems()
            .stream().map(PictureView::picture).toList();
      }
      return List.of();
    });
  }

  private boolean isDragAndDropValid(final @NotNull Dragboard dragboard) {
    List<File> files = dragboard.getFiles();
    return dragboard.hasFiles()
           && files.stream().allMatch(f -> Arrays.stream(Picture.FILE_EXTENSIONS).anyMatch(e -> f.getName().endsWith(e)));
  }

  /**
   * Update the list of images with the ones from the given tree, ignoring any that appear in the exclusion list.
   *
   * @param tree          Tree to pull images from.
   * @param exclusionList List of pictures to NOT add to the list view.
   */
  public void updateImageList(@NotNull FamilyTree tree, final @NotNull Collection<Picture> exclusionList) {
    this.tree = Objects.requireNonNull(tree);
    this.filterTextInput.setText(null);
    this.picturesList.clear();
    tree.pictures().stream()
        .filter(p -> !exclusionList.contains(p))
        .forEach(p -> this.picturesList.add(new PictureView(p, true, this.config)));
    this.picturesList.sort(null);
  }

  private void onAddImage() {
    Optional<Path> file = FileChoosers.showImageFileChooser(this.config, this.stage(), null);
    if (file.isPresent()) {
      String name = file.get().getFileName().toString();
      if (this.isFileImported(name)) {
        Alerts.warning(
            this.config,
            "alert.image_already_imported.header",
            null,
            null,
            new FormatArg("file_name", name)
        );
      } else {
        try {
          this.importFile(file.get());
        } catch (IOException e) {
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
   * Check whether a file name is already present in the image list.
   *
   * @param name Name of the file.
   * @return True if a file with the given name is in the list, false otherwise.
   */
  private boolean isFileImported(String name) {
    return this.tree.getPicture(name).isPresent();
  }

  /**
   * Import the given files from the filesystem into the current tree and image list.
   *
   * @param files List of files to import.
   */
  private void importFiles(final @NotNull List<Path> files) {
    boolean someAlreadyImported = false;
    int errorsNb = 0;
    for (Path file : files) {
      if (this.isFileImported(file.getFileName().toString())) {
        someAlreadyImported = true;
        continue;
      }
      try {
        this.importFile(file);
      } catch (IOException e) {
        errorsNb++;
      }
    }
    if (errorsNb != 0) {
      Alerts.error(
          this.config,
          "alert.load_errors.header",
          "alert.load_errors.content",
          "alert.load_errors.title",
          new FormatArg("nb", errorsNb)
      );
    }
    if (someAlreadyImported) {
      Alerts.warning(this.config, "alert.images_already_imported.header", null, null);
    }
  }

  /**
   * Import the given file from the filesystem into the current tree and image list.
   *
   * @param file The file to import.
   */
  private void importFile(final @NotNull Path file) throws IOException {
    Picture picture;
    try (var in = new FileInputStream(file.toFile())) {
      picture = new Picture(new Image(in), file, null, null);
    }
    PictureView pv = new PictureView(picture, true, this.config);
    this.picturesList.add(pv);
    this.picturesList.sort(null);
    this.imagesList.scrollTo(pv);
  }

  /**
   * Enable double-click on an image to select it.
   */
  private void onListClicked(final @NotNull MouseEvent event) {
    if (event.getClickCount() > 1) {
      ((Button) this.getDialogPane().lookupButton(ButtonTypes.OK)).fire();
    }
  }

  private void updateButtons() {
    boolean noSelection = this.imagesList.getSelectionModel().getSelectedItems().isEmpty();
    this.getDialogPane().lookupButton(ButtonTypes.OK).setDisable(noSelection);
  }
}
