package com.pdg.adventure.server.condition;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Optional;

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

        Optional<Variable> var1 = variableProvider.get(variableNameOne);
        Optional<Variable> var2 = variableProvider.get(variableNameTwo);
        if (var1.isEmpty() || var2.isEmpty()) {
            result.setExecutionState(ExecutionResult.State.FAILURE);
            return result;
        }
        final Variable variable1 = var1.get();
        final Variable variable2 = var2.get();
        if (variable1.value().equals(variable2.value())) {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
        } else {
            result.setExecutionState(ExecutionResult.State.FAILURE);
        }

        return result;
    }
}
