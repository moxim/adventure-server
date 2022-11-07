package com.pdg.adventure.server.condition;

import com.pdg.adventure.server.api.Containable;
import com.pdg.adventure.server.api.PreCondition;
import com.pdg.adventure.server.support.Environment;

public class PresentCondition implements PreCondition {
    private final Containable thing;
    public PresentCondition(Containable aThing) {
        thing = aThing;
    }

    public boolean isValid() {
        return Environment.getCurrentLocation().contains(thing);
    }
}
