package com.pdg.adventure.server.action;

import com.pdg.adventure.server.support.VariableProvider;

public abstract class AbstractVariableAction extends AbstractAction {
    protected final VariableProvider variableProvider;

    AbstractVariableAction(VariableProvider aVariableProvider) {
        variableProvider = aVariableProvider;
    }

    public VariableProvider getVariableProvider() {
        return variableProvider;
    }
}
