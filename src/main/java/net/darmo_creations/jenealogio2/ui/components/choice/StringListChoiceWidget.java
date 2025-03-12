package net.darmo_creations.jenealogio2.ui.components.choice;

import javafx.scene.control.*;
import net.darmo_creations.jenealogio2.config.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class StringListChoiceWidget extends LabeledChoiceWidget<Label, List<String>> {
  public StringListChoiceWidget(final @NotNull Config config, @NotNull String label) {
    super(config, label, new Label(), new Label(), true);
  }

  @Override
  protected boolean isValueEmpty(final List<String> value) {
    return value.isEmpty();
  }

  protected void updateNodes(final List<String> left, final List<String> right) {
    final String empty = this.config.language().translate("choice.empty");
    this.leftNode.setText(left.isEmpty() ? empty : String.join(" ", left));
    this.leftNode.setStyle(left.isEmpty() ? "-fx-font-style: italic" : "");
    this.rightNode.setText(right.isEmpty() ? empty : String.join(" ", right));
    this.rightNode.setStyle(right.isEmpty() ? "-fx-font-style: italic" : "");
  }
}
