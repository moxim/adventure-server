package com.pdg.adventure.server.condition;

import com.pdg.adventure.server.api.ExecutionResult;
import com.pdg.adventure.server.api.PreCondition;

public class AndCondition implements PreCondition {

    private final PreCondition preCondition;
    private final PreCondition anotherPreCondition;

    public AndCondition(PreCondition aPreCondition, PreCondition andAnotherPreCondition) {
        preCondition = aPreCondition;
        anotherPreCondition = andAnotherPreCondition;
    }

    @Override
    public ExecutionResult check() {
        ExecutionResult result = preCondition.check();
        if (result.getExecutionState() == ExecutionResult.State.SUCCESS) {
            ExecutionResult rightResult = anotherPreCondition.check();
            if (rightResult.getExecutionState() == ExecutionResult.State.FAILURE) {
                result = rightResult;
            }
        }
        return result;
    }
}
