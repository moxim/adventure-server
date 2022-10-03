package com.pdg.adventure.server.action;

import com.pdg.adventure.server.api.Container;
import com.pdg.adventure.server.support.Environment;
import com.pdg.adventure.server.tangible.Item;

public class MoveAction extends AbstractAction {

    private final Item target;
    private final Container destination;

    public MoveAction(Item aTarget, Container aDestination) {
        target = aTarget;
        destination = aDestination;
    }

    @Override
    public void execute() {
        Container parentContainer = target.getParentContainer();
        if (parentContainer != null) {
            parentContainer.removeItem(target);
        }
        destination.addItem(target);
        Environment.tell("You move the " + target.getShortDescription() + " into the " + destination.getShortDescription() + ".");
    }
}
