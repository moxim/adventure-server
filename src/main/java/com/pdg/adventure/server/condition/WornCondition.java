package com.pdg.adventure.server.condition;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.api.Wearable;
import com.pdg.adventure.server.parser.CommandExecutionResult;

public class WornCondition extends AbstractCondition {
    private final Wearable thing;

    public WornCondition(Wearable aThing) {
        thing = aThing;
    }

    @Override
    public ExecutionResult check() {
        ExecutionResult result = new CommandExecutionResult();
        if (thing.isWorn()) {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
        } else {
            result.setResultMessage(String.format("You are not wearing %s.", thing.getEnrichedBasicDescription()));
        }
        return result;
    }
}
