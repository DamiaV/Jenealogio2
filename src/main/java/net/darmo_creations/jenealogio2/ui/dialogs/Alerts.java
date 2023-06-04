package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import net.darmo_creations.jenealogio2.App;
import net.darmo_creations.jenealogio2.config.Language;
import net.darmo_creations.jenealogio2.utils.FormatArg;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

public final class Alerts {
  public static void info(
      @NotNull String headerKey, String contentKey, String titleKey, final FormatArg @NotNull ... contentArgs) {
    alert(Alert.AlertType.INFORMATION, headerKey, contentKey, titleKey, contentArgs);
  }

  public static void warning(
      @NotNull String headerKey, String contentKey, String titleKey, final FormatArg @NotNull ... contentArgs) {
    alert(Alert.AlertType.WARNING, headerKey, contentKey, titleKey, contentArgs);
  }

  public static void error(
      @NotNull String headerKey, String contentKey, String titleKey, final FormatArg @NotNull ... contentArgs) {
    alert(Alert.AlertType.ERROR, headerKey, contentKey, titleKey, contentArgs);
  }

  public static boolean confirmation(
      @NotNull String headerKey, String contentKey, String titleKey, final FormatArg @NotNull ... contentArgs) {
    Optional<ButtonType> result = alert(Alert.AlertType.CONFIRMATION, headerKey, contentKey, titleKey, contentArgs);
    return result.isPresent() && !result.get().getButtonData().isCancelButton();
  }

  public static <T> Optional<T> chooser(
      @NotNull String headerKey, String titleKey, final Collection<T> choices, final FormatArg @NotNull ... contentArgs) {
    if (choices.isEmpty()) {
      throw new IllegalArgumentException("empty choices");
    }
    Alert alert = getAlert(Alert.AlertType.CONFIRMATION, headerKey, titleKey, contentArgs);
    HBox hBox = new HBox();
    ComboBox<T> choicesCombo = new ComboBox<>();
    choicesCombo.getItems().addAll(choices);
    hBox.getChildren().add(choicesCombo);
    hBox.setAlignment(Pos.CENTER);
    alert.getDialogPane().setContent(hBox);
    Optional<ButtonType> buttonType = alert.showAndWait();
    if (buttonType.isPresent() && !buttonType.get().getButtonData().isCancelButton()) {
      return Optional.of(choicesCombo.getSelectionModel().getSelectedItem());
    }
    return Optional.empty();
  }

  private static Optional<ButtonType> alert(
      @NotNull Alert.AlertType type,
      @NotNull String headerKey,
      String contentKey,
      String titleKey,
      final @NotNull FormatArg... contentArgs
  ) {
    Alert alert = getAlert(type, headerKey, titleKey, contentArgs);
    if (contentKey != null) {
      alert.setContentText(App.config().language().translate(contentKey, contentArgs));
    }
    return alert.showAndWait();
  }

  private static Alert getAlert(
      @NotNull Alert.AlertType type, @NotNull String headerKey, String titleKey, final FormatArg @NotNull ... contentArgs) {
    if (type == Alert.AlertType.NONE) {
      throw new IllegalArgumentException(type.name());
    }
    Alert alert = new Alert(type);
    App.config().theme().getStyleSheet()
        .ifPresent(url -> alert.getDialogPane().getStylesheets().add(url.toExternalForm()));
    if (titleKey == null) {
      titleKey = "alert.%s.title".formatted(type.name().toLowerCase());
    }
    Language language = App.config().language();
    alert.setTitle(language.translate(titleKey));
    alert.setHeaderText(language.translate(headerKey, contentArgs));
    return alert;
  }

  private Alerts() {
  }
}
