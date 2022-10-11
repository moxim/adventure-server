package com.pdg.adventure.server.support;

import com.pdg.adventure.server.api.Command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandProvider {
    private final Map<String, Command> availableCommands;

    public CommandProvider() {
        availableCommands = new HashMap<>();
    }

    public void addCommand(Command aCommand) {
        availableCommands.put(aCommand.getDescription(), aCommand);
    }

    public void removeCommand(Command aCommand) {
        availableCommands.remove(aCommand.getDescription());
    }

    public boolean hasCommand(String aCommand) {
        return availableCommands.containsKey(aCommand);
    }

    public List<Command> getCommands() {
        return new ArrayList<>(availableCommands.values());
    }

    public boolean applyCommand(String aCommand) {
        Command command = availableCommands.get(aCommand);
        if (command != null) {
            return command.execute();
        }
        return false;
    }
}
