package com.pdg.adventure.server.condition;

import com.pdg.adventure.server.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.support.VariableProvider;

public class SameCondition extends AbstractVariableCondition {

    private final String variableNameOne;
    private final String variableNameTwo;

    public SameCondition(String aVariableNameOne, String aVariableNameTwo, VariableProvider aVariableProvider) {
        super(aVariableProvider);
        variableNameOne = aVariableNameOne;
        variableNameTwo = aVariableNameTwo;
    }

    @Override
    public ExecutionResult check() {
        ExecutionResult result = new CommandExecutionResult();

        if (variableProvider.get(variableNameOne).equals(variableProvider.get(variableNameTwo))) {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
        };

        return result;
    }
}
