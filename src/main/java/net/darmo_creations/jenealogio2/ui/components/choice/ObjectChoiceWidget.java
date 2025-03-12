package net.darmo_creations.jenealogio2.ui.components.choice;

import javafx.scene.control.*;
import net.darmo_creations.jenealogio2.config.*;
import org.jetbrains.annotations.*;

public class ObjectChoiceWidget extends LabeledChoiceWidget<Label, Object> {
  public ObjectChoiceWidget(
      final @NotNull Config config,
      @NotNull String label,
      boolean enableBoth
  ) {
    super(config, label, new Label(), new Label(), enableBoth);
  }

  @Override
  protected boolean isValueEmpty(final Object value) {
    return value == null;
  }

  protected void updateNodes(final Object left, final Object right) {
    final String empty = this.config.language().translate("choice.empty");
    this.leftNode.setText(left == null ? empty : String.valueOf(left));
    this.leftNode.setStyle(left == null ? "-fx-font-style: italic" : "");
    this.rightNode.setText(right == null ? empty : String.valueOf(right));
    this.rightNode.setStyle(right == null ? "-fx-font-style: italic" : "");
  }
}
