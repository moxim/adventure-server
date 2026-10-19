package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.model.PictureMode;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.location.Location;
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
        Location.LocationDescription description = destination.getArrivalDescription();
        final DescribeAction describeAction = new DescribeAction(description::text);
        ExecutionResult result = describeAction.execute();
        gameContext.setCurrentPictureId(arrivalPictureId(description));
        variableProvider.set(VariableProvider.VISITED_VARIABLE_NAME, destination.getTimesVisited());
        destination.setTimesVisited(destination.getTimesVisited() + 1);
        String arrivalMessage = gameContext.runArrivalProcesses().getResultMessage();
        if (!arrivalMessage.isEmpty()) {
            result.setResultMessage(result.getResultMessage() + "\n" + arrivalMessage);
        }
        return result;
    }

    /** The picture to show on arrival, by the picture mode: the description's own is the DEFAULT (first visit only). */
    private String arrivalPictureId(Location.LocationDescription aDescription) {
        PictureMode mode = gameContext.getPictureMode();
        if (mode == null) {
            return aDescription.pictureId();
        }
        return switch (mode) {
            case ON -> destination.getVisiblePictureId();
            case OFF -> null;
            case DEFAULT -> aDescription.pictureId();
        };
    }
}
