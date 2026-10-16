package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.api.Containable;
import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.parser.CommandExecutionResult;

@ExtendWith(MockitoExtension.class)
class DestroyActionTest {

    @Mock private Containable thing;
    @Mock private Container parentContainer;
    @Mock private GameContext gameContext;

    @Test
    void execute_onSuccess_setsResultMessageAndReturnsSuccess() {
        when(thing.getParentContainer()).thenReturn(parentContainer);
        when(parentContainer.remove(thing)).thenReturn(new CommandExecutionResult(ExecutionResult.State.SUCCESS));
        when(gameContext.playerNoticesChangeIn(parentContainer)).thenReturn(true);
        when(thing.getStrippedBasicDescription()).thenReturn("the crystal ball");

        ExecutionResult result = new DestroyAction(thing, gameContext).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage()).contains("crystal ball");
        verify(parentContainer).remove(thing);
    }

    @Test
    void execute_whenThePlayerCannotNotice_removesThingWithoutAMessage() {
        when(thing.getParentContainer()).thenReturn(parentContainer);
        when(parentContainer.remove(thing)).thenReturn(new CommandExecutionResult(ExecutionResult.State.SUCCESS));
        when(gameContext.playerNoticesChangeIn(parentContainer)).thenReturn(false);

        ExecutionResult result = new DestroyAction(thing, gameContext).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage()).isEmpty();
        verify(parentContainer).remove(thing);
    }

    @Test
    void execute_onFailure_returnsContainerResult() {
        when(thing.getParentContainer()).thenReturn(parentContainer);
        when(parentContainer.remove(thing)).thenReturn(new CommandExecutionResult(ExecutionResult.State.FAILURE, "Cannot remove"));

        ExecutionResult result = new DestroyAction(thing, gameContext).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
    }
}
