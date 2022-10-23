package com.pdg.adventure.server.parser;

import com.pdg.adventure.server.api.Command;

import java.util.ArrayList;
import java.util.List;

public class CommandChain {
    private final List<Command> commands;

    public CommandChain() {
        commands = new ArrayList<>();
    }

    public void addCommand(Command aCommand) {
        commands.add(aCommand);
    }

    public List<Command> getCommands() {
        return new ArrayList<>(commands);
    }

    public boolean execute() {
        boolean result = false;
        for (Command command : commands) {
            result |= command.execute();
        }
        return result;
    }
}
