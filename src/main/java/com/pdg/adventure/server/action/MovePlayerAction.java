package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class MovePlayerAction extends AbstractAction {

    @Getter
    private final Location destination;
    private final transient GameContext gameContext;
    private final transient VariableProvider variableProvider;

    public MovePlayerAction(Location aDestination, GameContext aGameContext, VariableProvider aVariableProvider) {
        destination = aDestination;
        gameContext = aGameContext;
        variableProvider = aVariableProvider;
    }

    @Override
    public ExecutionResult execute() {
        gameContext.setCurrentLocation(destination);
        final DescribeAction describeAction = new DescribeAction(destination::getArrivalDescription);
        ExecutionResult result = describeAction.execute();
        variableProvider.set(new Variable(VariableProvider.VISITED_VARIABLE_NAME, (int) destination.getTimesVisited()));
        destination.setTimesVisited(destination.getTimesVisited() + 1);
        String arrivalMessage = gameContext.runArrivalProcesses().getResultMessage();
        if (!arrivalMessage.isEmpty()) {
            result.setResultMessage(result.getResultMessage() + "\n" + arrivalMessage);
        }
        return result;
    }
}
