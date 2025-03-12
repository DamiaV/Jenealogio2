package net.darmo_creations.jenealogio2.ui.components.choice;

import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.ui.components.*;
import org.jetbrains.annotations.*;

public class GenderChoiceWidget extends LabeledChoiceWidget<GenderLabel, Gender> {
  public GenderChoiceWidget(final @NotNull Config config, @NotNull String label) {
    super(
        config,
        label,
        new GenderLabel(null, true, config),
        new GenderLabel(null, true, config),
        false
    );
  }

  @Override
  protected boolean isValueEmpty(Gender value) {
    return value == null;
  }

  protected void updateNodes(Gender left, Gender right) {
    final String empty = this.config.language().translate("choice.empty");
    if (left != null) {
      this.leftNode.setGender(left);
      this.leftNode.setStyle("");
    } else {
      this.leftNode.setGender(null);
      this.leftNode.setText(empty);
      this.leftNode.setStyle("-fx-font-style: italic;");
    }
    if (right != null) {
      this.rightNode.setGender(right);
      this.rightNode.setStyle("");
    } else {
      this.rightNode.setGender(null);
      this.rightNode.setText(empty);
      this.rightNode.setStyle("-fx-font-style: italic;");
    }
  }
}
