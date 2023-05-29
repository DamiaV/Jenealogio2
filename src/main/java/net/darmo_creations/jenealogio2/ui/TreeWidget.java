package net.darmo_creations.jenealogio2.ui;

import javafx.scene.layout.Pane;
import net.darmo_creations.jenealogio2.model.Person;
import net.darmo_creations.jenealogio2.model.Registries;
import net.darmo_creations.jenealogio2.model.Registry;
import net.darmo_creations.jenealogio2.model.RegistryEntryKey;

import java.util.List;

public class TreeWidget extends Pane {
  public TreeWidget() {
    // DEBUG
    Person person = new Person();
    person.setLegalFirstNames(List.of("John", "Jack", "iuaeuieauieauieauieaiueaea"));
    person.setLegalLastName("Doe");
    person.setGender(Registries.GENDERS.getEntry(new RegistryEntryKey(Registry.BUILTIN_NS, "female")));
    PersonWidget w = new PersonWidget(person);
    this.getChildren().add(w);
    w.setLayoutX(20);
    w.setLayoutY(20);

    Person person2 = new Person();
    person2.setLegalFirstNames(List.of("John"));
    person2.setLegalLastName("Yo");
    person2.setGender(Registries.GENDERS.getEntry(new RegistryEntryKey(Registry.BUILTIN_NS, "non_binary")));
    PersonWidget w1 = new PersonWidget(person2);
    this.getChildren().add(w1);
    w1.setLayoutX(20);
    w1.setLayoutY(200);
  }
}
