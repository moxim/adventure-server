package com.pdg.adventure.server.action;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.engine.Environment;

public class MovePlayerAction extends AbstractAction {
    private final Location destination;

    public MovePlayerAction(Location aDestination) {
        destination = aDestination;
    }

    @Override
    public ExecutionResult execute() {
        Environment.setCurrentLocation(destination);
        ExecutionResult result = new CommandExecutionResult(ExecutionResult.State.SUCCESS);
        result.setResultMessage(destination.getLongDescription());
        destination.setHasBeenVisited(true);
        return result;
    }

    public Location getDestination() {
        return destination;
    }
}
