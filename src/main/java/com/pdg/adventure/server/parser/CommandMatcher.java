package com.pdg.adventure.server.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.pdg.adventure.api.CommandChain;
import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.api.Containable;

public class CommandMatcher {

    public List<CommandChain> getMatchingCommands(final GenericCommandProvider aCommandProvider,
                                                  final CommandDescription aCommandDescription) {
        return aCommandProvider.getMatchingCommandChain(aCommandDescription);

    }

    public Collection<CommandChain> getMatchingCommands(final List<Containable> aContainableList,
                                                        final CommandDescription aCommandDescription) {
        Collection<CommandChain> result = new ArrayList<>();
        for (Containable item : aContainableList) {
            final List<CommandChain> commandChain = item.getMatchingCommandChain(aCommandDescription);
            result.addAll(commandChain);
        }
        return result;
    }

    public Collection<CommandChain> getMatchingCommandsFromCommandProvider(final List<Containable> aContainableList,
                                                                           final CommandDescription aCommandDescription) {
        Collection<CommandChain> result = new ArrayList<>();
        for (Containable item : aContainableList) {
            final List<CommandChain> command = item.getCommandProvider().getMatchingCommands(aCommandDescription);
            result.addAll(command);
        }
        return result;
    }
}
