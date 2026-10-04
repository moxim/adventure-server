package com.pdg.adventure.view.support;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Comparator;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.server.support.VariableProvider;

/** The variable names an author may pick in an editor: the adventure's defined variables plus the engine's own. */
public final class VariableChoices {

    private static final Comparator<String> ORDER =
            String.CASE_INSENSITIVE_ORDER.thenComparing(Comparator.naturalOrder());

    private VariableChoices() {
    }

    /** Evaluated afresh on every call, so a variable defined a moment ago is included. */
    public static Collection<String> of(AdventureData anAdventure) {
        SortedSet<String> names = new TreeSet<>(ORDER);
        names.add(VariableProvider.VISITED_VARIABLE_NAME);
        if (anAdventure != null) {
            names.addAll(anAdventure.variableNames());
        }
        return names;
    }
}
