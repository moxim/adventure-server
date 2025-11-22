package com.pdg.adventure.server.condition;

import lombok.EqualsAndHashCode;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.api.PreCondition;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class NotCondition extends AbstractCondition {

    private final PreCondition wrappedCondition;

    public NotCondition(PreCondition aWrappedCondition) {

        wrappedCondition = aWrappedCondition;
    }

    @Override
    public ExecutionResult check() {
        ExecutionResult result = wrappedCondition.check();
        result.setResultMessage("");
        if (result.getExecutionState() == ExecutionResult.State.SUCCESS) {
            result.setExecutionState(ExecutionResult.State.FAILURE);
        } else {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
        }
        return result;
    }
}
