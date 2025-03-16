package net.darmo_creations.jenealogio2.ui.components;

import javafx.scene.control.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.themes.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * This list cell class displays {@link ObjectAnnotation}s.
 * If the annotation represents a {@link Person}, it’s name is displayed in full, along with a person icon.
 * If the annotation represents a {@link LifeEvent}, it’s type and actors are displayed, along with a clock icon.
 * In both cases, if a note exits, it is displayed next to the above text.
 */
public class ObjectAnnotationListCell extends ListCell<ObjectAnnotation> {
  private final Config config;

  public ObjectAnnotationListCell(final @NotNull Config config) {
    this.config = config;
  }

  @Override
  protected void updateItem(final ObjectAnnotation item, boolean empty) {
    super.updateItem(item, empty);
    final Language language = this.config.language();
    if (item != null) {
      final GenealogyObject<?> object = item.object();
      final Optional<String> note = item.note();

      final String serializedObject;
      final Icon icon;
      if (object instanceof Person p) {
        serializedObject = p.toString();
        icon = Icon.PERSON_ANNOTATION;
      } else if (object instanceof LifeEvent e) {
        final String eventName;
        if (e.type().isBuiltin())
          eventName = language.translate("life_event_types." + e.type().key().name());
        else
          eventName = Objects.requireNonNull(e.type().userDefinedName());
        final List<Person> actors = e.actors()
            .stream()
            .sorted(Person.lastThenFirstNamesComparator())
            .toList();
        serializedObject = language.translate(
            "life_event_type_with_actors",
            new FormatArg("event", eventName),
            new FormatArg("actors", language.makeList(actors, false))
        );
        icon = Icon.EVENT_ANNOTATION;
      } else throw new RuntimeException("Unknown object type: " + object.getClass());

      final String text;
      if (note.isPresent()) {
        text = language.translate(
            "annotations_list.item_with_note",
            new FormatArg("object", serializedObject),
            new FormatArg("note", note.get())
        );
      } else text = serializedObject;
      this.setText(text);
      this.setGraphic(this.config.theme().getIcon(icon, Icon.Size.SMALL));
    } else {
      this.setText(null);
      this.setGraphic(null);
    }
  }
}
