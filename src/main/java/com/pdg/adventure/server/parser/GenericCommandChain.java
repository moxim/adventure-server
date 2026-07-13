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
        List<String> messages = new ArrayList<>();
        boolean anyApplied = false;
        ExecutionResult last = new CommandExecutionResult();   // FAILURE / empty default
        for (Command command : commands) {
            ExecutionResult fromCommand = command.execute();
            last = fromCommand;
            if (fromCommand.getExecutionState() == ExecutionResult.State.SUCCESS) {
                anyApplied = true;
                String msg = fromCommand.getResultMessage();
                if (msg != null && !msg.isBlank()) {
                    messages.add(msg);
                }
            }
        }
        ExecutionResult result = new CommandExecutionResult();
        if (anyApplied) {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
            result.setResultMessage(String.join(System.lineSeparator(), messages));
        } else {
            result.setResultMessage(last.getResultMessage());  // surface last failure; empty → "You can't do that."
        }
        return result;
    }
}
