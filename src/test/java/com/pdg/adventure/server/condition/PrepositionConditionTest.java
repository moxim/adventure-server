package com.pdg.adventure.server.condition;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;

class PrepositionConditionTest {

    private final GameContext gameContext = mock(GameContext.class);

    @Test
    void check_succeeds_whenGameContextsCurrentPrepositionMatches() {
        when(gameContext.getCurrentPreposition()).thenReturn("on");
        PrepositionCondition sut = new PrepositionCondition("on", gameContext);

        assertThat(sut.check().getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }

    @Test
    void check_fails_whenGameContextsCurrentPrepositionDiffers() {
        when(gameContext.getCurrentPreposition()).thenReturn("off");
        PrepositionCondition sut = new PrepositionCondition("on", gameContext);

        assertThat(sut.check().getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
    }

    @Test
    void check_onMismatch_carriesNoResultMessage() {
        // A mismatch here should be silent - it just means a sibling row (or nothing) handles
        // this input, not that the player did something wrong; see GenericCommandChain/Workflow,
        // which surface only the LAST failure's message when nothing in the chain succeeds.
        when(gameContext.getCurrentPreposition()).thenReturn("off");
        PrepositionCondition sut = new PrepositionCondition("on", gameContext);

        assertThat(sut.check().getResultMessage()).isEmpty();
    }

    @Test
    void getPreposition() {
        PrepositionCondition sut = new PrepositionCondition("on", gameContext);
        assertThat(sut.getPreposition()).isEqualTo("on");
    }
}
