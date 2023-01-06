package com.pdg.adventure.server.action;

import com.pdg.adventure.api.Containable;
import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.ExecutionResult;

public class DestroyAction extends AbstractAction {

    private final Containable thing;
    private Containable anotherThing;
    private Container container;

    public DestroyAction(Containable aThing, Container aContainer) {
        thing = aThing;
        container = aContainer;
    }

    public DestroyAction(Containable aThing, Containable andAnotherThing) {
        thing = aThing;
        anotherThing = andAnotherThing;
    }

    @Override
    public ExecutionResult execute() {
        if (container != null) {
            return executeInContainer(container);
        } else {
            return executeInAnotherThing();
        }
    }

    private ExecutionResult executeInAnotherThing() {
        Container anotherThingParentContainer = anotherThing.getParentContainer();
        return executeInContainer(anotherThingParentContainer);
    }

    private ExecutionResult executeInContainer(Container aContainer) {
        ExecutionResult result = aContainer.remove(thing);
        if (result.getExecutionState() == ExecutionResult.State.SUCCESS) {
            // TODO
            //  really do this?
            result.setResultMessage("The " + thing.getShortDescription() + " evaporates into thin air.");
        }
        return result;
    }
}
