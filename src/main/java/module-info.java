module net.darmo_creations.jenealogio2 {
  requires javafx.controls;
  requires javafx.fxml;
  requires javafx.web;
  requires org.jetbrains.annotations;
  requires ini4j;
  requires commons.cli;
  requires com.google.gson;

  opens net.darmo_creations.jenealogio2 to javafx.fxml;
  exports net.darmo_creations.jenealogio2;
  opens net.darmo_creations.jenealogio2.ui to javafx.fxml;
  exports net.darmo_creations.jenealogio2.utils;
  exports net.darmo_creations.jenealogio2.config;
  exports net.darmo_creations.jenealogio2.themes;
}
