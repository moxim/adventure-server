package com.pdg.adventure.server.parser;

import com.pdg.adventure.server.api.Command;
import com.pdg.adventure.server.api.CommandChain;
import com.pdg.adventure.server.api.ExecutionResult;

import java.util.ArrayList;
import java.util.List;

public class GenericCommandChain implements CommandChain {
    private final List<Command> commands;

    public GenericCommandChain() {
        commands = new ArrayList<>();
    }

    @Override
    public void addCommand(Command aCommand) {
        commands.add(aCommand);
    }

    @Override
    public List<Command> getCommands() {
        return new ArrayList<>(commands);
    }

    @Override
    public ExecutionResult execute() {
        ExecutionResult result = new CommandExecutionResult();
        for (Command command : commands) {
            if(result.getExecutionState() != ExecutionResult.State.SUCCESS) {
                ExecutionResult fromAction = command.execute();
                result.setResultMessage(fromAction.getResultMessage());
                if (fromAction.getExecutionState() == ExecutionResult.State.SUCCESS) {
                    result.setExecutionState(ExecutionResult.State.SUCCESS);
                    break;
                }
            }
        }
        return result;
    }
}
