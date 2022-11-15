package com.pdg.adventure.server.testhelper;

import com.pdg.adventure.server.api.Containable;
import com.pdg.adventure.server.api.Container;
import com.pdg.adventure.server.api.ExecutionResult;
import com.pdg.adventure.server.api.PreCondition;
import com.pdg.adventure.server.parser.GenericCommandDescription;

public class TestSupporter {
    private TestSupporter() {
        // don't instantiate me
    }

    public static boolean conditionToBoolean(PreCondition aCondition) {
        final ExecutionResult executionResult = aCondition.check();
        return executionResult.getExecutionState()== ExecutionResult.State.SUCCESS;
    }

    public static boolean addItemToBoolean(Container aContainer, Containable anItem) {
        final ExecutionResult executionResult = aContainer.add(anItem);
        return executionResult.getExecutionState()== ExecutionResult.State.SUCCESS;
    }

    public static boolean removeItemToBoolean(Container aContainer, Containable anItem) {
        final ExecutionResult executionResult = aContainer.remove(anItem);
        return executionResult.getExecutionState()== ExecutionResult.State.SUCCESS;
    }

    public static boolean applyCommandToBoolean(Containable anItem, GenericCommandDescription aCommandDescription) {
        final ExecutionResult executionResult = anItem.applyCommand(aCommandDescription);
        return executionResult.getExecutionState()== ExecutionResult.State.SUCCESS;
    }
}
