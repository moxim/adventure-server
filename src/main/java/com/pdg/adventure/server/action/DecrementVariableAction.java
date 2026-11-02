package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.support.VariableProvider;

@Getter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class DecrementVariableAction extends AbstractVariableAction {
    private final Integer value;

    public DecrementVariableAction(String aName, Integer aValue, VariableProvider aVariableProvider) {
        super(aName, aVariableProvider);
        value = aValue;
    }

    @Override
    public ExecutionResult execute() {
        return super.execute();
    }

    protected void changeValue(Integer aBaseValue) {
        variableProvider.set(getVariableName(), aBaseValue - value);
    }
}
