package com.pdg.adventure.server.condition;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;

public class EqualsCondition extends AbstractVariableCondition {

    private final String variableName;
    private final String value;

    public EqualsCondition(String aVariableName, String aValue, VariableProvider aVariableProvider) {
        super(aVariableProvider);
        variableName = aVariableName;
        value = aValue;
    }


    @Override
    public ExecutionResult check() {
        ExecutionResult result = new CommandExecutionResult();
        final Variable envVariable = getVariable(variableName);
        if (envVariable.aValue().equals(value)) {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
        }
        return result;
    }

}
