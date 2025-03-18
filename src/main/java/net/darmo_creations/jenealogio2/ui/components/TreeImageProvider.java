package net.darmo_creations.jenealogio2.ui.components;

import javafx.scene.image.*;
import net.darmo_creations.jenealogio2.model.*;

import java.util.*;

public interface TreeImageProvider {
  Image exportAsImage();

  Optional<Person> targettedPerson();
}
