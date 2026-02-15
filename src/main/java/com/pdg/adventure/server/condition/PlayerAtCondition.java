package com.pdg.adventure.server.condition;

import lombok.EqualsAndHashCode;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.CommandExecutionResult;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class PlayerAtCondition extends AbstractCondition {
    private final Location location;
    private final GameContext gameContext;

    public PlayerAtCondition(Location aLocation, GameContext aGameContext) {
        location = aLocation;
        gameContext = aGameContext;
    }

    @Override
    public ExecutionResult check() {
        ExecutionResult result = new CommandExecutionResult();
        if (gameContext.getCurrentLocation().equals(location)) {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
        }
        return result;
    }
}
