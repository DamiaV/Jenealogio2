package net.darmo_creations.jenealogio2.ui.components;

import javafx.beans.property.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * This panel shows counts of first names, last names, occupations, and places from a given {@link FamilyTree}.
 */
public class StatisticsPanel extends GridPane {
  private final StatsPanel firstNamesStatsPanel;
  private final StatsPanel lastNamesStatsPanel;
  private final StatsPanel occupationsStatsPanel;
  private final StatsPanel placesStatsPanel;

  private FamilyTree familyTree;
  private final Config config;

  public StatisticsPanel(final @NotNull Config config) {
    this.config = config;
    this.firstNamesStatsPanel = new StatsPanel("first_names");
    this.lastNamesStatsPanel = new StatsPanel("last_names");
    this.occupationsStatsPanel = new StatsPanel("occupations");
    this.placesStatsPanel = new StatsPanel("places");

    final Language language = config.language();
    int i = 0;

    {
      final Label label1 = new Label(language.translate("statistics.first_names.title"));
      label1.setPadding(new Insets(5));
      setHalignment(label1, HPos.CENTER);
      final Label label2 = new Label(language.translate("statistics.last_names.title"));
      label2.setPadding(new Insets(5));
      setHalignment(label2, HPos.CENTER);
      this.addRow(i++, label1, label2);
      final var rc = new RowConstraints();
      rc.setVgrow(Priority.SOMETIMES);
      this.getRowConstraints().add(rc);
    }

    {
      this.addRow(i++, this.firstNamesStatsPanel, this.lastNamesStatsPanel);
      final var rc = new RowConstraints();
      rc.setPercentHeight(48); // Try to account for previous row’s height
      rc.setVgrow(Priority.ALWAYS);
      this.getRowConstraints().add(rc);
    }

    {
      final Label label1 = new Label(language.translate("statistics.occupations.title"));
      label1.setPadding(new Insets(5));
      setHalignment(label1, HPos.CENTER);
      final Label label2 = new Label(language.translate("statistics.places.title"));
      label2.setPadding(new Insets(5));
      setHalignment(label2, HPos.CENTER);
      this.addRow(i++, label1, label2);
      final var rc = new RowConstraints();
      rc.setVgrow(Priority.SOMETIMES);
      this.getRowConstraints().add(rc);
    }

    {
      this.addRow(i, this.occupationsStatsPanel, this.placesStatsPanel);
      final var rc = new RowConstraints();
      rc.setVgrow(Priority.ALWAYS);
      this.getRowConstraints().add(rc);
    }

    final var cc1 = new ColumnConstraints();
    cc1.setPercentWidth(50);
    cc1.setHgrow(Priority.ALWAYS);
    final var cc2 = new ColumnConstraints();
    cc2.setPercentWidth(50);
    cc2.setHgrow(Priority.ALWAYS);
    this.getColumnConstraints().addAll(cc1, cc2);
  }

  public void setFamilyTree(final FamilyTree familyTree) {
    this.familyTree = familyTree;
    this.refresh();
  }

  public void refresh() {
    if (this.familyTree == null) return;

    final List<String> firstNames = new LinkedList<>();
    final List<String> lastNames = new LinkedList<>();
    final List<String> occupations = new LinkedList<>();
    final List<String> places = new LinkedList<>();

    for (final Person person : this.familyTree.persons()) {
      firstNames.addAll(person.legalFirstNames());
      firstNames.addAll(person.publicFirstNames());
      person.legalLastName().ifPresent(lastNames::add);
      person.publicLastName().ifPresent(lastNames::add);
      person.mainOccupation().ifPresent(occupations::add);
    }
    for (final LifeEvent lifeEvent : this.familyTree.lifeEvents())
      lifeEvent.place().ifPresent(place -> places.add(place.address()));

    final var firstNamesStats = StringUtils.computeWordStatistics(firstNames, true);
    final var lastNamesStats = StringUtils.computeWordStatistics(lastNames, true);
    final var occupationsStats = StringUtils.computeWordStatistics(occupations, false);
    final var placesStats = StringUtils.computeWordStatistics(places, true);
    this.firstNamesStatsPanel.setContent(firstNamesStats);
    this.lastNamesStatsPanel.setContent(lastNamesStats);
    this.occupationsStatsPanel.setContent(occupationsStats);
    this.placesStatsPanel.setContent(placesStats);
  }

  /**
   * This panel shows word counts in a {@link TableView}.
   */
  public final class StatsPanel extends AnchorPane {
    private final TableView<TableItem> content = new TableView<>();

    public StatsPanel(@NotNull String label) {
      final Language language = StatisticsPanel.this.config.language();

      this.content.setPlaceholder(new Text(language.translate("table.empty")));

      final TableColumn<TableItem, String> valueCol =
          new TableColumn<>(language.translate("statistics.%s.table.value".formatted(label)));
      valueCol.setPrefWidth(300);
      valueCol.setCellValueFactory(new PropertyValueFactory<>("label"));
      final TableColumn<TableItem, Long> countCol =
          new TableColumn<>(language.translate("statistics.%s.table.count".formatted(label)));
      countCol.setCellValueFactory(new PropertyValueFactory<>("count"));
      //noinspection unchecked
      this.content.getColumns().addAll(valueCol, countCol);

      final ScrollPane scrollPane = new ScrollPane(this.content);
      scrollPane.setFitToHeight(true);
      scrollPane.setFitToWidth(true);
      scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
      AnchorPane.setLeftAnchor(scrollPane, 0.0);
      AnchorPane.setTopAnchor(scrollPane, 0.0);
      AnchorPane.setRightAnchor(scrollPane, 0.0);
      AnchorPane.setBottomAnchor(scrollPane, 0.0);
      this.getChildren().add(scrollPane);
    }

    /**
     * Set the data to display.
     *
     * @param counts A map associating strings with their count.
     */
    public void setContent(final @NotNull Map<String, Long> counts) {
      this.content.getItems().clear();
      counts.entrySet().stream()
          .sorted(Map.Entry.comparingByValue(Comparator.comparingLong(Long::longValue).reversed()))
          .forEach(entry ->
              this.content.getItems().add(new TableItem(entry.getKey(), entry.getValue())));
    }

    /**
     * Wrapper class for entries in the {@link TableView} of the {@link StatsPanel} class.
     */
    public static final class TableItem {
      private final StringProperty label = new SimpleStringProperty();
      private final LongProperty count = new SimpleLongProperty();

      TableItem(@NotNull String label, long count) {
        Objects.requireNonNull(label);
        this.label.set(label);
        this.count.set(count);
      }

      @SuppressWarnings("unused") // Called by PropertyValueFactory
      public StringProperty labelProperty() {
        return this.label;
      }

      @SuppressWarnings("unused") // Called by PropertyValueFactory
      public LongProperty countProperty() {
        return this.count;
      }
    }
  }
}
