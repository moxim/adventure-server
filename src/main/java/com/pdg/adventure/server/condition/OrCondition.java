package com.pdg.adventure.server.condition;

import lombok.EqualsAndHashCode;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.api.PreCondition;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class OrCondition extends AbstractCondition {

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
