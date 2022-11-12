package com.pdg.adventure.server.support;

import com.pdg.adventure.server.api.Command;
import com.pdg.adventure.server.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandChain;
import com.pdg.adventure.server.parser.CommandDescription;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.vocabulary.Vocabulary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandProvider {
    private final Map<CommandDescription, CommandChain> availableCommands;

    public CommandProvider() {
        availableCommands = new HashMap<>();
    }

    public void addCommand(Command aCommand) {
        CommandChain chain = availableCommands.get(aCommand.getDescription());
        if (chain == null) {
            chain = new CommandChain();
        }
        chain.addCommand(aCommand);
        availableCommands.put(aCommand.getDescription(), chain);
    }

    public void removeCommand(Command aCommand) {
        availableCommands.remove(aCommand.getDescription());
    }

    public boolean hasCommand(CommandDescription aCommand) {
        return availableCommands.containsKey(aCommand);
    }

    public List<Command> getCommands() {
        List<Command> commands = new ArrayList<>();
        for (CommandChain chain : availableCommands.values()) {
            commands.addAll(chain.getCommands());
        }
        return commands;
    }

    public ExecutionResult applyCommand(CommandDescription aCommandDescription) {
        ExecutionResult result = new CommandExecutionResult();
        CommandChain commandChain = find(aCommandDescription);
        if (commandChain != null) {
            return commandChain.execute();
        }
        return result;
    }

    private CommandChain find(CommandDescription aCommandDescription) {
        String verb = aCommandDescription.getVerb();
        String adjective = aCommandDescription.getAdjective();
        String noun = aCommandDescription.getNoun();

        for (Map.Entry<CommandDescription, CommandChain> entry : availableCommands.entrySet()) {
            CommandDescription description = entry.getKey();
            if (description.getVerb().equals(verb) &&
                    (description.getNoun().equals(noun) || Vocabulary.EMPTY_STRING.equals(noun)) &&
                    (description.getAdjective().equals(adjective) || Vocabulary.EMPTY_STRING.equals(adjective))) {
                return entry.getValue();
            }
        }
        return null;
    }

    public boolean hasVerb(String aVerb) {
        for (CommandDescription description : availableCommands.keySet()) {
            if (description.getVerb().equals(aVerb)) {
                return true;
            }
        }
        ;
        return false;
    }
}
