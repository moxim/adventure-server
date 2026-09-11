package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.api.Wearable;

@ExtendWith(MockitoExtension.class)
class RemoveActionTest {

    @Mock private Wearable thing;

    @Test
    void execute_itemIsWorn_setsUnwornAndReturnsSuccess() {
        when(thing.isWorn()).thenReturn(true);

        ExecutionResult result = new RemoveAction(thing).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        verify(thing).setIsWorn(false);
    }

    @Test
    void execute_itemNotWorn_returnsFailureWithMessage() {
        when(thing.isWorn()).thenReturn(false);
        when(thing.getStrippedBasicDescription()).thenReturn("robe");

        ExecutionResult result = new RemoveAction(thing).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
        assertThat(result.getResultMessage()).contains("robe");
        verify(thing, never()).setIsWorn(anyBoolean());
    }
}
