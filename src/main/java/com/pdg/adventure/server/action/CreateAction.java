package com.pdg.adventure.server.action;

import java.util.function.Supplier;

import com.pdg.adventure.api.Containable;
import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.ExecutionResult;

public class CreateAction extends AbstractAction {

    private final Containable thing;
    private Supplier<Container> containerProvider;

    public CreateAction(Containable aThing, Supplier<Container> aContainerProvider) {
        thing = aThing;
        containerProvider = aContainerProvider;
    }

    @Override
    public ExecutionResult execute() {
        Container container = containerProvider.get();
        ExecutionResult result = container.add(thing);
        if (result.getExecutionState() == ExecutionResult.State.SUCCESS) {
            // TODO
            //  really do this?
            result.setResultMessage("A " + thing.getShortDescription() + " appears in the " + container.getShortDescription() + ".");
        }
        return result;
    }
}
