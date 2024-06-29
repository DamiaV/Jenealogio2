package net.darmo_creations.jenealogio2.ui.components.map_view;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.themes.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * A tooltip that shows the number of events at a given address.
 */
public class EventTypesTooltip extends VBox {
  /**
   * Create an event type tooltip.
   *
   * @param placeAdress      The address for the events.
   * @param eventTypesCounts A map associating {@link LifeEventType}s to their counts.
   * @param config           The app’s config.
   */
  public EventTypesTooltip(
      @NotNull String placeAdress,
      @NotNull Map<LifeEventType, Integer> eventTypesCounts,
      final @NotNull Config config
  ) {
    super(5);
    this.getStyleClass().add("events-map-tooltip");
    this.setPadding(new Insets(5));

    Label titleLabel = new Label();
    titleLabel.getStyleClass().add("title");
    this.getChildren().add(titleLabel);
    String[] split = placeAdress.split(",", 2);
    String titleText;
    if (split.length == 2) {
      titleText = split[0].strip();
      Label subTitleLabel = new Label(split[1].strip());
      subTitleLabel.getStyleClass().add("subtitle");
      this.getChildren().add(subTitleLabel);
    } else {
      titleText = placeAdress;
    }
    int totalCount = eventTypesCounts.values().stream().mapToInt(i -> i).sum();
    titleLabel.setText(config.language().translate(
        "dialog.map.place_count",
        totalCount,
        new FormatArg("address", titleText),
        new FormatArg("count", totalCount)
    ));

    Language language = config.language();
    eventTypesCounts.entrySet().stream()
        .sorted(Comparator.comparing(e -> language.translate("life_event_types." + e.getKey().key().name())))
        .forEach(e -> {
          int count = e.getValue();
          LifeEventType type = e.getKey();
          RegistryEntryKey key = type.key();
          String name;
          if (key.namespace().equals(Registry.BUILTIN_NS)) {
            name = language.translate("life_event_types." + key.name(), count);
          } else {
            name = type.userDefinedName();
          }
          Label label = new Label(
              language.translate(
                  "dialog.map.tooltip.life_event_type_count",
                  new FormatArg("type_name", name),
                  new FormatArg("count", count)
              ),
              config.theme().getIcon(Icon.BULLET, Icon.Size.SMALL)
          );
          label.getStyleClass().add("event-type-count");
          this.getChildren().add(label);
        });
  }
}
