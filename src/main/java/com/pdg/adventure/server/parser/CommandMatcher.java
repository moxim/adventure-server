package com.pdg.adventure.server.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.pdg.adventure.api.CommandChain;
import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.api.Containable;

/**
 * Utility class for matching commands across a list of {@link Containable} items.
 * Single-provider matching is handled directly by {@link GenericCommandProvider}
 * (or delegated to it via {@link CommandHandler}).
 */
public class CommandMatcher {

    private CommandMatcher() {}

    /**
     * Collects matching command chains from every item in the list by calling each
     * item's own {@link Containable#getMatchingCommandChain} (which may include its children).
     */
    public static Collection<CommandChain> getMatchingCommands(List<Containable> aContainableList,
                                                               CommandDescription aCommandDescription) {
        Collection<CommandChain> result = new ArrayList<>();
        for (Containable item : aContainableList) {
            result.addAll(item.getMatchingCommandChain(aCommandDescription));
        }
        return result;
    }
}
