package com.pdg.adventure.server.action;

import com.pdg.adventure.server.api.Containable;
import com.pdg.adventure.server.api.Container;

public class DestroyAction extends AbstractAction {

    private final Containable thing;
    private final Container container;

    public DestroyAction(Containable aThing, Container aContainer) {
        thing = aThing;
        container = aContainer;
    }

    @Override
    public void execute() {
        container.remove(thing);
//        Environment.tell("The " + thing.getShortDescription() + " evaporates into thin air.");
    }
}
