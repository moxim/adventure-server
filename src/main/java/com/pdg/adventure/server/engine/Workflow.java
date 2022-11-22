package com.pdg.adventure.server.engine;

import com.pdg.adventure.server.api.Command;
import com.pdg.adventure.server.api.CommandDescription;
import com.pdg.adventure.server.api.ExecutionResult;
import com.pdg.adventure.server.parser.GenericCommandDescription;

import java.util.Map;
import java.util.TreeMap;

public class Workflow {
    private final Map<CommandDescription, Command> preCommands;
    private final Map<CommandDescription, Command> interceptorCommands;

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

    public boolean interceptCommands(CommandDescription aCommand) {
        Command command = interceptorCommands.get(aCommand);
        if (command != null) {
            command.execute();
            return true;
        }
        return false;
    }

    private void process(Map<CommandDescription, Command> commands) {
        for (Map.Entry<CommandDescription, Command> commandEntry : commands.entrySet()) {
            ExecutionResult result = commandEntry.getValue().execute();
            Environment.tell(result.getResultMessage());
        }
    }
}
