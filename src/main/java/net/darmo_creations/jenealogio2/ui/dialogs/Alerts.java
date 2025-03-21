package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.application.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import net.darmo_creations.jenealogio2.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * This class provides methods to open alert dialogs.
 */
public final class Alerts {
  /**
   * Open an alert dialog to show some information.
   *
   * @param config      The app’s config.
   * @param headerKey   Header text key.
   * @param contentKey  Content text key.
   * @param titleKey    Title key.
   * @param contentArgs Format arguments to apply to the header, content and title.
   */
  public static void info(
      final @NotNull Config config,
      @NotNull String headerKey,
      String contentKey,
      String titleKey,
      final @NotNull FormatArg... contentArgs
  ) {
    alert(config, AlertType.INFORMATION, headerKey, contentKey, titleKey, contentArgs);
  }

  /**
   * Open an alert dialog to show a warning message.
   *
   * @param config      The app’s config.
   * @param headerKey   Header text key.
   * @param contentKey  Content text key.
   * @param titleKey    Title key.
   * @param contentArgs Format arguments to apply to the header, content and title.
   */
  public static void warning(
      final @NotNull Config config,
      @NotNull String headerKey,
      String contentKey,
      String titleKey,
      final @NotNull FormatArg... contentArgs
  ) {
    alert(config, AlertType.WARNING, headerKey, contentKey, titleKey, contentArgs);
  }

  /**
   * Open an alert dialog to show an error message.
   *
   * @param config      The app’s config.
   * @param headerKey   Header text key.
   * @param contentKey  Content text key.
   * @param titleKey    Title key.
   * @param contentArgs Format arguments to apply to the header, content and title.
   */
  public static void error(
      final @NotNull Config config,
      @NotNull String headerKey,
      String contentKey,
      String titleKey,
      final @NotNull FormatArg... contentArgs
  ) {
    alert(config, AlertType.ERROR, headerKey, contentKey, titleKey, contentArgs);
  }

  /**
   * Open an alert dialog to prompt the user for confirmation.
   *
   * @param config      The app’s config.
   * @param headerKey   Header text key.
   * @param contentKey  Content text key.
   * @param titleKey    Title key.
   * @param contentArgs Format arguments to apply to the header, content and title.
   * @return True if the user clicked OK, false if they clicked CANCEL or dismissed the dialog.
   */
  public static boolean confirmation(
      final @NotNull Config config,
      @NotNull String headerKey,
      String contentKey,
      String titleKey,
      final @NotNull FormatArg... contentArgs
  ) {
    final var result = alert(config, AlertType.CONFIRMATION, headerKey, contentKey, titleKey, contentArgs);
    return result.isPresent() && !result.get().getButtonData().isCancelButton();
  }

  /**
   * Open an alert dialog to prompt the user for confirmation.
   *
   * @param config      The current config.
   * @param headerKey   Header text key.
   * @param contentKey  Content text key.
   * @param titleKey    Title key.
   * @param contentArgs Format arguments to apply to the header, content and title.
   * @return True if the user clicked YES, false if they clicked NO,
   * an empty {@link Optional} if they clicked CANCEL or dismissed the dialog.
   */
  public static Optional<Boolean> confirmationWithCancel(
      final @NotNull Config config,
      @NotNull String headerKey,
      String contentKey,
      String titleKey,
      final @NotNull FormatArg... contentArgs
  ) {
    final var result = alert(config, AlertType.CONFIRMATION_CANCEL, headerKey, contentKey, titleKey, contentArgs);
    if (result.isEmpty() || result.get() == ButtonTypes.CANCEL)
      return Optional.empty();
    return Optional.of(!result.get().getButtonData().isCancelButton());
  }

  /**
   * Open an alert dialog to prompt the uses to input some text.
   *
   * @param config        The app’s config.
   * @param headerKey     Header text key.
   * @param labelKey      Text field label text key.
   * @param titleKey      Title key.
   * @param defaultText   Text to put into the text field.
   * @param textFormatter An optional text formatter to apply to the text field.
   * @param contentArgs   Format arguments to apply to the header, label and title.
   * @return The selected item.
   */
  public static Optional<String> textInput(
      final @NotNull Config config,
      @NotNull String headerKey,
      @NotNull String labelKey,
      String titleKey,
      String defaultText,
      TextFormatter<?> textFormatter,
      final @NotNull FormatArg... contentArgs
  ) {
    final Alert alert = getAlert(config, AlertType.TEXT, headerKey, titleKey, contentArgs);
    final HBox hBox = new HBox(5);
    final TextField textField = new TextField();
    textField.textProperty().addListener((observable, oldValue, newValue) ->
        alert.getDialogPane().lookupButton(ButtonTypes.OK).setDisable(StringUtils.stripNullable(newValue).isEmpty()));
    if (textFormatter != null)
      textField.setTextFormatter(textFormatter);
    textField.setText(defaultText);
    final Label label = new Label(config.language().translate(labelKey, contentArgs));
    hBox.getChildren().addAll(label, textField);
    hBox.setAlignment(Pos.CENTER);
    alert.getDialogPane().setContent(hBox);
    alert.setOnShown(e -> {
      Platform.runLater(textField::requestFocus);
      e.consume();
    });
    final Optional<ButtonType> buttonType = alert.showAndWait();
    if (buttonType.isPresent() && !buttonType.get().getButtonData().isCancelButton())
      return StringUtils.stripNullable(textField.getText());
    return Optional.empty();
  }

  private static Optional<ButtonType> alert(
      final @NotNull Config config,
      @NotNull AlertType type,
      @NotNull String headerKey,
      String contentKey,
      String titleKey,
      final @NotNull FormatArg... contentArgs
  ) {
    final Alert alert = getAlert(config, type, headerKey, titleKey, contentArgs);
    if (contentKey != null)
      alert.setContentText(config.language().translate(contentKey, contentArgs));
    return alert.showAndWait();
  }

  /**
   * Create a basic alert dialog.
   *
   * @param config      The app’s config.
   * @param type        Alert type.
   * @param headerKey   Header text key.
   * @param titleKey    Title key.
   * @param contentArgs Format arguments to apply to the header and title.
   * @return The alert dialog.
   */
  private static Alert getAlert(
      final @NotNull Config config,
      @NotNull AlertType type,
      @NotNull String headerKey,
      String titleKey,
      final @NotNull FormatArg... contentArgs
  ) {
    final Alert alert = new Alert(type.type());
    final DialogPane dialogPane = alert.getDialogPane();
    // Replace default buttons to have proper translations
    dialogPane.getButtonTypes().setAll(switch (type) {
      case INFORMATION, WARNING, ERROR -> List.of(ButtonTypes.OK);
      case CONFIRMATION -> List.of(ButtonTypes.YES, ButtonTypes.NO);
      case CONFIRMATION_CANCEL -> List.of(ButtonTypes.YES, ButtonTypes.NO, ButtonTypes.CANCEL);
      case TEXT -> List.of(ButtonTypes.OK, ButtonTypes.CANCEL);
    });
    config.theme().getStyleSheets()
        .forEach(url -> dialogPane.getStylesheets().add(url.toExternalForm()));
    if (titleKey == null)
      titleKey = "alert.%s.title".formatted(type.key());
    final Language language = config.language();
    alert.setTitle(language.translate(titleKey) + " – " + App.NAME);
    alert.setHeaderText(language.translate(headerKey, contentArgs));
    ((Stage) dialogPane.getScene().getWindow()).getIcons().add(config.theme().getAppIcon());
    return alert;
  }

  private Alerts() {
  }

  public enum AlertType {
    CONFIRMATION(Alert.AlertType.CONFIRMATION, "confirmation"),
    CONFIRMATION_CANCEL(Alert.AlertType.CONFIRMATION, "confirmation"),
    INFORMATION(Alert.AlertType.INFORMATION, "information"),
    ERROR(Alert.AlertType.ERROR, "error"),
    WARNING(Alert.AlertType.ERROR, "warning"),
    TEXT(Alert.AlertType.CONFIRMATION, "text"),
    ;

    private final Alert.AlertType type;
    private final String key;

    AlertType(@NotNull Alert.AlertType type, String key) {
      this.type = type;
      this.key = key;
    }

    public Alert.AlertType type() {
      return this.type;
    }

    public String key() {
      return this.key;
    }
  }
}
