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
class WearActionTest {

    @Mock private Wearable thing;
    @Mock private MessagesHolder messagesHolder;

    @Test
    void execute_wearableAndNotWorn_setsWornAndReturnsSuccess() {
        when(thing.isWearable()).thenReturn(true);
        when(thing.isWorn()).thenReturn(false);

        ExecutionResult result = new WearAction(thing, messagesHolder).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        verify(thing).setIsWorn(true);
    }

    @Test
    void execute_alreadyWorn_returnsFailureWithMessage() {
        when(thing.isWearable()).thenReturn(true);
        when(thing.isWorn()).thenReturn(true);
        when(messagesHolder.getMessage("-6")).thenReturn("You already wear %s.");
        when(thing.getEnrichedBasicDescription()).thenReturn("the helmet");

        ExecutionResult result = new WearAction(thing, messagesHolder).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
        assertThat(result.getResultMessage()).contains("helmet");
        verify(thing, never()).setIsWorn(anyBoolean());
    }

    @Test
    void execute_notWearable_returnsFailureWithMessage() {
        when(thing.isWearable()).thenReturn(false);
        when(messagesHolder.getMessage("-6")).thenReturn("You cannot wear %s.");
        when(thing.getEnrichedBasicDescription()).thenReturn("the sword");

        ExecutionResult result = new WearAction(thing, messagesHolder).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
        assertThat(result.getResultMessage()).contains("sword");
        verify(thing, never()).setIsWorn(anyBoolean());
    }
}
