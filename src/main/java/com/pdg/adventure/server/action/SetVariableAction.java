package com.pdg.adventure.server.action;

import com.pdg.adventure.server.support.Environment;

public class SetVariableAction extends AbstractAction {

    private final String name;
    private final String value;

    public SetVariableAction(String aName, String aValue) {
        name = aName;
        value = aValue;
    }

    @Override
    public void execute() {
        Environment.setVariable(name, value);
    }
}
