package com.pdg.adventure.server.action;

import com.pdg.adventure.server.api.Containable;
import com.pdg.adventure.server.api.Container;
import com.pdg.adventure.server.api.ExecutionResult;

public class CreateAction extends AbstractAction {

    private final Containable thing;
    private final Container container;

    public CreateAction(Containable aThing, Container aContainer) {
        thing = aThing;
        container = aContainer;
    }

    @Override
    public ExecutionResult execute() {
        ExecutionResult result = container.add(thing);

        if (result.getExecutionState() == ExecutionResult.State.SUCCESS) {
            // TODO
            //  really do this?
            result.setResultMessage("A " + thing.getShortDescription() + " appears in the " + container.getShortDescription() + ".");
        }

        return result;
    }
}
