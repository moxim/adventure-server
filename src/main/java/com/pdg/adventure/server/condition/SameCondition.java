package com.pdg.adventure.server.condition;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class SameCondition extends AbstractVariableCondition {

    @Getter
    private final String variableNameOne;
    @Getter
    private final String variableNameTwo;

    public SameCondition(String aVariableNameOne, String aVariableNameTwo, VariableProvider aVariableProvider) {
        super(aVariableProvider);
        variableNameOne = aVariableNameOne;
        variableNameTwo = aVariableNameTwo;
    }

    @Override
    public ExecutionResult check() {
        ExecutionResult result = new CommandExecutionResult();

        final Variable variable1 = variableProvider.get(variableNameOne);
        final Variable variable2 = variableProvider.get(variableNameTwo);
        if (variable1.value().equals(variable2.value())) {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
        }

        return result;
    }
}
