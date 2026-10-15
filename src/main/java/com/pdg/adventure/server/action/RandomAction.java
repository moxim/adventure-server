package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntUnaryOperator;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.support.VariableProvider;

/**
 * RANDOM: sets a variable to a random integer from 0 to 100, both included. A variable that does not exist yet is
 * created, so unlike the other variable actions this one never reports an unknown variable.
 */
@Getter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class RandomAction extends AbstractAction {
    public static final int MIN_VALUE = 0;
    public static final int MAX_VALUE = 99;

    private final String name;
    private final transient VariableProvider variableProvider;
    private final transient IntUnaryOperator randomBelow;

    public RandomAction(String aName, VariableProvider aVariableProvider) {
        this(aName, aVariableProvider, bound -> ThreadLocalRandom.current().nextInt(bound));
    }

    /**
     * @param aRandomBelow returns a random integer from 0 (included) to the given bound (excluded); lets a test
     *                     decide the outcome.
     */
    RandomAction(String aName, VariableProvider aVariableProvider, IntUnaryOperator aRandomBelow) {
        name = aName;
        variableProvider = aVariableProvider;
        randomBelow = aRandomBelow;
    }

    @Override
    public ExecutionResult execute() {
        // the range is taken as 0 to 99 inclusive (100 possible values)
        variableProvider.set(name, MIN_VALUE + randomBelow.applyAsInt(MAX_VALUE - MIN_VALUE + 1));
        return new CommandExecutionResult(ExecutionResult.State.SUCCESS);
    }
}
