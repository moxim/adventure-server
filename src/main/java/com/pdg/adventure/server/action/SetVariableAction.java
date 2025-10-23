package com.pdg.adventure.server.action;

import lombok.Getter;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;

@Getter
public class SetVariableAction extends AbstractVariableAction {

    private final String variableName;
    private final String variableValue;

    public SetVariableAction(String aName, String aValue, VariableProvider aVariableProvider,
                             MessagesHolder aMessagesHolder) {
        super(aVariableProvider, aMessagesHolder);
        variableName = aName;
        variableValue = aValue;
    }

    @Override
    public ExecutionResult execute() {
        variableProvider.set(new Variable(variableName, variableValue));
        return new CommandExecutionResult(ExecutionResult.State.SUCCESS);
    }
}
