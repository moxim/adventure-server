package com.pdg.adventure.server.action;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;

public class SetVariableAction extends AbstractVariableAction {

    private final String name;
    private final String value;

    public SetVariableAction(String aName, String aValue, VariableProvider aVariableProvider) {
        super(aVariableProvider);
        name = aName;
        value = aValue;
    }

    @Override
    public ExecutionResult execute() {
        variableProvider.set(new Variable(name, value));
        return new CommandExecutionResult(ExecutionResult.State.SUCCESS);
    }
}
