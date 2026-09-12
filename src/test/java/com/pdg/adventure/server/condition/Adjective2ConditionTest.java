package com.pdg.adventure.server.condition;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;

class Adjective2ConditionTest {

    private final GameContext gameContext = mock(GameContext.class);

    @Test
    void check_succeeds_whenGameContextsCurrentAdjective2Matches() {
        when(gameContext.getCurrentAdjective2()).thenReturn("ancient");
        Adjective2Condition sut = new Adjective2Condition("ancient", gameContext);

        assertThat(sut.check().getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }

    @Test
    void check_fails_whenGameContextsCurrentAdjective2Differs() {
        when(gameContext.getCurrentAdjective2()).thenReturn("rusty");
        Adjective2Condition sut = new Adjective2Condition("ancient", gameContext);

        assertThat(sut.check().getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
    }

    @Test
    void check_onMismatch_carriesNoResultMessage() {
        when(gameContext.getCurrentAdjective2()).thenReturn("rusty");
        Adjective2Condition sut = new Adjective2Condition("ancient", gameContext);

        assertThat(sut.check().getResultMessage()).isEmpty();
    }

    @Test
    void getAdjective2() {
        Adjective2Condition sut = new Adjective2Condition("ancient", gameContext);
        assertThat(sut.getAdjective2()).isEqualTo("ancient");
    }
}
