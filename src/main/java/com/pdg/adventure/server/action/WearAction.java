package com.pdg.adventure.server.action;

import com.pdg.adventure.server.api.ExecutionResult;
import com.pdg.adventure.server.api.Wearable;
import com.pdg.adventure.server.parser.CommandExecutionResult;

public class WearAction extends AbstractAction {
    private final Wearable thing;

    public WearAction(Wearable aThing) {
        thing = aThing;
    }

    @Override
    public ExecutionResult execute() {
        ExecutionResult result = new CommandExecutionResult();
        if (thing.isWearable() && !thing.isWorn()) {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
            thing.setIsWorn(true);
        } else {
            result.setResultMessage("You can't wear " + thing.getEnrichedBasicDescription() + ".");
        }
        return result;
    }
}
