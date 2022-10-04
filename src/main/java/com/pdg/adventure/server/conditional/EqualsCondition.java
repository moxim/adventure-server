package com.pdg.adventure.server.conditional;

import com.pdg.adventure.server.api.PreCondition;
import com.pdg.adventure.server.support.Variable;

public class EqualsCondition implements PreCondition  {

    private final Variable variable;
    private final String value;

    public EqualsCondition(Variable aVariable, String aValue) {
        variable = aVariable;
        value = aValue;
    }

    @Override
    public boolean isValid() {
        return variable.aValue().equals(value);
    }
}
