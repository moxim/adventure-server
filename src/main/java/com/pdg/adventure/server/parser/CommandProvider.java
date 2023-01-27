package com.pdg.adventure.server.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pdg.adventure.api.*;
import com.pdg.adventure.server.vocabulary.Vocabulary;

public class CommandProvider implements Ided {
    private final Map<CommandDescription, CommandChain> availableCommands;
    private String id;

    public CommandProvider() {
        availableCommands = new HashMap<>();
    }

    public Map<CommandDescription, CommandChain> getAvailableCommands() {
        return availableCommands;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String anId) {
        id = anId;
    }

    public void addCommand(Command aCommand) {
        CommandChain chain = availableCommands.get(aCommand.getDescription());
        if (chain == null) {
            chain = new GenericCommandChain();
        }
        chain.addCommand(aCommand);
        availableCommands.put(aCommand.getDescription(), chain);
    }

    public void removeCommand(Command aCommand) {
        availableCommands.remove(aCommand.getDescription());
    }

    public boolean hasCommand(GenericCommandDescription aCommand) {
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
        List<CommandChain> commandChain = getMatchingCommandChain(aCommandDescription);
        for (CommandChain chain : commandChain) {
            result = chain.execute();
            result.setCommandHasMatched();
        }
        return result;
    }

    public List<CommandChain> getMatchingCommandChain(CommandDescription aCommandDescription) {
        String verb = aCommandDescription.getVerb();
        String adjective = aCommandDescription.getAdjective();
        String noun = aCommandDescription.getNoun();

        List<CommandChain> result = new ArrayList<>();
        for (Map.Entry<CommandDescription, CommandChain> entry : availableCommands.entrySet()) {
            CommandDescription description = entry.getKey();
            if (description.getVerb().equals(verb) &&
                    (description.getNoun().equals(noun) || Vocabulary.EMPTY_STRING.equals(noun)) &&
                    (description.getAdjective().equals(adjective) || Vocabulary.EMPTY_STRING.equals(adjective))) {
                result.add(entry.getValue());
            }
        }

        return result;
    }

    public boolean hasVerb(String aVerb) {
        for (CommandDescription description : availableCommands.keySet()) {
            if (description.getVerb().equals(aVerb)) {
                return true;
            }
        }
        return false;
    }
}
