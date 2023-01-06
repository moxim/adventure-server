package com.pdg.adventure.server.condition;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.api.PreCondition;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.engine.Environment;

public class PlayerAtCondition implements PreCondition {
    private final Location location;

    public PlayerAtCondition(Location aLocation) {
        location = aLocation;
    }

    @Override
    public ExecutionResult check() {
        ExecutionResult result = new CommandExecutionResult();
        if (Environment.getCurrentLocation().equals(location)) {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
        }
        return result;
    }
}
