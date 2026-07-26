package com.pdg.adventure.server.condition;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.support.VariableProvider;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class GreaterThanCondition extends AbstractVariableCondition {

    @Getter
    private final String variableName;
    @Getter
    private final Integer value;

    public GreaterThanCondition(String aVariableName, Integer aValue, VariableProvider aVariableProvider) {
        super(aVariableProvider);
        variableName = aVariableName;
        value = aValue;
    }

    @Override
    public ExecutionResult check() {
        ExecutionResult result = new CommandExecutionResult();
        var envVal = extractVariableValue(variableName);
        if (envVal > value) {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
        }
        return result;
    }
}
