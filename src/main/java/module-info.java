/**
 * This module defines all requirements for the application.
 */
module net.darmo_creations.jenealogio2 {
  requires java.net.http;
  requires javafx.controls;
  requires javafx.fxml;
  requires javafx.web;
  requires javafx.swing;
  requires java.desktop;
  requires org.controlsfx.controls;
  requires org.jetbrains.annotations;
  requires ini4j;
  requires commons.cli;
  requires com.google.gson;
  requires lib.french.revolutionary.calendar;
  requires com.gluonhq.maps;
  requires net.time4j.base;

  // Make App accessible to JavaFX, other classes accessible from App’s public interface
  // are not exported because it’s not necessary
  exports net.darmo_creations.jenealogio2 to javafx.graphics;

  // Open to JUnit tests
  opens net.darmo_creations.jenealogio2.model;
  opens net.darmo_creations.jenealogio2.model.datetime;
  opens net.darmo_creations.jenealogio2.utils;
  opens net.darmo_creations.jenealogio2.utils.text_parser;
}
