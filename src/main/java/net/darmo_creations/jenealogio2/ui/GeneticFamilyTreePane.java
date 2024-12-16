package net.darmo_creations.jenealogio2.ui;

import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.ui.components.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.stream.*;

/**
 * JavaFX component displaying a part of a genetic family tree’s graph.
 */
public class GeneticFamilyTreePane extends PersonTreeView {
  public static final int MIN_ALLOWED_HEIGHT = 1;
  public static final int MAX_ALLOWED_HEIGHT = 7;
  public static final int DEFAULT_MAX_HEIGHT = 4;

  private int maxHeight;

  /**
   * Create an empty genetic family tree pane.
   *
   * @param config The app’s config.
   */
  public GeneticFamilyTreePane(final @NotNull Config config) {
    super(config);
  }

  /**
   * Set the maximum number of levels to display above the center widget.
   *
   * @param height The new maximum height.
   */
  public void setMaxHeight(int height) {
    if (height < MIN_ALLOWED_HEIGHT || height > MAX_ALLOWED_HEIGHT)
      throw new IllegalArgumentException("invalid max height");
    this.maxHeight = height;
  }

  @Override
  protected PersonWidget buildTree(final @NotNull FamilyTree familyTree, final @NotNull Person targettedPerson) {
    final PersonWidget root = this.buildParentsTree(familyTree, targettedPerson);
    this.drawChildToParentsLines();
    this.buildChildrenAndSiblingsAndPartnersTree(root, familyTree);
    return root;
  }

  /**
   * Create and position widgets for each ascendant of the current root.
   *
   * @param familyTree Tree to draw.
   * @return The widget for the tree’s root.
   */
  private PersonWidget buildParentsTree(final @NotNull FamilyTree familyTree, final @NotNull Person targettedPerson) {
    final PersonWidget root = this.createPersonWidget(targettedPerson, null, false, true, familyTree);

    final List<List<PersonWidget>> levels = new ArrayList<>();
    levels.add(List.of(root));
    // Build levels breadth-first
    for (int treeLevel = 1; treeLevel <= this.maxHeight; treeLevel++) {
      final List<PersonWidget> widgets = new ArrayList<>();
      levels.add(widgets);
      for (final PersonWidget childWidget : levels.get(treeLevel - 1)) {
        if (childWidget != null) {
          final Optional<Person> childOpt = childWidget.person();
          if (childOpt.isPresent()) {
            final Person child = childOpt.get();
            final List<Person> parents = child.getGeneticParents().stream()
                .sorted(Person.birthDateThenNameComparator(false))
                .toList();
            final Person parent1 = !parents.isEmpty() ? parents.get(0) : null;
            final Person parent2 = parents.size() > 1 ? parents.get(1) : null;
            final Set<Person> children = new HashSet<>();
            if (parent1 != null)
              children.addAll(parent1.getPartnersAndGeneticChildren().get(Optional.ofNullable(parent2)));
            final PersonWidget parent1Widget = this.createParentWidget(familyTree, parent1, child, children, treeLevel);
            final PersonWidget parent2Widget = this.createParentWidget(familyTree, parent2, child, children, treeLevel);
            widgets.add(parent1Widget);
            widgets.add(parent2Widget);
            childWidget.setParentWidget1(parent1Widget);
            childWidget.setParentWidget2(parent2Widget);
            continue;
          }
        }
        widgets.add(null);
        widgets.add(null);
      }
      if (widgets.stream().allMatch(Objects::isNull)) {
        levels.remove(treeLevel);
        break;
      }
    }

    // Widgets positioning
    for (int level = levels.size() - 1, row = 0; level >= 0; level--, row++) {
      final List<PersonWidget> widgets = levels.get(level);
      final double y = VGAP + (VGAP + PersonWidget.HEIGHT) * row;
      final double gap = (Math.pow(2, row) - 1) * (PersonWidget.WIDTH + HGAP);
      final double betweenCardsGap = HGAP + gap;
      double x = HGAP + 0.5 * gap;
      for (final PersonWidget widget : widgets) {
        if (widget != null) {
          widget.setLayoutX(x);
          widget.setLayoutY(y);
        }
        x += PersonWidget.WIDTH + betweenCardsGap;
      }
    }

    return root;
  }

  private PersonWidget createParentWidget(
      final @NotNull FamilyTree familyTree,
      final Person parent,
      final Person displayedChild,
      final @NotNull Set<Person> children,
      int treeLevel
  ) {
    final List<ChildInfo> childrenInfos = children.stream().map(child -> {
      if (parent == null) return new ChildInfo(child, ParentalRelationType.BIOLOGICAL_PARENT);
      return new ChildInfo(child, child.getParentType(parent).orElse(ParentalRelationType.BIOLOGICAL_PARENT));
    }).toList();
    final PersonWidget personWidget = this.createPersonWidget(
        parent,
        childrenInfos,
        parent != null && this.hasHiddenRelatives(parent, treeLevel, children, 1),
        false,
        familyTree
    );
    if (parent != null)
      personWidget.getStyleClass().add(CSS_CLASSES.get(displayedChild.getParentType(parent).orElseThrow()));
    return personWidget;
  }

  private boolean hasHiddenRelatives(
      final @NotNull Person parent,
      int treeLevel,
      final Set<Person> children,
      int minPartners
  ) {
    final var partnersAndGeneticChildren = parent.getPartnersAndGeneticChildren();
    // Any hidden parents
    return treeLevel == this.maxHeight && !parent.getGeneticParents().isEmpty() ||
        // Any hidden partners
        partnersAndGeneticChildren.keySet().stream()
            .filter(Optional::isPresent)
            .count() > minPartners ||
        // Any hidden children
        treeLevel > 1 && children != null && children.size() > 1;
  }

  /**
   * Draw lines from each person to their respective parents.
   */
  private void drawChildToParentsLines() {
    this.personWidgets().forEach(personWidget -> {
      final Optional<PersonWidget> parent1 = personWidget.parentWidget1();
      final Optional<PersonWidget> parent2 = personWidget.parentWidget2();
      if (parent1.isPresent() && parent2.isPresent()) {
        final double cx = personWidget.getLayoutX() + PersonWidget.WIDTH / 2.0;
        final double cy = personWidget.getLayoutY();
        final double p1x = parent1.get().getLayoutX() + PersonWidget.WIDTH;
        final double p1y = parent1.get().getLayoutY() + PersonWidget.HEIGHT / 2.0;
        final double p2x = parent2.get().getLayoutX();
        final double p2y = parent2.get().getLayoutY() + PersonWidget.HEIGHT / 2.0;
        // Insert at the start to render them underneath the cards
        this.drawLine(p1x, p1y, p2x, p2y); // Line between parents
        this.drawLine(cx, cy, (p1x + p2x) / 2, p1y); // Child to parents line
      }
    });
  }

  /**
   * Build the tree of partners, siblings and direct children of the tree’s root.
   *
   * @param root       Tree’s root.
   * @param familyTree Tree being drawn.
   */
  private void buildChildrenAndSiblingsAndPartnersTree(
      final @NotNull PersonWidget root,
      final @NotNull FamilyTree familyTree
  ) {
    // Get root’s partners sorted by birth date and name
    final Person rootPerson = root.person().orElseThrow();

    final double personW = PersonWidget.WIDTH;
    final int personH = PersonWidget.HEIGHT;
    final double rootX = root.getLayoutX();
    final double rootY = root.getLayoutY();

    final Set<Person> siblings = rootPerson.getSameGeneticParentsSiblings();
    final List<Person> olderSiblings = siblings.stream()
        .filter(p -> Person.birthDateThenNameComparator(false).compare(p, rootPerson) < 0)
        .sorted(Person.birthDateThenNameComparator(false))
        .toList();
    final List<Person> youngerSiblings = siblings.stream()
        .filter(p -> Person.birthDateThenNameComparator(false).compare(p, rootPerson) >= 0)
        .sorted(Person.birthDateThenNameComparator(true))
        .toList();

    final Map<Optional<Person>, List<Person>> childrenMap = rootPerson.getPartnersAndGeneticChildren()
        .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> {
          final List<Person> list = new ArrayList<>(e.getValue());
          list.sort(Person.birthDateThenNameComparator(false));
          return list;
        }));

    final List<Optional<Person>> partners = childrenMap.keySet().stream()
        .sorted(Person.optionalBirthDateThenNameComparator())
        .toList();

    // Render older siblings to the left of root
    double x = rootX;
    for (int i = olderSiblings.size() - 1; i >= 0; i--) {
      x -= personW + HGAP;
      this.drawSibling(familyTree, root, olderSiblings.get(i), x, rootX, rootY);
    }

    x = rootX;
    // Render partners with enough space for direct children
    final int partnersNb = partners.size();
    for (int i = 0; i < partnersNb; i++) {
      final Optional<Person> partnerOpt = partners.get(i);
      x += HGAP + personW;

      final List<Person> children = childrenMap.get(partnerOpt);
      final boolean hasHiddenParents = partnerOpt.map(Person::hasAnyParents).orElse(false);
      final List<ChildInfo> childInfo = new LinkedList<>();
      final Person partner = partnerOpt.orElse(null);
      for (final Person child : children) {
        final var parentType = Optional.ofNullable(partner)
            .flatMap(child::getParentType)
            .orElse(ParentalRelationType.BIOLOGICAL_PARENT);
        childInfo.add(new ChildInfo(child, parentType));
      }
      final PersonWidget partnerWidget = this.createPersonWidget(partner, childInfo, hasHiddenParents, false, familyTree);
      partnerWidget.setLayoutX(x);
      partnerWidget.setLayoutY(rootY);

      // Draw line from root to partner
      final double lineY = rootY + personH / 2.0 + (partnersNb - i * LINE_GAP);
      this.drawLine(rootX + personW, lineY, x, lineY);

      final int childrenNb = children.size();
      final double halfChildrenWidth = (childrenNb * personW + (childrenNb - 1) * HGAP) / 2;
      // Compute gap to fit children of this partner and the next
      double nextXOffset = Math.max(0, halfChildrenWidth - personW);
      if (i < partnersNb - 1) {
        final int nextChildrenNb = childrenMap.get(partners.get(i + 1)).size();
        nextXOffset += (nextChildrenNb * personW + (nextChildrenNb - 1) * HGAP) / 2;
      }

      // Render children
      final double rightParentX = partnerWidget.getLayoutX() - HGAP / 2.0;
      double childX = x - halfChildrenWidth - HGAP / 2.0;
      final double childY = rootY + VGAP + personH;
      for (final Person child : children) {
        final boolean hasHiddenChildren = this.hasHiddenRelatives(child, 0, null, 0);
        final PersonWidget childWidget = this.createPersonWidget(child, null, hasHiddenChildren, false, familyTree);
        childWidget.getStyleClass().add(CSS_CLASSES.get(child.getParentType(rootPerson).orElseThrow()));
        childWidget.setLayoutX(childX);
        childWidget.setLayoutY(childY);
        childWidget.setParentWidget1(root);
        childWidget.setParentWidget2(partnerWidget);
        final double lineX = childX + personW / 2;
        this.drawLine(lineX, childY, lineX, childY - VGAP / 2.0);
        this.drawLine(lineX, childY - VGAP / 2.0, rightParentX, childY - VGAP / 2.0);
        childX += personW + HGAP;
      }
      if (!children.isEmpty())
        this.drawLine(rightParentX, childY - VGAP / 2.0, rightParentX, lineY);
      x += nextXOffset;
    }

    // Render younger sibling to the right of root’s partners
    for (int i = youngerSiblings.size() - 1; i >= 0; i--) {
      x += HGAP + personW;
      this.drawSibling(familyTree, root, youngerSiblings.get(i), x, rootX, rootY);
    }
  }

  private void drawSibling(
      final @NotNull FamilyTree familyTree,
      final @NotNull PersonWidget root,
      final @NotNull Person sibling,
      double x,
      double rootX,
      double rootY
  ) {
    final double personW = PersonWidget.WIDTH;
    final boolean hasHiddenChildren = this.hasHiddenRelatives(sibling, 0, null, 0);
    final PersonWidget widget = this.createPersonWidget(sibling, null, hasHiddenChildren, false, familyTree);
    widget.setLayoutX(x);
    widget.setLayoutY(rootY);
    widget.setParentWidget1(root.parentWidget1().orElse(null));
    widget.setParentWidget2(root.parentWidget2().orElse(null));
    final double lineX = x + personW / 2;
    final double lineY = rootY - VGAP / 2.0;
    this.drawLine(lineX, rootY, lineX, lineY);
    this.drawLine(lineX, lineY, rootX + personW / 2.0, lineY);
  }
}
