package com.pdg.adventure.server.action;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class SetVariableAction extends AbstractVariableAction {

    private String variableName;
    private Integer variableValue;

    public SetVariableAction(String aName, Integer aValue, VariableProvider aVariableProvider) {
        super(aVariableProvider);
        variableName = aName;
        variableValue = aValue;
    }

    @Override
    public ExecutionResult execute() {
        variableProvider.set(new Variable(variableName, variableValue));
        return new CommandExecutionResult(ExecutionResult.State.SUCCESS);
    }
}
