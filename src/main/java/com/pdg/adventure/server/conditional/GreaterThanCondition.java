package com.pdg.adventure.server.conditional;

import com.pdg.adventure.server.api.PreCondition;
import com.pdg.adventure.server.support.Environment;
import com.pdg.adventure.server.support.Variable;

public class GreaterThanCondition  implements PreCondition {

    private final String variableName;
    private final Object value;

    public GreaterThanCondition(String aVariableName, Number aValue) {
        variableName = aVariableName;
        value = aValue;
    }

    @Override
    public boolean isValid() {
        final Variable envVariable = Environment.getVariable(variableName);
        if (envVariable == null) {
            throw new IllegalArgumentException("Variable " + variableName + " does not exist!");
        }
        try {
            Integer envVal = Integer.valueOf(envVariable.aValue());
            Integer iVal = Integer.valueOf(value.toString());
            return envVal > iVal;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
