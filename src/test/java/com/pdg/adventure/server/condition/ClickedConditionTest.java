package com.pdg.adventure.server.condition;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;

class ClickedConditionTest {

    private final GameContext gameContext = mock(GameContext.class);

    @Test
    void check_succeeds_whenTheClickIsAtItsCell() {
        when(gameContext.isClickAt(3, 4)).thenReturn(true);
        ClickedCondition sut = new ClickedCondition(3, 4, gameContext);

        assertThat(sut.check().getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }

    @Test
    void check_fails_silently_whenTheClickIsElsewhere() {
        when(gameContext.isClickAt(3, 4)).thenReturn(false);
        ClickedCondition sut = new ClickedCondition(3, 4, gameContext);

        ExecutionResult result = sut.check();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
        assertThat(result.getResultMessage()).isEmpty();
    }

    @Test
    void check_withAMissingCoordinate_neverMatchesAndNeverThrows() {
        GameContext real = new GameContext();
        real.beginClick(3, 4);

        assertThat(new ClickedCondition(null, 4, real).check().getExecutionState())
                .isEqualTo(ExecutionResult.State.FAILURE);
        assertThat(new ClickedCondition(3, null, real).check().getExecutionState())
                .isEqualTo(ExecutionResult.State.FAILURE);
    }

    @Test
    void check_withAnOutOfRangeCoordinate_neverMatchesAndNeverThrows() {
        GameContext real = new GameContext();
        real.beginClick(3, 4);

        assertThat(new ClickedCondition(12, 3, real).check().getExecutionState())
                .isEqualTo(ExecutionResult.State.FAILURE);
        assertThat(new ClickedCondition(-1, 4, real).check().getExecutionState())
                .isEqualTo(ExecutionResult.State.FAILURE);
    }

    @Test
    void getters_returnTheCoordinates() {
        ClickedCondition sut = new ClickedCondition(3, 4, gameContext);

        assertThat(sut.getX()).isEqualTo(3);
        assertThat(sut.getY()).isEqualTo(4);
    }

    @Test
    void commandApplied_marksTheClickHandled_whenTheClickIsAtItsCell() {
        GameContext real = new GameContext();
        real.beginClick(3, 4);

        new ClickedCondition(3, 4, real).commandApplied();

        assertThat(real.isClickHandled()).isTrue();
    }

    @Test
    void commandApplied_doesNothing_whenTheClickIsElsewhere() {
        GameContext real = new GameContext();
        real.beginClick(5, 5);

        new ClickedCondition(3, 4, real).commandApplied();

        assertThat(real.isClickHandled()).isFalse();
    }

    @Test
    void commandApplied_ofANotCondition_neverMarksTheClickHandled() {
        GameContext real = new GameContext();
        real.beginClick(3, 4);

        new NotCondition(new ClickedCondition(3, 4, real)).commandApplied();

        assertThat(real.isClickHandled()).isFalse();
    }
}
