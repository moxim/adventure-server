package com.pdg.adventure.server.condition;

import lombok.Getter;
import lombok.EqualsAndHashCode;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.api.PreCondition;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class AndCondition extends AbstractCondition {

    @Getter
    private final PreCondition preCondition;
    @Getter
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
