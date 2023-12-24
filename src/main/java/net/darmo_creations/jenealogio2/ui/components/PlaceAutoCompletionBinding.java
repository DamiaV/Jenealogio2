package net.darmo_creations.jenealogio2.ui.components;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Consumer;

class PlaceAutoCompletionBinding extends AutoCompletionBinding<Place> {
  private final ChangeListener<String> textChangeListener;
  private final ChangeListener<Boolean> focusChangedListener;
  private final Consumer<Place> onCompletion;

  /**
   * Create a new binding for the given {@link TextField}.
   *
   * @param addressField       Text field to listen to.
   * @param suggestionProvider Function that provides a list of places that match the user’s input.
   * @param onCompletion       A callback that is called when the user selects a suggestion.
   */
  PlaceAutoCompletionBinding(
      @NotNull TextField addressField,
      @NotNull Callback<ISuggestionRequest, Collection<Place>> suggestionProvider,
      @NotNull Consumer<Place> onCompletion
  ) {
    super(addressField, suggestionProvider, new StringConverter<>() {
      @Override
      public String toString(Place place) {
        return place.address();
      }

      @Override
      public Place fromString(String s) {
        return null; // Unused
      }
    });
    this.onCompletion = onCompletion;
    this.textChangeListener = (obs, oldText, newText) -> {
      if (this.getCompletionTarget().isFocused()) {
        this.setUserInput(newText);
      }
    };
    this.focusChangedListener = (obs, oldFocused, newFocused) -> {
      if (!newFocused) {
        this.hidePopup();
      }
    };
    this.getCompletionTarget().textProperty().addListener(this.textChangeListener);
    this.getCompletionTarget().focusedProperty().addListener(this.focusChangedListener);
  }

  public TextField getCompletionTarget() {
    return (TextField) super.getCompletionTarget();
  }

  @Override
  public void dispose() {
    this.getCompletionTarget().textProperty().removeListener(this.textChangeListener);
    this.getCompletionTarget().focusedProperty().removeListener(this.focusChangedListener);
  }

  @Override
  protected void completeUserInput(@NotNull Place completion) {
    String newText = completion.address();
    this.getCompletionTarget().setText(newText);
    this.getCompletionTarget().positionCaret(newText.length());
    this.onCompletion.accept(completion);
  }
}
