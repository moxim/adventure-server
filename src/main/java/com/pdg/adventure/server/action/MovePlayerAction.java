package com.pdg.adventure.server.action;

import lombok.Getter;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.Environment;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.storage.messages.MessagesHolder;

public class MovePlayerAction extends AbstractAction {
    @Getter
    private final Location destination;
//    private final Consumer<Location> currentLocationHolder;

    public MovePlayerAction(Location aDestination,
                            MessagesHolder aMessagesHolder) {
        super(aMessagesHolder);
        destination = aDestination;
//        currentLocationHolder = aCurrentLocationHolder;
    }

    @Override
    public ExecutionResult execute() {
        Environment.setCurrentLocation(destination);
//        currentLocationHolder.accept(destination);
        ExecutionResult result = new CommandExecutionResult(ExecutionResult.State.SUCCESS);
        result.setResultMessage(destination.getLongDescription());
        destination.setTimesVisited(destination.getTimesVisited() + 1);
        return result;
    }
}
