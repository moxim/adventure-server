package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.tangible.Item;

@ExtendWith(MockitoExtension.class)
class LightActionTest {

    @Mock private Item torch;

    @Test
    void execute_setsTheItemsLightToTheGivenLumen() {
        new LightAction(torch, 50).execute();

        verify(torch).setLight(50);
    }

    @Test
    void execute_returnsSuccessWithAMessageNamingTheItemAndTheNewLumen() {
        when(torch.getStrippedBasicDescription()).thenReturn("torch");

        ExecutionResult result = new LightAction(torch, 50).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage()).contains("torch").contains("50");
    }

    @Test
    void execute_canDimAnItemBackDown() {
        new LightAction(torch, 0).execute();

        verify(torch).setLight(0);
    }
}
