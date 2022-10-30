package com.pdg.adventure.server.engine;

import com.pdg.adventure.server.api.Command;
import com.pdg.adventure.server.parser.CommandDescription;

import java.util.Map;
import java.util.TreeMap;

public class Workflow {
    private final Map<CommandDescription, Command> preCommands;
    private final Map<CommandDescription, Command> interceptorCommands;

    public Workflow() {
        preCommands = new TreeMap<>();
        interceptorCommands = new TreeMap<>()    ;
    }

    public void addPreCommand(CommandDescription aCommandDescription, Command aCommand) {
        preCommands.put(aCommandDescription, aCommand);
    }

    public void addInterceptorCommand(CommandDescription aCommandDescription, Command aCommand) {
        interceptorCommands.put(aCommandDescription, aCommand);
    }

    public void removePreCommand(CommandDescription aCommandDescription, Command aCommand) {
        preCommands.remove(aCommandDescription, aCommand);
    }

    public void removeInterceptorCommand(CommandDescription aCommandDescription, Command aCommand) {
        interceptorCommands.remove(aCommandDescription, aCommand);
    }

    public void preProcess() {
        process(preCommands);
    }

    public boolean interceptCommands(CommandDescription aCommand) {
        return applyCommand(interceptorCommands, aCommand);
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
