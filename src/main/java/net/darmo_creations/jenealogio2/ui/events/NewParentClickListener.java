package net.darmo_creations.jenealogio2.ui.events;

import net.darmo_creations.jenealogio2.ui.ChildInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A function that takes in some {@link ChildInfo} and is called when a parent should be created for a given person.
 */
public interface NewParentClickListener {
  void onClick(@NotNull List<ChildInfo> childInfo);
}
