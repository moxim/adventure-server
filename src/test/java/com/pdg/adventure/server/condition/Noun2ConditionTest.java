package com.pdg.adventure.server.condition;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;

class Noun2ConditionTest {

    private final GameContext gameContext = mock(GameContext.class);

    @Test
    void check_succeeds_whenGameContextsCurrentNoun2Matches() {
        when(gameContext.getCurrentNoun2()).thenReturn("machine");
        Noun2Condition sut = new Noun2Condition("machine", gameContext);

        assertThat(sut.check().getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }

    @Test
    void check_fails_whenGameContextsCurrentNoun2Differs() {
        when(gameContext.getCurrentNoun2()).thenReturn("engine");
        Noun2Condition sut = new Noun2Condition("machine", gameContext);

        assertThat(sut.check().getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
    }

    @Test
    void check_onMismatch_carriesNoResultMessage() {
        when(gameContext.getCurrentNoun2()).thenReturn("engine");
        Noun2Condition sut = new Noun2Condition("machine", gameContext);

        assertThat(sut.check().getResultMessage()).isEmpty();
    }

    @Test
    void getNoun2() {
        Noun2Condition sut = new Noun2Condition("machine", gameContext);
        assertThat(sut.getNoun2()).isEqualTo("machine");
    }
}
