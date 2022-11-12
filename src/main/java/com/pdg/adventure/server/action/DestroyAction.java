package com.pdg.adventure.server.action;

import com.pdg.adventure.server.api.Containable;
import com.pdg.adventure.server.api.Container;
import com.pdg.adventure.server.api.ExecutionResult;

public class DestroyAction extends AbstractAction {

    private final Containable thing;
    private final Container container;

    public DestroyAction(Containable aThing, Container aContainer) {
        thing = aThing;
        container = aContainer;
    }

    @Override
    public ExecutionResult execute() {
        ExecutionResult result = container.remove(thing);
        if (result.getExecutionState() == ExecutionResult.State.SUCCESS) {
            // TODO
            //  really do this?
            result.setResultMessage("The " + thing.getShortDescription() + " evaporates into thin air.");
        }
        return result;
    }
}
