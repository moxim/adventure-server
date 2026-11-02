package com.pdg.adventure.server.action;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.support.VariableProvider;

/**
 * Sets the value of a variable to a specific value. It is independent of the
 * actual value of the variable, so it does not need to be retrieved first.
 */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class SetVariableAction extends AbstractVariableAction {

    private Integer variableValue;

    public SetVariableAction(String aName, Integer aValue, VariableProvider aVariableProvider) {
        super(aName, aVariableProvider);
        variableValue = aValue;
    }

    @Override
    public ExecutionResult execute() {
        changeValue(variableValue);
        return new CommandExecutionResult(ExecutionResult.State.SUCCESS);
    }

    @Override
    protected void changeValue(final Integer aBaseValue) {
        variableProvider.set(getVariableName(), variableValue);
    }
}
