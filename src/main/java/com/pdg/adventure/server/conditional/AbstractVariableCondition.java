package com.pdg.adventure.server.conditional;

import com.pdg.adventure.server.api.PreCondition;
import com.pdg.adventure.server.support.VariableProvider;

public abstract class AbstractVariableCondition implements PreCondition {
    protected final VariableProvider variableProvider;

    AbstractVariableCondition(VariableProvider aVariableProvider) {
        variableProvider = aVariableProvider;
    }

    public VariableProvider getVariableProvider() {
        return variableProvider;
    }
}
