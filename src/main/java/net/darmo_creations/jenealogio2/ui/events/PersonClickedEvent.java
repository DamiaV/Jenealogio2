package net.darmo_creations.jenealogio2.ui.events;

import javafx.scene.input.MouseButton;
import net.darmo_creations.jenealogio2.model.Person;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * This event indicates that a node wrapping the given {@link Person} object should be selected.
 *
 * @param person The person to select.
 * @param action The type of action to perform on the person.
 */
public record PersonClickedEvent(@NotNull Person person, @NotNull PersonClickedEvent.Action action)
    implements PersonClickEvent {
  public PersonClickedEvent {
    Objects.requireNonNull(person);
    Objects.requireNonNull(action);
  }

  /**
   * Return the {@link Action} constant for the given click event.
   * <p>
   * Actions are returned using the following rules:
   * <li>{@link Action#SET_AS_TARGET}: If {@code mouseButton} is {@link MouseButton#SECONDARY},
   * regardless of click count.</li>
   * <li>{@link Action#SELECT}: If {@code mouseButton} is <em>not</em> {@link MouseButton#SECONDARY}
   * and click count is 1.</li>
   * <li>{@link Action#EDIT}: If {@code mouseButton} is <em>not</em> {@link MouseButton#SECONDARY}
   * and click count is <em>not</em> 1.</li>
   *
   * @param clickCount  Number of clicks.
   * @param mouseButton Clicked mouse button.
   * @return The corresponding action.
   * @throws IllegalArgumentException If {@code clickCount ≤ 0}.
   */
  public static Action getClickType(int clickCount, @NotNull MouseButton mouseButton) {
    if (clickCount <= 0) {
      throw new IllegalArgumentException("Click count == 0");
    }
    Objects.requireNonNull(mouseButton);
    if (mouseButton == MouseButton.SECONDARY) {
      return Action.SET_AS_TARGET;
    }
    if (clickCount == 1) {
      return Action.SELECT;
    }
    return Action.EDIT;
  }

  /**
   * Enumeration of all actions that can be performed on {@link Person} objects through this event.
   */
  public enum Action {
    SELECT(false),
    EDIT(false),
    SET_AS_TARGET(true),
    ;

    private final boolean updateTarget;

    Action(boolean updateTarget) {
      this.updateTarget = updateTarget;
    }

    /**
     * Indicate whether this action should update the targetted person.
     */
    public boolean shouldUpdateTarget() {
      return this.updateTarget;
    }
  }
}
