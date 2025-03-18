package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.embed.swing.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.ui.components.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import javax.imageio.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * This dialogs is used to export the currently shown trees as PNG images.
 */
public class ExportTreeAsImageDialog extends DialogBase<Void> {
  private final ToggleGroup group = new ToggleGroup();

  private final TreeImageProvider geneticTreeImageProvider;
  private final TreeImageProvider memberConstellationImageProvider;

  /**
   * Create a new image exporting dialog.
   *
   * @param config                           The current app’s config.
   * @param geneticTreeImageProvider         An object that will provide the image for the genetic family tree.
   * @param memberConstellationImageProvider An object that will provide the image for the member full view.
   */
  public ExportTreeAsImageDialog(
      final @NotNull Config config,
      @NotNull TreeImageProvider geneticTreeImageProvider,
      @NotNull TreeImageProvider memberConstellationImageProvider
  ) {
    super(
        config,
        "export_tree_as_image",
        false,
        ButtonTypes.OK,
        ButtonTypes.CANCEL
    );
    this.geneticTreeImageProvider = Objects.requireNonNull(geneticTreeImageProvider);
    this.memberConstellationImageProvider = Objects.requireNonNull(memberConstellationImageProvider);

    final Language language = config.language();

    final VBox content = new VBox(5);
    content.setPrefWidth(400);

    content.getChildren().add(new Label(language.translate("dialog.export_tree_as_image.selection")));

    for (final var exportSelection : ExportSelection.values()) {
      final RadioButton radioButton = new RadioButton(language.translate(
          "dialog.export_tree_as_image.selection." + exportSelection.name().toLowerCase()));
      radioButton.setToggleGroup(this.group);
      radioButton.setUserData(exportSelection);
      content.getChildren().add(radioButton);
    }

    this.getDialogPane().setContent(content);

    this.setOnShown(event -> this.group.selectToggle(this.group.getToggles().get(0)));

    this.setResultConverter(b -> {
      if (!b.getButtonData().isCancelButton())
        this.exportToFile();
      return null;
    });
  }

  private void exportToFile() {
    final var exportSelection = (ExportSelection) ((RadioButton) this.group.getSelectedToggle()).getUserData();
    if (exportSelection.exportGeneticTree()) {
      final boolean exported = this.exportImage(
          "save_genetic_tree_image",
          this.geneticTreeImageProvider
      );
      // User wants to abort, no need to show them the next file saver
      if (exportSelection == ExportSelection.BOTH && !exported) return;
    }
    if (exportSelection.exportMemberConstellation())
      this.exportImage(
          "save_member_constellation_image",
          this.memberConstellationImageProvider
      );
  }

  private boolean exportImage(@NotNull String titleKey, @NotNull TreeImageProvider imageProvider) {
    final Optional<Person> person = imageProvider.targettedPerson();
    if (person.isEmpty()) return false;

    final String personName = person.get()
        .toString()
        .replace(" ", "_");
    final Optional<Path> file = FileChoosers.showFileSaver(
        this.config,
        this.stage(),
        titleKey,
        "png_image",
        this.config.language().translate(
            "dialog.%s.default_name".formatted(titleKey),
            new FormatArg("person", personName)
        ),
        ".png"
    );
    if (file.isEmpty()) return false;

    try {
      ImageIO.write(
          SwingFXUtils.fromFXImage(imageProvider.exportAsImage(), null),
          "png",
          file.get().toFile()
      );
      return true;
    } catch (final IOException e) {
      Alerts.error(
          this.config,
          "dialog.save_error.header",
          null,
          null,
          new FormatArg("trace", e.getMessage())
      );
      return false;
    }
  }

  private enum ExportSelection {
    BOTH(true, true),
    GENETIC_TREE(true, false),
    MEMBER_CONSTELLATION(false, true),
    ;

    private final boolean exportGeneticTree;
    private final boolean exportMemberConstellation;

    ExportSelection(boolean exportGeneticTree, boolean exportMemberConstellation) {
      this.exportGeneticTree = exportGeneticTree;
      this.exportMemberConstellation = exportMemberConstellation;
    }

    public boolean exportGeneticTree() {
      return this.exportGeneticTree;
    }

    public boolean exportMemberConstellation() {
      return this.exportMemberConstellation;
    }
  }
}
