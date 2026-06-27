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
class DestroyActionTest {

    @Mock private Containable thing;
    @Mock private Container parentContainer;
    @Mock private MessagesHolder messagesHolder;

    @Test
    void execute_onSuccess_setsResultMessageAndReturnsSuccess() {
        when(thing.getParentContainer()).thenReturn(parentContainer);
        when(parentContainer.remove(thing)).thenReturn(new CommandExecutionResult(ExecutionResult.State.SUCCESS));
        when(messagesHolder.getMessage("-11")).thenReturn("You destroyed %s.");
        when(thing.getShortDescription()).thenReturn("the crystal ball");

        ExecutionResult result = new DestroyAction(thing, messagesHolder).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage()).contains("crystal ball");
        verify(parentContainer).remove(thing);
    }

    @Test
    void execute_onFailure_returnsContainerResult() {
        when(thing.getParentContainer()).thenReturn(parentContainer);
        when(parentContainer.remove(thing)).thenReturn(new CommandExecutionResult(ExecutionResult.State.FAILURE, "Cannot remove"));

        ExecutionResult result = new DestroyAction(thing, messagesHolder).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
        verify(messagesHolder, never()).getMessage(any());
    }
}
