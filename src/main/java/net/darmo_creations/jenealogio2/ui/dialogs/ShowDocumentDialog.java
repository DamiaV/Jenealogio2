package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import net.darmo_creations.jenealogio2.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.io.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.themes.*;
import net.darmo_creations.jenealogio2.ui.components.*;
import net.darmo_creations.jenealogio2.ui.events.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * This dialog shows the metadata and annotations of a document.
 */
public class ShowDocumentDialog extends DocumentViewDialogBase<Void>
    implements PersonClickObservable, LifeEventClickObservable {
  private final TabPane tabPane = new TabPane();
  private final Tab fileMetadataTab = new Tab();
  private final Tab annotationsTab = new Tab();
  private final Label fileExtensionLabel = new Label();
  private final DateLabel dateTimeLabel;
  private final TextFlow descriptionPanel = new TextFlow();
  private final Map<AnnotationType, ListView<ObjectAnnotation>> annotationLists = new HashMap<>();

  private final List<PersonClickListener> personClickListeners = new LinkedList<>();
  private final List<LifeEventClickListener> eventClickListeners = new LinkedList<>();

  public ShowDocumentDialog(final @NotNull Config config) {
    super(
        config,
        "show_document",
        true,
        false,
        ButtonTypes.CLOSE
    );

    this.dateTimeLabel = new DateLabel("-", config);
    this.setupFileMetadataTab();
    this.setupAnnotationsTab();

    this.tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    this.tabPane.getTabs().addAll(this.fileMetadataTab, this.annotationsTab);

    this.setContent(this.tabPane);

    this.setResultConverter(b -> null);
  }

  private void setupFileMetadataTab() {
    final Language language = this.config.language();

    final HBox hBox = new HBox(
        5,
        this.fileExtensionLabel,
        new Label(language.translate("dialog.show_document.date")),
        this.dateTimeLabel
    );

    final ScrollPane descriptionScroll = new ScrollPane(this.descriptionPanel);
    VBox.setVgrow(descriptionScroll, Priority.ALWAYS);
    descriptionScroll.setPadding(new Insets(5));

    final VBox content = new VBox(
        5,
        hBox,
        new Label(language.translate("dialog.show_document.description")),
        descriptionScroll
    );
    content.setPadding(new Insets(5, 0, 0, 0));
    this.fileMetadataTab.setText(language.translate("dialog.show_document.tab.file_metadata"));
    this.fileMetadataTab.setContent(content);
  }

  private void setupAnnotationsTab() {
    final Language language = this.config.language();

    final VBox content = new VBox(5);

    for (final var annotationType : AnnotationType.values()) {
      final ListView<ObjectAnnotation> listView = new ListView<>();
      listView.setCellFactory(param -> new ObjectAnnotationListCell(this.config));
      listView.setOnMouseClicked(event -> {
        if (event.getClickCount() > 1)
          this.onAnnotationListDoubleClicked(listView);
      });
      this.annotationLists.put(annotationType, listView);
      VBox.setVgrow(listView, Priority.ALWAYS);
      content.getChildren().addAll(
          new Label(language.translate("dialog.show_document.annotations." + annotationType.name().toLowerCase())),
          listView
      );
    }

    content.setPadding(new Insets(5, 0, 0, 0));
    this.annotationsTab.setText(language.translate("dialog.show_document.tab.annotations"));
    this.annotationsTab.setContent(content);
  }

  private void onAnnotationListDoubleClicked(final @NotNull ListView<ObjectAnnotation> listView) {
    final var selectedItem = listView.getSelectionModel().getSelectedItem();
    if (selectedItem == null) return;
    final var object = selectedItem.object();
    if (object instanceof Person p)
      this.personClickListeners.forEach(
          l -> l.onClick(new PersonClickedEvent(p, PersonClickedEvent.Action.SET_AS_TARGET)));
    else if (object instanceof LifeEvent e)
      this.eventClickListeners.forEach(
          l -> l.onClick(new LifeEventClickedEvent(e)));
  }

  public void setDocument(final @NotNull AttachedDocument document) {
    this.setTitle(document.fileName());
    final Icon fileTypeIcon = Icon.forFile(document.fileName());
    final Image image;
    if (document instanceof Picture pic)
      image = pic.image().orElse(this.config.theme().getIconImage(Icon.NO_IMAGE, Icon.Size.BIG));
    else image = this.config.theme().getIconImage(fileTypeIcon, Icon.Size.BIG);
    this.imageView.setImage(image);
    this.fileExtensionLabel.setGraphic(this.config.theme().getIcon(fileTypeIcon, Icon.Size.SMALL));
    this.dateTimeLabel.setDateTime(document.date().orElse(null));
    this.descriptionPanel.getChildren().clear();
    document.description().ifPresent(
        s -> this.descriptionPanel.getChildren().addAll(StringUtils.parseText(s, App::openURL)));

    for (final var annotationType : AnnotationType.values()) {
      final var listView = this.annotationLists.get(annotationType);
      listView.getItems().clear();
      document.annotatedObjects(annotationType).entrySet()
          .stream()
          .map(e -> new ObjectAnnotation(e.getKey(), e.getValue().orElse(null)))
          .forEach(listView.getItems()::add);
      listView.getItems().sort(null);
    }

    if (!this.isShowing())
      this.tabPane.getSelectionModel().select(this.fileMetadataTab);
  }

  @Override
  public List<PersonClickListener> personClickListeners() {
    return this.personClickListeners;
  }

  @Override
  @Contract("-> fail")
  public List<NewParentClickListener> newParentClickListeners() {
    throw new UnsupportedOperationException("newParentClickListeners");
  }

  @Override
  public List<LifeEventClickListener> lifeEventClickListeners() {
    return this.eventClickListeners;
  }
}
