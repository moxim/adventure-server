package com.pdg.adventure.server.action;

import com.pdg.adventure.server.api.Containable;
import com.pdg.adventure.server.api.Container;
import com.pdg.adventure.server.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;

public class MoveItemAction extends AbstractAction {

    private final Containable target;
    private final Container destination;

    public MoveItemAction(Containable aTarget, Container aDestination) {
        target = aTarget;
        destination = aDestination;
    }

    @Override
    public ExecutionResult execute() {
        ExecutionResult result = new CommandExecutionResult(ExecutionResult.State.SUCCESS);
        if (destination.getSize() < destination.getMaxSize()) {
            Container parentContainer = target.getParentContainer();
            if (parentContainer != null) {
                result = parentContainer.remove(target);
            }
            if (result.getExecutionState() == ExecutionResult.State.SUCCESS) {
                result = destination.add(target);
                result.setResultMessage("You put " + target.getEnrichedShortDescription() + " into " + destination
                        .getEnrichedBasicDescription() + ".");
            }
        } else {
            result.setExecutionState(ExecutionResult.State.FAILURE);
            result.setResultMessage("The " + destination.getShortDescription() + " is full.");
        }
        return result;
    }
}
