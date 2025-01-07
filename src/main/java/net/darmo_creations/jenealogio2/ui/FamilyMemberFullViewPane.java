package net.darmo_creations.jenealogio2.ui;

import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.ui.components.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.*;

import static net.darmo_creations.jenealogio2.model.ParentalRelationType.*;

public class FamilyMemberFullViewPane extends PersonTreeView {
  public FamilyMemberFullViewPane(final @NotNull Config config) {
    super(config, ParentalRelationType.values());
  }

  @Override
  protected PersonWidget buildTree(final @NotNull FamilyTree familyTree, final @NotNull Person targettedPerson) {
    final PersonWidget root = this.createPersonWidget(
        targettedPerson,
        null,
        false,
        true,
        familyTree
    );
    this.drawParents(root, familyTree);
    this.drawSiblings(root, familyTree);
    this.drawDonatedChildren(root, familyTree);
    this.drawPartnersAndChildren(root, familyTree);
    return root;
  }

  private void drawParents(
      final @NotNull PersonWidget root,
      final @NotNull FamilyTree familyTree
  ) {
    final Person targettedPerson = root.person().orElseThrow();
    final var biologicalParents = this.createParentWidgetsForType(targettedPerson, BIOLOGICAL_PARENT, familyTree);
    final var nonBiologicalParents = this.createParentWidgetsForType(targettedPerson, NON_BIOLOGICAL_PARENT, familyTree);
    final var eggDonor = this.createParentWidgetForType(targettedPerson, EGG_DONOR, familyTree);
    final var spermDonor = this.createParentWidgetForType(targettedPerson, SPERM_DONOR, familyTree);
    final var surrogateParent = this.createParentWidgetForType(targettedPerson, SURROGATE_PARENT, familyTree);
    final var adoptiveParents = this.createParentWidgetsForType(targettedPerson, ADOPTIVE_PARENT, familyTree);
    final var fosterParents = this.createParentWidgetsForType(targettedPerson, FOSTER_PARENT, familyTree);
    final var godparents = this.createParentWidgetsForType(targettedPerson, GODPARENT, familyTree);

    final double rootX = root.getLayoutX();
    final double rootY = root.getLayoutY();
    final double rootMiddleX = rootX + PersonWidget.WIDTH / 2.0;
    final double bottomY = rootY - 4 * VGAP;

    double x1 = rootMiddleX + HGAP / 2.0;
    final double yOffset1 = 0;
    boolean anyGeneticParent = false;

    this.drawParentWidgets(
        biologicalParents,
        BIOLOGICAL_PARENT,
        x1,
        bottomY,
        rootMiddleX,
        rootY,
        false,
        yOffset1
    );
    if (!biologicalParents.isEmpty()) {
      x1 += HGAP + PersonWidget.WIDTH;
      anyGeneticParent = true;
    }

    if (eggDonor.isPresent()) {
      this.drawParentWidgets(
          List.of(eggDonor.get()),
          EGG_DONOR,
          x1,
          bottomY,
          rootMiddleX,
          rootY,
          !biologicalParents.isEmpty(),
          yOffset1
      );
      x1 += HGAP + PersonWidget.WIDTH;
      anyGeneticParent = true;
    }

    if (spermDonor.isPresent()) {
      this.drawParentWidgets(
          List.of(spermDonor.get()),
          SPERM_DONOR,
          x1,
          bottomY,
          rootMiddleX,
          rootY,
          !biologicalParents.isEmpty() || eggDonor.isPresent(),
          yOffset1
      );
      x1 += HGAP + PersonWidget.WIDTH;
      anyGeneticParent = true;
    }

    if (surrogateParent.isPresent()) {
      this.drawParentWidgets(
          List.of(surrogateParent.get()),
          SURROGATE_PARENT,
          x1,
          bottomY,
          rootMiddleX + (anyGeneticParent ? LINE_GAP : 0),
          rootY,
          false,
          0
      );
      anyGeneticParent = true;
    }

    final double xOffset = anyGeneticParent ? LINE_GAP : 0;
    double x2 = rootMiddleX;
    double targetX = rootMiddleX - xOffset;
    double yOffset2 = 0;

    x2 -= HGAP / 2.0 + PersonWidget.WIDTH + xOffset;

    this.drawParentWidgets(
        nonBiologicalParents,
        NON_BIOLOGICAL_PARENT,
        x2,
        bottomY,
        targetX,
        rootY,
        false,
        0
    );
    if (!nonBiologicalParents.isEmpty()) {
      x2 -= HGAP + PersonWidget.WIDTH;
      targetX -= LINE_GAP;
    }

    this.drawParentWidgets(
        adoptiveParents,
        ADOPTIVE_PARENT,
        x2,
        bottomY,
        targetX,
        rootY,
        false,
        yOffset2
    );
    if (!adoptiveParents.isEmpty()) {
      x2 -= HGAP + PersonWidget.WIDTH;
      yOffset2 += LINE_GAP;
      targetX -= LINE_GAP;
    }

    this.drawParentWidgets(
        fosterParents,
        FOSTER_PARENT,
        x2,
        bottomY,
        targetX,
        rootY,
        false,
        yOffset2
    );
    if (!fosterParents.isEmpty()) {
      x2 -= HGAP + PersonWidget.WIDTH;
      yOffset2 += LINE_GAP;
      targetX -= LINE_GAP;
    }

    this.drawParentWidgets(
        godparents,
        GODPARENT,
        x2,
        bottomY,
        targetX,
        rootY,
        false,
        yOffset2
    );
  }

  private void drawParentWidgets(
      final @NotNull List<PersonWidget> widgets,
      @NotNull ParentalRelationType parentType,
      double x,
      double y,
      double targetX,
      double targetY,
      boolean linkToSameLevel,
      double bottomLineOffset
  ) {
    if (widgets.isEmpty()) return;

    final String cssClass = CSS_CLASSES.get(parentType);
    final int count = widgets.size();

    final double lineX;
    if (linkToSameLevel)
      lineX = targetX < x ? x - HGAP : x + PersonWidget.WIDTH + HGAP;
    else
      lineX = targetX < x ? x - HGAP / 2.0 : x + PersonWidget.WIDTH + HGAP / 2.0;

    for (int i = 0; i < count; i++) {
      final PersonWidget widget = widgets.get(i);
      widget.setLayoutX(x);
      final double y2 = y - i * VGAP - (i + 1) * PersonWidget.HEIGHT;
      widget.setLayoutY(y2);
      // Small line between widget and vertical line
      final double yH = y2 + PersonWidget.HEIGHT / 2.0;
      this.drawLine(
          lineX,
          yH,
          targetX < x ? x : x + PersonWidget.WIDTH,
          yH,
          cssClass
      );
    }

    if (!linkToSameLevel) {
      // Vertical line from bottom to top widgets
      final double lineY = y + VGAP + bottomLineOffset;
      this.drawLine(
          lineX,
          lineY,
          lineX,
          y - (count - 0.5) * PersonWidget.HEIGHT - (count - 1) * VGAP,
          cssClass
      );
      // Horizontal line to x0
      if (lineX != targetX) this.drawLine(lineX, lineY, targetX, lineY, cssClass);
      // Vertical line to x0
      this.drawLine(targetX, lineY, targetX, targetY, cssClass);
    }
  }

  private Optional<PersonWidget> createParentWidgetForType(
      final @NotNull Person targettedPerson,
      @NotNull ParentalRelationType parentType,
      final @NotNull FamilyTree familyTree
  ) {
    return targettedPerson.parents(parentType).stream()
        .findFirst()
        .map(p -> this.createParentWidget(p, targettedPerson, CSS_CLASSES.get(parentType), familyTree));
  }

  private List<PersonWidget> createParentWidgetsForType(
      final @NotNull Person targettedPerson,
      @NotNull ParentalRelationType parentType,
      final @NotNull FamilyTree familyTree
  ) {
    return targettedPerson.parents(parentType).stream()
        .sorted(Person.birthDateThenNameComparator(false))
        .map(p -> this.createParentWidget(p, targettedPerson, CSS_CLASSES.get(parentType), familyTree))
        .toList();
  }

  private PersonWidget createParentWidget(
      final @NotNull Person person,
      final @NotNull Person targettedPerson,
      @NotNull String cssClass,
      final @NotNull FamilyTree familyTree
  ) {
    final boolean showMoreIcon = person.hasAnyParents() ||
        // Any children that are not the root?
        !Sets.difference(Sets.merge(person.children()), Set.of(targettedPerson)).isEmpty() ||
        !person.getPartnersAndChildren().isEmpty() ||
        !person.getSiblings().isEmpty();
    final PersonWidget widget = this.createPersonWidget(
        person,
        null,
        showMoreIcon,
        false,
        familyTree
    );
    widget.getStyleClass().add(cssClass);
    return widget;
  }

  private void drawSiblings(
      final @NotNull PersonWidget root,
      final @NotNull FamilyTree familyTree
  ) {
    final Person targettedPerson = root.person().orElseThrow();
    final Set<Person> parents = targettedPerson.parents()
        .values()
        .stream()
        .reduce(new HashSet<>(), (s1, s2) -> {
          s1.addAll(s2);
          return s1;
        });
    final var allSiblings = targettedPerson.getSiblings().stream()
        // Sort by descending number of shared parents with targettedPerson
        .sorted(Comparator.comparing(p -> {
          final Set<Person> s = new HashSet<>(p.parents());
          s.retainAll(parents);
          return -s.size();
        }))
        .map(p -> new Pair<>(p.parents(), p.children().stream()
            .sorted(Person.birthDateThenNameComparator(true))
            .toList()))
        .toList();

    final double rootX = root.getLayoutX();
    final double rootY = root.getLayoutY();

    double x = rootX;
    final double lineY = rootY - VGAP;
    for (final var siblings : allSiblings) {
      for (final Person sibling : siblings.right()) {
        x -= HGAP + PersonWidget.WIDTH;
        final PersonWidget widget = this.createPersonWidget(
            sibling,
            null,
            true,
            false,
            familyTree
        );
        widget.setLayoutX(x);
        widget.setLayoutY(rootY);
        final double lineX = x + PersonWidget.WIDTH / 2.0;
        this.drawLine(lineX, rootY, lineX, lineY);
      }
    }
    if (!allSiblings.isEmpty()) {
      final double lineX = rootX + LINE_GAP;
      this.drawLine(lineX, lineY, x + PersonWidget.WIDTH / 2.0, lineY);
      this.drawLine(lineX, rootY, lineX, lineY);
    }
  }

  private void drawDonatedChildren(
      final @NotNull PersonWidget root,
      final @NotNull FamilyTree familyTree
  ) {
    final Person targettedPerson = root.person().orElseThrow();

    final double rootX = root.getLayoutX();
    final double rootY = root.getLayoutY();
    final double rootMiddleX = rootX + PersonWidget.WIDTH / 2.0;

    double x = rootX - (PersonWidget.WIDTH + HGAP) / 2.0;
    final double y = rootY + 4 * VGAP + PersonWidget.HEIGHT;
    double lineX = rootMiddleX;
    final double lineY = rootY + PersonWidget.HEIGHT;
    double lineYOffset = 0;
    final ParentalRelationType[] relationTypes = {SURROGATE_PARENT, EGG_DONOR, SPERM_DONOR};
    for (int i = 0; i < relationTypes.length; i++) {
      final var relationType = relationTypes[i];
      this.drawDonatedChildrenType(
          targettedPerson,
          relationType,
          x,
          y,
          lineX,
          lineY,
          lineYOffset,
          familyTree
      );
      if (!targettedPerson.children(relationType).isEmpty()) {
        x -= PersonWidget.WIDTH + HGAP;
        lineX -= LINE_GAP;
        if (i > 0)
          lineYOffset += LINE_GAP;
      }
    }
  }

  private void drawDonatedChildrenType(
      final @NotNull Person targettedPerson,
      @NotNull ParentalRelationType childType,
      double x,
      double y,
      double targetX,
      double targetY,
      double topLineOffset,
      final @NotNull FamilyTree familyTree
  ) {
    final List<Person> children = targettedPerson.children(childType).stream()
        .sorted(Person.birthDateThenNameComparator(false))
        .toList();
    final double lineX = x + PersonWidget.WIDTH + HGAP / 2.0;
    final int count = children.size();
    final String cssClass = CSS_CLASSES.get(childType);

    for (int i = 0; i < children.size(); i++) {
      final Person child = children.get(i);
      final boolean showMoreIcon =
          // Any parents that are not the root?
          !Sets.difference(Sets.merge(child.parents()), Set.of(targettedPerson)).isEmpty() ||
              !child.getPartnersAndChildren().isEmpty() ||
              !child.getSiblings().isEmpty();
      final PersonWidget widget = this.createPersonWidget(
          child,
          null,
          showMoreIcon,
          false,
          familyTree
      );
      widget.getStyleClass().add(cssClass);
      widget.setLayoutX(x);
      final double widgetY = y + i * (VGAP + PersonWidget.HEIGHT);
      widget.setLayoutY(widgetY);
      // Horizontal line to vertical line
      final double lineY = widgetY + PersonWidget.HEIGHT / 2.0;
      this.drawLine(x + PersonWidget.WIDTH, lineY, lineX, lineY, cssClass);
    }

    if (!children.isEmpty()) {
      // Vertical line from bottom to top widgets
      final double horizY = y - VGAP - topLineOffset;
      this.drawLine(
          lineX,
          horizY,
          lineX,
          y + (count - 0.5) * PersonWidget.HEIGHT + (count - 1) * VGAP,
          cssClass
      );
      // Horizontal line
      if (lineX != targetX)
        this.drawLine(lineX, horizY, targetX, horizY, cssClass);
      // Vertical line from horizontal to root
      this.drawLine(targetX, horizY, targetX, targetY, cssClass);
    }
  }

  private void drawPartnersAndChildren(
      final @NotNull PersonWidget root,
      final @NotNull FamilyTree familyTree
  ) {
    final Person targettedPerson = root.person().orElseThrow();

    final double rootX = root.getLayoutX();
    final double rootY = root.getLayoutY();
    double x = rootX;
    final double lineXStart = rootX + PersonWidget.WIDTH;
    double lineY = rootY + PersonWidget.HEIGHT / 2.0;

    final Function<Person.FamilyUnit, Optional<Person>> getYoungestPartner =
        familyUnit -> familyUnit.parents()
            .stream()
            .min(Person.birthDateThenNameComparator(false));
    final var allPartnersAndChildren = targettedPerson.getPartnersAndChildren().stream()
        // Sort by birth date and name of the youngest partner in each family unit
        .sorted((fu1, fu2) -> Person.optionalBirthDateThenNameComparator()
            .compare(getYoungestPartner.apply(fu1), getYoungestPartner.apply(fu2)))
        .toList();

    for (final var partnersAndChildren : allPartnersAndChildren) {
      final List<Person> partners = partnersAndChildren.parents()
          .stream()
          .sorted(Person.birthDateThenNameComparator(false))
          .toList();
      final List<Person> children = partnersAndChildren.children()
          .stream()
          .sorted(Person.birthDateThenNameComparator(false))
          .toList();

      x += PersonWidget.WIDTH + HGAP;

      final Set<Person> visibleParents = new HashSet<>(partners);
      visibleParents.add(targettedPerson);
      this.drawChildrenWidgets(
          targettedPerson,
          visibleParents,
          children,
          x,
          rootY + PersonWidget.HEIGHT + 4 * VGAP,
          lineY,
          familyTree
      );

      if (partners.isEmpty()) {
        final List<ChildInfo> childInfo = children.stream()
            .map(c -> new ChildInfo(c, BIOLOGICAL_PARENT))
            .toList();
        final PersonWidget widget = this.createPersonWidget(
            null,
            childInfo,
            false,
            false,
            familyTree
        );
        widget.setLayoutX(x);
        widget.setLayoutY(rootY);
      } else {
        final int partnersCount = partners.size();
        for (int i = 0; i < partnersCount; i++) {
          final Person partner = partners.get(i);
          final boolean showMoreIcon = partner.hasAnyParents() ||
              !partner.getSiblings().isEmpty() ||
              // Any children that are not shown?
              !Sets.difference(Sets.merge(partner.parents()), partnersAndChildren.children()).isEmpty();
          final PersonWidget widget = this.createPersonWidget(
              partner,
              null,
              showMoreIcon,
              false,
              familyTree
          );
          widget.setLayoutX(x);
          widget.setLayoutY(rootY);
          if (i < partnersCount - 1)
            x += PersonWidget.WIDTH + HGAP;
          else
            x += 2 * HGAP;
        }
      }

      // Line between root and partners
      this.drawLine(lineXStart, lineY, x, lineY);

      lineY -= LINE_GAP;
    }
  }

  private void drawChildrenWidgets(
      final @NotNull Person targettedPerson,
      final @NotNull Set<Person> visibleParents,
      final @NotNull List<Person> children,
      double x,
      double y,
      double y0,
      final @NotNull FamilyTree familyTree
  ) {
    final double lineX = x - HGAP / 2.0;
    double widgetY = y;
    for (final Person child : children) {
      final boolean showMoreIcon = !Sets.difference(Sets.merge(child.parents()), visibleParents).isEmpty() ||
          !child.getSiblings().isEmpty() ||
          !child.getPartnersAndChildren().isEmpty();
      final PersonWidget widget = this.createPersonWidget(
          child,
          null,
          showMoreIcon,
          false,
          familyTree
      );
      widget.setLayoutX(x);
      widget.setLayoutY(widgetY);
      widget.getStyleClass().add(CSS_CLASSES.get(child.getParentType(targettedPerson).orElseThrow()));
      final double lineY = widgetY + PersonWidget.HEIGHT / 2.0;
      // Line from vertical line to widget
      this.drawLine(lineX, lineY, x, lineY);
      widgetY += PersonWidget.HEIGHT + VGAP;
    }
    if (!children.isEmpty()) {
      final int count = children.size();
      // Vertical line from bottom widget to parents’ line
      this.drawLine(lineX, y0, lineX, y + (count - 0.5) * PersonWidget.HEIGHT + (count - 1) * VGAP);
    }
  }
}
