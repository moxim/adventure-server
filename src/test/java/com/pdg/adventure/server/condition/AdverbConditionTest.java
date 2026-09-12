package com.pdg.adventure.server.condition;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;

class AdverbConditionTest {

    private final GameContext gameContext = mock(GameContext.class);

    @Test
    void check_succeeds_whenGameContextsCurrentAdverbMatches() {
        when(gameContext.getCurrentAdverb()).thenReturn("slowly");
        AdverbCondition sut = new AdverbCondition("slowly", gameContext);

        assertThat(sut.check().getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }

    @Test
    void check_fails_whenGameContextsCurrentAdverbDiffers() {
        when(gameContext.getCurrentAdverb()).thenReturn("quickly");
        AdverbCondition sut = new AdverbCondition("slowly", gameContext);

        assertThat(sut.check().getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
    }

    @Test
    void check_onMismatch_carriesNoResultMessage() {
        when(gameContext.getCurrentAdverb()).thenReturn("quickly");
        AdverbCondition sut = new AdverbCondition("slowly", gameContext);

        assertThat(sut.check().getResultMessage()).isEmpty();
    }

    @Test
    void getAdverb() {
        AdverbCondition sut = new AdverbCondition("slowly", gameContext);
        assertThat(sut.getAdverb()).isEqualTo("slowly");
    }
}
