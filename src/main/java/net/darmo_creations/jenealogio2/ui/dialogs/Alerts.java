package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import net.darmo_creations.jenealogio2.App;
import net.darmo_creations.jenealogio2.config.Language;
import net.darmo_creations.jenealogio2.utils.FormatArg;
import org.jetbrains.annotations.NotNull;

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

  public static Optional<ButtonType> confirmation(
      @NotNull String headerKey, String contentKey, String titleKey, final FormatArg @NotNull ... contentArgs) {
    return alert(Alert.AlertType.CONFIRMATION, headerKey, contentKey, titleKey, contentArgs);
  }

  private static Optional<ButtonType> alert(
      @NotNull Alert.AlertType type,
      @NotNull String headerKey,
      String contentKey,
      String titleKey,
      final @NotNull FormatArg... contentArgs
  ) {
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
    if (contentKey != null) {
      alert.setContentText(language.translate(contentKey, contentArgs));
    }
    return alert.showAndWait();
  }

  private Alerts() {
  }
}
