package com.pdg.adventure.server.parser;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.pdg.adventure.api.Command;
import com.pdg.adventure.api.CommandChain;
import com.pdg.adventure.api.ExecutionResult;

public class GenericCommandChain implements CommandChain {
    private final List<Command> commands;
    @Getter
    @Setter
    private String id;

    public GenericCommandChain() {
        commands = new ArrayList<>();
        id = UUID.randomUUID().toString();
    }

    @Override
    public void addCommand(Command aCommand) {
        commands.add(aCommand);
    }

    @Override
    public void removeCommand(Command aCommand) {
        commands.remove(aCommand);
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
