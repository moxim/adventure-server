package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;

class PlayActionTest {

    private final GameContext gameContext = mock(GameContext.class);

    @Test
    void execute_queuesTheSound_andSucceeds() {
        ExecutionResult result = new PlayAction("pirate_bay", 2, gameContext).execute();

        verify(gameContext).playSound("pirate_bay", 2);
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage()).isEmpty();
    }

    @Test
    void execute_withNullTimes_queuesNothing_andStillSucceeds() {
        ExecutionResult result = new PlayAction("pirate_bay", null, gameContext).execute();

        verifyNoInteractions(gameContext);
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }

    @Test
    void isInformationalOnly_isTrue() {
        assertThat(new PlayAction("pirate_bay", 1, gameContext).isInformationalOnly()).isTrue();
    }
}
