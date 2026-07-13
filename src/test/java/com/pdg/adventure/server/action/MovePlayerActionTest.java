package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.storage.message.MessagesHolder;

@ExtendWith(MockitoExtension.class)
class MovePlayerActionTest {

    @Mock private Location destination;
    @Mock private GameContext gameContext;
    @Mock private MessagesHolder messagesHolder;

    @Test
    void execute_setsCurrentLocationOnGameContext() {
        when(destination.getLongDescription()).thenReturn("A dark cave.");
        when(destination.getTimesVisited()).thenReturn(0L);

        new MovePlayerAction(destination, messagesHolder, gameContext).execute();

        verify(gameContext).setCurrentLocation(destination);
    }

    @Test
    void execute_returnsDescriptionOfDestination() {
        when(destination.getLongDescription()).thenReturn("A sunlit meadow.");
        when(destination.getTimesVisited()).thenReturn(2L);

        ExecutionResult result = new MovePlayerAction(destination, messagesHolder, gameContext).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage()).isEqualTo("A sunlit meadow.");
    }

    @Test
    void execute_incrementsTimesVisited() {
        when(destination.getLongDescription()).thenReturn("A tower.");
        when(destination.getTimesVisited()).thenReturn(3L);

        new MovePlayerAction(destination, messagesHolder, gameContext).execute();

        verify(destination).setTimesVisited(4L);
    }
}
