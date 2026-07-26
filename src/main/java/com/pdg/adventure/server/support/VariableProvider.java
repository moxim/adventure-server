package com.pdg.adventure.server.support;

import java.util.HashMap;
import java.util.Map;

/**
 * Plain POJO, not a Spring bean - its single instance is registered via
 * {@link com.pdg.adventure.server.AdventureConfig#allVariables()}, matching the pattern used by
 * the other shared game-state holders ({@code Vocabulary}, {@code MessagesHolder}, etc.).
 * Component-scanning this class in addition would register a second, independent instance,
 * silently splitting variable writers from readers.
 */
public class VariableProvider {
    private final Map<String, Variable> variables;

    public VariableProvider() {
        variables = new HashMap<>();
    }

    public void set(Variable aVariable) {
        variables.put(aVariable.name(), aVariable);
    }

    public Variable get(String aName) {
        return variables.getOrDefault(aName, new Variable(aName, 0));
    }
}
