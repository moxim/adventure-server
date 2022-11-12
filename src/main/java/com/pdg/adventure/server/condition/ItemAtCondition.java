package com.pdg.adventure.server.condition;

import com.pdg.adventure.server.api.Containable;
import com.pdg.adventure.server.api.ExecutionResult;
import com.pdg.adventure.server.api.PreCondition;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.CommandExecutionResult;

public class ItemAtCondition implements PreCondition {
    private final Location location;
    private final Containable thing;

    public ItemAtCondition(Containable aThing, Location aLocation) {
        thing = aThing;
        location = aLocation;
    }

    @Override
    public ExecutionResult check() {
        ExecutionResult result = new CommandExecutionResult();
        if (location.contains(thing)) {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
        }
        return result;
    }
}
