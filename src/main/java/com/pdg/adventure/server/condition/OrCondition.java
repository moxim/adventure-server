package com.pdg.adventure.server.condition;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.api.PreCondition;

public class OrCondition implements PreCondition {

    private final PreCondition preCondition;
    private final PreCondition anotherPreCondition;

    public OrCondition(PreCondition aPreCondition, PreCondition andAnotherPreCondition) {
        preCondition = aPreCondition;
        anotherPreCondition = andAnotherPreCondition;
    }

    @Override
    public ExecutionResult check() {
        ExecutionResult result = preCondition.check();
        if (result.getExecutionState() == ExecutionResult.State.FAILURE) {
            ExecutionResult rightResult = anotherPreCondition.check();
            if (rightResult.getExecutionState() == ExecutionResult.State.SUCCESS) {
                result = rightResult;
            }
        }
        return result;
    }
}
