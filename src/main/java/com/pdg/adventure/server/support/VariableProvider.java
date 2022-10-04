package com.pdg.adventure.server.support;

import java.util.HashMap;
import java.util.Map;

public class VariableProvider {
    private final Map<String, Variable> variables;

    public VariableProvider() {
        variables = new HashMap<>();
    }

    public void set(Variable aVariable) {
        variables.put(aVariable.aName(), aVariable);
    }

    public Variable get(String aName) {
        return variables.get(aName);
    }
}
