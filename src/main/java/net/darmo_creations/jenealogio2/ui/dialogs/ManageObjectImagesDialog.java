package net.darmo_creations.jenealogio2.ui.dialogs;

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
 * This dialog manages the images of {@link GenealogyObject}s.
 */
public class ManageObjectImagesDialog extends DialogBase<ManageObjectImagesDialog.Result> {
  private final SelectImageDialog selectImageDialog;
  private final EditImageDialog editImageDialog;

  private final Label buttonDescriptionLabel = new Label();

  private final ImageView mainImageView = new ImageView();
  private final Button removeMainImageButton = new Button();
  private final Button setAsMainImageButton = new Button();
  private final Button removeImageButton = new Button();
  private final Button editImageDescButton = new Button();
  private final Button deleteImageButton = new Button();
  private final ListView<PictureView> imagesList = new ListView<>();

  /**
   * The object’s current main picture.
   */
  private Picture mainPicture;
  /**
   * Set of pictures to remove from the current tree.
   */
  private final Set<Picture> picturesToDelete = new HashSet<>();
  /**
   * Set of pictures to remove from the current object.
   */
  private final Set<Picture> picturesToRemove = new HashSet<>();
  /**
   * Set of pictures to add to the current object.
   */
  private final Set<Picture> picturesToAdd = new HashSet<>();

  private boolean anyImageUpdated = false;

  /**
   * The object to manage the images of.
   */
  private GenealogyObject<?> genealogyObject;
  /**
   * The family tree the object belongs to.
   */
  private FamilyTree familyTree;

  /**
   * Create a new dialog to manage images.
   *
   * @param config The app’s config.
   */
  public ManageObjectImagesDialog(final @NotNull Config config) {
    super(config, "manage_object_images", true, ButtonTypes.OK, ButtonTypes.CANCEL);

    Language language = config.language();
    Theme theme = config.theme();

    this.selectImageDialog = new SelectImageDialog(config);
    this.editImageDialog = new EditImageDialog(config);

    VBox content = new VBox(5);
    content.getChildren().add(new HBox(5,
        new Label(language.translate("dialog.manage_object_images.main_image")),
        this.removeMainImageButton
    ));
    this.mainImageView.setPreserveRatio(true);
    this.mainImageView.setFitWidth(100);
    this.mainImageView.setFitHeight(100);
    content.getChildren().add(this.mainImageView);

    Pane spacer = new Pane();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    this.removeMainImageButton.setText(language.translate("dialog.manage_object_images.remove_main_image"));
    this.removeMainImageButton.setGraphic(theme.getIcon(Icon.REMOVE_MAIN_IMAGE, Icon.Size.SMALL));
    this.removeMainImageButton.setOnAction(e -> this.onRemoveMainImage());
    this.removeMainImageButton.hoverProperty().addListener((observable, oldValue, newValue)
        -> this.showButtonDescription(newValue, "dialog.manage_object_images.remove_main_image"));

    Button addImageButton = new Button(
        language.translate("dialog.manage_object_images.add_image"),
        theme.getIcon(Icon.ADD_IMAGE, Icon.Size.SMALL)
    );
    addImageButton.setOnAction(e -> this.onAddImage());
    addImageButton.hoverProperty().addListener((observable, oldValue, newValue)
        -> this.showButtonDescription(newValue, "dialog.manage_object_images.add_image"));

    this.setAsMainImageButton.setText(language.translate("dialog.manage_object_images.set_as_main_image"));
    this.setAsMainImageButton.setGraphic(theme.getIcon(Icon.SET_AS_MAIN_IMAGE, Icon.Size.SMALL));
    this.setAsMainImageButton.setOnAction(e -> this.onSetAsMainImage());
    this.setAsMainImageButton.hoverProperty().addListener((observable, oldValue, newValue)
        -> this.showButtonDescription(newValue, "dialog.manage_object_images.set_as_main_image"));

    this.removeImageButton.setText(language.translate("dialog.manage_object_images.remove_image"));
    this.removeImageButton.setGraphic(theme.getIcon(Icon.REMOVE_IMAGE, Icon.Size.SMALL));
    this.removeImageButton.setOnAction(e -> this.onRemoveImages());
    this.removeImageButton.hoverProperty().addListener((observable, oldValue, newValue)
        -> this.showButtonDescription(newValue, "dialog.manage_object_images.remove_image"));

    this.editImageDescButton.setText(language.translate("dialog.manage_object_images.edit_image_desc"));
    this.editImageDescButton.setGraphic(theme.getIcon(Icon.EDIT_IMAGE_DESC, Icon.Size.SMALL));
    this.editImageDescButton.setOnAction(e -> this.onEditImageDesc());
    this.editImageDescButton.hoverProperty().addListener((observable, oldValue, newValue)
        -> this.showButtonDescription(newValue, "dialog.manage_object_images.edit_image_desc"));

    this.deleteImageButton.setText(language.translate("dialog.manage_object_images.delete_image"));
    this.deleteImageButton.setGraphic(theme.getIcon(Icon.DELETE_IMAGE, Icon.Size.SMALL));
    this.deleteImageButton.setOnAction(e -> this.onDeleteImages());
    this.deleteImageButton.hoverProperty().addListener((observable, oldValue, newValue)
        -> this.showButtonDescription(newValue, "dialog.manage_object_images.delete_image"));

    HBox title = new HBox(
        5,
        new Label(language.translate("dialog.manage_object_images.list")),
        spacer,
        this.setAsMainImageButton,
        addImageButton,
        this.removeImageButton,
        this.editImageDescButton,
        this.deleteImageButton
    );
    content.getChildren().add(title);

    this.imagesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    this.imagesList.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> this.updateButtons());
    this.imagesList.setOnMouseClicked(this::onListClicked);
    VBox.setVgrow(this.imagesList, Priority.ALWAYS);
    content.getChildren().add(this.imagesList);

    this.buttonDescriptionLabel.getStyleClass().add("help-text");
    content.getChildren().add(this.buttonDescriptionLabel);

    content.setPrefWidth(800);
    content.setPrefHeight(600);
    this.getDialogPane().setContent(content);

    Stage stage = this.stage();
    stage.setMinWidth(400);
    stage.setMinHeight(400);
    this.setIcon(config.theme().getAppIcon());

    this.setResultConverter(buttonType -> {
      boolean updated = !buttonType.getButtonData().isCancelButton();
      if (updated)
        this.updateObject();
      return new Result(updated, this.anyImageUpdated);
    });
  }

  public void setObject(@NotNull GenealogyObject<?> object, @NotNull FamilyTree familyTree) {
    this.genealogyObject = object;
    this.familyTree = familyTree;
    this.picturesToDelete.clear();
    this.picturesToRemove.clear();
    this.picturesToAdd.clear();
    Language language = this.config.language();
    this.setTitle(language.translate("dialog.manage_object_images.title",
        new FormatArg("name", object.name(language))));
    Optional<Picture> image = this.genealogyObject.mainPicture();
    this.mainPicture = image.orElse(null);
    Image defaultImage = object instanceof Person ? PersonWidget.DEFAULT_IMAGE : PersonDetailsView.DEFAULT_EVENT_IMAGE;
    this.mainImageView.setImage(image.map(Picture::image).orElse(defaultImage));
    this.removeMainImageButton.setDisable(image.isEmpty());
    this.imagesList.getItems().clear();
    for (Picture picture : this.genealogyObject.pictures()) {
      this.imagesList.getItems().add(new PictureView(picture, true, this.config));
    }
    this.imagesList.getItems().sort(null);
    this.anyImageUpdated = false;
    this.updateButtons();
  }

  private void removeMainImage() {
    this.mainPicture = null;
    this.mainImageView.setImage(PersonWidget.DEFAULT_IMAGE);
  }

  private void onRemoveMainImage() {
    if (this.mainPicture == null) {
      return;
    }
    this.removeMainImage();
    this.updateButtons();
  }

  private void onSetAsMainImage() {
    List<PictureView> selection = this.getSelectedImages();
    if (selection.size() != 1) {
      return;
    }
    PictureView pv = selection.get(0);
    this.mainPicture = pv.picture();
    this.mainImageView.setImage(this.mainPicture.image());
    this.updateButtons();
  }

  private void onAddImage() {
    var exclusionList = new LinkedList<>(this.imagesList.getItems().stream().map(PictureView::picture).toList());
    exclusionList.addAll(this.picturesToDelete);
    this.selectImageDialog.updateImageList(this.familyTree, exclusionList);
    this.selectImageDialog.showAndWait().ifPresent(pictures -> {
      pictures.forEach(p -> {
        PictureView pv = new PictureView(p, true, this.config);
        this.imagesList.getItems().add(pv);
        this.imagesList.scrollTo(pv);
        this.picturesToAdd.add(p);
        this.picturesToRemove.remove(p);
        this.picturesToDelete.remove(p);
      });
      this.imagesList.getItems().sort(null);
      this.updateButtons();
    });
  }

  private void onRemoveImages() {
    List<PictureView> selection = this.getSelectedImages();
    if (selection.isEmpty()) {
      return;
    }
    selection.forEach(pv -> {
      Picture picture = pv.picture();
      if (picture.equals(this.mainPicture)) {
        this.removeMainImage();
      }
      this.picturesToRemove.add(picture);
      this.picturesToAdd.remove(picture);
      this.imagesList.getItems().remove(pv);
    });
    this.updateButtons();
  }

  private void onDeleteImages() {
    List<PictureView> selection = this.getSelectedImages();
    if (selection.isEmpty()) {
      return;
    }
    if (!Alerts.confirmation(
        this.config, "alert.delete_images.header", null, "alert.delete_images.title")) {
      return;
    }
    selection.forEach(pv -> {
      Picture picture = pv.picture();
      if (picture.equals(this.mainPicture)) {
        this.removeMainImage();
      }
      this.picturesToDelete.add(picture);
      this.picturesToAdd.remove(picture);
      this.imagesList.getItems().remove(pv);
    });
    this.updateButtons();
  }

  private void onEditImageDesc() {
    List<PictureView> selection = this.getSelectedImages();
    if (selection.size() == 1) {
      this.openImageEditDialog(selection.get(0));
    }
  }

  private void onListClicked(final @NotNull MouseEvent event) {
    if (event.getClickCount() > 1) {
      this.onEditImageDesc();
    }
  }

  private void openImageEditDialog(@NotNull PictureView pictureView) {
    this.editImageDialog.setPicture(pictureView.picture(), this.familyTree);
    this.editImageDialog.showAndWait().ifPresent(b -> {
      pictureView.refresh();
      this.anyImageUpdated = true;
    });
    this.updateButtons();
  }

  private void showButtonDescription(boolean show, String i18nKey) {
    this.buttonDescriptionLabel.setText(show ? this.config.language().translate(i18nKey + ".tooltip") : null);
  }

  private List<PictureView> getSelectedImages() {
    return new ArrayList<>(this.imagesList.getSelectionModel().getSelectedItems());
  }

  private void updateButtons() {
    this.removeMainImageButton.setDisable(this.mainPicture == null);
    var selectionModel = this.imagesList.getSelectionModel();
    boolean noSelection = selectionModel.isEmpty();
    this.removeImageButton.setDisable(noSelection);
    this.deleteImageButton.setDisable(noSelection);
    var selectedItems = selectionModel.getSelectedItems();
    boolean not1Selected = selectedItems.size() != 1;
    this.setAsMainImageButton.setDisable(
        not1Selected ||
        selectedItems.get(0) != null // Selection list sometimes contains null
        && selectedItems.get(0).picture().equals(this.mainPicture)
    );
    this.editImageDescButton.setDisable(not1Selected);
  }

  private void updateObject() {
    this.picturesToRemove.forEach(picture -> this.familyTree.removePictureFromObject(picture.name(), this.genealogyObject));
    this.picturesToDelete.forEach(picture -> this.familyTree.removePicture(picture.name()));
    this.picturesToAdd.forEach(p -> {
      if (this.familyTree.getPicture(p.name()).isEmpty()) {
        this.familyTree.addPicture(p);
      }
      this.familyTree.addPictureToObject(p.name(), this.genealogyObject);
    });
    if (this.mainPicture != null) {
      this.familyTree.setMainPictureOfObject(this.mainPicture.name(), this.genealogyObject);
    } else {
      this.familyTree.setMainPictureOfObject(null, this.genealogyObject);
    }
  }

  public record Result(boolean personUpdated, boolean anyImageUpdated) {
  }
}
