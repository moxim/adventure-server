package com.pdg.adventure.server.conditional;

import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;

public class LowerThanCondition extends AbstractVariableCondition {

    private final String variableName;
    private final Object value;

    public LowerThanCondition(String aVariableName, Number aValue, VariableProvider aVariableProvider) {
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
        try {
            Integer envVal = Integer.valueOf(envVariable.aValue());
            Integer iVal = Integer.valueOf(value.toString());
            return envVal < iVal;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
