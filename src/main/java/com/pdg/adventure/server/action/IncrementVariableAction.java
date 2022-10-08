package com.pdg.adventure.server.action;

import com.pdg.adventure.server.support.Environment;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;

public class IncrementVariableAction extends AbstractVariableAction {
    private final String name;
    private final String value;


    public IncrementVariableAction(String aName, String aValue, VariableProvider aVariableProvider) {
        super(aVariableProvider);
        name = aName;
        value = aValue;
    }

    @Override
    public void execute() {
        Variable envVariable = variableProvider.get(name);
        if (envVariable == null) {
            throw new IllegalArgumentException("Variable " + name + " does not exist!");
        }
        Long envVal = Long.valueOf(envVariable.aValue());
        Long iVal = Long.valueOf(value.toString());
        variableProvider.set(new Variable(name, String.valueOf(Long.valueOf(envVal + iVal))));
    }
}
