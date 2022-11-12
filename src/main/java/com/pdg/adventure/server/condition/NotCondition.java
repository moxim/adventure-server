package com.pdg.adventure.server.condition;

import com.pdg.adventure.server.api.ExecutionResult;
import com.pdg.adventure.server.api.PreCondition;

public class NotCondition implements PreCondition {

    private final PreCondition wrappedCondition;

    public NotCondition(PreCondition aWrappedCondition) {

        wrappedCondition = aWrappedCondition;
    }

    @Override
    public ExecutionResult check() {
        ExecutionResult result =                wrappedCondition.check();
        result.setResultMessage("");
        if (result.getExecutionState() == ExecutionResult.State.SUCCESS) {
            result.setExecutionState(ExecutionResult.State.FAILURE);
        } else {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
        }
        return result;
    }
}
