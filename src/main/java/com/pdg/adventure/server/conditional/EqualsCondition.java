package com.pdg.adventure.server.conditional;

import com.pdg.adventure.server.api.PreCondition;
import com.pdg.adventure.server.support.Environment;
import com.pdg.adventure.server.support.Variable;

public class EqualsCondition implements PreCondition  {

    private final String variableName;
    private final String value;

    public EqualsCondition(String aVariableName, String aValue) {
        variableName = aVariableName;
        value = aValue;
    }

    @Override
    public boolean isValid() {
        final Variable envVariable = Environment.getVariable(variableName);
        if (envVariable == null) {
            throw new IllegalArgumentException("Variable " + variableName + " does not exist!");
        }
        return envVariable.aValue().equals(value);
    }
}
