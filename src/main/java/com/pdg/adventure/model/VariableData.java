package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pdg.adventure.model.basic.BasicData;

/**
 * A number of variables an author has defined for an adventure, embedded in {@link AdventureData#getVariableData()}
 * (owned 1:1 by its adventure, never a document of its own).
 * A variable is created the moment an author names a variable in a Set Variable action;
 * conditions and Increment/Decrement actions then pick from these.
 * A variables default value is 0, but the author can set it to any integer in the Set Variable action.
 * Variable names are free text, but may not contain characters that are illegal in Mongo field names ('.', leading '$').
 * At runtime it becomes a {@code Variable} in the shared {@code VariableProvider}.
 */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class VariableData extends BasicData {

    private Map<String, Integer> variables;

    public VariableData() {
        this(new HashMap<>());
    }

    public VariableData(Map<String, Integer> aBagOfVariables) {
        this.variables = aBagOfVariables;
    }

    public Integer getVariableValue(String aVariableName) {
        return variables.get(aVariableName);
    }

    public boolean addVariable(String aVariableName) {
        return addVariable(aVariableName, 0);
    }

    public boolean addVariable(String aVariableName, int aVariableValue) {

        if (aVariableName == null || aVariableName.trim().isEmpty()) {
            return false; // Invalid variable name
        }
        String trimmedName = aVariableName.trim();

        if (variables.containsKey(trimmedName)) {
            return false; // Variable already exists
        }

        variables.put(trimmedName, aVariableValue);
        return true;
    }

    public List<String> getVariableNames() {
        return new ArrayList<>(variables.keySet());
    }
}
