package net.darmo_creations.jenealogio2.ui.components.choice;

import javafx.scene.*;
import javafx.scene.control.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import org.jetbrains.annotations.*;

import java.util.*;

public abstract class ChoiceWidget<NT extends Node, DT> {
  private final Set<SelectionListener> listeners = new HashSet<>();

  protected final Config config;
  private final Label titleLabel = new Label();
  private final RadioButton leftRadio = new RadioButton();
  private final RadioButton rightRadio = new RadioButton();
  private final RadioButton bothRadio = new RadioButton();
  protected final NT leftNode;
  protected final NT rightNode;
  private final boolean allowBoth;
  private boolean bothEnabled;
  private boolean enabled = true;
  private boolean forceSelectionUpdate;

  protected ChoiceWidget(
      final @NotNull Config config,
      @NotNull String title,
      @NotNull NT leftNode,
      @NotNull NT rightNode,
      boolean allowBoth
  ) {
    this.config = config;
    this.allowBoth = allowBoth;
    this.titleLabel.setText(Objects.requireNonNull(title));
    this.leftNode = Objects.requireNonNull(leftNode);
    this.rightNode = Objects.requireNonNull(rightNode);
    final ToggleGroup group = new ToggleGroup();
    this.leftRadio.setToggleGroup(group);
    this.rightRadio.setToggleGroup(group);
    this.bothRadio.setToggleGroup(group);
    final Language language = config.language();
    this.bothRadio.setText(language.translate("dialog.merge_persons.choice.choice_both"));
    group.selectedToggleProperty().addListener(
        (observable, oldValue, newValue) ->
            this.listeners.forEach(l -> l.onSelectionChange(this.selection()))
    );
  }

  public Label titleLabel() {
    return this.titleLabel;
  }

  public RadioButton leftRadio() {
    return this.leftRadio;
  }

  public RadioButton rightRadio() {
    return this.rightRadio;
  }

  public RadioButton bothRadio() {
    return this.bothRadio;
  }

  public NT leftNode() {
    return this.leftNode;
  }

  public NT rightNode() {
    return this.rightNode;
  }

  public void setData(final DT left, final DT right) {
    final boolean leftPresent = !this.isValueEmpty(left);
    final boolean rightPresent = !this.isValueEmpty(right);
    this.enabled = leftPresent || rightPresent || !Objects.equals(left, right);
    this.bothEnabled = this.allowBoth && leftPresent && rightPresent;
    this.forceSelectionUpdate = true;
    if (this.enabled) {
      if (Objects.equals(left, right))
        this.setSelection(this.bothEnabled ? PersonMergeInfo.Which.BOTH : PersonMergeInfo.Which.LEFT);
      else if (left != null) this.setSelection(PersonMergeInfo.Which.LEFT);
      else this.setSelection(PersonMergeInfo.Which.RIGHT);
    } else this.setSelection(PersonMergeInfo.Which.NONE);
    this.forceSelectionUpdate = false;
    this.leftRadio.setDisable(!this.enabled);
    this.leftNode.setDisable(!this.enabled);
    this.rightRadio.setDisable(!this.enabled);
    this.rightNode.setDisable(!this.enabled);
    this.bothRadio.setDisable(!this.enabled || !this.bothEnabled);
    this.bothRadio.setVisible(this.bothEnabled);
    this.updateNodes(left, right);
  }

  protected abstract boolean isValueEmpty(final DT value);

  protected abstract void updateNodes(final DT left, final DT right);

  public PersonMergeInfo.Which selection() {
    if (this.leftRadio.isSelected()) return PersonMergeInfo.Which.LEFT;
    if (this.rightRadio.isSelected()) return PersonMergeInfo.Which.RIGHT;
    if (this.bothRadio.isSelected()) return PersonMergeInfo.Which.BOTH;
    return PersonMergeInfo.Which.NONE;
  }

  public void setSelection(@NotNull PersonMergeInfo.Which selection) {
    if (!this.enabled && !this.forceSelectionUpdate) return;
    switch (selection) {
      case LEFT -> this.leftRadio.setSelected(true);
      case RIGHT -> this.rightRadio.setSelected(true);
      case BOTH -> {
        if (this.bothEnabled) this.bothRadio.setSelected(true);
        else throw new IllegalArgumentException("BOTH is disabled");
      }
      default -> {
        this.leftRadio.setSelected(false);
        this.rightRadio.setSelected(false);
        this.bothRadio.setSelected(false);
      }
    }
  }

  public boolean isEnabled() {
    return this.enabled;
  }

  public boolean bothEnabled() {
    return this.bothEnabled;
  }

  public void addSelectionListener(final SelectionListener listener) {
    this.listeners.add(listener);
  }

  public interface SelectionListener {
    void onSelectionChange(@NotNull PersonMergeInfo.Which selection);
  }
}
