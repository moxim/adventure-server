package com.pdg.adventure.server.action;

import com.pdg.adventure.server.api.Containable;
import com.pdg.adventure.server.api.Container;
import com.pdg.adventure.server.support.Environment;

public class MoveItemAction extends AbstractAction {

    private final Containable target;
    private final Container destination;

    public MoveItemAction(Containable aTarget, Container aDestination) {
        target = aTarget;
        destination = aDestination;
    }

    @Override
    public void execute() {
        if (destination.getSize() < destination.getMaxSize()) {
            Container parentContainer = target.getParentContainer();
            if (parentContainer != null) {
                parentContainer.remove(target);
            }
            destination.add(target);
            Environment.tell("You put " + target.getEnrichedShortDescription() + " into " + destination
            .getEnrichedBasicDescription() + ".");
        } else {
            Environment.tell("The " + destination.getShortDescription() + " is full.");
        }
    }
}
