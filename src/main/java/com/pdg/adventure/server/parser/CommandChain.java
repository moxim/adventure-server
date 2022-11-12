package com.pdg.adventure.server.parser;

import com.pdg.adventure.server.api.Command;
import com.pdg.adventure.server.api.ExecutionResult;

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

    public ExecutionResult execute() {
        ExecutionResult result = new CommandExecutionResult();
        for (Command command : commands) {
            ExecutionResult fromAction = command.execute();
            result.setResultMessage(fromAction.getResultMessage());
            if (fromAction.getExecutionState() == ExecutionResult.State.SUCCESS) {
                result.setExecutionState(ExecutionResult.State.SUCCESS);
                break;
            }
        }
        return result;
    }
}
