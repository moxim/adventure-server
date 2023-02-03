package com.pdg.adventure.server.action;

import com.pdg.adventure.api.Containable;
import com.pdg.adventure.api.ExecutionResult;

public class DestroyAction extends AbstractAction {

    private final Containable thing;

    public DestroyAction(Containable aThing) {
        thing = aThing;
    }

    @Override
    public ExecutionResult execute() {
        ExecutionResult result = thing.getParentContainer().remove(thing);
        if (result.getExecutionState() == ExecutionResult.State.SUCCESS) {
            // TODO
            //  really do this?
            result.setResultMessage("The " + thing.getShortDescription() + " evaporates into thin air.");
        }
        return result;
    }
}
