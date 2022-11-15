package com.pdg.adventure.server.action;

import com.pdg.adventure.server.api.Containable;
import com.pdg.adventure.server.api.Container;
import com.pdg.adventure.server.api.ExecutionResult;

public class CreateAction extends AbstractAction {

    private final Containable thing;
    private Containable anotherThing;
    private Container container;

    public CreateAction(Containable aThing, Container aContainer) {
        thing = aThing;
        container = aContainer;
    }

    public CreateAction(Containable aThing, Containable andAnotherThing) {
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
        ExecutionResult result = aContainer.add(thing);
        if (result.getExecutionState() == ExecutionResult.State.SUCCESS) {
            // TODO
            //  really do this?
            result.setResultMessage("A " + thing.getShortDescription() + " appears in the " + aContainer.getShortDescription() + ".");
        }
        return result;
    }
}
