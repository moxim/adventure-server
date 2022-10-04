package com.pdg.adventure.server.conditional;

import com.pdg.adventure.server.api.Container;
import com.pdg.adventure.server.api.Describable;
import com.pdg.adventure.server.api.PreCondition;

public class PresentCondition implements PreCondition {
    private final Describable thing;
    private final Container container;

    public PresentCondition(Describable aThing, Container aContainer) {
        thing = aThing;
        container = aContainer;
    }

    public boolean isValid() {
        return container.contains(thing);
    }
}
