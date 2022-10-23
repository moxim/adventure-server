package com.pdg.adventure.server.support;

import com.pdg.adventure.server.api.Command;
import com.pdg.adventure.server.parser.CommandChain;
import com.pdg.adventure.server.parser.CommandDescription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandProvider {
    private final Map<String, CommandChain> availableCommands;

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

    public boolean hasCommand(String aCommand) {
        return availableCommands.containsKey(aCommand);
    }

    public List<Command> getCommands() {
        List<Command> commands = new ArrayList<>();
        for (CommandChain chain : availableCommands.values()) {
            commands.addAll(chain.getCommands());
        }
        return commands;
    }

    public boolean applyCommand(CommandDescription aCommand) {
        CommandChain command = availableCommands.get(aCommand.getDescription());
        if (command != null) {
            return command.execute();
        }
        return false;
    }
}
