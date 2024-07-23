package com.pdg.adventure.server.action;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.storage.messages.MessagesHolder;

import java.util.function.Consumer;

public class MovePlayerAction extends AbstractAction {
    private final Location destination;
    private final Consumer<Location> currentLocationHolder;

    public MovePlayerAction(Location aDestination, Consumer<Location> aCurrentLocationHolder,
                            MessagesHolder aMessagesHolder) {
        super(aMessagesHolder);
        destination = aDestination;
        currentLocationHolder = aCurrentLocationHolder;
    }

    @Override
    public ExecutionResult execute() {
        currentLocationHolder.accept(destination);
        ExecutionResult result = new CommandExecutionResult(ExecutionResult.State.SUCCESS);
        result.setResultMessage(destination.getLongDescription());
        destination.setTimesVisited(destination.getTimesVisited() + 1);
        return result;
    }
}
