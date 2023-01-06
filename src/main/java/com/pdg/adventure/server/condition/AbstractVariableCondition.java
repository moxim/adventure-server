package com.pdg.adventure.server.condition;

import com.pdg.adventure.api.PreCondition;
import com.pdg.adventure.server.exception.ConfigurationException;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;

public abstract class AbstractVariableCondition implements PreCondition {
    protected final VariableProvider variableProvider;

    AbstractVariableCondition(VariableProvider aVariableProvider) {
        variableProvider = aVariableProvider;
    }

    public VariableProvider getVariableProvider() {
        return variableProvider;
    }

    protected Variable getVariable(String aVariableName) {
        final Variable envVariable = variableProvider.get(aVariableName);
        if (envVariable == null) {
            throw new ConfigurationException("Variable " + aVariableName + " does not exist!");
        }
        return envVariable;
    }
}
