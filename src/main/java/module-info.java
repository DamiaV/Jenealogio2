/**
 * This module defines all requirements for the application.
 */
module net.darmo_creations.jenealogio2 {
  requires javafx.controls;
  requires javafx.fxml;
  requires javafx.web;
  requires org.jetbrains.annotations;
  requires ini4j;
  requires commons.cli;
  requires com.google.gson;
  requires org.threeten.extra;
  requires lib.french.revolutionary.calendar;

  // Make AppController open for reflection to JavaFX’s FXML system only
  opens net.darmo_creations.jenealogio2 to javafx.fxml;
  // Make App accessible to JavaFX, other classes accessible from App’s public interface
  // are not exported because it’s not necessary
  exports net.darmo_creations.jenealogio2 to javafx.graphics;
  // Make dialog controllers open for reflection to JavaFX’s FXML system only
  opens net.darmo_creations.jenealogio2.ui.dialogs to javafx.fxml;
}
