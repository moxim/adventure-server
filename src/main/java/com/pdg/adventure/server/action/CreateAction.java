package com.pdg.adventure.server.action;

import com.pdg.adventure.server.api.Containable;
import com.pdg.adventure.server.api.Container;
import com.pdg.adventure.server.support.Environment;

public class CreateAction extends AbstractAction {

    private final Containable thing;
    private final Container container;

    public CreateAction(Containable aThing, Container aContainer) {
        thing = aThing;
        container = aContainer;
    }

    @Override
    public void execute() {
        container.add(thing);
        Environment.tell("A " + thing.getShortDescription() + " appears in the " + container.getShortDescription() +
                ".");
    }
}
