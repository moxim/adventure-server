package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.location.Location;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class MovePlayerAction extends AbstractAction {
    @Getter
    private final Location destination;
    private final transient GameContext gameContext;

    public MovePlayerAction(Location aDestination, GameContext aGameContext) {
        destination = aDestination;
        gameContext = aGameContext;
    }

    @Override
    public ExecutionResult execute() {
        gameContext.setCurrentLocation(destination);
        final DescribeAction describeAction = new DescribeAction(destination::getArrivalDescription);
        ExecutionResult result = describeAction.execute();
        destination.setTimesVisited(destination.getTimesVisited() + 1);
        gameContext.runArrivalProcesses();
        return result;
    }
}
