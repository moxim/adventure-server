package com.pdg.adventure.server.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.support.VariableProvider;

@ExtendWith(MockitoExtension.class)
class MovePlayerActionTest {

    @Mock private Location destination;
    @Mock private GameContext gameContext;
    @Mock private VariableProvider variableProvider;

    @BeforeEach
    void setUp() {
        // execute() always calls gameContext.runArrivalProcesses().getResultMessage() now - stub a
        // no-op (empty message) result so tests that don't care about arrival processes don't NPE.
        when(gameContext.runArrivalProcesses()).thenReturn(new CommandExecutionResult(ExecutionResult.State.SUCCESS));
    }

    @Test
    void execute_setsCurrentLocationOnGameContext() {
        when(destination.getArrivalDescription()).thenReturn(new Location.LocationDescription("A dark cave.", null));
        when(destination.getTimesVisited()).thenReturn(0);

        new MovePlayerAction(destination, gameContext, variableProvider).execute();

        verify(gameContext).setCurrentLocation(destination);
    }

    @Test
    void execute_returnsArrivalDescriptionOfDestination() {
        when(destination.getArrivalDescription()).thenReturn(new Location.LocationDescription("A sunlit meadow.", null));
        when(destination.getTimesVisited()).thenReturn(2);

        ExecutionResult result = new MovePlayerAction(destination, gameContext, variableProvider).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage()).isEqualTo("A sunlit meadow.");
    }

    @Test
    void execute_incrementsTimesVisited() {
        when(destination.getArrivalDescription()).thenReturn(new Location.LocationDescription("A tower.", null));
        when(destination.getTimesVisited()).thenReturn(3);

        new MovePlayerAction(destination, gameContext, variableProvider).execute();

        verify(destination).setTimesVisited(4);
    }

    @Test
    void execute_setsVisitedVariableToTheNumberOfTimesTheLocationHadAlreadyBeenVisited() {
        when(destination.getArrivalDescription()).thenReturn(new Location.LocationDescription("A tower.", null));
        when(destination.getTimesVisited()).thenReturn(3);

        new MovePlayerAction(destination, gameContext, variableProvider).execute();

        verify(variableProvider).set(VariableProvider.VISITED_VARIABLE_NAME, 3);
    }

    @Test
    void execute_setsVisitedVariableToZero_onFirstVisit() {
        when(destination.getArrivalDescription()).thenReturn(new Location.LocationDescription("A dark cave.", null));
        when(destination.getTimesVisited()).thenReturn(0);

        new MovePlayerAction(destination, gameContext, variableProvider).execute();

        verify(variableProvider).set(VariableProvider.VISITED_VARIABLE_NAME, 0);
    }

    @Test
    void execute_setsVisitedVariable_beforeIncrementingTimesVisited() {
        when(destination.getArrivalDescription()).thenReturn(new Location.LocationDescription("A tower.", null));
        when(destination.getTimesVisited()).thenReturn(3);

        new MovePlayerAction(destination, gameContext, variableProvider).execute();

        org.mockito.InOrder inOrder = inOrder(variableProvider, destination);
        inOrder.verify(variableProvider).set(VariableProvider.VISITED_VARIABLE_NAME, 3);
        inOrder.verify(destination).setTimesVisited(4);
    }

    @Test
    void execute_runsArrivalProcesses_afterSettingTheNewLocation() {
        when(destination.getArrivalDescription()).thenReturn(new Location.LocationDescription("A dark cave.", null));
        when(destination.getTimesVisited()).thenReturn(0);

        new MovePlayerAction(destination, gameContext, variableProvider).execute();

        org.mockito.InOrder inOrder = inOrder(gameContext);
        inOrder.verify(gameContext).setCurrentLocation(destination);
        inOrder.verify(gameContext).runArrivalProcesses();
    }

    @Test
    void execute_setsCurrentPictureId_toTheArrivalDescriptionsPicture() {
        when(destination.getArrivalDescription())
                .thenReturn(new Location.LocationDescription("A dark cave.", "cave-entrance"));
        when(destination.getTimesVisited()).thenReturn(0);

        new MovePlayerAction(destination, gameContext, variableProvider).execute();

        verify(gameContext).setCurrentPictureId("cave-entrance");
    }

    @Test
    void execute_setsCurrentPictureIdToNull_whenTheArrivalDescriptionHasNoPicture() {
        when(destination.getArrivalDescription())
                .thenReturn(new Location.LocationDescription("A tower.", null));
        when(destination.getTimesVisited()).thenReturn(3);

        new MovePlayerAction(destination, gameContext, variableProvider).execute();

        verify(gameContext).setCurrentPictureId(null);
    }
}
