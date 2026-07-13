package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.pdg.adventure.api.Containable;
import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.storage.message.MessagesHolder;

@ExtendWith(MockitoExtension.class)
class CreateActionTest {

    @Mock private Containable thing;
    @Mock private Container container;
    @Mock private MessagesHolder messagesHolder;

    @Test
    void execute_onSuccess_addsThingAndSetsResultMessage() {
        when(container.add(thing)).thenReturn(new CommandExecutionResult(ExecutionResult.State.SUCCESS));
        when(messagesHolder.getMessage("-12")).thenReturn("%s appears in %s.");
        when(thing.getShortDescription()).thenReturn("a torch");
        when(container.getShortDescription()).thenReturn("the room");

        ExecutionResult result = new CreateAction(thing, () -> container, messagesHolder).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage()).contains("torch").contains("room");
        verify(container).add(thing);
    }

    @Test
    void execute_onFailure_returnsContainerResult() {
        when(container.add(thing)).thenReturn(new CommandExecutionResult(ExecutionResult.State.FAILURE, "Container full"));

        ExecutionResult result = new CreateAction(thing, () -> container, messagesHolder).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
        verify(messagesHolder, never()).getMessage(any());
    }
}
