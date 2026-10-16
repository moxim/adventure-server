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
class CreateActionTest {

    @Mock private Containable thing;
    @Mock private Container container;
    @Mock private GameContext gameContext;

    @Test
    void execute_onSuccess_addsThingAndSetsResultMessage() {
        when(container.add(thing)).thenReturn(new CommandExecutionResult(ExecutionResult.State.SUCCESS));
        when(gameContext.playerNoticesChangeIn(container)).thenReturn(true);
        when(thing.getEnrichedShortDescription()).thenReturn("a torch");
        when(container.getStrippedBasicDescription()).thenReturn("room");

        ExecutionResult result = new CreateAction(thing, () -> container, gameContext).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage()).contains("A torch").contains("room");
        verify(container).add(thing);
    }

    @Test
    void execute_whenThePlayerCannotNotice_addsThingWithoutAMessage() {
        when(container.add(thing)).thenReturn(new CommandExecutionResult(ExecutionResult.State.SUCCESS));
        when(gameContext.playerNoticesChangeIn(container)).thenReturn(false);

        ExecutionResult result = new CreateAction(thing, () -> container, gameContext).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage()).isEmpty();
        verify(container).add(thing);
    }

    @Test
    void execute_onFailure_returnsContainerResult() {
        when(container.add(thing)).thenReturn(new CommandExecutionResult(ExecutionResult.State.FAILURE, "Container full"));

        ExecutionResult result = new CreateAction(thing, () -> container, gameContext).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
    }
}
