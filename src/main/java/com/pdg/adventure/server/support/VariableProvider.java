package com.pdg.adventure.server.support;

import java.util.*;

/**
 * Plain POJO, not a Spring bean - its single instance is registered via
 * {@link com.pdg.adventure.server.AdventureConfig#allVariables()}, matching the pattern used by
 * the other shared game-state holders ({@code Vocabulary}, {@code MessagesHolder}, etc.).
 * Component-scanning this class in addition would register a second, independent instance,
 * silently splitting variable writers from readers.
 * <p>
 * Holds all variables of the loaded adventure: those the author defined (loaded by
 * {@code VariableMapper} when an adventure is mapped, via {@link #define}) as well as any the engine
 * creates on the fly (e.g. {@code VISITED}). Reading an unknown name still yields 0.
 */
public class VariableProvider {
    public static final String VISITED_VARIABLE_NAME = "VISITED";

    private final Map<String, Integer> variables;

    public VariableProvider() {
        this(new HashMap<>());
    }

    public VariableProvider(Map<String, Integer> aBagOfVariables) {
        variables = new HashMap<>(aBagOfVariables);
    }

    /**
     * Registers a variable and its current value, replacing any variable of the same name.
     */
    public void define(Variable aVariable) {
        set(aVariable.name(), aVariable.value());
    }

    public void set(String aName, int aValue) {
        variables.put(aName, aValue);
    }

    public boolean isDefined(String aName) {
        return variables.containsKey(aName);
    }

    public Collection<Variable> getAll() {
        return variables.entrySet()
                        .stream()
                        .map(entry ->
                                     new Variable(entry.getKey(), entry.getValue()))
                       .toList();
    }

    public Map<String, Integer> getAllAsMap() {
        return new HashMap<>(variables);
    }

    /**
     * Forgets every variable, e.g. before the next adventure's variables are defined.
     */
    public void clear() {
        variables.clear();
    }

    public Optional<Variable> get(String aName) {
        if (variables.containsKey(aName)) {
            return Optional.of(new Variable(aName, variables.get(aName)));
        }
        return Optional.empty();
    }
}
