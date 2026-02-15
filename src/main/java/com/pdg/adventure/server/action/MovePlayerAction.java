package com.pdg.adventure.server.action;

import lombok.Getter;

import java.util.function.Supplier;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.storage.message.MessagesHolder;

public class MovePlayerAction extends AbstractAction {
    @Getter
    private final Location destination;
    private final GameContext gameContext;

    public MovePlayerAction(Location aDestination,
                            MessagesHolder aMessagesHolder,
                            GameContext aGameContext) {
        super(aMessagesHolder);
        destination = aDestination;
        gameContext = aGameContext;
    }

    @Override
    public ExecutionResult execute() {
        gameContext.setCurrentLocation(destination);
        final DescribeAction describeAction = new DescribeAction(new Supplier<String>() {
            @Override
            public String get() {
                return destination.getLongDescription();
            }
        }, messagesHolder);
        ExecutionResult result = describeAction.execute();
        destination.setTimesVisited(destination.getTimesVisited() + 1);
        return result;
    }
}
