package net.darmo_creations.jenealogio2.ui.components.choice;

import javafx.scene.control.*;
import net.darmo_creations.jenealogio2.config.*;
import org.jetbrains.annotations.*;

public abstract class LabeledChoiceWidget<LT extends Label, DT> extends ChoiceWidget<LT, DT> {
  public LabeledChoiceWidget(
      final @NotNull Config config,
      @NotNull String label,
      @NotNull LT leftLabel,
      @NotNull LT rightLabel,
      boolean enableBoth
  ) {
    super(config, label, leftLabel, rightLabel, enableBoth);
    leftLabel.setOnMouseClicked(event -> {
      if (this.isEnabled()) this.leftRadio().setSelected(true);
    });
    rightLabel.setOnMouseClicked(event -> {
      if (this.isEnabled()) this.rightRadio().setSelected(true);
    });
  }
}
