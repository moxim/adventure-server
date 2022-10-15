package com.pdg.adventure.server.action;

import com.pdg.adventure.server.api.Containable;
import com.pdg.adventure.server.api.Container;

public class MoveItemAction extends AbstractAction {

    private final Containable target;
    private final Container destination;

    public MoveItemAction(Containable aTarget, Container aDestination) {
        target = aTarget;
        destination = aDestination;
    }

    @Override
    public void execute() {
        Container parentContainer = target.getParentContainer();
        if (parentContainer != null) {
            parentContainer.remove(target);
        }
        destination.add(target);
//        Environment.tell("You move the " + target.getShortDescription() + " into the " + destination
//        .getShortDescription() + ".");
    }
}
