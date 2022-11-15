package com.pdg.adventure.server.condition;

import com.pdg.adventure.server.api.ExecutionResult;
import com.pdg.adventure.server.api.PreCondition;
import com.pdg.adventure.server.api.Wearable;
import com.pdg.adventure.server.parser.CommandExecutionResult;

public class WornCondition implements PreCondition {
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
