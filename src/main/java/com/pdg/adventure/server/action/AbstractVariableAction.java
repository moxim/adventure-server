package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;

import java.util.Optional;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public abstract class AbstractVariableAction extends AbstractAction {
    protected final transient VariableProvider variableProvider;

    AbstractVariableAction(VariableProvider aVariableProvider) {
        variableProvider = aVariableProvider;
    }

    public VariableProvider getVariableProvider() {
        return variableProvider;
    }

    protected ExecutionResult execute(final String aName, final Integer aValue) {
        CommandExecutionResult result = new CommandExecutionResult();
        Optional<Variable> variableOptional = variableProvider.get(aName);
        if (variableOptional.isEmpty()) {
            result.setResultMessage("Variable " + aName + " does not exist!");
            return result;
        }
        Integer baseValue = variableOptional.get().value();
        changeValue(baseValue);
        result.setExecutionState(ExecutionResult.State.SUCCESS);
        return result;
    }

    protected abstract void changeValue(Integer aBaseValue);
}
