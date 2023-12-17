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
   * @param headerKey   Header text key.
   * @param contentKey  Content text key.
   * @param titleKey    Title key.
   * @param contentArgs Format arguments to apply to the header, content and title.
   */
  public static void info(
      @NotNull String headerKey, String contentKey, String titleKey, final FormatArg @NotNull ... contentArgs) {
    alert(Alert.AlertType.INFORMATION, headerKey, contentKey, titleKey, contentArgs);
  }

  /**
   * Open an alert dialog to show a warning message.
   *
   * @param headerKey   Header text key.
   * @param contentKey  Content text key.
   * @param titleKey    Title key.
   * @param contentArgs Format arguments to apply to the header, content and title.
   */
  public static void warning(
      @NotNull String headerKey, String contentKey, String titleKey, final FormatArg @NotNull ... contentArgs) {
    alert(Alert.AlertType.WARNING, headerKey, contentKey, titleKey, contentArgs);
  }

  /**
   * Open an alert dialog to show an error message.
   *
   * @param headerKey   Header text key.
   * @param contentKey  Content text key.
   * @param titleKey    Title key.
   * @param contentArgs Format arguments to apply to the header, content and title.
   */
  public static void error(
      @NotNull String headerKey, String contentKey, String titleKey, final FormatArg @NotNull ... contentArgs) {
    alert(Alert.AlertType.ERROR, headerKey, contentKey, titleKey, contentArgs);
  }

  /**
   * Open an alert dialog to prompt the user for confirmation.
   *
   * @param headerKey   Header text key.
   * @param contentKey  Content text key.
   * @param titleKey    Title key.
   * @param contentArgs Format arguments to apply to the header, content and title.
   * @return True if the user clicked OK, false if they clicked CANCEL or dismissed the dialog.
   */
  public static boolean confirmation(
      @NotNull String headerKey, String contentKey, String titleKey, final FormatArg @NotNull ... contentArgs) {
    Optional<ButtonType> result = alert(Alert.AlertType.CONFIRMATION, headerKey, contentKey, titleKey, contentArgs);
    return result.isPresent() && !result.get().getButtonData().isCancelButton();
  }

  /**
   * Open an alert dialog to ask the user to choose an option from a combobox.
   *
   * @param headerKey   Header text key.
   * @param labelKey    Combobox label text key.
   * @param titleKey    Title key.
   * @param choices     The choices for the combobox.
   * @param contentArgs Format arguments to apply to the header and title.
   * @return The selected item.
   */
  public static <T> Optional<T> chooser(
      @NotNull String headerKey,
      @NotNull String labelKey,
      String titleKey,
      final Collection<T> choices,
      final FormatArg @NotNull ... contentArgs
  ) {
    if (choices.isEmpty()) {
      throw new IllegalArgumentException("empty choices");
    }
    Alert alert = getAlert(Alert.AlertType.CONFIRMATION, headerKey, titleKey, contentArgs);
    HBox hBox = new HBox(4);
    ComboBox<T> choicesCombo = new ComboBox<>();
    choicesCombo.getItems().addAll(choices);
    choicesCombo.getSelectionModel().select(0);
    Label label = new Label(App.config().language().translate(labelKey, contentArgs));
    hBox.getChildren().addAll(label, choicesCombo);
    hBox.setAlignment(Pos.CENTER);
    alert.getDialogPane().setContent(hBox);
    alert.setOnShown(e -> {
      Platform.runLater(choicesCombo::requestFocus);
      e.consume();
    });
    Optional<ButtonType> buttonType = alert.showAndWait();
    if (buttonType.isPresent() && !buttonType.get().getButtonData().isCancelButton()) {
      return Optional.of(choicesCombo.getSelectionModel().getSelectedItem());
    }
    return Optional.empty();
  }

  /**
   * Open an alert dialog to prompt the use to input some text.
   *
   * @param headerKey   Header text key.
   * @param labelKey    Text field label text key.
   * @param titleKey    Title key.
   * @param defaultText Text to put into the text field.
   * @param contentArgs Format arguments to apply to the header, label and title.
   * @return The selected item.
   */
  public static Optional<String> textInput(
      @NotNull String headerKey,
      @NotNull String labelKey,
      String titleKey,
      String defaultText,
      final FormatArg @NotNull ... contentArgs
  ) {
    Alert alert = getAlert(Alert.AlertType.CONFIRMATION, headerKey, titleKey, contentArgs);
    HBox hBox = new HBox(4);
    TextField textField = new TextField();
    textField.textProperty().addListener((observable, oldValue, newValue) ->
        alert.getDialogPane().lookupButton(ButtonTypes.OK).setDisable(StringUtils.stripNullable(newValue).isEmpty()));
    textField.setText(defaultText);
    Label label = new Label(App.config().language().translate(labelKey, contentArgs));
    hBox.getChildren().addAll(label, textField);
    hBox.setAlignment(Pos.CENTER);
    alert.getDialogPane().setContent(hBox);
    alert.setOnShown(e -> {
      Platform.runLater(textField::requestFocus);
      e.consume();
    });
    Optional<ButtonType> buttonType = alert.showAndWait();
    if (buttonType.isPresent() && !buttonType.get().getButtonData().isCancelButton()) {
      return StringUtils.stripNullable(textField.getText());
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

  /**
   * Create a basic alert dialog.
   *
   * @param type        Alert type. {@link Alert.AlertType#NONE} is not allowed.
   * @param headerKey   Header text key.
   * @param titleKey    Title key.
   * @param contentArgs Format arguments to apply to the header and title.
   * @return The alert dialog.
   */
  private static Alert getAlert(
      @NotNull Alert.AlertType type, @NotNull String headerKey, String titleKey, final FormatArg @NotNull ... contentArgs) {
    if (type == Alert.AlertType.NONE) {
      throw new IllegalArgumentException(type.name());
    }
    Alert alert = new Alert(type);
    DialogPane dialogPane = alert.getDialogPane();
    // Replace default buttons to have proper translations
    dialogPane.getButtonTypes().setAll(switch (type) {
      case INFORMATION, WARNING, ERROR -> List.of(ButtonTypes.OK);
      case CONFIRMATION -> List.of(ButtonTypes.OK, ButtonTypes.CANCEL);
      case NONE -> throw new IllegalArgumentException(type.name()); // Should never happen
    });
    App.config().theme().getStyleSheets()
        .forEach(url -> dialogPane.getStylesheets().add(url.toExternalForm()));
    if (titleKey == null) {
      titleKey = "alert.%s.title".formatted(type.name().toLowerCase());
    }
    Language language = App.config().language();
    alert.setTitle(language.translate(titleKey));
    alert.setHeaderText(language.translate(headerKey, contentArgs));
    ((Stage) dialogPane.getScene().getWindow()).getIcons().add(App.config().theme().getAppIcon());
    return alert;
  }

  private Alerts() {
  }
}
