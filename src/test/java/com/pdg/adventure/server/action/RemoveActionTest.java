package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.api.Wearable;
import com.pdg.adventure.server.storage.message.MessagesHolder;

@ExtendWith(MockitoExtension.class)
class RemoveActionTest {

    @Mock private Wearable thing;
    @Mock private MessagesHolder messagesHolder;

    @Test
    void execute_itemIsWorn_setsUnwornAndReturnsSuccess() {
        when(thing.isWorn()).thenReturn(true);

        ExecutionResult result = new RemoveAction(thing, messagesHolder).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        verify(thing).setIsWorn(false);
    }

    @Test
    void execute_itemNotWorn_returnsFailureWithMessage() {
        when(thing.isWorn()).thenReturn(false);
        when(messagesHolder.getMessage("-7")).thenReturn("You are not wearing %s.");
        when(thing.getEnrichedBasicDescription()).thenReturn("the robe");

        ExecutionResult result = new RemoveAction(thing, messagesHolder).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
        assertThat(result.getResultMessage()).contains("robe");
        verify(thing, never()).setIsWorn(anyBoolean());
    }
}
