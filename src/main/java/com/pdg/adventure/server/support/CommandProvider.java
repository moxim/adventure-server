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

    public boolean hasCommand(Command aCommand) {
        return availableCommands.containsKey(aCommand.getDescription());
    }

    public List<Command> getCommands() {
        return new ArrayList<>(availableCommands.values());
    }

    public boolean couldApplyCommand(Command aCommand) {
        if (availableCommands.containsKey(aCommand.getDescription())) {
            aCommand.execute();
            return true;
        }
        return false;
    }
}
