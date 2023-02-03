package com.pdg.adventure.server.condition;

import com.pdg.adventure.api.Containable;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.api.PreCondition;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.engine.Environment;

public class PresentCondition extends AbstractCondition {
    private final Containable thing;

    public PresentCondition(Containable aThing) {
        thing = aThing;
    }

    public ExecutionResult check() {
        ExecutionResult result = new CommandExecutionResult();
        if (Environment.getCurrentLocation().contains(thing)) {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
        } else {
            result.setResultMessage(String.format("There is no %s here.", thing.getNoun()));
        }
        return result;
    }
}
