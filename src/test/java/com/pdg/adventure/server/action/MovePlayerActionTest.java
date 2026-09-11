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

@ExtendWith(MockitoExtension.class)
class MovePlayerActionTest {

    @Mock private Location destination;
    @Mock private GameContext gameContext;

    @Test
    void execute_setsCurrentLocationOnGameContext() {
        when(destination.getArrivalDescription()).thenReturn("A dark cave.");
        when(destination.getTimesVisited()).thenReturn(0L);

        new MovePlayerAction(destination, gameContext).execute();

        verify(gameContext).setCurrentLocation(destination);
    }

    @Test
    void execute_returnsArrivalDescriptionOfDestination() {
        when(destination.getArrivalDescription()).thenReturn("A sunlit meadow.");
        when(destination.getTimesVisited()).thenReturn(2L);

        ExecutionResult result = new MovePlayerAction(destination, gameContext).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage()).isEqualTo("A sunlit meadow.");
    }

    @Test
    void execute_incrementsTimesVisited() {
        when(destination.getArrivalDescription()).thenReturn("A tower.");
        when(destination.getTimesVisited()).thenReturn(3L);

        new MovePlayerAction(destination, gameContext).execute();

        verify(destination).setTimesVisited(4L);
    }

    @Test
    void execute_runsArrivalProcesses_afterSettingTheNewLocation() {
        when(destination.getArrivalDescription()).thenReturn("A dark cave.");
        when(destination.getTimesVisited()).thenReturn(0L);

        new MovePlayerAction(destination, gameContext).execute();

        org.mockito.InOrder inOrder = inOrder(gameContext);
        inOrder.verify(gameContext).setCurrentLocation(destination);
        inOrder.verify(gameContext).runArrivalProcesses();
    }
}
