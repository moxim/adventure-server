package com.pdg.adventure.server.conditional;

import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;

public class EqualsCondition extends AbstractVariableCondition  {

    private final String variableName;
    private final String value;

    public EqualsCondition(String aVariableName, String aValue, VariableProvider aVariableProvider) {
        super(aVariableProvider);
        variableName = aVariableName;
        value = aValue;
    }

    @Override
    public boolean isValid() {
        final Variable envVariable = variableProvider.get(variableName);
        if (envVariable == null) {
            throw new IllegalArgumentException("Variable " + variableName + " does not exist!");
        }
        return envVariable.aValue().equals(value);
    }
}
