package com.pdg.adventure.server.engine;

import com.pdg.adventure.server.api.Command;
import com.pdg.adventure.server.parser.GenericCommandDescription;

import java.util.Map;
import java.util.TreeMap;

public class Workflow {
    private final Map<GenericCommandDescription, Command> preCommands;
    private final Map<GenericCommandDescription, Command> interceptorCommands;

    public Workflow() {
        preCommands = new TreeMap<>();
        interceptorCommands = new TreeMap<>()    ;
    }

    public void addPreCommand(GenericCommandDescription aCommandDescription, Command aCommand) {
        preCommands.put(aCommandDescription, aCommand);
    }

    public void addInterceptorCommand(GenericCommandDescription aCommandDescription, Command aCommand) {
        interceptorCommands.put(aCommandDescription, aCommand);
    }

    public void removePreCommand(GenericCommandDescription aCommandDescription, Command aCommand) {
        preCommands.remove(aCommandDescription, aCommand);
    }

    public void removeInterceptorCommand(GenericCommandDescription aCommandDescription, Command aCommand) {
        interceptorCommands.remove(aCommandDescription, aCommand);
    }

    public void preProcess() {
        process(preCommands);
    }

    public boolean interceptCommands(GenericCommandDescription aCommand) {
        return applyCommand(interceptorCommands, aCommand);
    }

    private boolean applyCommand(Map<GenericCommandDescription, Command> aAlwaysCommands, GenericCommandDescription aCommand) {
        Command command = aAlwaysCommands.get(aCommand);
        if (command != null) {
            command.execute();
            return true;
        }
        return false;
    }

    private void process(Map<GenericCommandDescription, Command> commands) {
        for (Map.Entry<GenericCommandDescription, Command> commandEntry : commands.entrySet()) {
            commandEntry.getValue().execute();
        }
    }
}
