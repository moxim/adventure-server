package com.pdg.adventure.server.engine;

import com.pdg.adventure.server.api.Command;
import com.pdg.adventure.server.parser.CommandDescription;

import java.util.Map;
import java.util.TreeMap;

public class Workflow {
    private final Map<CommandDescription, Command> preCommands;
    private final Map<CommandDescription, Command> postCommands;
    private final Map<CommandDescription, Command> alwaysCommands;

    public Workflow() {
        preCommands = new TreeMap<>();
        postCommands = new TreeMap<>();
        alwaysCommands = new TreeMap<>()    ;
    }

    public void addPreCommand(CommandDescription aCommandDescription, Command aCommand) {
        preCommands.put(aCommandDescription, aCommand);
    }

    public void addPostCommand(CommandDescription aCommandDescription, Command aCommand) {
        postCommands.put(aCommandDescription, aCommand);
    }

    public void addAlwaysCommand(CommandDescription aCommandDescription, Command aCommand) {
        alwaysCommands.put(aCommandDescription, aCommand);
    }

    public void removePreCommand(CommandDescription aCommandDescription, Command aCommand) {
        preCommands.remove(aCommandDescription, aCommand);
    }

    public void removePostCommand(CommandDescription aCommandDescription, Command aCommand) {
        postCommands.remove(aCommandDescription, aCommand);
    }

    public void removeAlwaysCommand(CommandDescription aCommandDescription, Command aCommand) {
        alwaysCommands.remove(aCommandDescription, aCommand);
    }

    public void preProcess() {
        process(preCommands);
    }

    public void postProcess() {
        process(postCommands);
    }

    public boolean alwaysProcess(CommandDescription aCommand) {
        return applyCommand(alwaysCommands, aCommand);
    }

    private boolean applyCommand(Map<CommandDescription, Command> aAlwaysCommands, CommandDescription aCommand) {
        Command command = aAlwaysCommands.get(aCommand);
        if (command != null) {
            command.execute();
            return true;
        }
        return false;
    }

    private void process(Map<CommandDescription, Command> commands) {
        for (Map.Entry<CommandDescription, Command> commandEntry : commands.entrySet()) {
            commandEntry.getValue().execute();
        }
    }
}
