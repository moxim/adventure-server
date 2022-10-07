package com.pdg.adventure.server.action;

import com.pdg.adventure.server.support.Environment;
import com.pdg.adventure.server.support.Variable;

public class IncrementVariableAction extends AbstractAction {
    private final String name;
    private final String value;


    public IncrementVariableAction(String aName, String aValue) {
        name = aName;
        value = aValue;
    }

    @Override
    public void execute() {
        Variable envVariable = Environment.getVariable(name);
        if (envVariable == null) {
            throw new IllegalArgumentException("Variable " + name + " does not exist!");
        }
        Long envVal = Long.valueOf(envVariable.aValue());
        Long iVal = Long.valueOf(value.toString());
        Environment.setVariable(name, String.valueOf(Long.valueOf(envVal + iVal)));
    }
}
